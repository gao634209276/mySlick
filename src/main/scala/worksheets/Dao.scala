package worksheets

import java.sql.Timestamp
import javax.sql.DataSource

import com.typesafe.config.ConfigFactory
import org.apache.commons.dbcp.BasicDataSource
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import slick.driver.JdbcProfile

import scala.reflect.runtime.universe

case class ContextInfo(id: String, name: String,
                       config: String, actorAddress: Option[String],
                       startTime: DateTime, endTime: Option[DateTime],
                       state: String, error: Option[Throwable])

class DB {

  val logger = LoggerFactory.getLogger("Test")
  val config = ConfigFactory.load()

  //val config = ConfigFactory.parseFile(new File("")).withFallback(defaultConfig).resolve()
  // slick.driver.MySQLDriver
  val slickDriverClass = config.getString("spark.jobserver.sqldao.slick-driver")
  val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
  val profileModule = runtimeMirror.staticModule(slickDriverClass)
  // import slick.driver.MySQLDriver.api._
  val profile = runtimeMirror.reflectModule(profileModule).instance.asInstanceOf[JdbcProfile]

  import profile.api._

  // jdbc
  val jdbcDriverClass = config.getString("spark.jobserver.sqldao.jdbc-driver")
  val jdbcUrl = config.getString("spark.jobserver.sqldao.jdbc.url")
  val jdbcUser = config.getString("spark.jobserver.sqldao.jdbc.user")
  val jdbcPassword = config.getString("spark.jobserver.sqldao.jdbc.password")
  val enableDbcp = config.getBoolean("spark.jobserver.sqldao.dbcp.enabled")

  // db init
  val db = if (enableDbcp) {
    logger.info("DBCP enabled")
    val dbcpMaxActive = config.getInt("spark.jobserver.sqldao.dbcp.maxactive")
    val dbcpMaxIdle = config.getInt("spark.jobserver.sqldao.dbcp.maxidle")
    val dbcpInitialSize = config.getInt("spark.jobserver.sqldao.dbcp.initialsize")
    val dataSource: DataSource = {
      val ds = new BasicDataSource
      ds.setDriverClassName(jdbcDriverClass)
      ds.setUsername(jdbcUser)
      ds.setPassword(jdbcPassword)
      ds.setMaxActive(dbcpMaxActive)
      ds.setMaxIdle(dbcpMaxIdle)
      ds.setInitialSize(dbcpInitialSize)
      ds.setUrl(jdbcUrl)
      ds
    }
    Database.forDataSource(dataSource)
  } else {
    logger.info("DBCP disabled")
    Database.forURL(jdbcUrl, driver = jdbcDriverClass, user = jdbcUser, password = jdbcPassword)
  }

  class Jobs(tag: Tag) extends Table[(String, String, String, Int, String, String, Timestamp,
    Option[Timestamp], Option[String], Option[String], Option[String], Option[String])](tag, "JOBS") {
    def jobId = column[String]("JOB_ID", O.PrimaryKey)
    def contextId = column[String]("CONTEXT_ID")
    def contextName = column[String]("CONTEXT_NAME")
    def binId = column[Int]("BIN_ID")
    // FK to JARS table
    def classPath = column[String]("CLASSPATH")
    def state = column[String]("STATE")
    def startTime = column[Timestamp]("START_TIME")
    def endTime = column[Option[Timestamp]]("END_TIME")
    def error = column[Option[String]]("ERROR")
    def errorClass = column[Option[String]]("ERROR_CLASS")
    def errorStackTrace = column[Option[String]]("ERROR_STACK_TRACE")
    def result = column[Option[String]]("Result")
    def * = (jobId, contextId, contextName, binId, classPath, state,
      startTime, endTime, error, errorClass, errorStackTrace, result)
  }


  val jobs = TableQuery[Jobs]

  class Contexts(tag: Tag) extends Table[(String, String, String, Option[String], Timestamp,
    Option[Timestamp], String, Option[String])](tag, "CONTEXTS") {
    def id = column[String]("ID", O.PrimaryKey)
    def name = column[String]("NAME")
    def config = column[String]("CONFIG")
    def actorAddress = column[Option[String]]("ACTOR_ADDRESS")
    def startTime = column[Timestamp]("START_TIME")
    def endTime = column[Option[Timestamp]]("END_TIME")
    def state = column[String]("STATE")
    def error = column[Option[String]]("ERROR")
    def * = (id, name, config, actorAddress, startTime, endTime, state, error)
  }

  val contexts = TableQuery[Contexts]

  private def contextInfoFromRow(row: (String, String, String, Option[String],
    Timestamp, Option[Timestamp], String, Option[String])): ContextInfo = row match {
    case (id, name, config, actorAddress, start, end, state, error) =>
      ContextInfo(
        id,
        name,
        config,
        actorAddress,
        convertDateSqlToJoda(start),
        end.map(convertDateSqlToJoda),
        state,
        error.map(new Throwable(_))
      )
  }

  def getContextInfosByName(name: String): Future[Seq[ContextInfo]] = {
    val query = contexts.filter(_.name === name).
      filter(_.state.inSet(ContextStatus.getNonFinalStates())).sortBy(_.startTime.desc).result
    db.run(query).map(r => r.map(contextInfoFromRow))
  }
}

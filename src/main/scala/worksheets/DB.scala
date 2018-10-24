package worksheets

import javax.sql.DataSource

import com.typesafe.config.ConfigFactory
import org.apache.commons.dbcp.BasicDataSource
import org.slf4j.LoggerFactory
import slick.driver.JdbcProfile

import scala.reflect.runtime.universe

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
}

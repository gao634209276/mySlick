package worksheets

import slick.driver.H2Driver.api._

// Slick: Scala language-integrated connection kit
// https://cloud.tencent.com/developer/article/1014008
object slickIntro extends App {

  case class Coffee(id: Int = 0,
                    name: String,
                    supID: Int = 0,
                    price: Double,
                    sales: Int = 0,
                    total: Int = 0)

  class Coffees(tag: Tag) extends Table[Coffee](tag, "COFFEES") {
    def id = column[Int]("COF_ID", O.PrimaryKey, O.AutoInc)

    def name = column[String]("COF_NAME")

    def supID = column[Int]("SUP_ID")

    def price = column[Double]("PRICE")

    def sales = column[Int]("SALES", O.Default(0))

    def total = column[Int]("TOTAL", O.Default(0))

    def * = (id, name, supID, price, sales, total) <> (Coffee.tupled, Coffee.unapply)
  }

  val coffees = TableQuery[Coffees]

  val limit = 10.0
  // // 写Query时就像下面这样:
  var res = (for (c <- coffees; if c.price < limit) yield c.name).result
  println(res)
  // 相当于 SQL: select COF_NAME from COFFEES where PRICE < 10.0

  // 返回"name"字段的Query
  // 相当于 SQL: select NAME from COFFEES
  println(coffees.map(_.name))
  // 选择 price < 10.0 的所有记录Query
  // 相当于 SQL: select * from COFFEES where PRICE < 10.0
  println(coffees.filter(_.price < 10.0))
  //coffees.map(_.prices)
  //编译错误：value prices is not a member of worksheets.slickIntro.Coffees


  import scala.concurrent.ExecutionContext.Implicits.global

  val qDelete = coffees.filter(_.price > 0.0).delete
  val qAdd1 = (coffees returning coffees.map(_.id)) += Coffee(name = "Columbia", price = 128.0)
  val qAdd2 = (coffees returning coffees.map(_.id)) += Coffee(name = "Blue Mountain", price = 828.0)

  def getNameAndPrice(n: Int) = coffees.filter(_.id === n)
    .map(r => (r.name, r.price)).result.head

  val actions = for {
    _ <- coffees.schema.create
    _ <- qDelete
    c1 <- qAdd1
    c2 <- qAdd2
    (n1, p1) <- getNameAndPrice(c1)
    (n2, p2) <- getNameAndPrice(c2)
  } yield (n1, p1, n2, p2)


  import java.sql.SQLException
  import scala.concurrent.Await
  import scala.concurrent.duration._

  val db = Database.forURL("jdbc:h2:mem:demo", driver = "org.h2.Driver")
  Await.result(
    db.run(actions.transactionally).map { res =>
      println(s"Add coffee: ${res._1},${res._2} and ${res._3},${res._4}")
    }.recover {
      case e: SQLException => println("Caught exception: " + e.getMessage)
    }, Duration.Inf)


}
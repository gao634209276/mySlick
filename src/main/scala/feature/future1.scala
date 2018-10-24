package feature

import scala.concurrent.Future
import scala.util.{Success, Failure}

object future1 {

  def computation(): Int = {
    25 + 50
  }


  def main(args: Array[String]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val theFuture = Future {
      computation()
    }
    theFuture.onComplete {
      case Success(result) => println(result)
      case Failure(t) => println(s"Error: %{t.getMessage}")
    }

  }

}

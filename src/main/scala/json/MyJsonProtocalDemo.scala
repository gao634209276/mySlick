package json

import MyJsonProtocal._
import spray.json._

object MyJsonProtocalDemo extends App {

  val json = Color("CadetBlue", 95, 158, 160).toJson
  println(json)
  println(json.compactPrint) // 压缩打印
  println(json.prettyPrint) // 格式化打印

  val color = json.convertTo[Color]
  println(color)

}

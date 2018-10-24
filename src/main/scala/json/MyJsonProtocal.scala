package json

import spray.json._
import spray.json.DefaultJsonProtocol

// 提供 JsonFormats 的自定义Case类
case class Color(name: String, red: Int, green: Int, blue: Int)

case class Result(status: String, result: String) {
  def isSuccess: Boolean = status == "SUCCESS"
}


// 提供自己的协议
object MyJsonProtocal extends DefaultJsonProtocol {

  // 为DefaultJsonProtocol增加JsonFormat[T]
  implicit val resultFormat: RootJsonFormat[Result] = jsonFormat2(Result)
  implicit val colorFormat: RootJsonFormat[Color] = jsonFormat4(Color)

  // 当然也能用序列化和反序列化的不是case class类的类型逻辑
  implicit object ColorJsonFormat extends RootJsonFormat[Color] {
    def write(c: Color) = JsArray(JsString(c.name), JsNumber(c.red), JsNumber(c.green), JsNumber(c.blue))

    def read(value: JsValue) = value match {
      case JsArray(Vector(JsString(name), JsNumber(red), JsNumber(green), JsNumber(blue))) =>
        new Color(name, red.toInt, green.toInt, blue.toInt)
      case _ => deserializationError("Color expected")
    }
  }

}

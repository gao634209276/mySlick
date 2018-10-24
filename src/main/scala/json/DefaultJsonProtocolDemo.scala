package json

import spray.json._
import DefaultJsonProtocol._

// https://fangjian0423.github.io/2015/12/23/scala-spray-json/
object DefaultJsonProtocolDemo extends App {

  val str = """{"name": "Ed", "age": 24}"""
  // 黑魔法。不是String的parseJson方法，而是使用了隐式转换，隐式转换成PimpedString类。
  // PimpedString里有parseJson方法，转换成JsValue对象
  val jsonVal = str.parseJson
  // jsonVal是个JsObject对象，也是个JsValue实例。JsValue对象都有compactPrint和prettyPrint方法
  println(jsonVal.compactPrint) // 压缩打印
  println(jsonVal.prettyPrint) // 格式化打印

  val str2 = "\"test\""
  val strJson = str2.parseJson
  val j = JsonParser

  println(strJson)


  // 手动构建一个JsObject
  val jsonObj = JsObject(
    ("name", JsString("format")), ("age", JsNumber(99))
  )

  println(jsonObj.compactPrint)
  println(jsonObj.prettyPrint)

  // 黑方法，不是List的toJson方法，而是使用了隐式转换，隐式转换成PimpedAny类，PimpedAny类里有toList方法，转换成对应的类型
  val jsonList = List(1, 2, 3).toJson

  // JsValue的toString方法引用了compactPrint方法
  println(jsonList)

}

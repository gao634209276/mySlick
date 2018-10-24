package json

import spray.json._
import spray.json.DefaultJsonProtocol._

import scala.collection.JavaConverters._
//import scala.collection.mutable


/**
  * JSON Serialization utilities for spray-json
  * Most of this is necessary because Spray-json doesn't deal well with Any types.
  */
object JsonUtils {

  // Allows the conversion of flexible Maps that hold ints, strings, lists, maps
  // Note that this implicit conversion will only apply in this scope....
  // we have to be careful to make implicits that convert Any no wider in scope than needed
  implicit object AnyJsonFormat extends JsonFormat[Any] {
    def write(x: Any): JsValue = x match {
      case n: Int => JsNumber(n)
      case l: Long => JsNumber(l)
      case d: Double => JsNumber(d)
      case f: Float => JsNumber(f.toDouble)
      case s: String => JsString(s)
      case x: Seq[_] => seqFormat[Any].write(x)
      case m: Map[_, _] if m.isEmpty => JsObject(Map[String, JsValue]())
      // Get the type of map keys from the first key, translate the rest the same way
      case m: Map[_, _] => m.keys.head match {
        case sym: Symbol =>
          val map = m.asInstanceOf[Map[Symbol, _]]
          val pairs = map.map { case (sym, v) => (sym.name -> write(v)) }
          JsObject(pairs)
        case s: String => mapFormat[String, Any].write(m.asInstanceOf[Map[String, Any]])
        case a: Any =>
          val map = m.asInstanceOf[Map[Any, _]]
          val pairs = map.map { case (sym, v) => (sym.toString -> write(v)) }
          JsObject(pairs)
      }
      case a: Array[_] => seqFormat[Any].write(a.toSeq)
      case true => JsTrue
      case false => JsFalse
      case p: Product => seqFormat[Any].write(p.productIterator.toSeq)
      case null => JsNull
      case m: java.util.Map[_, _] => AnyJsonFormat.write(m.asScala.toMap)
      case l: java.util.List[_] => seqFormat[Any].write(l.asScala)
      case x => JsString(x.toString)
    }

    def read(value: JsValue): Any = value match {
      case JsNumber(n) => n.intValue()
      case JsString(s) => s
      case a: JsArray => listFormat[Any].read(value)
      case o: JsObject => mapFormat[String, Any].read(value)
      case JsTrue => true
      case JsFalse => false
      case x => deserializationError("Do not understand how to deserialize " + x)
    }
  }

  def mapToJson(map: Map[String, Any], compact: Boolean = true): String = {
    val jsonAst = map.toJson
    if (compact) jsonAst.compactPrint else jsonAst.prettyPrint
  }

  def listToJson(list: Seq[Any], compact: Boolean = true): String = {
    val jsonAst = list.toJson
    if (compact) jsonAst.compactPrint else jsonAst.prettyPrint
  }

  def mapFromJson(json: String): Map[String, Any] = json.parseJson.convertTo[Map[String, Any]]

  def listFromJson(json: String): Seq[Any] = json.parseJson.convertTo[Seq[Any]]

  def test_map() = {
    val m = Map[String, Int](
      "a" -> 1,
      "b" -> 2)
    val jsonVal = AnyJsonFormat.write(m)
    println(jsonVal)
    println(jsonVal.getClass)
    val jsonStr = jsonVal.prettyPrint
    println(jsonStr)
    val any = jsonStr.parseJson
    //val any = AnyJsonFormat.read(jsonStr.parseJson)
    //val any = mapFromJson(jsonStr)
    //val any = AnyJsonFormat.read(jsonVal)
    println(any)
    print(any.getClass)
  }

  def test_seq() = {
    val s = Seq[String]("a", "b")
    val jsonVal = AnyJsonFormat.write(s)
    println(jsonVal)
    println(jsonVal.getClass)
    val jsonStr = jsonVal.prettyPrint
    println(jsonStr)
    val any = jsonStr.parseJson
    println(any)
    print(any.getClass)
  }

  def test_neat(): Unit = {
    val n = Map[String, Any](
      "a" -> Seq[String]("b", "c"),
      "b" -> Map[String, String]("b" -> "c"),
      "c" -> "d",
      "d" -> 1
    )
    println(n.getClass)
    val jsonVal = AnyJsonFormat.write(n)
    println(jsonVal)
    println(jsonVal.getClass)
    val jsonStr = jsonVal.prettyPrint
    println(jsonStr)
    val any = jsonStr.parseJson
    println(any)
    println(any.getClass)
    val n_2 = AnyJsonFormat.read(any)
    println(n_2.getClass)

  }

  def main(args: Array[String]): Unit = {

    //val m = new mutable.HashMap[String, Int]
    //m.put("a", 1)
    //m.put("b", 2)
    test_neat()
  }
}

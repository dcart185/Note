package models

import play.api.libs.json._
import play.api.libs.functional.syntax._


case class Person(id:Option[Long], firstName:String, lastName:String, email:String, password:Option[String],
                  masterKey:Option[String],userKey:Option[String],userIv:Option[String])

object Person {
  import Person._


  implicit val personReads : Reads[Person] = (
    (JsPath \ "id").readNullable[Long] and
    (JsPath \ "firstName").read[String] and
    (JsPath \ "lastName").read[String] and
    (JsPath \ "email").read[String] and
    (JsPath \ "password").readNullable[String] and
    (JsPath \ "masterKey").readNullable[String] and
    (JsPath \ "userKey").readNullable[String] and
    (JsPath \ "userIv").readNullable[String]
    )(Person.apply _)


  implicit val personWrites : Writes[Person] = new Writes[Person] {
    override def writes(person: Person): JsValue = Json.obj(
      "id" -> person.id,
      "firstName" -> person.firstName,
      "lastName" -> person.lastName,
      "email" -> person.email,
      "userKey" -> person.userKey
    )
  }
}
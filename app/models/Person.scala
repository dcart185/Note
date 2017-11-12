package models

import play.api.libs.json._

case class Person(id:Option[Long], firstName:String, lastName:String, email:String, password:Option[String])

object Person {
  import Person._

  implicit val personWrites : Writes[Person] = new Writes[Person] {
    override def writes(person: Person): JsValue = Json.obj(
      "id" -> person.id,
      "firstName" -> person.firstName,
      "lastName" -> person.lastName,
      "email" -> person.email
    )
  }
}
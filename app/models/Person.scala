package models

case class Person(id:Option[Long], firstName:String, lastName:String, email:String, password:Option[String])

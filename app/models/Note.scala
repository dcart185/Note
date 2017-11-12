package models

case class Note(id:Option[Long], personId:Long, subject:String, note:String)

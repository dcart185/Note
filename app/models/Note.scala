package models

import play.api.libs.json._
import play.api.libs.functional.syntax._


case class Note(id:Option[Long], personId:Long, subject:String, note:String)

object Note {
  import Note._

  implicit val notesReads : Reads[Note] = (
    (JsPath \ "id").readNullable[Long] and
      (JsPath \ "personId").read[Long] and
      (JsPath \ "subject").read[String] and
      (JsPath \ "note").read[String]
    )(Note.apply _)

  implicit val noteWrites : Writes[Note] = new Writes[Note] {
    override def writes(note: Note): JsValue = Json.obj(
      "id" -> note.id,
      "personId" -> note.personId,
      "subject" -> note.subject,
      "note" -> note.note
    )
  }
}

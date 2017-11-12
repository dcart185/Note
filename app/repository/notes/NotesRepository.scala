package repository.notes

import models.Note

trait NotesRepository {
  def insertNote(note:Note):Long
  def updateNote(note:Note):Long
  def deleteNote(id:Long):Long
  def getNote(id:Long):Option[Note]
}

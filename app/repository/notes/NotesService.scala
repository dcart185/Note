package repository.notes

import models.Note

class NotesService(notesRepository: NotesRepository) {

  def insertNote(note:Note):Note ={
    val noteId:Long = notesRepository.insertNote(note)
    note.copy(id = Some(noteId))
  }
  def updateNote(note:Note):Boolean ={
    val updateCount:Long = notesRepository.updateNote(note)
    if(updateCount == 1) true
    else false
  }
  def deleteNote(id:Long):Boolean ={
    val updateCount : Long = notesRepository.deleteNote(id)
    if(updateCount == 1) true
    else false
  }
  def getNote(id:Long):Option[Note] ={
    notesRepository.getNote(id)
  }
}

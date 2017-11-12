package repository.notes

import globals.{NotesHelper, PersonHelper}
import models.Note
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import play.api.db.evolutions.Evolutions
import play.api.db.{Database, Databases}
import repository.person.PersonRepositoryDatabase

class NoteRepositoryDatabaseSpec extends PlaySpec with NotesHelper with PersonHelper with BeforeAndAfter{
  var database : Database = null

  before {
    database = Databases(
      driver = "com.mysql.cj.jdbc.Driver",
      url = "jdbc:mysql://127.0.0.1:3306/notepadTest?autoReconnect=true&useSSL=false",
      config = Map(
        "username" -> "root",
        "password" -> "password"
      )
    )
    Evolutions.applyEvolutions(database)
  }

  after {
    Evolutions.cleanupEvolutions(database)
    database.shutdown()
  }

  "A NotesRepositoryDatabase" must {
    "be able to insert a note" in {
      val personRepositoryDatabase = new PersonRepositoryDatabase(database)
      val personId = personRepositoryDatabase.insertPerson(person1)

      val notesRepositoryDatabase = new NotesRepositoryDatabase(database)
      val noteId : Long = notesRepositoryDatabase.insertNote(note1)



      val expectedNote : Note = note1.copy(id=Some(noteId))
      val actualNote = notesRepositoryDatabase.getNote(noteId)

      expectedNote mustBe actualNote
    }
    "be able to delete note" in {
      val personRepositoryDatabase = new PersonRepositoryDatabase(database)
      val personId = personRepositoryDatabase.insertPerson(person1)

      val notesRepositoryDatabase = new NotesRepositoryDatabase(database)
      val noteId : Long = notesRepositoryDatabase.insertNote(note1)

      notesRepositoryDatabase.deleteNote(noteId)

      val actualNote : Note = notesRepositoryDatabase.getNote(noteId)
      actualNote mustBe null

    }
  }
}

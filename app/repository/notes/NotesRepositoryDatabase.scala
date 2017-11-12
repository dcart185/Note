package repository.notes
import java.sql.{CallableStatement, Connection, ResultSet}
import javax.inject.Inject

import models.Note
import play.api.db.Database
import utility.DBUtil

class NotesRepositoryDatabase @Inject()(db:Database) extends NotesRepository {

  override def insertNote(note: Note): Long = {
    var connection : Connection = null
    var cstm : CallableStatement = null

    try{
      connection = db.getConnection()
      cstm = connection.prepareCall("{CALL SP_INSERT_NOTE(?,?,?,?)}")
      cstm.setLong("in_person_id",note.personId)
      cstm.setString("in_subject",note.subject)
      cstm.setString("in_note",note.note)
      cstm.executeUpdate()
      cstm.getLong("insert_id")
    }
    finally {
      DBUtil.closeQuietlyConnectionStatement(cstm)
      DBUtil.closeQuietlyConnection(connection)
    }
  }

  override def updateNote(note: Note): Long = {
    var connection : Connection = null
    var cstm : CallableStatement = null

    try{
      connection = db.getConnection()
      cstm = connection.prepareCall("{CALL SP_UPDATE_NOTE(?,?,?,?)}")
      cstm.setLong("in_id",note.id.get)
      cstm.setLong("in_person_id",note.personId)
      cstm.setString("in_subject",note.subject)
      cstm.setString("in_note",note.note)
      cstm.executeUpdate()
      cstm.getUpdateCount
    }
    finally {
      DBUtil.closeQuietlyConnectionStatement(cstm)
      DBUtil.closeQuietlyConnection(connection)
    }
  }

  override def deleteNote(id: Long): Long = {
    var connection : Connection = null
    var cstm : CallableStatement = null

    try{
      connection = db.getConnection()
      cstm = connection.prepareCall("{CALL SP_DELETE_NOTE(?)}")
      cstm.setLong("in_id",id)
      cstm.execute()
      cstm.getUpdateCount()
    }
    finally {
      DBUtil.closeQuietlyConnectionStatement(cstm)
      DBUtil.closeQuietlyConnection(connection)
    }
  }

  override def getNote(id: Long): Option[Note] = {
    var connection : Connection = null
    var cstm : CallableStatement = null
    var rs:ResultSet = null
    var note : Option[Note] = None
    try{
      connection = db.getConnection()
      cstm = connection.prepareCall("{CALL SP_GET_NOTE(?)}")
      cstm.setLong("in_id",id)
      rs = cstm.executeQuery()
      if(rs.next())
        note = Some(resultSetToNote(rs))
      note
    }
    finally {
      DBUtil.closeQuietlyResultSet(rs)
      DBUtil.closeQuietlyConnectionStatement(cstm)
      DBUtil.closeQuietlyConnection(connection)
    }
  }


  private def resultSetToNote(resultSet: ResultSet):Note={
    val id = resultSet.getLong("id")
    val personId = resultSet.getLong("person_id")
    val subject= resultSet.getString("subject")
    val note= resultSet.getString("note")

    Note(Some(id),personId,subject,note)
  }
}

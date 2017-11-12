package repository.person
import java.sql.{CallableStatement, Connection, ResultSet}
import javax.inject.Inject

import play.api.db._
import models.Person
import utility.DBUtil

class PersonRepositoryDatabase @Inject()(db:Database) extends PersonRepository {

  override def insertPerson(person: Person): Long = {
    var connection : Connection = null
    var cstm : CallableStatement = null

    try{
      connection = db.getConnection()
      cstm = connection.prepareCall("{CALL SP_INSERT_PERSON(?,?,?,?,?)}")
      cstm.setString("in_first_name",person.firstName)
      cstm.setString("in_last_name",person.lastName)
      cstm.setString("in_email",person.email)
      cstm.setString("in_password",person.password.get)
      cstm.executeUpdate()
      cstm.getLong("insert_id")
    }
    finally {
      DBUtil.closeQuietlyConnectionStatement(cstm)
      DBUtil.closeQuietlyConnection(connection)
    }
  }

  override def getPerson(personId: Long) : Option[Person] = {
    var connection : Connection = null
    var cstm : CallableStatement = null
    var rs:ResultSet = null
    var person : Option[Person]= None
    try{
      connection = db.getConnection()
      cstm = connection.prepareCall("{CALL SP_GET_PERSON(?)}")
      cstm.setLong("in_id",personId)
      rs = cstm.executeQuery()
      if(rs.next())
        person = Some(resultSetToPerson(rs))
      person
    }
    finally {
      DBUtil.closeQuietlyResultSet(rs)
      DBUtil.closeQuietlyConnectionStatement(cstm)
      DBUtil.closeQuietlyConnection(connection)
    }
  }


  override def getPersonByEmail(email: String) : Option[Person] = {
    var connection : Connection = null
    var cstm : CallableStatement = null
    var rs:ResultSet = null
    var person : Option[Person] = None
    try{
      connection = db.getConnection()
      cstm = connection.prepareCall("{CALL SP_GET_PERSON_BY_EMAIL(?)}")
      cstm.setString("in_email",email)
      rs = cstm.executeQuery()
      if(rs.next())
        person = Some(resultSetToPerson(rs))
      person
    }
    finally {
      DBUtil.closeQuietlyResultSet(rs)
      DBUtil.closeQuietlyConnectionStatement(cstm)
      DBUtil.closeQuietlyConnection(connection)
    }
  }

  private def resultSetToPerson(resultSet: ResultSet):Person={
    val id = resultSet.getLong("id")
    val firstName = resultSet.getString("first_name")
    val lastName = resultSet.getString("last_name")
    val email = resultSet.getString("email")
    val password = resultSet.getString("password")

    Person(Some(id),firstName,lastName,email,Some(password))
  }
}

package repository.person

import globals.PersonHelper
import models.Person
import org.scalatestplus.play.PlaySpec
import play.api.db.{Database, Databases}
import play.api.db.evolutions._
import org.scalatest.BeforeAndAfter


class PersonRepositoryDatabaseSpec extends PlaySpec with PersonHelper with BeforeAndAfter {

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


  "A PersonRepositoryDatabase" must {
    "be able to insert a person" in {
      val personRepositoryDatabase = new PersonRepositoryDatabase(database)
      val personId: Long = personRepositoryDatabase.insertPerson(person1)

      val expectedPerson : Person = person1.copy(id=Some(personId))
      val actualPerson : Person = personRepositoryDatabase.getPerson(personId)

      expectedPerson mustBe actualPerson
    }

    "be able to get a person by email" in {
      val personRepositoryDatabase = new PersonRepositoryDatabase(database)
      val personId: Long = personRepositoryDatabase.insertPerson(person1)

      val expectedPerson : Person = person1.copy(id=Some(personId))
      val actualPerson : Person = personRepositoryDatabase.getPersonByEmail(person1.email)

      expectedPerson mustBe actualPerson
    }
  }

}

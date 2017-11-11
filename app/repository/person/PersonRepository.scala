package repository.person

import models.Person

trait PersonRepository {
  def insertPerson(person:Person):Long
  def getPerson(personId:Long):Person
  def getPersonByEmail(email:String):Person
}

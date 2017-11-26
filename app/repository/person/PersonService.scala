package repository.person

import models.Person

class PersonService(personRepository: PersonRepository) {

  def insertPerson(person:Person):Person={
    val personId : Long = personRepository.insertPerson(person)
    person.copy(id = Some(personId))
  }

  def getPerson(personId:Long):Option[Person] ={
    personRepository.getPerson(personId)
  }

  def getPersonByEmail(email:String):Option[Person] ={
    personRepository.getPersonByEmail(email)
  }
}

package controllers

import javax.inject.{Inject, Singleton}

import models.Person
import play.api.libs.json.Json
import play.api.mvc._
import repository.person.{PersonRepository, PersonService}
import views.html.defaultpages.{badRequest, notFound}

@Singleton
class PersonController @Inject()(cc: ControllerComponents, personRepository: PersonRepository)
  extends AbstractController(cc) {

 val personService : PersonService = new PersonService(personRepository)

  def insertPerson() = Action {
    Ok("t")
  }

  def getPerson(id:Long) = Action {

    val personOption : Option[Person] = personService.getPerson(id)

    personOption match{
      case None =>{
        NotFound("Person does not exist")
      }
      case Some(person)=>{
        val json = Json.toJson(person)
        Ok(json)
      }
    }
  }
}

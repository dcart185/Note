package controllers

import javax.inject.{Inject, Singleton}

import models.Person
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import repository.person.{PersonRepository, PersonService}


@Singleton
class PersonController @Inject()(cc: ControllerComponents, personRepository: PersonRepository)
  extends AbstractController(cc) {

 val personService : PersonService = new PersonService(personRepository)

  def insertPerson() = Action(parse.json) { implicit request =>

    val personResult = request.body.validate[Person]
    personResult.fold(
      errors => {
        BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toJson(errors)))
      },
      person => {
        println(s"The person is $person")
        Ok(Json.obj("status" ->"OK", "message" -> ("Place '"+person.firstName+"' saved.") ))
      }
    )
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

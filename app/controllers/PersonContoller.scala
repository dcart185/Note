package controllers

import javax.inject.{Inject, Singleton}

import models.Person
import org.mindrot.jbcrypt.BCrypt
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
        BadRequest(Json.obj("status" ->"KO", "message" -> "invalid input"))
      },
      person => {

        if(person.password.nonEmpty) {
          val hashedPassword: String = BCrypt.hashpw(person.password.get, BCrypt.gensalt())
          val toBeSavedPerson : Person = person.copy(password = Some(hashedPassword))
          val savedPerson: Person = personService.insertPerson(toBeSavedPerson)
          Ok(Json.obj("status" -> "OK", "message" -> (s"The user has been saved with id: ${savedPerson.id.get}")))
        }
        else{
          BadRequest(Json.obj("status" ->"KO", "message" -> "invalid input"))
        }
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

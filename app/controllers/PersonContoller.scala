package controllers

import javax.inject.{Inject, Singleton}

import com.typesafe.config.Config
import io.jsonwebtoken.{CompressionCodecs, Jwts, SignatureAlgorithm}
import models.Person
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc._
import repository.person.{PersonRepository, PersonService}
import play.api.libs.functional.syntax._

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class PersonController @Inject()(cc: ControllerComponents, personRepository: PersonRepository,config:Config)
                                (implicit ec:ExecutionContext) extends AbstractController(cc) {

  val personService : PersonService = new PersonService(personRepository)
  val token = "token"

  def insertPerson() = Action.async(parse.json) { implicit request =>
    val personResult = request.body.validate[Person]
    personResult.fold(
      errors => {
        Future(BadRequest(Json.obj("status" ->"KO", "message" -> "invalid input")))
      },
      person => {

        if(person.password.nonEmpty) {
          val hashedPassword: String = BCrypt.hashpw(person.password.get, BCrypt.gensalt())
          val toBeSavedPerson : Person = person.copy(password = Some(hashedPassword))
          val savedPerson: Future[Person] = Future(personService.insertPerson(toBeSavedPerson))
          savedPerson.map(person=>{
            Ok(Json.obj("status" -> "OK", "message" -> (s"The user has been saved with id: ${person.id.get}")))
          }).recover{
            case e:Exception=>{
              InternalServerError(Json.obj("status" ->"KO", "message" -> "something went wrong"))
            }
          }
        }
        else{
          Future(BadRequest(Json.obj("status" ->"KO", "message" -> "invalid input")))
        }
      }
    )
  }

  def getPerson(id:Long)  = Action.async { implicit request =>
    val personOptionFuture : Future[Option[Person]] = Future(personService.getPerson(id))
    personOptionFuture.map(personOption=>{
      personOption match {
        case None =>{
          NotFound("Person does not exist")
        }
        case Some(person)=>{
          val json = Json.toJson(person)
          Ok(json)
        }
      }
    }).recover{
      case e:Exception=>{
        InternalServerError(Json.obj("status" ->"KO", "message" -> "something went wrong"))
      }
    }
  }

  def authenticateUserCreds() = Action.async(parse.json) { implicit request: Request[JsValue] =>
    val email : String = (request.body \ "email").as[String]
    val candidate : String = (request.body \ "password").as[String]

    val personOptionFuture : Future[Option[Person]] = Future(personService.getPersonByEmail(email))
    personOptionFuture.map(personOpt =>{
      personOpt match {
        case Some(person)=>{
          if (BCrypt.checkpw(candidate, person.password.get)) {
            val key = config.getString("jwt.key")
            val json = Json.toJson(person)
            val compactJws : String = Jwts.builder()
              .setPayload(json.toString()).signWith(SignatureAlgorithm.HS512,key)
              .compact()

            Ok(json).withHeaders(token->compactJws)
          }
          else
            Forbidden(Json.obj("message" -> "invalid"))
        }
        case None =>{
          Forbidden(Json.obj("message" -> "invalid"))
        }
      }
    }).recover{
      case e:Exception=>{
        InternalServerError(Json.obj("status" ->"KO", "message" -> "something went wrong"))
      }
    }
  }
}

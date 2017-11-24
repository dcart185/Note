package actions


import javax.inject.Inject

import com.typesafe.config.Config
import models.Person
import play.api.Logger
import play.api.mvc._
import play.api.mvc.Results._
import repository.person.{PersonRepository, PersonService}
import utility.JwtUtil

import scala.concurrent.{ExecutionContext, Future}


case class PersonRequest[A](person: Person, request: Request[A]) extends WrappedRequest(request)

class JWTAuthentication @Inject()(val parser: BodyParsers.Default, personRepository: PersonRepository,config:Config)
                                 (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[PersonRequest, AnyContent] {
  val logger  = Logger(this.getClass()).logger


  val personService : PersonService = new PersonService(personRepository)

  def invokeBlock[A](request: Request[A], block: (PersonRequest[A]) => Future[Result]): Future[Result] = {
    implicit val req = request
    val jwtToken = request.headers.get("token").getOrElse("")
    val key = config.getString("jwt.key")
    val personOpt : Option[Person] = JwtUtil.parserClaims(jwtToken,key)


    personOpt match {
      case Some(person)=> {
        person.id match {
          case Some(id)=>{
            val personOptionFuture : Future[Option[Person]] = Future(personService.getPerson(id))

            personOptionFuture.flatMap {
              case Some(person) =>{
                block(PersonRequest(person,request))
              }
              case None => Future(Unauthorized("Invalid credential"))
            }
          }
          case None =>{
            logger.warn("id doesn't exist")
            Future(Unauthorized("Invalid credential"))
          }
        }
      }
      case None => {
        logger.warn("invalid cred")
        Future.successful(Unauthorized("Invalid credential"))
      }
    }
  }
}
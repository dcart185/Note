package controllers

import javax.inject.Singleton

import actions.JWTAuthentication
import com.google.inject.Inject
import models.Note
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import repository.notes.{NotesRepository, NotesService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotesController @Inject()(cc: ControllerComponents,jwtAuthentication:JWTAuthentication,
                                notesRepository: NotesRepository)(implicit ec:ExecutionContext)
  extends AbstractController(cc){

  val logger: Logger = Logger(this.getClass())
  val notesService : NotesService = new NotesService(notesRepository)

  def getNote(noteId:Long) = jwtAuthentication.async{ implicit request =>

    val noteOptionFuture : Future[Option[Note]] = Future(notesService.getNote(noteId))

    noteOptionFuture.map({
        case None =>{
          NotFound("Note does not exist")
        }
        case Some(note)=>{
          val json = Json.toJson(note)
          Ok(json)
        }
    }).recover{
      case e:Exception=>{
        InternalServerError(Json.obj("status" ->"KO", "message" -> "something went wrong"))
      }
    }
  }

  def insertNote = jwtAuthentication.async(parse.json){ implicit request =>

    val noteResult = request.request.body.validate[Note]

    noteResult.fold(
      errors => {
        logger.logger.error("Invalid input")
        Future(BadRequest(Json.obj("status" ->"KO", "message" -> "invalid input")))
      },
      note => {
        val newNoteFuture : Future[Note]= Future(notesService.insertNote(note))
        newNoteFuture.map(newNote =>{
          Ok(Json.obj("status" -> "OK", "message" -> (s"The note has been saved with id: ${newNote.id.get}")))
        }).recover{
          case e:Exception =>{
            logger.logger.error("something went wrong",e)
            InternalServerError(Json.obj("status" ->"KO", "message" -> "something went wrong"))
          }
        }
      }
    )
  }
}

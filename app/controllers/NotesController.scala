package controllers

import javax.inject.Singleton

import actions.JWTAuthentication
import com.google.inject.Inject
import models.Note
import play.api.libs.json.Json
import play.api.mvc._
import repository.notes.{NotesRepository, NotesService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotesController @Inject()(cc: ControllerComponents,jwtAuthentication:JWTAuthentication,
                                notesRepository: NotesRepository)(implicit ec:ExecutionContext)
  extends AbstractController(cc){

  val notesService : NotesService = new NotesService(notesRepository)

  def getNote(noteId:Long) = jwtAuthentication.async{ request =>

    val noteOptionFuture : Future[Option[Note]] = Future(notesService.getNote(noteId))

    noteOptionFuture.map(noteOption=>{
      noteOption match {
        case None =>{
          NotFound("Note does not exist")
        }
        case Some(note)=>{
          val json = Json.toJson(note)
          Ok(json)
        }
      }
    }).recover{
      case e:Exception=>{
        InternalServerError(Json.obj("status" ->"KO", "message" -> "something went wrong"))
      }
    }
  }
}

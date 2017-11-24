package controllers

import actions.JWTAuthentication
import com.google.inject.Inject
import play.api.mvc._

import scala.concurrent.ExecutionContext


class NotesController @Inject()(cc: ControllerComponents,jwtAuthentication:JWTAuthentication)
                               (implicit ec:ExecutionContext) extends AbstractController(cc){

  def getNote(noteId:Long) = jwtAuthentication{ request =>

    Ok("it worked")

  }

}

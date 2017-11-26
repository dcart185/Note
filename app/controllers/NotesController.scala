package controllers

import java.util.Base64
import javax.inject.Singleton

import actions.JWTAuthentication
import com.google.inject.Inject
import com.typesafe.config.Config
import models.{Note, Person}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import repository.notes.{NotesRepository, NotesService}
import utility.CryptoUtil
import utility.CryptoUtil.DecryptFailure

import scala.concurrent.{ExecutionContext, Future}

trait UnableToDecrypt
case object KeyDoesntExist extends UnableToDecrypt
case object FailedDecryption extends UnableToDecrypt

@Singleton
class NotesController @Inject()(cc: ControllerComponents,jwtAuthentication:JWTAuthentication,
                                notesRepository: NotesRepository,config:Config)(implicit ec:ExecutionContext)
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

  /**
    * Encrypts the note's contents
    *
    * @param note
    * @param person
    * @return the encrypted contents as a string
    */
  def encryptedNotesContents(note:Note,person:Person) : Either[UnableToDecrypt,String] = {
    (person.userKey,person.masterKey) match {
      case (Some(userKey),Some(masterKey))=>{
        val macKeyString : String = config.getString("mac.key")

        //decrypt the master key with the user key
        val encryptedMasterKeyAsBytes : Array[Byte] = Base64.getDecoder().decode(masterKey)
        val userKeyAsBytes : Array[Byte] = Base64.getDecoder().decode(userKey)
        val decryptedMasterKeyEither : Either[DecryptFailure,Array[Byte]] = CryptoUtil
          .macThenDecrypt(encryptedMasterKeyAsBytes,userKeyAsBytes, macKeyString.getBytes())

        decryptedMasterKeyEither match {
          case Left(decryptFailure) => Left(FailedDecryption)
          case Right(decryptedMasterKeyInBytes)=>{
            val encryptedNoteContentsInBytes : Array[Byte] = CryptoUtil.encryptThenMac(note.note.getBytes(),
              decryptedMasterKeyInBytes, macKeyString.getBytes())

            val encryptedNoteContents : String = Base64.getEncoder().encodeToString(encryptedNoteContentsInBytes)

            Right(encryptedNoteContents)
          }
        }
      }
      case _ => {
        Left(KeyDoesntExist)
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
        //encrypt the note
        val encryptedNoteEither : Either[UnableToDecrypt,String] = encryptedNotesContents(note,request.person)

        encryptedNoteEither match {
          case Left(unableToDecrypt)=>{
            logger.logger.error(s"Unable to decrypt $unableToDecrypt")
            Future(InternalServerError(Json.obj("status" ->"KO", "message" -> "something went wrong")))
          }
          case Right(encryptedNote)=>{
            val toInsertNote : Note = note.copy(personId = request.person.id,note = encryptedNote)
            val newNoteFuture : Future[Note]= Future(notesService.insertNote(toInsertNote))
            newNoteFuture.map(newNote =>{
              Ok(Json.obj("status" -> "OK", "message" -> (s"The note has been saved with id: ${newNote.id.get}")))
            }).recover{
              case e:Exception =>{
                logger.logger.error("something went wrong",e)
                InternalServerError(Json.obj("status" ->"KO", "message" -> "something went wrong"))
              }
            }
          }
        }
      }
    )
  }

  def updateNote(noteId:Long) = jwtAuthentication.async(parse.json){ implicit  request =>

    val noteResult = request.request.body.validate[Note]

    noteResult.fold(
      errors => {
        logger.logger.error("Invalid input")
        Future(BadRequest(Json.obj("status" ->"KO", "message" -> "invalid input")))
      },
      note => {
        val toInsertNote : Note = note.copy(id=Some(noteId),personId = request.person.id)
        val didNoteSucceedFuture : Future[Boolean]= Future(notesService.updateNote(toInsertNote))
        didNoteSucceedFuture.map(status =>{
          Ok(Json.obj("status" -> "OK", "message" -> (s"The note has been updated")))
        }).recover{
          case e:Exception =>{
            logger.logger.error("something went wrong",e)
            InternalServerError(Json.obj("status" ->"KO", "message" -> "something went wrong"))
          }
        }
      }
    )
  }

  def deleteNote(noteId:Long) = jwtAuthentication.async{ implicit request =>

    val noteFuture : Future[Option[Note]] = Future(notesService.getNote(noteId))

    noteFuture.flatMap({
      case Some(note)=>{

        if(note.personId == request.person.id){
          val deleteResultFuture : Future[Boolean] = Future(notesService.deleteNote(noteId))
          deleteResultFuture.map(result =>{
            if(result){
              Ok(Json.obj("status" -> "OK", "message" -> (s"The note has been deleted")))
            }
            else{
              BadRequest(Json.obj("status" ->"KO", "message" -> "Unable to delete note"))
            }
          }).recover{
            case e:Exception =>{
              logger.logger.error("something went wrong",e)
              InternalServerError(Json.obj("status" ->"KO", "message" -> "something went wrong"))
            }
          }
        }
        else{
          Future(Forbidden("You don't have access to delete this note"))
        }

        Future(Ok(Json.obj("status" -> "OK", "message" -> (s"The note has been deleted"))))
      }
      case None =>{
        Future(BadRequest(Json.obj("status" ->"KO", "message" -> "Unable to delete note since it does not exist")))
      }
    })
  }
}

package utility

import java.security.SignatureException

import io.jsonwebtoken._
import models.Person
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.{JsValue, Json}

object JwtUtil {
  val logger: Logger = Logger(this.getClass())

  def createTokenFromPerson(person:Person,key:String,expiration:DateTime):String ={
    val json = Json.toJson(person)
    Jwts.builder()
      .setSubject(json.toString()).signWith(SignatureAlgorithm.HS512,key)
      .setExpiration(expiration.toDate)
      .compressWith(CompressionCodecs.DEFLATE)
      .compact()

  }

  def parserClaims(token:String,key:String):Option[Person]={
    try{
      val jwsClaims:Jws[Claims] = Jwts.parser().setSigningKey(key).parseClaimsJws(token)
      val subject = jwsClaims.getBody.getSubject
      val json : JsValue = Json.parse(subject)
      Json.fromJson(json)(Person.personReads).asOpt
    }
    catch {
      case se:SignatureException=> {
        logger.logger.error("Unable too get person",se)
        None
      }
      case e:Exception =>{
        logger.logger.error("general exception",e)
        None
      }

    }
  }
}

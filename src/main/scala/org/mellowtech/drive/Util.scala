package org.mellowtech.drive

/**
 * @author msvens
 */
object Util {
  import scala.util.{Try,Failure,Success}
 
  
  case class TokenResponse(access_token: String, token_type: String, expires_in: Int, 
      id_token: String, refresh_token: Option[String])
      

  
}
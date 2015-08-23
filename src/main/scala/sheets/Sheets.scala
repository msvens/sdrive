package sheets

import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

import scala.util.{Try, Failure, Success}
import com.google.api.client.auth.oauth2.BrowserClientRequestUrl
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential

/**
 * Created by msvens on 23/08/15.
 */
class Sheets(var token: String, val refreshToken: Option[String], val clientId: String, val clientSecret: String)
            (implicit val ec: ExecutionContext) extends LazyLogging{

  val httpTransport = new NetHttpTransport
  val jsonFactory = new JacksonFactory

  var sheet: SpreadsheetService = {
    val s = new SpreadsheetService("mellwotechService")
    val c = new GoogleCredential.Builder()
      .setJsonFactory(jsonFactory)
      .setTransport(httpTransport)
      .setClientSecrets(clientId, clientSecret).build()

    c.setAccessToken(token)
    s.setHeader("Authorization", "Bearer " + token)
    s.setOAuth2Credentials(c)
    s
  }

  def refresh(): Try[Unit] = refreshToken match {
    case None => Failure(new Error("no refresh token provicded"))
    case Some(rt) => {
      val c = new GoogleCredential.Builder()
        .setTransport(httpTransport).setJsonFactory(jsonFactory)
        .setClientSecrets(clientId, clientSecret).build

      c.setRefreshToken(rt)

      if(c.refreshToken){
        token = c.getRefreshToken
        c.setAccessToken(token)
        println("refreshing drive")
        val s = new SpreadsheetService("mellwotechService")

        sheet = new SpreadsheetService("mellwotechService")
        s.setHeader("Authorization", "Bearer " + token)
        s.setOAuth2Credentials(c)
        Success(Unit)
      } else Failure(new Error("could not retrieve new token"))
    }
  }




}

object Sheets{
  val SCOPES = "https://spreadsheets.google.com/feeds https://docs.google.com/feeds"
  val FEED_URL = "https://spreadsheets.google.com/feeds/spreadsheets/private/full"
}

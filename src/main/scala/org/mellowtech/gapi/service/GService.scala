package org.mellowtech.gapi.service

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.typesafe.scalalogging.LazyLogging
import org.mellowtech.gapi.model.ClientCredentials

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Success, Failure, Try}

/**
 * Created by msvens on 24/08/15.
 */
trait GService[A] extends LazyLogging {

  def cred: ClientCredentials

  implicit def ec: ExecutionContext

  def credential: GoogleCredential = {
    new GoogleCredential.Builder()
      .setJsonFactory(GService.jsonFactory)
      .setTransport(GService.httpTransport)
      .setClientSecrets(cred.clientId, cred.clientSecret).build()
  }

  def service: A
  def service_(a: A) : Unit

  def refreshToken(f: GoogleCredential => A): Try[Unit] = cred.refreshToken match {
    case None => Failure(new Error("no refresh token provided"))
    case Some(rt) => {
      val c = credential
      c.setRefreshToken(rt)
      c.refreshToken match {
        case true => {
          c.setAccessToken(c.getRefreshToken)
          service_(f(c))
          Success(Unit)
        }
        case false => Failure(new Error("could not retrive new token"))
      }
    }
  }

  def refresh: Try[Unit]

  def retry[T](f: => T): Try[T] = try {
    Success(f)
  }catch {case e: Exception =>
    //e.printStackTrace(System.out)
    refresh match {
      case Failure(fail) => Failure(fail)
      case Success(_) => try {
        Success(f)
      } catch {case e: Exception => Failure(e)}
    }
  }

  /*def asyncRetry[T](f: => T): Future[T] =
    Future(withRetry(f)).flatMap{
      case Success(t: T) => Future.successful(t)
      case Failure(e) => Future.failed(e)
    }

  def async[T](f: => T): Future[T] = Future(f).flatMap{
    case Success(t: T) => Future.successful(t)
    case Failure(e) => Future.failed(e)
  }*/

}


object GService {
  val httpTransport = new NetHttpTransport
  val jsonFactory = new JacksonFactory

  /*def retry[A,B](f: => B)(implicit s: GService[A]): Try[B] = try {
    Success(f)
  } catch {case e: Exception =>
      s.refresh match {
        case Failure(fail) => Failure(fail)
        case Success(_) => try {
          Success(f)
        } catch {case e: Exception => Failure(e)}
      }
  }*/

  def async[A,B](f: => B, retry: Boolean = true)(implicit s: GService[A]): Future[B] = {
    implicit val context = s.ec
    retry match {
      case true => Future(s.retry(f)).flatMap {
        case Success(b: B) => Future.successful(b)
        case Failure(e) => Future.failed(e)
      }
      case false => Future(f).flatMap {
        case Success(b: B) => Future.successful(b)
        case Failure(e) => Future.failed(e)
      }
    }
  }

}
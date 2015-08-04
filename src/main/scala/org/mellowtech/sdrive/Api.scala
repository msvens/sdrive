package org.mellowtech.sdrive

import scala.util.{Try, Failure, Success}
import scala.concurrent._
import com.typesafe.scalalogging._

/**
 * @author msvens
 */
class GDrive(var token: String, val refreshToken: Option[String], val clientId: String, val clientSecret: String)
    (implicit val ec: ExecutionContext) extends LazyLogging{
  import scala.util.{Try, Failure, Success}
  import com.google.api.client.auth.oauth2.BrowserClientRequestUrl
  import com.google.api.client.http.javanet.NetHttpTransport
  import com.google.api.client.json.jackson2.JacksonFactory
  import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
  import com.google.api.services.drive.Drive
  val httpTransport = new NetHttpTransport
  val jsonFactory = new JacksonFactory
  
  var drive: Drive =  {
    val c = new GoogleCredential.Builder()
      .setJsonFactory(jsonFactory)
      .setTransport(httpTransport)
      .setClientSecrets(clientId, clientSecret).build()
    
    c.setAccessToken(token)
    new Drive.Builder(httpTransport, jsonFactory, c).build()
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
        drive = new Drive.Builder(httpTransport, jsonFactory, c).build()
        Success(Unit)
      } else Failure(new Error("could not retrieve new token"))
    }
  }
  
  val about = try {
    drive.about.get.execute
  } catch {case e: Exception =>
    refresh match {
      case Success(_) => drive.about.get.execute
      case Failure(e) => throw new Error("could not initialize drive")
    }
  }

  
  def files = drive.files
  def children = drive.children
  
      

}

object GApi extends LazyLogging{
  import com.google.api.services.drive.Drive
  //import ExecutionContext.Implicits.global
  import ModelConversions._
  import scala.collection.JavaConverters._
  import com.google.api.services.drive.model._
  
  val folderType = "application/vnd.google-apps.folder"
  val spreadsheetType = "application/vnd.google-apps.spreadsheet"
  val documentType = "application/vnd.google-apps.document"
  
  private def withRetry[T](f: => T)(implicit d: GDrive): Try[T] = try {
    Success(f)
  }catch {case e: Exception =>
    d.refresh match {
      case Failure(fail) => Failure(fail)
      case Success(_) => try {
        Success(f)
      } catch {case e: Exception => Failure(e)}
    }
  }
  
  def asyncRetry[T](f: => T)(implicit d: GDrive): Future[Try[T]] = scala.concurrent.Future(withRetry(f))(d.ec)
  
  def async[T](f: => T)(implicit d: GDrive): Future[T] = scala.concurrent.Future(f)(d.ec)
  
  def file(id: String)(implicit d: GDrive): SFile = {
    val t1 = System.currentTimeMillis()
    val r = d.files.get(id).execute
    val t2 = System.currentTimeMillis()
    println("elapsed: "+(t2-t1)+" millis")
    r
  }
  
  private def files(r: Drive#Files#List): Seq[SFile] = {
    var res: List[SFile] = Nil
    do {
      val files = r.execute
      res ++= files.getItems.asScala.map(asSFile(_))
      r.setPageToken(files.getNextPageToken)
    } while (r.getPageToken() != null && r.getPageToken().length() > 0)
    res
  }
  /**
   * Retrieves all files owned by user
  * @param d Google drive service wrapper
  * @return list of files
  */
def files(implicit d: GDrive): Seq[SFile] = files(d.files.list)
    
  
  
  /*def files(dirId: Option[String])(implicit d: GDrive): Seq[SFile] = {
    val id = dirId.getOrElse(d.about.getRootFolderId)
    list(id).map(cr => file(cr.id.get))
  }*/
  def files(dirId: Option[String])(implicit d: GDrive): Seq[SFile] = {
    val id = dirId.getOrElse(d.about.getRootFolderId)
    val qstring = "'"+id+"' in parents"
    val r = d.files.list.setQ(qstring)
    files(r)
  }
  
  def files(ids: Seq[String])(implicit d: GDrive): Seq[SFile] = ids.map(file(_))
  
  def isFolder(f: SFile) = f.mimeType match {
    case None => false
    case Some(mt) => mt == folderType
  }
  
  def list(implicit d: GDrive): Seq[SChildReference] = list(d.about.getRootFolderId)
  
  def list(dirId: String)(implicit d: GDrive): Seq[SChildReference] = {
    var res: List[SChildReference] = Nil
    val r = d.children.list(dirId)
    println("in listing children")
    do {
        val children = r.execute()
        println("got some childrien "+children.size)
        res ++= children.getItems.asScala.map(asSChildReference(_))
        r.setPageToken(children.getNextPageToken)
    } while (r.getPageToken() != null &&
             r.getPageToken().length() > 0);
    res
  }
  
  
  def addDir(name: String)(implicit d: GDrive): String = addDir(name, None)
  
  def addDir(name: String, parentId: Option[String])(implicit d: GDrive): String = createFile(name, parentId, folderType)
  
  def createFile(name: String, parentId: Option[String], mimeType: String)(implicit d: GDrive): String = {
    val f: File = new File
    f.setMimeType(mimeType)
    if(parentId != None) f.setParents(List(new ParentReference().setId(parentId.get)).asJava)
    f.setTitle(name)
    val ff = d.files.insert(f).execute()
    ff.getId
  }
  
  
  
  
 
  
  
  
  
}
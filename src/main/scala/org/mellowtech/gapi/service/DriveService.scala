package org.mellowtech.gapi.service

import java.io.ByteArrayOutputStream

import com.google.api.client.http.{ByteArrayContent, FileContent}
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.{ParentReference, File, About}
import com.typesafe.scalalogging._
import org.mellowtech.gapi.model.{SChildReference, ModelConversions, SFile, ClientCredentials}
import org.mellowtech.mpoi.Workbook

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

/**
 * Created by msvens on 26/08/15.
 */
class DriveService(var cred: ClientCredentials)(implicit val ec: ExecutionContext) extends GService[Drive]{

  import ModelConversions._
  import scala.collection.JavaConverters._

  private var drive: Drive = {
    val c = credential
    c.setAccessToken(cred.token)
    new Drive.Builder(GService.httpTransport, GService.jsonFactory, c).build()
  }

  override def refresh: Try[Unit] = refreshToken(c => {
    cred = cred.copy(token = c.getAccessToken)
    new Drive.Builder(GService.httpTransport, GService.jsonFactory, c).build()
  })

  override def service: Drive = drive

  override def service_(a: Drive): Unit = drive = a

  //Api methods

  lazy val about: About = retry(drive.about.get.execute()) match {
      case Success(a: About) => a
      case Failure(e) => throw new Error("Could not get about")
    }

  def createFolder(name: String, parentId: Option[String] = None): String = createFile(name, parentId, DriveService.FOLDER_TYPE)

  def createFile(name: String, parentId: Option[String], mimeType: String): String = {
    val f: File = new File
    f.setMimeType(mimeType)
    if(parentId.isDefined) f.setParents(List(new ParentReference().setId(parentId.get)).asJava)
    f.setTitle(name)
    val ff = drive.files.insert(f).execute()
    ff.getId
  }

  def deleteFile(id: String): Unit = drive.files.delete(id).execute()

  /**
   * Retrieve the file with a given id
   * @param id file id
   * @return file
   */
  def file(id: String): SFile = drive.files.get(id).execute

  private def files(r: Drive#Files#List): Seq[SFile] = {
    var res: List[SFile] = Nil
    do {
      val files = r.execute
      res ++= files.getItems.asScala.map(asSFile)
      r.setPageToken(files.getNextPageToken)
    } while (r.getPageToken != null && r.getPageToken.length() > 0)
    res
  }

  /**
   * Retrieves all files owned by user
   * @return list of files
   */
  def files: Seq[SFile] = files(drive.files.list)


  /**
   * Retrieves all files from a specific dirictory
   * @param dirId if None the user root folder will be used
   * @return a sequence of files
   */
  def files(dirId: Option[String]): Seq[SFile] = {
    val id = dirId.getOrElse(about.getRootFolderId)
    val qstring = "'"+id+"' in parents"
    val r = drive.files.list.setQ(qstring)
    files(r)
  }

  /**
   * Retrievs the files for the the given file ids
   * @param ids - sequence of file ids
   * @return a seqence of files
   */
  def files(ids: Seq[String]): Seq[SFile] = ids.map(file)

  /**
   * List the files in the given folder. The difference between list and files
   * is the return type which is much smaller for list (mainly carries the file id)
   * @param dirId folder to list
   * @return seqence of child references
   */
  def list(dirId: String = about.getRootFolderId): Seq[SChildReference] = {
    var res: List[SChildReference] = Nil
    val r = drive.children.list(dirId)
    do {
      val children = r.execute()
      res ++= children.getItems.asScala.map(asSChildReference)
      r.setPageToken(children.getNextPageToken)
    } while (r.getPageToken != null &&
      r.getPageToken.length() > 0)
    res
  }

  def uploadfile(name: String, parentId: Option[String], mimeType: String, localFile: String, convert: Boolean = false): String = {
    val f: File = new File
    f.setMimeType(mimeType)
    f.setTitle(name)
    if(parentId.isDefined) f.setParents(List(new ParentReference().setId(parentId.get)).asJava)
    val fileContent = new java.io.File(localFile)
    val mediaContent = new FileContent(mimeType, fileContent)
    val ff = drive.files.insert(f, mediaContent).setConvert(convert).execute()
    ff.getId
  }

  def uploadSheet(name: String, parentId: Option[String], wb: Workbook): String = {
    val mimetype = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    import org.mellowtech.mpoi.XSSFConverter


    val bos = new ByteArrayOutputStream()
    val conv = XSSFConverter.export(wb)
    conv.write(bos)
    val f: File = new File
    f.setMimeType(DriveService.SHEET_TYPE)
    f.setTitle(name)
    if(parentId.isDefined) f.setParents(List(new ParentReference().setId(parentId.get)).asJava)
    val mediaContent = new  ByteArrayContent(mimetype, bos.toByteArray)
    val ff = drive.files.insert(f, mediaContent).setConvert(true).execute()
    ff.getId
  }






}

object DriveService {

  val FOLDER_TYPE = "application/vnd.google-apps.folder"
  val SHEET_TYPE = "application/vnd.google-apps.spreadsheet"
  val DOC_TYPE = "application/vnd.google-apps.document"

  val SCOPES = "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/drive https://www.googleapis.com/auth/drive.file"

  def apply(cc: ClientCredentials)(implicit ec: ExecutionContext): DriveService = new DriveService(cc)
  def apply(token: String, refreshToken: Option[String], clientId: String, clientSecret: String)(implicit ec: ExecutionContext): DriveService = {
    apply(ClientCredentials(clientId,clientSecret, token, refreshToken))
  }

  /**
   * check if a file is a folder
   * @param f file
   * @return true if mime type is folder
   */
  def isFolder(f: SFile) = f.mimeType match {
    case None => false
    case Some(mt) => mt == DriveService.FOLDER_TYPE
  }

}
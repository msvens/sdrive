package org.mellowtech.gapi.service

import java.net.URL

import com.google.gdata.client.spreadsheet.{FeedURLFactory, SpreadsheetService}
import com.google.gdata.data.batch.{BatchOperationType, BatchUtils}
import com.google.gdata.data.spreadsheet._
import com.google.gdata.data.{ILink, PlainTextConstruct}
import org.mellowtech.gapi.model.ClientCredentials

import scala.concurrent.ExecutionContext


case class CellAddr(row: Int, col: Int, value: String){
  lazy val idString = s"R${row}C$col"
}

/**
 * Created by msvens on 23/08/15.
 */
class SheetService(var cred: ClientCredentials)
            (implicit val ec: ExecutionContext) extends GService[SpreadsheetService]{

  import scala.collection.JavaConverters._

  private var sheet: SpreadsheetService = {
    val s = new SpreadsheetService("mellwotechService")
    val c = credential

    c.setAccessToken(cred.token)
    s.setHeader("Authorization", "Bearer " + cred.token)
    s.setOAuth2Credentials(c)
    s
  }

  private val urlFactory = FeedURLFactory.getDefault

  override def service = sheet
  override def service_(s: SpreadsheetService) = sheet = s

  override def refresh = refreshToken(c => {
    val s = new SpreadsheetService("mellwotechService")
    cred = cred.copy(token = c.getAccessToken)
    //s.setHeader("Authorization", "Bearer " + c.getAccessToken)
    s.setOAuth2Credentials(c)
    s
  })


  //Api
  def sheets: Seq[SpreadsheetEntry] = {
    val f = service.getFeed(urlFactory.getSpreadsheetsFeedUrl, classOf[SpreadsheetFeed])
    f.getEntries.asScala
  }

  def sheet(fileId: String): SpreadsheetEntry = {
    service.getEntry(new URL(SheetService.BASE_SPREADSHEET_FEED_URL+fileId), classOf[SpreadsheetEntry])
  }

  def worksheet(fileId: String, title: String): WorksheetEntry = {
    sheet(fileId).getWorksheets.asScala.find(_.getTitle.getPlainText.equals(title)).head
  }

  def createWorksheet(fileId: String, title: String, cols: Option[Int] = None, rows: Option[Int] = None): Unit = {
    val s = sheet(fileId)
    val w = new WorksheetEntry()
    w.setTitle(new PlainTextConstruct(title))
    w.setColCount(cols.getOrElse(26))
    w.setRowCount(rows.getOrElse(1000))
    val u = s.getWorksheetFeedUrl
    service.insert(u, w)
  }

  def insertRow(fileId: String, ws: String, row: (String,String)*): Unit = {
    val entry = worksheet(fileId, ws)
    val listFeedUrl = entry.getListFeedUrl
    //val listFeed = service.getFeed(listFeedUrl, classOf[ListFeed])
    val listEntry = new ListEntry()
    row foreach{case (h,v) => listEntry.getCustomElements.setValueLocal(h,v)}
    val r = service.insert(listFeedUrl,listEntry)
  }

  def insertCell(fileId: String, ws: String, cell: CellAddr): Unit = {
    val entry = worksheet(fileId, ws)
    val cellFeedUrl = entry.getCellFeedUrl
    val cellFeed = service.getFeed(cellFeedUrl, classOf[CellFeed])
    cellFeed.insert(new CellEntry(cell.row, cell.col, cell.value))


  }


  /*def insertCells(fileId: String, worksheet: String, cells: CellAddr*): Unit = {

    val cellFeedURL = urlFactory.getCellFeedUrl(fileId, worksheet, "private", "full")
    val cellFeed = service.getFeed(cellFeedURL, classOf[CellFeed])

    val cellEntries = cellEntryMap(cellFeedURL, cells)
    val batchRequest = new CellFeed()
    cells.foreach { ca =>
      val batchEntry = new CellEntry(cellEntries(ca.idString))
      batchEntry.changeInputValueLocal(ca.value)
      BatchUtils.setBatchId(batchEntry, ca.idString)
      BatchUtils.setBatchOperationType(batchEntry, BatchOperationType.UPDATE)
      batchRequest.getEntries.add(batchEntry)
    }
    val batchLink = cellFeed.getLink(ILink.Rel.FEED_BATCH, ILink.Type.ATOM)
    val batchResponse = service.batch(new URL(batchLink.getHref), batchRequest)

    //verify result (should probably be removed)
    var isSuccess = true
    for(e <- batchResponse.getEntries.asScala){
      val batchId = BatchUtils.getBatchId(e)
      BatchUtils.isSuccess(e) match {
        case true =>
        case false =>
          isSuccess = false
          val status = BatchUtils.getBatchStatus(e)
          logger.error(batchId+" failed for reason "+status.getReason)
      }

    }
  }*/

  def insertCells(fileId: String, ws: String, cells: Seq[CellAddr]): Unit = try {
    val batchRequest = new CellFeed()
    val entry = worksheet(fileId, ws)
    val cellFeedURL = entry.getCellFeedUrl
    cells foreach {ca => batchRequest.getEntries.add(createUpdateOperation(cellFeedURL, ca))}

    // Get the batch feed URL and submit the batch request
    val feed = service.getFeed(cellFeedURL, classOf[CellFeed])
    val batchLink = feed.getLink(ILink.Rel.FEED_BATCH, ILink.Type.ATOM)
    val batchUrl = new URL(batchLink.getHref())
    val batchResponse = service.batch(batchUrl, batchRequest)

    //print any errors
    var isSuccess = true
    for(e <- batchResponse.getEntries.asScala){
      val batchId = BatchUtils.getBatchId(e)
      BatchUtils.isSuccess(e) match {
        case true =>
        case false =>
          isSuccess = false
          val status = BatchUtils.getBatchStatus(e)
          logger.error(batchId+" failed for reason "+status.getReason)
      }
    }
  } catch {
    case e: Exception =>
      e.printStackTrace(System.out)
      throw new Error(e)
  }

  private def createUpdateOperation(cellFeedUrl: URL, cell: CellAddr): CellEntry = {
    val entryURL = new URL(cellFeedUrl.toString + "/" + cell.idString)
    val entry = service.getEntry(entryURL, classOf[CellEntry])
    entry.changeInputValueLocal(cell.value)
    BatchUtils.setBatchId(entry, cell.idString)
    BatchUtils.setBatchOperationType(entry, BatchOperationType.UPDATE)
    entry
  }

  private def cellEntryMap(cellFeedUrl: URL, cellAddrs: Seq[CellAddr]): Map[String,CellEntry] = {
    val batchRequest = new CellFeed()
    cellAddrs.foreach(addr => {
      val batchEntry = new CellEntry(addr.row, addr.col, addr.value)
      batchEntry.setId(s"${cellFeedUrl.toString}/${addr.idString}")
      BatchUtils.setBatchId(batchEntry, addr.idString)
      BatchUtils.setBatchOperationType(batchEntry, BatchOperationType.QUERY)
      batchRequest.getEntries.add(batchEntry)
    })


    val cellFeed = service.getFeed(cellFeedUrl, classOf[CellFeed])
    val queryBatchResponse =
      service.batch(new URL(cellFeed.getLink(ILink.Rel.FEED_BATCH, ILink.Type.ATOM).getHref),
        batchRequest)
    queryBatchResponse.getEntries.asScala.foldLeft(Map[String,CellEntry]())((m,e) => {
      m + ((BatchUtils.getBatchId(e),e))
    })
    null
  }

  /*def createWorksheet(fileId: String, title: String, header: Option[Seq[String]], vals: Option[Seq[Seq[String]]]): Unit = {
    val c = {
      var hcount = header.getOrElse(Nil).size
      if(hcount == 0){

      }

    }

  }*/

}

object SheetService{

  val SCOPES = "https://spreadsheets.google.com/feeds https://docs.google.com/feeds"
  val FULL = "/private/full"
  val BASE_SPREADSHEET_FEED_URL = "https://spreadsheets.google.com/feeds/spreadsheets/"

  def apply(cc: ClientCredentials)(implicit ec: ExecutionContext) = new SheetService(cc)
  def apply(token: String, refreshToken: Option[String], clientId: String, clientSecret: String)(implicit ec: ExecutionContext) = {
    new SheetService(ClientCredentials(clientId,clientSecret, token, refreshToken))
  }
}

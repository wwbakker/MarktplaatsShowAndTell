package controllers


import akka.stream.{IOResult, Materializer}
import akka.stream.scaladsl.{FileIO, Framing, Source}
import akka.util.ByteString
import javax.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class FileUploadController @Inject()(cc: ControllerComponents,
                                     implicit val mat : Materializer,
                                     implicit val ec : ExecutionContext) extends AbstractController(cc) {
  def upload = Action.async(parse.temporaryFile) { request =>
    val source = FileIO.fromPath(request.body.path)

//    FileIO.fromPath(request.body.path)
//      .via(Framing.delimiter(ByteString("\n"), 1024))
//      .map(_.utf8String.spl)
  }

  def fileFormatFuture(source : Source[ByteString, IOResult]) : Future[FileFormat] =
    firstLineFuture(source).map(firstLine =>
      if (firstLine.contains(","))
        CsvFormat
      else
        PrnFormat(determineColumnLengths(firstLine))
    )


  def firstLineFuture(source : Source[ByteString, IOResult]) : Future[String] =
    source
      // take bytes until the first newline is found
      .takeWhile(byteString => !byteString.utf8String.contains("\n"))
      // concatenate all the bytes until then
      .runFold(ByteString(""))(_.concat(_))
      // convert it to a string
      .map(_.utf8String)
      // remove the bytes after the newline
      .map(content => content.substring(0, content.indexOf('\n')))

  def determineColumnLengths(firstLine : String) : Seq[Int] = {
    // Columns consist of:
    // 1 or more non-whitespace characters
    // followed by 0 or more spaces
    val columnPattern = "\\S+\\s*".r
    columnPattern.findAllIn(firstLine).map(_.length).toSeq
  }

  sealed trait FileFormat
  case object CsvFormat extends FileFormat
  case class PrnFormat(columnLengths : Seq[Int]) extends FileFormat
}

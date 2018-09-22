package controllers


import akka.stream.{IOResult, Materializer}
import akka.stream.scaladsl.{FileIO, Framing, Source}
import akka.util.ByteString
import javax.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}
import parsers._
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

class FileUploadController @Inject()(cc: ControllerComponents,
                                     implicit val mat : Materializer,
                                     implicit val ec : ExecutionContext) extends AbstractController(cc) {
  val logger : Logger = Logger(classOf[FileUploadController])

  def upload = Action.async(parse.temporaryFile) { request =>
    val inputSource = FileIO.fromPath(request.body.path)
    val linesSourceFuture : Future[Source[FileFormat.SplitColumns, Future[IOResult]]] =
    fileFormatFuture(inputSource).map(fileFormat =>
      FileIO.fromPath(request.body.path)
        .via(Framing.delimiter(ByteString("\n"), 1024))
        .map(_.utf8String)
        .map(fileFormat.lineSplitInColumns)
        .map{parseResult =>
          parseResult.swap.foreach(logger.error(_))
          parseResult
        }.collect[Seq[String]]{
          case Right(columns) => columns
        }

    )
    val linesSource = Source.fromFutureSource(linesSourceFuture)

    ???
  }

  def fileFormatFuture(source : Source[ByteString, _]) : Future[FileFormat] =
    firstLineFuture(source).map(firstLine =>
      if (firstLine.contains(","))
        CsvFormat
      else
        PrnFormat(determineColumnLengths(firstLine))
    )


  def firstLineFuture(source : Source[ByteString, _]) : Future[String] =
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





}

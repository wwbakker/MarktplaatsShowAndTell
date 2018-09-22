package controllers


import akka.NotUsed
import akka.stream.scaladsl.{FileIO, Flow, Source}
import akka.stream.{IOResult, Materializer}
import javax.inject.Inject
import parsers.NewlineSeperatedFileFormat.{ParseError, SplitColumns}
import parsers._
import play.api.Logger
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class FileUploadController @Inject()(cc: ControllerComponents,
                                     implicit val mat : Materializer,
                                     implicit val ec : ExecutionContext) extends AbstractController(cc) {
  val logger : Logger = Logger(classOf[FileUploadController])

  def upload = Action.async(parse.temporaryFile) { request =>
    val inputSource = FileIO.fromPath(request.body.path)
    val linesSourceFuture : Future[Source[NewlineSeperatedFileFormat.SplitColumns, Future[IOResult]]] =
    NewlineSeperatedFileFormat.fileFormatFuture(inputSource).map(format =>
      inputSource
        .via(NewlineSeperatedFileFormat.lineReaderFlow)
        .drop(1) // ignore the header line
        .map(format.lineSplitInColumns)
        .via(logParseErrorAndCollectResultsFlow)
    )
    val linesSource : Source[NewlineSeperatedFileFormat.SplitColumns, Future[Future[IOResult]]] =
      Source.fromFutureSource(linesSourceFuture)

    ???
  }

  def logParseErrorAndCollectResultsFlow : Flow[Either[ParseError, SplitColumns], SplitColumns, NotUsed] =
    Flow[Either[ParseError, SplitColumns]].map{parseResult =>
      parseResult.swap.foreach(logger.error(_))
      parseResult
    }.collect[Seq[String]]{
      case Right(columns) => columns
    }


}

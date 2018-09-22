package controllers


import akka.NotUsed
import akka.stream.scaladsl.{FileIO, Flow, Source}
import akka.stream.{IOResult, Materializer}
import akka.util.ByteString
import javax.inject.Inject
import model.ImportEntry
import parsers.NewlineSeperatedFileFormat.{ParseError, SplitColumns}
import parsers._
import play.api.Logger
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext}

class FileUploadController @Inject()(cc: ControllerComponents,
                                     importEntryParser: ImportEntryParser,
                                     implicit val mat : Materializer,
                                     implicit val ec : ExecutionContext) extends AbstractController(cc) {
  implicit val logger : Logger = Logger(classOf[FileUploadController])

  def upload = Action.async(parse.temporaryFile) { request =>
    importEntryParser.importEntriesSource(request.body.path)
        .via(logParseErrorAndCollectResultsFlow)

    ???
  }

  def logParseErrorAndCollectResultsFlow[A] : Flow[Either[ParseError, A], A, NotUsed] =
    Flow[Either[ParseError, A]].map{parseResult =>
      parseResult.swap.foreach(logger.error(_))
      parseResult
    }.collect[A]{
      case Right(columns) => columns
    }


}

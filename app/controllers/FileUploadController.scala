package controllers


import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import importing.Importer
import importing.Importer.ImportErrorOr
import javax.inject.Inject
import play.api.Logger
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

class FileUploadController @Inject()(cc: ControllerComponents,
                                     importEntryParser: Importer,
                                     implicit val mat : Materializer,
                                     implicit val ec : ExecutionContext) extends AbstractController(cc) {
  implicit val logger : Logger = Logger(classOf[FileUploadController])

  def upload = Action.async(parse.temporaryFile) { request =>
    importEntryParser.importEntriesSource(request.body.path)
        .via(logParseErrorAndCollectResultsFlow)

    ???
  }

  def logParseErrorAndCollectResultsFlow[A] : Flow[ImportErrorOr[A], A, NotUsed] =
    Flow[ImportErrorOr[A]].map{parseResult =>
      parseResult.swap.foreach(logger.error(_))
      parseResult
    }.collect[A]{
      case Right(columns) => columns
    }


}

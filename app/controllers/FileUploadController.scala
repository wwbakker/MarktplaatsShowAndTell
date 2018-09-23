package controllers


import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink}
import importing.Importer
import importing.Importer.ImportErrorOr
import javax.inject.Inject
import play.api.Logger
import play.api.libs.Files
import play.api.mvc.{AbstractController, Action, ControllerComponents, MultipartFormData}
import repositories.CreditLimitRepository

import scala.concurrent.{ExecutionContext, Future}

class FileUploadController @Inject()(cc: ControllerComponents,
                                     importEntryParser: Importer,
                                     creditLimitRepository: CreditLimitRepository,
                                     implicit val mat : Materializer,
                                     implicit val ec : ExecutionContext) extends AbstractController(cc) {
  implicit val logger : Logger = Logger(classOf[FileUploadController])

  def upload: Action[MultipartFormData[Files.TemporaryFile]] = Action.async(parse.multipartFormData) { request =>
    request.body.file("temporaryFile").map(file =>
      importEntryParser.importEntriesSource(file.ref)
        .via(logParseErrorAndCollectResultsFlow)
        .mapAsync(1)(creditLimitRepository.insert)
        .runWith(Sink.ignore)
        .map(_ => Redirect(routes.HomeController.index()))
    ).getOrElse(Future.successful(NotAcceptable("Missing file")))
  }

  def logParseErrorAndCollectResultsFlow[A] : Flow[ImportErrorOr[A], A, NotUsed] =
    Flow[ImportErrorOr[A]].map{parseResult =>
      parseResult.swap.foreach(logger.error(_))
      parseResult
    }.collect[A]{
      case Right(columns) => columns
    }


}

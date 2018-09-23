package controllers


import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink}
import importing.CreditLimitReader
import importing.CreditLimitReader.ReadErrorOr
import javax.inject.Inject
import play.api.Logger
import play.api.libs.Files
import play.api.mvc.{AbstractController, Action, ControllerComponents, MultipartFormData}
import repositories.CreditLimitRepository

import scala.concurrent.{ExecutionContext, Future}

class FileUploadController @Inject()(cc: ControllerComponents,
                                     creditLimitReader: CreditLimitReader,
                                     creditLimitRepository: CreditLimitRepository,
                                     implicit val mat : Materializer,
                                     implicit val ec : ExecutionContext) extends AbstractController(cc) {
  implicit val logger : Logger = Logger(classOf[FileUploadController])

  def upload: Action[MultipartFormData[Files.TemporaryFile]] = Action.async(parse.multipartFormData) { request =>
    request.body.file("creditLimits").map(file =>
      creditLimitReader.readerSource(file.ref)
        .via(logReadErrorsAndCollectResultsFlow)
        .mapAsync(1)(creditLimitRepository.insert)
        .runWith(Sink.ignore)
        .map(_ => Redirect(routes.HomeController.index()))
    ).getOrElse(Future.successful(NotAcceptable("Missing file")))
  }

  def logReadErrorsAndCollectResultsFlow[A] : Flow[ReadErrorOr[A], A, NotUsed] =
    Flow[ReadErrorOr[A]].map{ parseResult =>
      parseResult.swap.foreach(logger.error(_))
      parseResult
    }.collect[A]{
      case Right(columns) => columns
    }


}

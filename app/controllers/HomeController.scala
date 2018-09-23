package controllers

import javax.inject._
import play.api.mvc._
import repositories.CreditLimitRepository

import scala.concurrent.ExecutionContext

@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               creditLimitRepository: CreditLimitRepository,
                               implicit val ec : ExecutionContext) extends AbstractController(cc) {

  def index(): Action[AnyContent] = Action.async(parse.anyContent) { implicit request: Request[AnyContent] =>
    creditLimitRepository.list()
      .map(creditLimits =>
        Ok(views.html.index(creditLimits)))
  }
}

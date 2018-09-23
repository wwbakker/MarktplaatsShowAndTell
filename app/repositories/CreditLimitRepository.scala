package repositories

import com.google.inject.ImplementedBy
import model.CreditLimit

import scala.concurrent.Future

@ImplementedBy(classOf[InMemoryCreditLimitRepository])
trait CreditLimitRepository {
  def insert(creditLimit : CreditLimit) : Future[CreditLimit]
  def list() : Future[Seq[CreditLimit]]
}

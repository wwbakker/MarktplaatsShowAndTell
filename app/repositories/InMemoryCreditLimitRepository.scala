package repositories
import java.util.UUID

import javax.inject.{Inject, Singleton}
import model.CreditLimit

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.concurrent.TrieMap

@Singleton
class InMemoryCreditLimitRepository @Inject()(implicit val ec : ExecutionContext) extends CreditLimitRepository {
  val creditLimits : TrieMap[UUID, CreditLimit] = new TrieMap[UUID, CreditLimit]()

  override def insert(creditLimit: CreditLimit): Future[CreditLimit] = {
    creditLimits.put(UUID.randomUUID(), creditLimit)
    Future.successful(creditLimit)
  }

  override def list(): Future[Seq[CreditLimit]] =
    Future.successful(creditLimits.values.toSeq)
}

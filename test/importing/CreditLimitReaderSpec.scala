package importing

import java.nio.file.Paths
import java.time.LocalDate

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import akka.stream.{ActorMaterializer, Materializer}
import akka.testkit._
import model.CreditLimit
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

class CreditLimitReaderSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  implicit val ec: ExecutionContext = system.dispatcher
  implicit val mat: Materializer = ActorMaterializer()
  val iep = new CreditLimitReader()

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val expectedDataSet = Vector(
    Right(CreditLimit("Johnson, John", "Voorstraat 32", "3122gg", "020 3849381", 1000000, LocalDate.parse("1987-01-01"))),
    Right(CreditLimit("Anderson, Paul", "Dorpsplein 3A", "4532 AA", "030 3458986", 10909300, LocalDate.parse("1965-12-03"))),
    Right(CreditLimit("Wicket, Steve", "Mendelssohnstraat 54d", "3423 ba", "0313-398475", 93400, LocalDate.parse("1964-06-03"))),
    Right(CreditLimit("Benetar, Pat", "Driehoog 3zwart", "2340 CC", "06-28938945", 54, LocalDate.parse("1964-09-04"))),
    Right(CreditLimit("Gibson, Mal", "Vredenburg 21", "3209 DD", "06-48958986", 5450, LocalDate.parse("1978-11-09"))),
    Right(CreditLimit("Friendly, User", "Sint Jansstraat 32", "4220 EE", "0885-291029", 6360, LocalDate.parse("1980-08-10"))),
    Right(CreditLimit("Smith, John", "Børkestraße 32", "87823", "+44 728 889838", 989830, LocalDate.parse("1999-09-20")))
  )

  "ImportEntryParser" must {
    "import prn format correctly" in {
      val result = Await.result(
        iep.readerSource(Paths.get("Workbook2.prn")).runWith(Sink.seq),
        10.seconds)
      result.zip(expectedDataSet).foreach{case (x, y) => x shouldBe y}
    }

    "import csv format correctly" in {
      val result = Await.result(
        iep.readerSource(Paths.get("Workbook2.csv")).runWith(Sink.seq),
        10.seconds)
      result.zip(expectedDataSet).foreach{case (x, y) => x shouldBe y}
    }

  }
}

package parsers

import java.nio.file.{Path, Paths}

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import akka.stream.{ActorMaterializer, Materializer}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec, WordSpecLike}
import akka.stream.testkit._
import akka.testkit._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

class ImportEntryParserSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  implicit val ec: ExecutionContext = system.dispatcher
  implicit val mat: Materializer = ActorMaterializer()
  val iep = new ImportEntryParser()

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "ImportEntryParser" must {
    "should import correctly" in {
      val result = Await.result(
        iep.importEntriesSource(Paths.get("Workbook2.prn")).runWith(Sink.seq),
        10.seconds)
      result shouldBe Seq()

    }

  }
}

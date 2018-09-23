package importing.parsers

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Framing}
import akka.util.ByteString
import importing.Importer

object NewlineSeperatedEntryParser {
  type SplitColumns = Seq[String]

  private def filterCarriageReturns : Flow[ByteString, ByteString, NotUsed] =
    Flow[ByteString].map(_.filter(_ != 13))

  private def splitOnNewlines : Flow[ByteString, ByteString, NotUsed] =
    Framing.delimiter(ByteString("\n"), 1024)

  def lineReaderFlow: Flow[ByteString, ByteString, NotUsed] =
    Flow[ByteString].via(filterCarriageReturns).via(splitOnNewlines)

}

trait NewlineSeperatedEntryParser {

  import NewlineSeperatedEntryParser._

  def lineSplitInColumns(line: String): Either[Importer.ErrorMessage, SplitColumns]
}
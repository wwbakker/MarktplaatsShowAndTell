package importing.parsers

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Framing}
import akka.util.ByteString
import importing.Importer

object NewlineSeperatedEntryParser {
  type SplitColumns = Seq[String]

  def lineReaderFlow: Flow[ByteString, ByteString, NotUsed] =
    Framing.delimiter(ByteString("\n"), 1024)

}

trait NewlineSeperatedEntryParser {

  import NewlineSeperatedEntryParser._

  def lineSplitInColumns(line: String): Either[Importer.ErrorMessage, SplitColumns]
}
package importing.parsers

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Framing}
import akka.util.ByteString
import importing.Importer

object NewlineSeperatedEntryParser {
  type SplitColumns = Seq[String]

  def lineReaderFlow: Flow[ByteString, String, NotUsed] =
    Framing.delimiter(ByteString("\n"), 1024)
      .map(_.utf8String)

}

trait NewlineSeperatedEntryParser {

  import NewlineSeperatedEntryParser._

  def lineSplitInColumns(line: String): Either[Importer.ErrorMessage, SplitColumns]
}
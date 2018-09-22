package parsers

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Framing, Source}
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}

object NewlineSeperatedFileFormat {
  type ParseError = String
  type SplitColumns = Seq[String]


  def lineReaderFlow: Flow[ByteString, String, NotUsed] =
    Framing.delimiter(ByteString("\n"), 1024)
      .map(_.utf8String)


  // Determines the file format based on the first line of the input.
  def fileFormatFuture(source: Source[ByteString, _])(implicit ec: ExecutionContext, mat: Materializer): Future[NewlineSeperatedFileFormat] =
    firstLineFuture(source).map(firstLine =>
      if (firstLine.contains(","))
        CsvFormat
      else
        PrnFormat(PrnFormat.determineColumnLengths(firstLine))
    )

  def firstLineFuture(source: Source[ByteString, _])(implicit ec: ExecutionContext, mat: Materializer): Future[String] =
    source
      // concatenate all the bytes until then
      .runFold(ByteString(""))(_.concat(_))
      // convert it to a string
      .map(_.utf8String)
      // remove the bytes after the newline
      .map ( content => content.takeWhile( _ != '\n') )


}

trait NewlineSeperatedFileFormat {

  import NewlineSeperatedFileFormat._

  def lineSplitInColumns(line: String): Either[ParseError, SplitColumns]
}
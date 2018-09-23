package importing

import java.nio.file.Path

import akka.stream.scaladsl.{FileIO, Source}
import akka.stream.{IOResult, Materializer}
import akka.util.ByteString
import importing.Importer.ImportErrorOr
import importing.mappers.{ColumnMapper, CsvImportEntryMapper, PrnImportEntryMapper}
import importing.parsers.{CsvParser, NewlineSeperatedEntryParser, PrnParser}
import javax.inject.Inject
import model.ImportEntry

import scala.concurrent.{ExecutionContext, Future}

object Importer {
  type ErrorMessage = String
  type ImportErrorOr[A] = Either[ErrorMessage, A]
}

class Importer @Inject()(implicit val mat : Materializer,
                         implicit val ec : ExecutionContext) {

  def importEntriesSource(file : Path) : Source[ImportErrorOr[ImportEntry], Future[Future[IOResult]]] = {
    def inputSource : Source[ByteString, Future[IOResult]] =
      FileIO.fromPath(file)
    val linesSourceFuture =
      parserAndMapperFuture(inputSource).map { case (parser, mapper) =>
        inputSource
          // split the files into lines
          .via(NewlineSeperatedEntryParser.lineReaderFlow)
          .drop(1) // ignore the header line
          // split up the columns
          .map(parser.lineSplitInColumns)
          // map the columns to an ImportEntry object
          .map(_.flatMap(mapper.fromColumns))
      }

    Source.fromFutureSource(linesSourceFuture)
  }

  // Auto-detects the file format based on the first line of the input.
  def parserAndMapperFuture(source: Source[ByteString, _])(implicit ec: ExecutionContext, mat: Materializer): Future[(NewlineSeperatedEntryParser, ColumnMapper)] =
    firstLineFuture(source).map(firstLine =>
      if (firstLine.contains(","))
        (CsvParser, CsvImportEntryMapper)
      else
        (PrnParser(PrnParser.determineColumnLengths(firstLine)), PrnImportEntryMapper)
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

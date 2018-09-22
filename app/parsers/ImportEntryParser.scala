package parsers

import java.nio.file.Path

import akka.stream.{IOResult, Materializer}
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import javax.inject.Inject
import model.ImportEntry

import scala.concurrent.{ExecutionContext, Future}

class ImportEntryParser @Inject()(implicit val mat : Materializer,
                                  implicit val ec : ExecutionContext) {

  def importEntriesSource(file : Path) : Source[Either[ImportEntry.ParserError, ImportEntry], Future[Future[IOResult]]] = {
    def inputSource : Source[ByteString, Future[IOResult]] =
      FileIO.fromPath(file)
    val linesSourceFuture =
      NewlineSeperatedFileFormat.fileFormatFuture(inputSource).map(format =>
        inputSource
          .via(NewlineSeperatedFileFormat.lineReaderFlow)
          .drop(1) // ignore the header line
          .map(format.lineSplitInColumns)
          .map(_.flatMap(ImportEntry.fromColumns))
      )

    Source.fromFutureSource(linesSourceFuture)
  }

}

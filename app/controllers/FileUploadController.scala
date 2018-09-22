package controllers


import akka.stream.{IOResult, Materializer}
import akka.stream.scaladsl.{FileIO, Framing, Source}
import akka.util.ByteString
import javax.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}
import cats._
import cats.implicits._

import scala.concurrent.{ExecutionContext, Future}

class FileUploadController @Inject()(cc: ControllerComponents,
                                     implicit val mat : Materializer,
                                     implicit val ec : ExecutionContext) extends AbstractController(cc) {
  def upload = Action.async(parse.temporaryFile) { request =>
    val source = FileIO.fromPath(request.body.path)

//    FileIO.fromPath(request.body.path)
//      .via(Framing.delimiter(ByteString("\n"), 1024))
//      .map(_.utf8String.spl)
  }

  def fileFormatFuture(source : Source[ByteString, IOResult]) : Future[FileFormat] =
    firstLineFuture(source).map(firstLine =>
      if (firstLine.contains(","))
        CsvFormat
      else
        PrnFormat(determineColumnLengths(firstLine))
    )


  def firstLineFuture(source : Source[ByteString, IOResult]) : Future[String] =
    source
      // take bytes until the first newline is found
      .takeWhile(byteString => !byteString.utf8String.contains("\n"))
      // concatenate all the bytes until then
      .runFold(ByteString(""))(_.concat(_))
      // convert it to a string
      .map(_.utf8String)
      // remove the bytes after the newline
      .map(content => content.substring(0, content.indexOf('\n')))

  def determineColumnLengths(firstLine : String) : Seq[Int] = {
    // Columns consist of:
    // 1 or more non-whitespace characters
    // followed by 0 or more spaces
    val columnPattern = "\\S+\\s*".r
    columnPattern.findAllIn(firstLine).map(_.length).toSeq
  }

  sealed trait FileFormat {
    type MatchError = String
    def lineSplitInColumns(line : String) : Either[MatchError, Seq[String]]
  }
  case object CsvFormat extends FileFormat {
    case class ParseColumnState(restOfLine : String, parsedColumns : Seq[String])


    def parseQuotedColumn(pcs : ParseColumnState) : Either[MatchError, ParseColumnState] = {
      // take the first match which is surrounded by quotes and ends in either a , or at the end of the line
      // this does not match on escaped double quotes inside a quoted string
      val fullMatchEither = Either.fromOption(
        "\".*?\"(,|$)".r.findFirstIn(pcs.restOfLine),
        s"Irregular amount of quotes in '${pcs.restOfLine}'"
      )
      val charactersRead = fullMatchEither.map(_.length).getOrElse(0)
      fullMatchEither
        // remove the leading "
        .map(_.substring(1))
        // remove trailing " and possibly ,
        .map(_.replace("\",?$", ""))
        // unescape escaped quotes
        .map(_.replace("\"\"", "\""))
        .map(columnValue => ParseColumnState(pcs.restOfLine.substring(charactersRead), pcs.parsedColumns :+ columnValue))
    }

    def parseUnquotedColumn(pcs : ParseColumnState) : Either[MatchError, ParseColumnState] = {
      val columnValue = pcs.restOfLine.takeWhile(_ != ',')
      val charactersRead = columnValue.length + 1
      Right(ParseColumnState(pcs.restOfLine.substring(charactersRead), pcs.parsedColumns :+ columnValue))
    }

    def parseColumns(pcs : ParseColumnState) : Either[MatchError, ParseColumnState] = {
      if (pcs.restOfLine.isEmpty)
        Right(pcs)
      else if (pcs.restOfLine.startsWith("\""))
        parseQuotedColumn(pcs).flatMap(parseColumns)
      else
        parseUnquotedColumn(pcs).flatMap(parseColumns)
    }

    override def lineSplitInColumns(line : String) : Either[MatchError, Seq[String]] =
      parseColumns(ParseColumnState(line, Seq.empty[String])).map(_.parsedColumns)

    private def firstColumn(line : String) : Option[String] =
      if (line.isEmpty)
        None
      else if (line.startsWith("\"")) {
        val fullMatchOption = "\".*?\"(,|$)".r.findFirstIn(line)
        fullMatchOption
          // remove the leading " and the trailing ",
          .map(fullMatch => fullMatch.substring(1, fullMatch.length - 2))
          // RFC-4180, paragraph "If double-quotes are used to enclose fields,
          // then a double-quote appearing inside a field must be escaped by preceding
          // it with another double quote."
          .map(_.replace("\"\"", "\""))
      } else {
        // take the the everything until
        line.split(",", 2).headOption
      }


  }

  case class PrnFormat(columnLengths : Seq[Int]) extends FileFormat {
    private case class SubstringIndices(startIndex : Int, // inclusive
                                        endIndex : Int  ) // exclusive

    private val startEndIndices : List[SubstringIndices] =
      columnLengths.foldLeft(List.empty[SubstringIndices]){ (accumulator, columnLength) =>
        val startIndex = accumulator.headOption.map(_.endIndex).getOrElse(0)
        SubstringIndices(startIndex, startIndex + columnLength) :: accumulator
      }.reverse

    override def lineSplitInColumns(line : String) : Either[MatchError, Seq[String]] =
      Either.catchOnly[StringIndexOutOfBoundsException](
        startEndIndices.map(indices => line.substring(indices.startIndex, indices.endIndex))
      ).swap.map(_ => "Content lines are not even with header line.").swap

  }
}

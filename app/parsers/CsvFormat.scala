package parsers

import cats._
import cats.implicits._

case object CsvFormat extends NewlineSeperatedFileFormat {

  import NewlineSeperatedFileFormat._

  case class ParseColumnState(restOfLine: String, parsedColumns: Seq[String])

  def parseQuotedColumn(pcs: ParseColumnState): Either[ParseError, ParseColumnState] = {
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
      .map(_.replaceFirst("\",?$", ""))
      // unescape escaped quotes
      .map(_.replaceFirst("\"\"", "\""))
      .map(columnValue => ParseColumnState(pcs.restOfLine.substring(charactersRead), pcs.parsedColumns :+ columnValue))
  }

  def parseUnquotedColumn(pcs: ParseColumnState): Either[ParseError, ParseColumnState] = {
    val columnValue = pcs.restOfLine.takeWhile(_ != ',')
    val commaAfterValue = pcs.restOfLine.contains(',')
    val nextPossibleColumnIndex =
      if (commaAfterValue) columnValue.length + 1
      else columnValue.length
    Right(
      ParseColumnState(
        restOfLine = pcs.restOfLine.substring(nextPossibleColumnIndex),
        parsedColumns = pcs.parsedColumns :+ columnValue)
    )
  }

  def parseColumns(pcs: ParseColumnState): Either[ParseError, ParseColumnState] = {
    if (pcs.restOfLine.isEmpty)
      Right(pcs)
    else if (pcs.restOfLine.startsWith("\""))
      parseQuotedColumn(pcs).flatMap(parseColumns)
    else
      parseUnquotedColumn(pcs).flatMap(parseColumns)
  }

  override def lineSplitInColumns(line: String): Either[ParseError, SplitColumns] =
    parseColumns(ParseColumnState(line, Seq.empty[String])).map(_.parsedColumns)

}
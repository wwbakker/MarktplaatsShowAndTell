package importing.parsers

import cats.implicits._
import importing.CreditLimitReader.ReadErrorOr

case object CsvParser extends NewlineSeperatedEntryParser {

  import NewlineSeperatedEntryParser._

  case class ParseColumnState(unparsedLine: String, parsedColumns: Seq[String])

  def parseQuotedColumn(pcs: ParseColumnState): ReadErrorOr[ParseColumnState] = {
    // take the first match which is surrounded by quotes and ends in either a , or at the end of the line
    // this does not match on escaped double quotes inside a quoted string
    val fullMatchEither = Either.fromOption(
      "\".*?\"(,|$)".r.findFirstIn(pcs.unparsedLine),
      s"Irregular amount of quotes in '${pcs.unparsedLine}'"
    )
    val charactersRead = fullMatchEither.map(_.length).getOrElse(0)
    fullMatchEither
      // remove the leading "
      .map(_.substring(1))
      // remove trailing " and possibly ,
      .map(_.replaceFirst("\",?$", ""))
      // unescape escaped quotes
      .map(_.replaceAll("\"\"", "\""))
      .map(columnValue => ParseColumnState(pcs.unparsedLine.substring(charactersRead), pcs.parsedColumns :+ columnValue))
  }

  def parseUnquotedColumn(pcs: ParseColumnState): ReadErrorOr[ParseColumnState] = {
    val columnValue = pcs.unparsedLine.takeWhile(_ != ',')
    val commaAfterValue = pcs.unparsedLine.contains(',')
    val nextPossibleColumnIndex =
      if (commaAfterValue) columnValue.length + 1
      else columnValue.length
    Right(
      ParseColumnState(
        unparsedLine = pcs.unparsedLine.substring(nextPossibleColumnIndex),
        parsedColumns = pcs.parsedColumns :+ columnValue)
    )
  }

  def parseColumns(pcs: ParseColumnState): ReadErrorOr[ParseColumnState] = {
    if (pcs.unparsedLine.isEmpty)
      Right(pcs)
    else if (pcs.unparsedLine.startsWith("\""))
      parseQuotedColumn(pcs).flatMap(parseColumns)
    else
      parseUnquotedColumn(pcs).flatMap(parseColumns)
  }

  override def lineSplitInColumns(line: String): ReadErrorOr[SplitColumns] =
    parseColumns(ParseColumnState(line, Seq.empty[String])).map(_.parsedColumns)

}
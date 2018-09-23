package importing.parsers

import cats.implicits._
import importing.CreditLimitReader.ReadErrorOr

object PrnParser {
  def determineColumnWidth(firstLine : String) : Seq[Int] = {
    // Columns consist of:
    // 1 or more non-whitespace characters
    // followed by 0 or more spaces
    val columnPattern = "\\S+\\s*".r
    columnPattern.findAllIn(firstLine).map(_.length).toSeq
  }
}

case class PrnParser(columnLengths : Seq[Int]) extends NewlineSeperatedEntryParser {
  import NewlineSeperatedEntryParser._

  private case class SubstringIndices(startIndex : Int, // inclusive
                                      endIndex : Int  ) // exclusive

  private val startEndIndices : List[SubstringIndices] =
    columnLengths.foldLeft(List.empty[SubstringIndices]){ (accumulator, columnLength) =>
      val startIndex = accumulator.headOption.map(_.endIndex).getOrElse(0)
      SubstringIndices(startIndex, startIndex + columnLength) :: accumulator
    }.reverse

  override def lineSplitInColumns(line : String) : ReadErrorOr[SplitColumns] =
    Either.catchOnly[StringIndexOutOfBoundsException](
      startEndIndices.map(indices => line.substring(indices.startIndex, indices.endIndex).trim)
    ).swap.map(_ => "A content line is not even with header line.").swap

}
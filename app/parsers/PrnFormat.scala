package parsers

import cats._
import cats.implicits._

object PrnFormat {
  def determineColumnLengths(firstLine : String) : Seq[Int] = {
    // Columns consist of:
    // 1 or more non-whitespace characters
    // followed by 0 or more spaces
    val columnPattern = "\\S+\\s*".r
    columnPattern.findAllIn(firstLine).map(_.length).toSeq
  }
}

case class PrnFormat(columnLengths : Seq[Int]) extends NewlineSeperatedFileFormat {
  import NewlineSeperatedFileFormat._

  private case class SubstringIndices(startIndex : Int, // inclusive
                                      endIndex : Int  ) // exclusive

  private val startEndIndices : List[SubstringIndices] =
    columnLengths.foldLeft(List.empty[SubstringIndices]){ (accumulator, columnLength) =>
      val startIndex = accumulator.headOption.map(_.endIndex).getOrElse(0)
      SubstringIndices(startIndex, startIndex + columnLength) :: accumulator
    }.reverse

  override def lineSplitInColumns(line : String) : Either[ParseError, SplitColumns] =
    Either.catchOnly[StringIndexOutOfBoundsException](
      startEndIndices.map(indices => line.substring(indices.startIndex, indices.endIndex))
    ).swap.map(_ => "Content lines are not even with header line.").swap

}
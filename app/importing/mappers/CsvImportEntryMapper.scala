package importing.mappers

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import cats.implicits._
import importing.Importer.ImportErrorOr
import model.ImportEntry

object CsvImportEntryMapper extends ColumnMapper {
  override def fromColumns(columns : Seq[String]) : ImportErrorOr[ImportEntry] =
    Either.catchNonFatal(columns.toList match {
      case name :: address :: postalCode :: phoneNumber :: creditLimitInEuros :: birthday :: Nil =>
        ImportEntry(name, address, postalCode,
          phoneNumber, eurosToCents(creditLimitInEuros),
          parseBirthDay(birthday))
    }).swap.map{
      case e : MatchError =>
        "Cannot parse ImportEntry: A line does not have the correct number of columns"
      case e =>
        "Cannot parse ImportEntry:" + e.toString
    }.swap

  def eurosToCents(creditLimitInEuros : String) : Int =
    (creditLimitInEuros.toFloat * 100f).floor.toInt

  lazy val slashPattern : DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/uuuu")

  def parseBirthDay(s : String) : LocalDate =
      LocalDate.parse(s, slashPattern)
}

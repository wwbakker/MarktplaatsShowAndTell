package importing.mappers

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import cats.implicits._
import importing.Importer.ImportErrorOr
import model.ImportEntry


object PrnImportEntryMapper extends ColumnMapper {

  override def fromColumns(columns: Seq[String]): ImportErrorOr[ImportEntry] =
    Either.catchNonFatal(columns.toList match {
      case name :: address :: postalCode :: phoneNumber :: creditLimitInCents :: birthday :: Nil =>
        ImportEntry(name, address, postalCode,
          phoneNumber, creditLimitInCents.toInt,
          parseBirthDay(birthday))
    }).swap.map {
      case e: MatchError =>
        "Cannot parse ImportEntry: A line does not have the correct number of columns"
      case e =>
        "Cannot parse ImportEntry:" + e.toString
    }.swap

  lazy val compactPattern: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd")

  def parseBirthDay(s: String): LocalDate =
    LocalDate.parse(s, compactPattern)

}
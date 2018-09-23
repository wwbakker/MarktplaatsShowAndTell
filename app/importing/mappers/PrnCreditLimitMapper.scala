package importing.mappers

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import cats.implicits._
import importing.Importer.ImportErrorOr
import model.CreditLimit


object PrnCreditLimitMapper extends ColumnMapper {

  override def fromColumns(columns: Seq[String]): ImportErrorOr[CreditLimit] =
    Either.catchNonFatal(columns.toList match {
      case name :: address :: postalCode :: phoneNumber :: creditLimitInCents :: birthday :: Nil =>
        CreditLimit(name, address, postalCode,
          phoneNumber, creditLimitInCents.toInt,
          parseBirthDay(birthday))
    }).swap.map {
      case e: MatchError =>
        "Cannot parse credit limit: A line does not have the correct number of columns"
      case e =>
        "Cannot parse credit limit:" + e.toString
    }.swap

  lazy val compactPattern: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd")

  def parseBirthDay(s: String): LocalDate =
    LocalDate.parse(s, compactPattern)

}
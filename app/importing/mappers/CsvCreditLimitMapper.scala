package importing.mappers

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import cats.implicits._
import importing.CreditLimitReader.ReadErrorOr
import model.CreditLimit

object CsvCreditLimitMapper extends ColumnMapper {

  override def fromColumns(columns : Seq[String]) : ReadErrorOr[CreditLimit] =
    columns.toList match {
      case name :: address :: postalCode :: phoneNumber :: creditLimitInEuros :: birthday :: Nil =>
        Either.catchNonFatal(CreditLimit(name, address, postalCode,
          phoneNumber, eurosToCents(creditLimitInEuros),
          parseBirthDay(birthday)))
          .leftMap(e => "Cannot parse credit limit:" + e.toString)
      case _ =>
        Left("Cannot parse credit limit: A line does not have the correct number of columns")
    }

  def eurosToCents(creditLimitInEuros : String) : Int =
    (creditLimitInEuros.toFloat * 100f).floor.toInt

  lazy val slashPattern : DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/uuuu")

  def parseBirthDay(s : String) : LocalDate =
      LocalDate.parse(s, slashPattern)
}

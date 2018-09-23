package importing.mappers

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import cats.implicits._
import importing.CreditLimitReader.ReadErrorOr
import model.CreditLimit

object PrnCreditLimitMapper extends ColumnMapper {

  override def fromColumns(columns: Seq[String]): ReadErrorOr[CreditLimit] =
    columns.toList match {
      case name :: address :: postalCode :: phoneNumber :: creditLimitInCents :: birthday :: Nil =>
        Either.catchNonFatal(CreditLimit(name, address, postalCode,
          phoneNumber, creditLimitInCents.toInt,
          parseBirthDay(birthday)))
        .leftMap(e => "Cannot parse credit limit:" + e.toString)
      case _ =>
        Left("Cannot parse credit limit: A line does not have the correct number of columns")
    }

  lazy val compactPattern: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd")

  def parseBirthDay(s: String): LocalDate =
    LocalDate.parse(s, compactPattern)

}
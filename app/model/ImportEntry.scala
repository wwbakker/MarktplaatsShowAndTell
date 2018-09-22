package model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import cats.implicits._
import cats._

object ImportEntry {
  type ParserError = String
  def fromColumns(columns : Seq[String]) : Either[ParserError, ImportEntry] =
    Either.catchNonFatal(columns.toList match {
      case name :: address :: postalCode :: phoneNumber :: creditLimit :: birthday :: Nil =>
        ImportEntry(name, address, postalCode,
          phoneNumber, BigDecimal(creditLimit),
          parseBirthDay(birthday))
    }).swap.map{
      case e : MatchError =>
        "Cannot parse ImportEntry: A line does not have the correct number of columns"
      case e =>
        "Cannot parse ImportEntry:" + e.toString
    }.swap

  lazy val slashPattern : DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy")
  lazy val compactPattern : DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd")

  def parseBirthDay(s : String) : LocalDate =
    if (s.contains("/"))
      LocalDate.parse(s, slashPattern)
    else
      LocalDate.parse(s, compactPattern)

}

case class ImportEntry(name : String,
                       address: String,
                       postalCode : String,
                       phoneNumber : String,
                       creditLimit : BigDecimal,
                       birthday : LocalDate)

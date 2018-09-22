package parsers

object FileFormat {
  type ParseError = String
  type SplitColumns = Seq[String]
}
trait FileFormat {
  import FileFormat._
  def lineSplitInColumns(line : String) : Either[ParseError, SplitColumns]
}
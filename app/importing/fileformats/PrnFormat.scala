package importing.fileformats
import akka.util.ByteString
import importing.mappers.{ColumnMapper, PrnCreditLimitMapper}
import importing.parsers.{NewlineSeperatedEntryParser, PrnParser}

class PrnFormat(columnLengths : Seq[Int]) extends FileFormat {
  override def decoder: ByteString => String = _.decodeString("Cp1252")
  override def parser: NewlineSeperatedEntryParser = PrnParser(columnLengths)
  override def mapper: ColumnMapper = PrnCreditLimitMapper
}

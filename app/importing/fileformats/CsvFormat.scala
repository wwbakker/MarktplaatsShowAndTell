package importing.fileformats
import akka.util.ByteString
import importing.mappers.{ColumnMapper, CsvImportEntryMapper}
import importing.parsers.{CsvParser, NewlineSeperatedEntryParser}

object CsvFormat extends FileFormat {
  override def decoder: ByteString => String = _.utf8String
  override def parser: NewlineSeperatedEntryParser = CsvParser
  override def mapper: ColumnMapper = CsvImportEntryMapper
}

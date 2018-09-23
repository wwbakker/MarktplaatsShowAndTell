package importing.fileformats

import akka.util.ByteString
import importing.mappers.ColumnMapper
import importing.parsers.NewlineSeperatedEntryParser

trait FileFormat {
  def decoder : ByteString => String
  def parser : NewlineSeperatedEntryParser
  def mapper : ColumnMapper
}

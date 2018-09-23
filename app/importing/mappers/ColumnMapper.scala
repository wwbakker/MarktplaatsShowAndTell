package importing.mappers

import importing.Importer.ImportErrorOr
import model.ImportEntry

trait ColumnMapper {
  def fromColumns(columns : Seq[String]) : ImportErrorOr[ImportEntry]
}

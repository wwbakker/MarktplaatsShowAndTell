package importing.mappers

import importing.Importer.ImportErrorOr
import model.CreditLimit

trait ColumnMapper {
  def fromColumns(columns : Seq[String]) : ImportErrorOr[CreditLimit]
}

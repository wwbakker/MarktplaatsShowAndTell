package importing.mappers

import importing.CreditLimitReader.ReadErrorOr
import model.CreditLimit

trait ColumnMapper {
  def fromColumns(columns : Seq[String]) : ReadErrorOr[CreditLimit]
}

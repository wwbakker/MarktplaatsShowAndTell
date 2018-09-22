package parsers

import org.scalatest.{Matchers, WordSpec}

class CsvFormatSpec extends WordSpec with Matchers{
  "CsvFormat" should {
    "give the correct result with line without quotes" in {
      val line = """a,5,c"""
      CsvFormat.lineSplitInColumns(line) shouldBe Right(Seq("a","5","c"))
    }
    "give the correct result with all columns quoted" in {
      val line = """"a","5","c""""
      CsvFormat.lineSplitInColumns(line) shouldBe Right(Seq("a","5","c"))
    }
    "give the correct result with a combination of quoted and unquoted columns" in {
      val line = """a,"5","c""""
      CsvFormat.lineSplitInColumns(line) shouldBe Right(Seq("a","5","c"))
    }
    "give an error when the quotes are not correct" in {
      val line = """a,5,"c"""
      CsvFormat.lineSplitInColumns(line) shouldBe Left("Irregular amount of quotes in '\"c'")
    }
  }
}

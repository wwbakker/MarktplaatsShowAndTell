package parsers

import org.scalatest.{Matchers, WordSpec}

class PrnFormatSpec extends WordSpec with Matchers{
  "PrnFormat" should {
    val format = PrnFormat(Seq(5, 2, 3))
    "give the correct result with the exact size" in {
      val line = """12345ab123"""
      format.lineSplitInColumns(line) shouldBe Right(Seq("12345","ab","123"))
    }
    "give the correct result with smaller input left aligned" in {
      val line = """1    a 1  """
      format.lineSplitInColumns(line) shouldBe Right(Seq("1","a","1"))
    }
    "give the correct result with smaller input right aligned" in {
      val line = """    1 a  1"""
      format.lineSplitInColumns(line) shouldBe Right(Seq("1","a","1"))
    }
    "give a parse error when input is incorrect" in {
      val line = ""
      format.lineSplitInColumns(line) shouldBe Left("A content line is not even with header line.")
    }
  }
}

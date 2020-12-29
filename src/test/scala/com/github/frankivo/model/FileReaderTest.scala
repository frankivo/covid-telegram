package com.github.frankivo.model

import com.github.frankivo.FileReader
import utest.{ArrowAssert, TestSuite, Tests, test}

import java.time.LocalDate
import scala.io.Source

object FileReaderTest extends TestSuite {
  val tests: Tests = Tests {
    test("read national data with total") {
      val file = Source.fromResource("RIVM_NL_national_20200421.csv")
      val actual = FileReader.readDay(file)

      val expected = DayRecord(LocalDate.parse("2020-04-21"), 729)
      actual ==> expected
    }

    test("read national data without total") {
      val file = Source.fromResource("RIVM_NL_national_20200227.csv")
      val actual = FileReader.readDay(file)

      val expected = DayRecord(LocalDate.parse("2020-02-27"), 0)
      actual ==> expected
    }

    test("read municipal with counts") {
      val file = Source.fromResource("RIVM_NL_municipal_20201031.csv")
      val actual = FileReader.readMunicipal(file)

      actual.date ==> LocalDate.parse("2020-10-31")
      actual.municipalCount.size ==> 355
      actual.municipalCount("Wassenaar") ==> 12
      actual.municipalCount.values.sum ==> 9765
    }

    test("read municipal without counts") {
      val file = Source.fromResource("RIVM_NL_municipal_20200227.csv")
      val actual = FileReader.readMunicipal(file)

      actual.date ==> LocalDate.parse("2020-02-27")
      actual.municipalCount.size ==> 355
      actual.municipalCount.values.sum ==> 0
    }
  }
}

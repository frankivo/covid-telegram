package com.github.frankivo

import java.time.LocalDate

import com.github.frankivo.model.DayRecord
import utest.{ArrowAssert, TestSuite, Tests, test}

import scala.io.Source

object FileReaderTest extends TestSuite {
  val tests: Tests = Tests {
    test("read file with total") {
      val file = Source.fromResource("RIVM_NL_national_20200421.csv")
      val actual = FileReader.readDay(file)

      val expected = DayRecord(LocalDate.parse("2020-04-21"), 729)
      actual ==> expected
    }

    test("read file without total") {
      val file = Source.fromResource("RIVM_NL_national_20200227.csv")
      val actual = FileReader.readDay(file)

      val expected = DayRecord(LocalDate.parse("2020-02-27"), 0)
      actual ==> expected
    }
  }
}
package com.github.frankivo.util

import com.github.frankivo.model.DayRecord
import utest.{ArrowAssert, TestSuite, Tests, test}

import java.time.LocalDate
import scala.io.Source

object FileReaderTest extends TestSuite {
  val tests: Tests = Tests {
    test("read national data with total") {
      val file = Source.fromResource("corrections-2021-03-26.csv")
      val actual = FileReader.readDay(file)

      val expected = DayRecord(LocalDate.parse("2021-03-26"), 7644)
      actual ==> expected
    }
  }
}

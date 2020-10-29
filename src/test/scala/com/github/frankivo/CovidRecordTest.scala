package com.github.frankivo

import java.time.LocalDate

import com.github.frankivo.model.CovidRecord
import com.github.frankivo.model.CovidRecordHelper._
import utest.{ArrowAssert, TestSuite, Tests, test}

object CovidRecordTest extends TestSuite {
  val testData: Seq[CovidRecord] = Seq(
    CovidRecord(LocalDate.parse("1984-04-26"), 50),
    CovidRecord(LocalDate.parse("1984-04-28"), 200),
    CovidRecord(LocalDate.parse("1984-04-27"), 100),
    CovidRecord(LocalDate.parse("1984-04-29"), 400),
  )

  val tests: Tests = Tests {
    test("add day zero") {
      val actual = testData.withDayZero

      actual.length ==> testData.length + 1
      actual.head.date ==> LocalDate.parse("1984-04-25")
      actual.head.count ==> 0
    }

    test("order by date") {
      testData.orderByDate.last.date ==> LocalDate.parse("1984-04-29")
    }

    test("daily counts") {
      val expected: Seq[CovidRecord] = Seq(
        CovidRecord(LocalDate.parse("1984-04-26"), 50),
        CovidRecord(LocalDate.parse("1984-04-27"), 50),
        CovidRecord(LocalDate.parse("1984-04-28"), 100),
        CovidRecord(LocalDate.parse("1984-04-29"), 200),
      )

      val actual = testData.getDailyCounts

      actual.length ==> testData.length
      actual ==> expected
    }
  }
}

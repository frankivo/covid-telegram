package com.github.frankivo

import java.time.LocalDate

import com.github.frankivo.model.{CovidRecord, Statistics}
import utest.{ArrowAssert, TestSuite, Tests, test}

object StatisticsTest extends TestSuite {
  val testData: Statistics = Statistics(
    Seq(
      CovidRecord(LocalDate.parse("2020-04-01"), 1),
      CovidRecord(LocalDate.parse("2020-03-01"), 2),
      CovidRecord(LocalDate.parse("2020-06-11"), 3),
      CovidRecord(LocalDate.parse("2020-03-02"), 4),
      CovidRecord(LocalDate.parse("2020-05-25"), 5)
    )
  )

  val tests: Tests = Tests {
    test("get latest record") {
      testData.latest().count ==> 3
    }

    test("get record for existing date") {
      testData.findDayCount(LocalDate.parse("2020-03-02")).get.count ==> 4
    }

    test("get record for non existing date") {
      testData.findDayCount(LocalDate.parse("2020-01-01")).isEmpty ==> true
    }
  }
}

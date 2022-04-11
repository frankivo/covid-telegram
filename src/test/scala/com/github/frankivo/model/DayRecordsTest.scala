package com.github.frankivo.model

import utest.{ArrowAssert, TestSuite, Tests, test}

import java.time.LocalDate

object DayRecordsTest extends TestSuite {
  val testData: DayRecords = DayRecords(
    Seq(
      DayRecord(LocalDate.parse("2020-04-01"), 1),
      DayRecord(LocalDate.parse("2020-03-01"), 2),
      DayRecord(LocalDate.parse("2020-06-11"), 3),
      DayRecord(LocalDate.parse("2020-03-02"), 4),
      DayRecord(LocalDate.parse("2020-05-25"), 5),
      DayRecord(LocalDate.parse("2020-05-30"), 3)
    )
  )

  val tests: Tests = Tests {
    test("get latest record") {
      testData.latest().count ==> 3
    }

    test("get record for existing date") {
      testData.findDayCount(LocalDate.parse("2020-03-02")).get ==> 4
    }

    test("get record for non existing date") {
      testData.findDayCount(LocalDate.parse("2020-01-01")).isEmpty ==> true
    }

    test("get max record") {
      testData.max().count ==> 5
    }
  }
}

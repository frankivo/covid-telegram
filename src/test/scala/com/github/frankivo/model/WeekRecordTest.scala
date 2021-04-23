package com.github.frankivo.model

import com.github.frankivo.util.DateHelper.LocalDateExtender
import utest.{ArrowAssert, TestSuite, Tests, test}

import java.time.LocalDate

object WeekRecordTest extends TestSuite {

  val tests: Tests = Tests {
    test("current week should be true for today") {
      val now = LocalDate.now()

      val rec = WeekRecord(now.getYear, now.weekNumber, 42)
      rec.isCurrentWeek ==> true
    }
  }
}

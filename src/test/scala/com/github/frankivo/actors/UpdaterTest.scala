package com.github.frankivo.actors

import utest.{ArrowAssert, TestSuite, Tests, test}

import java.time.LocalDate

object UpdaterTest extends TestSuite {

  val tests: Tests = Tests {
    test("get dates for a week") {
      val dates: Seq[LocalDate] = Updater.downloadDates(LocalDate.now.minusWeeks(1))

      dates.length ==> 8 // Week ago + today
      dates.last ==> LocalDate.now()
    }
  }

}

package com.github.frankivo.actors

import utest.{ArrowAssert, TestSuite, Tests, test}

import java.time.LocalDate

object UpdaterTest extends TestSuite {

  val tests: Tests = Tests {
    test("week should return 8 days") {
      Updater
        .dateRange(LocalDate.now.minusWeeks(1))
        .length ==> 8 // Week ago + today
    }

    test("last date should be today") {
      Updater
        .dateRange()
        .last ==> LocalDate.now()
    }
  }
}

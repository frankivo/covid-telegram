package com.github.frankivo.actors

import utest.{ArrowAssert, TestSuite, Tests, test}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object UpdaterTest extends TestSuite {

  val tests: Tests = Tests {
    test("week should return 8 days") {
      Updater
        .downloadDates(LocalDate.now.minusWeeks(1))
        .length ==> 8 // Week ago + today
    }

    test("last date should be today") {
      Updater
        .downloadDates()
        .last ==> LocalDate.now()
    }

    test("formatted string should be correct date") {
      val date = LocalDate.parse("2020-12-29")
      Updater.formatDate(date) ==> "20201229"
    }
  }

}

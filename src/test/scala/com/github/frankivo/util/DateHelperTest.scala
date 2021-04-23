package com.github.frankivo.util

import com.github.frankivo.util.DateHelper.LocalDateExtender
import utest.{ArrowAssert, TestSuite, Tests, test}

import java.time.LocalDate

object DateHelperTest extends TestSuite {

  val tests: Tests = Tests {
    test("week 53") {
      LocalDate.parse("2020-12-31").weekNumber ==> 53
    }

    test("week 0") {
      LocalDate.parse("2021-01-01").weekNumber ==> 0
    }

    test("week 1") {
      LocalDate.parse("2021-01-04").weekNumber ==> 1
    }

    test("birthday") {
      LocalDate.parse("1984-04-25").weekNumber ==> 17
    }
  }
}

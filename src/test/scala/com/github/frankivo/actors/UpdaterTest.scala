package com.github.frankivo.actors

import utest.{ArrowAssert, TestSuite, Tests, test}

import java.time.LocalDate
import scala.io.Source

object UpdaterTest extends TestSuite {
  val tests: Tests = Tests {
    test("should be able to get reportdate from file") {
      val file = Source.fromResource("UpdaterTest.csv")
      Updater.reportDate(file) ==> LocalDate.parse("2021-04-19")
    }

    test("should return LocalDate.MIN on read failure") {
      val file = Source.fromResource("")
      Updater.reportDate(file) ==> LocalDate.MIN
    }
  }
}

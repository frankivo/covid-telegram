package com.github.frankivo.actors

import utest.{ArrowAssert, TestSuite, Tests, test}

import java.time.LocalDate
import scala.io.Source

object UpdaterTest extends TestSuite {
  val tests: Tests = Tests {
    test("should be able to get reportdate from file") {
      val file = Source.fromResource("UpdaterTest.csv")
      Updater.reportDate(file) ==> Some(LocalDate.parse("2021-04-19"))
    }

    test("should return None on read failure") {
      val file = Source.fromResource("")
      Updater.reportDate(file) ==> None
    }

    test("should be able to read csv data") {
      val file = Source.fromResource("UpdaterTest.csv")
      val data = Updater.readData(file)

      data.length ==> 7
      data.filter(_.date.equals(LocalDate.parse("2020-06-04"))).head.count ==> 1
    }
  }
}

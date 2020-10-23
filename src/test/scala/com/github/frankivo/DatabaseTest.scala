package com.github.frankivo

import java.io.File
import java.time.LocalDate

import utest.{ArrowAssert, TestSuite, Tests, test}

import scala.io.Source

object DatabaseTest extends TestSuite {
  new File("./test.db").delete()
  val db = new Database("test.db")

  def sqlFromFile(filename: String): String = Source.fromResource(s"sql/$filename.sql").getLines().mkString

  def count(table: String): Long = {
    val stmt = db.handle.createStatement
    val sql = sqlFromFile("count").format(table)
    val result = stmt.executeQuery(sql)
    result.next()
    val count = result.getLong(1)
    stmt.close()
    count
  }

  val tests: Tests = Tests {
    test("tables are created") {
      count("daily") ==> 0
    }

    test("record can be inserted") {
      db.insertCovidRecord(CovidRecord(LocalDate.now, 42))
      count("daily") ==> 1
    }

    test("multiple records can be inserted") {
      val records = Seq[CovidRecord](
        CovidRecord(LocalDate.parse("2020-10-01"), 10),
        CovidRecord(LocalDate.parse("2020-10-02"), 20),
        CovidRecord(LocalDate.parse("2020-10-03"), 30),
        CovidRecord(LocalDate.parse("2020-10-04"), 40),
      )
      db.clearDaily()
      db.insertCovidRecords(records: _*)
      count("daily") ==> 4
      val result = db.getDayCount(LocalDate.parse("2020-10-03"))
      result.get.count ==> 30
    }

    test("table can be purged") {
      db.insertCovidRecord(CovidRecord(LocalDate.now, 42))
      db.clearDaily()
      count("daily") ==> 0
    }
  }
}

package com.github.frankivo

import java.sql.{Connection, DriverManager}
import java.time.LocalDate

import scala.io.Source

// Examples from https://www.tutorialspoint.com/sqlite/sqlite_java.htm

class Database(filename: String = "covid.db") {
  val handle: Connection = open
  createDb()

  private def open: Connection = DriverManager.getConnection(s"jdbc:sqlite:$filename")

  private def sqlFromFile(filename: String): String = Source.fromResource(s"sql/$filename.sql").getLines().mkString

  private def update(sql: String): Unit = {
    val stmt = handle.createStatement
    stmt.executeUpdate(sql)
    stmt.close()
  }

  private def createDb(): Unit = update(sqlFromFile("createTables"))

  def clearDaily(): Unit = update(sqlFromFile("clearDaily"))

  def insertDailyCounts(rows: CovidRecord*): Unit = {
    val values = rows.map(r => s"""("${r.date}", ${r.count})""").mkString(", ")
    val sql = sqlFromFile("insertDailyCounts").format(values)
    update(sql)
  }

  def insertCovidRecords(rows: CovidRecord*): Unit = rows.foreach(insertCovidRecord)

  def getDayCount(date: LocalDate): Option[CovidRecord] = {
    val stmt = handle.createStatement
    val sql = sqlFromFile("getDayCount").format(date.toString)
    val result = stmt.executeQuery(sql)

    val hasData = result.next()
    val rec = if (hasData) Some(CovidRecord(date, result.getLong("count"))) else None
    stmt.close()

    rec
  }

}

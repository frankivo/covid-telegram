package com.github.frankivo

import java.sql.{Connection, DriverManager}
import java.time.LocalDate

import scala.io.Source

// Examples from https://www.tutorialspoint.com/sqlite/sqlite_java.htm

class Database {
  val handle: Connection = open
  createDb()

  private def open: Connection = DriverManager.getConnection("jdbc:sqlite:covid.db")

  private def sqlFromFile(filename: String): String = Source.fromResource(s"sql/$filename.sql").getLines().mkString

  private def update(sql: String): Unit = {
    val stmt = handle.createStatement
    stmt.executeUpdate(sql)
    stmt.close()
  }

  private def createDb(): Unit = update(sqlFromFile("createTables"))

  def clearDaily(): Unit = update(sqlFromFile("clearDaily"))

  def insertCovidRecord(row: CovidRecord): Unit = {
    update(sqlFromFile("insertRecord").format(row.date.toString, row.count))
  }

  def getDayCount(date: LocalDate): Option[CovidRecord] = {
    val stmt = handle.createStatement
    val result = stmt.executeQuery(sqlFromFile("getDayCount"))

    val hasData = result.next()

    stmt.close()

    if (hasData) Some(CovidRecord(date, result.getLong("count")))
    else None
  }
}

package com.github.frankivo

import java.sql.{Connection, Date, DriverManager}

import scala.collection.mutable.ListBuffer
import scala.io.Source

// Examples from https://www.tutorialspoint.com/sqlite/sqlite_java.htm

class Database {
  val handle: Connection = open
  createDb()

  private def open: Connection = DriverManager.getConnection("jdbc:sqlite:covid.db")

  private def sqlFromFile(filename: String): String = Source.fromResource(s"sql/${filename}.sql").getLines.mkString

  private def update(sql: String): Unit = {
    val stmt = handle.createStatement
    stmt.executeUpdate(sql)
    stmt.close()
  }

  private def createDb(): Unit = update(sqlFromFile("createTables"))

  def insert(row: CovidRecord): Unit = {
    update(sqlFromFile("insertRecord").format(row.date.toString, row.count))
  }

  def getAllData: Seq[CovidRecord] = {
    val stmt = handle.createStatement
    val result = stmt.executeQuery(sqlFromFile("selectAllData"))

    val data = new ListBuffer[CovidRecord]()
    while (result.next) {
      val date = Date.valueOf(result.getString("date"))
      val count = result.getLong("count")
      data += CovidRecord(date, count)
    }

    stmt.close()
    data.toSeq
  }

}

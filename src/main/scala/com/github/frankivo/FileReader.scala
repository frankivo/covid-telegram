package com.github.frankivo

import java.io.File
import java.time.LocalDate

import com.github.frankivo.model.DayRecord
import com.github.tototoshi.csv.CSVReader

import scala.io.BufferedSource

object FileReader {
  def readDay(source: BufferedSource): DayRecord = readDay(CSVReader.open(source))

  def readDay(file: File): DayRecord = readDay(CSVReader.open(file))

  def readDay(reader: CSVReader): DayRecord = {
    val record = reader
      .allWithHeaders()
      .filter(row => row("Type") == "Totaal")
      .map(row => {
        val date = LocalDate.parse(row("Datum"))
        val count = row("Aantal").toIntOption.getOrElse(0)
        DayRecord(date, count)
      })
      .head
    reader.close()
    record
  }
}

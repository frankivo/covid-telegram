package com.github.frankivo.util

import com.github.frankivo.model.DayRecord
import com.github.tototoshi.csv.{CSVReader, defaultCSVFormat}

import java.io.File
import java.time.LocalDate
import scala.io.BufferedSource

/**
 * Reads file using CSVReader.
 */
object FileReader {
  /**
   * Read daily data from a BufferedSource.
   *
   * @param source Data source.
   * @return DayRecord with covid data.
   */
  def readDay(source: BufferedSource): DayRecord = readDay(CSVReader.open(source))

  /**
   * Read daily data from a File.
   *
   * @param file Data file.
   * @return DayRecord with covid data.
   */
  def readDay(file: File): DayRecord = readDay(CSVReader.open(file))

  private def readDay(reader: CSVReader): DayRecord = {
    val record = reader
      .allWithHeaders()
      .map(row => {
        val date = LocalDate.parse(row("date"))
        val count = row("new.infection").toLongOption.getOrElse(0L)
        DayRecord(date, count)
      })
      .head
    reader.close()
    record
  }
}

package com.github.frankivo

import com.github.frankivo.model.{DayRecord, MunicipalRecord}
import com.github.tototoshi.csv.CSVReader

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

  /**
   * Read municipal data from file.
   *
   * @param source Location of the File.
   * @return MunicipalRecord with covid data.
   */
  def readMunicipal(source: BufferedSource): MunicipalRecord = {
    val reader = CSVReader.open(source)
    val data = reader
      .allWithHeaders()
      .filter(_ ("Type") == "Totaal")
      .filter(_ ("Gemeentecode") != "-1")

    val record = MunicipalRecord(
      LocalDate.parse(data.head("Datum")),

      data.map(row => {
        (
          row("Gemeentenaam"),
          row("Aantal").toLongOption.getOrElse(0L)
        )
      }).toMap
    )

    reader.close()

    record
  }
}

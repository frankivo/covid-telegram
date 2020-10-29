package com.github.frankivo

import java.io.File
import java.time.LocalDate

import com.github.frankivo.model.DayRecord

import scala.io.{BufferedSource, Source}
import scala.util.Try

object CsvReader {
  def readFile(file: File): DayRecord = readFile(Source.fromFile(file))

  def readFile(source: BufferedSource): DayRecord = {
    val rec = source
      .getLines()
      .toSeq
      .map(_.split(","))
      .filter(r => r(1).contains("Totaal"))
      .map(r => DayRecord(LocalDate.parse(r(0)), Try(r(2).toLong).getOrElse(0)))
      .head

    source.close
    rec
  }
}

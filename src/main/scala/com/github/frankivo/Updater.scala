package com.github.frankivo

import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Paths}
import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDate, LocalTime}

import akka.actor.Actor
import scalaj.http.Http

case class UpdateAll(destination: Option[Long] = None)

class Updater() extends Actor {

  var lastUpdated: LocalTime = LocalTime.MIN
  val MIN_AGE: Int = 15
  val FIRST_DATE: LocalDate = LocalDate.parse("2020-02-27")
  val DIR_DATA: Path = Paths.get(CovidBot.DIR_BASE.toString, "data")

  override def receive: Receive = {
    case u: UpdateAll =>
      val msg = refresh()
      u.destination.foreach(id => sender() ! TelegramMessage(id, msg))
  }

  private def refresh(): String = {
    downloadAll()
    readAllData()

    val count = DIR_DATA.toFile.listFiles().length
    s"Done: I have data for $count days"
  }

  private def downloadAll(): Unit = {
    val dayCounts = Duration.between(FIRST_DATE.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays

    (0 to dayCounts.toInt)
      .map(FIRST_DATE.plusDays(_))
      .foreach(downloadDay)
  }

  private def downloadDay(date: LocalDate): Unit = {
    DIR_DATA.toFile.mkdirs()

    val dateStr = date.format(DateTimeFormatter.ofPattern("YYYYMMdd"))

    val url = s"https://raw.githubusercontent.com/J535D165/CoronaWatchNL/master/data-geo/data-national/RIVM_NL_national_$dateStr.csv"
    val fileName = Paths.get(DIR_DATA.toString, url.split("/").last)

    if (!fileName.toFile.exists()) {
      val result = Http(url).asString

      if (result.isSuccess) {
        val out = new FileOutputStream(fileName.toFile)
        out.write(result.body.getBytes(StandardCharsets.UTF_8))
        out.close()
      }
    }
  }

  private def readAllData(): Unit = {
    val data = DIR_DATA
      .toFile
      .listFiles()
      .map(CsvReader.readFile)
      .toSeq

    CovidBot.ACTOR_STATS ! Statistics(data)
  }
}

package com.github.frankivo.actors

import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Paths}
import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDate}

import akka.actor.Actor
import com.github.frankivo.messages.{RefreshData, TelegramText, UpdateAll}
import com.github.frankivo.model.DayRecord
import com.github.frankivo.{CovidBot, CsvReader}
import scalaj.http.Http

import scala.util.Try

class Updater extends Actor {
  val FIRST_DATE: LocalDate = LocalDate.parse("2020-02-27")
  val DIR_DATA: Path = Paths.get(CovidBot.DIR_BASE.toString, "data")

  override def receive: Receive = onMessage(false)

  private def onMessage(hasRun: Boolean): Receive = {
    case u: UpdateAll =>
      val msg = refresh(hasRun)
      u.destination.foreach(id => CovidBot.ACTOR_TELEGRAM ! TelegramText(id, msg))
  }

  private def refresh(hasRun: Boolean): String = {
    val countBefore = fileCount
    downloadAll()

    val countAfter = fileCount
    val hasUpdates = countAfter > countBefore

    if (hasUpdates || !hasRun) {
      val data = readAllData()
      CovidBot.ACTOR_STATS ! RefreshData(data, hasUpdates)
    }

    context.become(onMessage(true))

    s"Done: I have data for $countAfter days"
  }

  def fileCount: Long = Try(DIR_DATA.toFile.listFiles().length).getOrElse(0).toLong

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

  private def readAllData(): Seq[DayRecord] = {
    DIR_DATA
      .toFile
      .listFiles()
      .map(CsvReader.readFile)
      .toSeq
  }
}

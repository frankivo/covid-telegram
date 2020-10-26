package com.github.frankivo

import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDate, LocalTime}

import akka.actor.{Actor, ActorRef}
import scalaj.http.Http

import scala.reflect.io.Directory

case class UpdateAll(force: Boolean, destination: Option[Long] = None)

class Updater(stats: ActorRef) extends Actor {

  var lastUpdated: LocalTime = LocalTime.MIN
  val MIN_AGE: Int = 15
  val FIRST_DATE: LocalDate = LocalDate.parse("2020-02-27")

  override def receive: Receive = {
    case u: UpdateAll =>
      val msg = refresh()
      u.destination.foreach(id => sender() ! TelegramMessage(id, msg))
  }

  private def refresh(): String = {
    downloadAll()

    val count = Directory(CovidBot.DIR_DATA.toFile).files.length
    s"Done: I have data for $count days"
  }

  private def downloadAll(): Unit = {
    val dayCounts = Duration.between(FIRST_DATE.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays

    (0 to dayCounts.toInt)
      .map(FIRST_DATE.plusDays(_))
      .foreach(downloadDay)
  }

  private def downloadDay(date: LocalDate): Unit = {
    Directory(CovidBot.DIR_DATA.toFile).createDirectory()

    val dateStr = date.format(DateTimeFormatter.ofPattern("YYYYMMdd"))

    val url = s"https://raw.githubusercontent.com/J535D165/CoronaWatchNL/master/data-geo/data-national/RIVM_NL_national_$dateStr.csv"
    val fileName = Paths.get(CovidBot.DIR_DATA.toString, url.split("/").last)

    if (!fileName.toFile.exists()) {
      val result = Http(url).asString

      if (result.isSuccess) {
        val out = new FileOutputStream(fileName.toFile)
        out.write(result.body.getBytes(StandardCharsets.UTF_8))
        out.close()
      }
    }
  }
}

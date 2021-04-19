package com.github.frankivo.actors

import akka.actor.Actor
import com.github.frankivo.CovidBot
import com.github.frankivo.messages.{RefreshData, RequestSource, TelegramText, UpdateAll}
import com.github.frankivo.model.DayRecord
import scalaj.http.Http

import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Paths}
import java.time.LocalDate
import scala.io.Source
import scala.util.Using

object Updater {
  /**
   * Data source.
   */
  val URL_SOURCE: String = "https://data.rivm.nl/geonetwork/srv/dut/catalog.search#/metadata/0f3336f5-0f16-462c-9031-bb60adde4af1"

  /**
   * String format with url to download national data.
   */
  val URL_NATIONAL: String = "https://data.rivm.nl/covid-19/COVID-19_uitgevoerde_testen.csv"
}

/**
 * Downloads CSV data from RIVM.
 * This data contains daily national covid statistics.
 */
class Updater extends Actor {

  private val DIR_DATA: Path = Paths.get(CovidBot.DIR_BASE.toString, "data")
  private val FILE_DATA: Path = Paths.get(DIR_DATA.toString, "covid19.csv")

  override def receive: Receive = onMessage(false)

  private def onMessage(hasRun: Boolean): Receive = {
    case u: UpdateAll =>
      val msg = refresh(hasRun)
      u.destination.foreach(dest => CovidBot.ACTOR_TELEGRAM ! TelegramText(dest, msg))
    case r: RequestSource => CovidBot.ACTOR_TELEGRAM ! TelegramText(r.destination, s"Source: ${Updater.URL_SOURCE}")
  }

  private def refresh(hasRun: Boolean): String = {
    val reportDateBefore = reportDate()

    download()

    val reportDateAfter = reportDate()
    val hasUpdates = reportDateAfter.isAfter(reportDateBefore)

    if (hasUpdates || !hasRun) {
      val daily = readData()
      CovidBot.ACTOR_STATS ! RefreshData(daily, hasUpdates)
    }

    context.become(onMessage(true))

    s"Done: Report date: $reportDateAfter"
  }

  private def download(): Unit = {
    DIR_DATA.toFile.mkdirs()

    if (FILE_DATA.toFile.exists)
      FILE_DATA.toFile.delete

    val result = Http(Updater.URL_NATIONAL).asString
    if (result.isSuccess) {
      val out = new FileOutputStream(FILE_DATA.toFile)
      out.write(result.body.getBytes(StandardCharsets.UTF_8))
      out.close()
    }
  }

  private def reportDate(): LocalDate = {
    Using(Source.fromFile(FILE_DATA.toFile)) {
      source =>
        val raw = source
          .getLines().slice(1, 2)
          .toSeq
          .map(_.split(";"))
          .map(_ (1))
          .head
          .substring(0, 10)
        LocalDate.parse(raw)
    }.getOrElse(LocalDate.MIN)
  }

  private def readData(): Seq[DayRecord] = {
    Using(Source.fromFile(FILE_DATA.toFile)) {
      source =>
        source
          .getLines()
          .drop(1)
          .map(_.split(";"))
          .map(rec => DayRecord(LocalDate.parse(rec(2)), rec(6).toLong))
          .toSeq
          .groupBy(_.date)
          .map(rec => DayRecord(rec._1, rec._2.map(_.count).sum))
          .toSeq
    }.get
  }
}

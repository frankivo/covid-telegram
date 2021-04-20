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
import scala.io.{BufferedSource, Source}
import scala.util.Try

object Updater {
  /**
   * Data source.
   */
  val URL_SOURCE: String = "https://data.rivm.nl/geonetwork/srv/dut/catalog.search#/metadata/0f3336f5-0f16-462c-9031-bb60adde4af1"

  /**
   * String format with url to download national data.
   */
  val URL_NATIONAL: String = "https://data.rivm.nl/covid-19/COVID-19_uitgevoerde_testen.csv"

  /**
   * Directory where data is stored.
   */
  val DIR_DATA: Path = Paths.get(CovidBot.DIR_BASE.toString, "data")

  /**
   * Location where data is stored.
   */
  val FILE_DATA: Path = Paths.get(DIR_DATA.toString, "covid19.csv")

  /**
   * Get report date from the csv-file.
   * This is the second column of the second row (and all the rows below).
   *
   * @return The report date from the csv file OR LocalDate.MIN if the file does not exist.
   */
  def reportDate(source: BufferedSource): LocalDate = {
    Try {
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

  /**
   * Read all the data from the CSV file.
   * This data is daily per region.
   * This function will summarize this per day.
   *
   * @return A list of DayRecord.
   */
  def readData(source: BufferedSource): Seq[DayRecord] = {
    source
      .getLines()
      .drop(1)
      .map(_.split(";"))
      .map(rec => DayRecord(LocalDate.parse(rec(2)), rec(6).toLong))
      .toSeq
      .groupBy(_.date)
      .map(rec => DayRecord(rec._1, rec._2.map(_.count).sum))
      .toSeq
  }
}

/**
 * Downloads CSV data from RIVM.
 * This data contains daily national covid statistics.
 */
class Updater extends Actor {

  override def receive: Receive = onMessage(false)

  private def onMessage(hasRun: Boolean): Receive = {
    case u: UpdateAll =>
      val msg = refresh(hasRun)
      u.destination.foreach(dest => CovidBot.ACTOR_TELEGRAM ! TelegramText(dest, msg))
    case r: RequestSource => CovidBot.ACTOR_TELEGRAM ! TelegramText(r.destination, s"Source: ${Updater.URL_SOURCE}")
  }

  /**
   * Refresh daily data.
   *
   * @param hasRun is false for first call.
   * @return String Message for Telegram.
   */
  private def refresh(hasRun: Boolean): String = {
    println("Refresh data")

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

  /**
   * Download file from the interwebs.
   */
  private def download(): Unit = {
    Updater.DIR_DATA.toFile.mkdirs()

    if (Updater.FILE_DATA.toFile.exists)
      Updater.FILE_DATA.toFile.delete

    val result = Http(Updater.URL_NATIONAL).asString
    if (result.isSuccess) {
      val out = new FileOutputStream(Updater.FILE_DATA.toFile)
      out.write(result.body.getBytes(StandardCharsets.UTF_8))
      out.close()
    }
  }

  def reportDate(): LocalDate = {
    Try(Updater.reportDate(Source.fromFile(Updater.FILE_DATA.toFile)))
      .getOrElse(LocalDate.MIN)
  }

  def readData(): Seq[DayRecord] = {
    Try(Updater.readData(Source.fromFile(Updater.FILE_DATA.toFile)))
      .getOrElse(Seq())
  }
}

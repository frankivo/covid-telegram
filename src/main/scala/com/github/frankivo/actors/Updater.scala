package com.github.frankivo.actors

import akka.actor.Actor
import com.github.frankivo.CovidBot
import com.github.frankivo.messages.{RefreshData, RequestSource, TelegramText, UpdateAll}
import com.github.frankivo.model.DayRecord
import com.github.frankivo.util.FileReader
import sttp.client3.{HttpURLConnectionBackend, basicRequest}
import sttp.model.Uri

import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Paths}
import java.time.{Duration, LocalDate}
import scala.util.Try

object Updater {
  /**
   * First cases of Covid in The Netherlands.
   */
  val COVID_EPOCH: LocalDate = LocalDate.parse("2020-07-02")

  /**
   * Get a date range from start to today.
   *
   * @param start Start date.
   * @return Date range.
   */
  def dateRange(start: LocalDate = COVID_EPOCH): Seq[LocalDate] = {
    val dayCounts = Duration.between(start.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays

    (0 to dayCounts.toInt).map(start.plusDays(_))
  }

  /**
   * Git repo with data.
   */
  val URL_SOURCE: String = "https://github.com/mzelst/covid-19"

  /**
   * String format with url to download national data.
   */
  val URL_NATIONAL: String = "https://raw.githubusercontent.com/mzelst/covid-19/master/corrections/corrections_per_day/corrections-%s.csv"
}

/**
 * Downloads CSV data from GitHub.
 * This data contains daily national covid statistics.
 */
class Updater extends Actor {

  private val DIR_DATA: Path = Paths.get(CovidBot.DIR_BASE.toString, "data")

  override def receive: Receive = onMessage(false)

  private def onMessage(hasRun: Boolean): Receive = {
    case u: UpdateAll =>
      val msg = refresh(hasRun)
      u.destination.foreach(dest => CovidBot.ACTOR_TELEGRAM ! TelegramText(dest, msg))
    case r: RequestSource => CovidBot.ACTOR_TELEGRAM ! TelegramText(r.destination, s"Source: ${Updater.URL_SOURCE}")
  }

  /**
   * Downloads all the CSV files that are not yet present on the local filesystem.
   *
   * @param hasRun True after the first run.
   * @return Result message.
   */
  private def refresh(hasRun: Boolean): String = {
    val countBefore = fileCount()
    downloadAll()

    val countAfter = fileCount()
    val hasUpdates = countAfter > countBefore

    if (hasUpdates || !hasRun) {
      val daily = readDailyData()
      CovidBot.ACTOR_STATS ! RefreshData(daily, hasUpdates)
    }

    context.become(onMessage(true))

    val latest = latestDate()
    s"Done: latest file is from ${latest.toString}"
  }

  /**
   * Count the amount of files in a given directory.
   *
   * @return Count of files. 0 if failed.
   */
  def fileCount(): Long = Try(DIR_DATA.toFile.listFiles().length).getOrElse(0).toLong

  private def downloadAll(): Unit = {
    Updater
      .dateRange()
      .foreach(download)
  }

  /**
   * Downloads a file into a directory.
   *
   * @param date The date to download statistics for.
   */
  private def download(date: LocalDate): Unit = {
    DIR_DATA.toFile.mkdirs()

    val url = Updater.URL_NATIONAL.format(date)
    val uri = Uri.parse(url).getOrElse(throw new Exception(s"Cannot parse URL: ${url} "))

    val fileName = Paths.get(DIR_DATA.toString, url.split("/").last)

    if (!fileName.toFile.exists()) {
      println(s"Download $url")
      val request = basicRequest.get(uri)
      val backend = HttpURLConnectionBackend()
      val response = request.send(backend)

      if (response.code.isSuccess) {
        val out = new FileOutputStream(fileName.toFile)
        val body = response.body.getOrElse("")
        out.write(body.getBytes(StandardCharsets.UTF_8))
        out.close()
      }
      else println(s"Could not download: $url")
    }
    else println(s"Already exists: $fileName")
  }

  private def readDailyData(): Seq[DayRecord] = {
    DIR_DATA
      .toFile
      .listFiles()
      .map(FileReader.readDay)
      .toSeq
  }

  private def latestDate(): LocalDate = {
    DIR_DATA
      .toFile
      .listFiles()
      .map(_.getName)
      .map(_.substring(12, 22))
      .map(LocalDate.parse(_))
      .maxBy(d => d)
  }
}

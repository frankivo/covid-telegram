package com.github.frankivo.actors

import akka.actor.Actor
import com.github.frankivo.messages.{RefreshData, RequestSource, TelegramText, UpdateAll}
import com.github.frankivo.model.DayRecord
import com.github.frankivo.{CovidBot, FileReader}
import scalaj.http.Http

import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Paths}
import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDate}
import scala.util.Try

object Updater {
  /**
   * First cases of Covid in The Netherlands.
   */
  val COVID_EPOCH: LocalDate = LocalDate.parse("2020-02-27")

  /**
   * Get a date range from start to today.
   *
   * @param start Start date.
   * @return Date range.
   */
  def dateRange(start: LocalDate = COVID_EPOCH): Seq[LocalDate] = {
    val dayCounts = Duration.between(start.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays

    (0 to dayCounts.toInt)
      .map(start.plusDays(_))
  }

  /**
   * Format a date.
   *
   * @param date Date to format.
   * @return Formatted String.
   */
  def formatDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

  /**
   * Git repo with data.
   */
  val URL_SOURCE: String = "https://github.com/J535D165/CoronaWatchNL/tree/master/data-geo"

  /**
   * String format with url to download national data.
   */
  val URL_NATIONAL: String = "https://raw.githubusercontent.com/J535D165/CoronaWatchNL/master/data-geo/data-national/RIVM_NL_national_%s.csv"

  /**
   * String format with url to download municipal data.
   */
  val URL_MUNICIPAL: String = "https://raw.githubusercontent.com/J535D165/CoronaWatchNL/master/data-geo/data-municipal/RIVM_NL_municipal_%s.csv"
}

/**
 * Downloads CSV data from GitHub.
 * This data contains daily national and municipal covid statistics.
 */
class Updater extends Actor {

  private val DIR_DATA: Path = Paths.get(CovidBot.DIR_BASE.toString, "data")
  private val DIR_DATA_NATIONAL: Path = Paths.get(DIR_DATA.toString, "national")
  private val DIR_DATA_MUNICIPAL: Path = Paths.get(DIR_DATA.toString, "municipal")

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
    val countBefore = fileCount(DIR_DATA_NATIONAL)
    downloadAll()

    val countAfter = fileCount(DIR_DATA_NATIONAL)
    val hasUpdates = countAfter > countBefore

    if (hasUpdates || !hasRun) {
      val daily = readDailyData()
      CovidBot.ACTOR_STATS ! RefreshData(daily, hasUpdates)
    }

    context.become(onMessage(true))

    s"Done: I have data for $countAfter days"
  }

  /**
   * Count the amount of files in a given directory.
   *
   * @param directory The directory to inspect.
   * @return Count of files. 0 if failed.
   */
  def fileCount(directory: Path): Long = Try(directory.toFile.listFiles().length).getOrElse(0).toLong

  private def downloadAll(): Unit = {
    Updater.dateRange()
      .map(Updater.formatDate)
      .foreach(day => {
        downloadNational(day)
        downloadMunicipal(day)
      })
  }

  private def downloadNational(date: String): Unit = download(Updater.URL_NATIONAL.format(date), DIR_DATA_NATIONAL)

  private def downloadMunicipal(date: String): Unit = download(Updater.URL_MUNICIPAL.format(date), DIR_DATA_MUNICIPAL)


  /**
   * Downloads a file into a directory.
   *
   * @param url       The file to download.
   * @param targetDir The directory to store the file in.
   */
  private def download(url: String, targetDir: Path): Unit = {
    targetDir.toFile.mkdirs()

    val fileName = Paths.get(targetDir.toString, url.split("/").last)

    if (!fileName.toFile.exists()) {
      println(s"Download $url")

      val result = Http(url).asString
      if (result.isSuccess) {
        val out = new FileOutputStream(fileName.toFile)
        out.write(result.body.getBytes(StandardCharsets.UTF_8))
        out.close()
      }
      else println(s"Could not download: $url")
    }
    else println(s"Already exists: $fileName")
  }

  private def readDailyData(): Seq[DayRecord] = {
    DIR_DATA_NATIONAL
      .toFile
      .listFiles()
      .map(FileReader.readDay)
      .toSeq
  }
}

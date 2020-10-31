package com.github.frankivo.actors

import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Paths}
import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDate}

import akka.actor.Actor
import com.github.frankivo.messages.{RefreshData, TelegramText, UpdateAll}
import com.github.frankivo.model.DayRecord
import com.github.frankivo.{CovidBot, FileReader}
import scalaj.http.Http

import scala.util.Try

/**
 * Downloads CSV data from GitHub.
 * This data contains daily national and municipal covid statistics.
 */
class Updater extends Actor {
  /**
   * First cases of Covid in The Netherlands.
   */
  val COVID_EPOCH: LocalDate = LocalDate.parse("2020-02-27")

  private val DIR_DATA: Path = Paths.get(CovidBot.DIR_BASE.toString, "data")
  private val DIR_DATA_NATIONAL: Path = Paths.get(DIR_DATA.toString, "national")
  private val DIR_DATA_MUNICIPAL: Path = Paths.get(DIR_DATA.toString, "municipal")

  override def receive: Receive = onMessage(false)

  private def onMessage(hasRun: Boolean): Receive = {
    case u: UpdateAll =>
      val msg = refresh(hasRun)
      u.destination.foreach(id => CovidBot.ACTOR_TELEGRAM ! TelegramText(id, msg))
  }

  /**
   * Downloads all the CSV files that are not yet present on the local filesystem.
   * @param hasRun True after the first run.
   * @return Result message.
   */
  private def refresh(hasRun: Boolean): String = {
    val countBefore = fileCount(DIR_DATA_NATIONAL)
    downloadAll()

    val countAfter = fileCount(DIR_DATA_NATIONAL)
    val hasUpdates = countAfter > countBefore

    if (hasUpdates || !hasRun) {
      val data = readDailyData(DIR_DATA_NATIONAL)
      CovidBot.ACTOR_STATS ! RefreshData(data, hasUpdates)
    }

    context.become(onMessage(true))

    s"Done: I have data for $countAfter days"
  }

  /**
   * Count the amount of files in a given directory.
   * @param directory The directory to inspect.
   * @return Count of files. 0 if failed.
   */
  def fileCount(directory: Path): Long = Try(directory.toFile.listFiles().length).getOrElse(0).toLong

  private def downloadAll(): Unit = {
    val dayCounts = Duration.between(COVID_EPOCH.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays

    (0 to dayCounts.toInt)
      .map(COVID_EPOCH.plusDays(_))
      .map(_.format(DateTimeFormatter.ofPattern("YYYYMMdd")))
      .foreach(day => {
        downloadNational(day)
        downloadMunicipal(day)
      })
  }

  private def downloadNational(date: String): Unit = {
    val url = s"https://raw.githubusercontent.com/J535D165/CoronaWatchNL/master/data-geo/data-national/RIVM_NL_national_$date.csv"
    download(url, DIR_DATA_NATIONAL)
  }

  private def downloadMunicipal(date: String): Unit = {
    val url = s"https://raw.githubusercontent.com/J535D165/CoronaWatchNL/master/data-geo/data-municipal/RIVM_NL_municipal_$date.csv"
    download(url, DIR_DATA_MUNICIPAL)
  }

  /**
   * Downloads a file into a directory.
   * @param url The file to download.
   * @param targetDir The directory to store the file in.
   */
  private def download(url: String, targetDir: Path): Unit = {
    targetDir.toFile.mkdirs()

    val fileName = Paths.get(targetDir.toString, url.split("/").last)

    if (!fileName.toFile.exists()) {
      val result = Http(url).asString

      if (result.isSuccess) {
        val out = new FileOutputStream(fileName.toFile)
        out.write(result.body.getBytes(StandardCharsets.UTF_8))
        out.close()
      }
    }
  }

  private def readDailyData(directory: Path): Seq[DayRecord] = {
    directory
      .toFile
      .listFiles()
      .map(FileReader.readDay)
      .toSeq
  }
}

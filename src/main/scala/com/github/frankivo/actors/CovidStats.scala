package com.github.frankivo.actors

import akka.actor.Actor
import com.github.frankivo.CovidBot
import com.github.frankivo.messages._
import com.github.frankivo.model.{DayRecord, DayRecords, WeekRecord}

import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import scala.util.Try

object CovidStats {
  val ROLLING_DAYS: Int = 100
}

class CovidStats extends Actor {

  override def receive: Receive = onMessage(null)

  private def onMessage(stats: DayRecords): Receive = {
    case e: RequestCasesForDate => CovidBot.ACTOR_TELEGRAM ! TelegramText(e.destination, getDayCount(stats, e.date))
    case e: RefreshData => updateStats(stats, e)
  }

  private def updateStats(stats: DayRecords, update: RefreshData): Unit = {
    val isFirstRun = stats == null

    val newStats = DayRecords(update.data)
    context.become(onMessage(newStats))

    if (newStats != null) {
      graphMonths(newStats, isFirstRun)
      graphWeeks(newStats)
      graphRolling(newStats)
    }

    if (update.containsUpdates)
      broadcastToday(newStats)
  }

  /**
   * Creates a graph for each month. Every graph contains daily counts.
   *
   * @param stats      All covid daily data.
   * @param isFirstRun Will generate all months if false. Otherwise only the current month.
   */
  private def graphMonths(stats: DayRecords, isFirstRun: Boolean): Unit = {
    val grouped = stats
      .data
      .groupBy(r => (r.date.getYear, r.date.getMonthValue))

    if (isFirstRun)
      grouped.foreach(m => CovidBot.ACTOR_GRAPHS ! CreateMonthGraph(m._2))
    else {
      val curMonth = (LocalDate.now().getYear, LocalDate.now().getMonthValue)
      CovidBot.ACTOR_GRAPHS ! CreateMonthGraph(grouped(curMonth))
    }
  }

  /**
   * Creates a graph of the last N days.
   *
   * @param stats All covid daily data.
   */
  private def graphRolling(stats: DayRecords): Unit = {
    val data = stats
      .data
      .sortBy(_.date)
      .takeRight(CovidStats.ROLLING_DAYS)

    CovidBot.ACTOR_GRAPHS ! CreateRollingGraph(data)
  }

  /**
   * Creates a graph with weekly average counts.
   *
   * @param stats All covid daily data.
   */
  private def graphWeeks(stats: DayRecords): Unit = {
    val weekData = stats
      .data
      .groupBy(d => (d.date.getYear, weekNumber(d.date)))
      .map(x => WeekRecord(year = x._1._1, weekOfYear = x._1._2, count = x._2.map(c => c.count).sum))
      .toSeq
    CovidBot.ACTOR_GRAPHS ! CreateWeeklyGraph(weekData)
  }

  private def weekNumber(date: LocalDate): Int = date.get(WeekFields.of(Locale.GERMANY).weekOfYear())

  private def getDayCount(stats: DayRecords, date: Option[String]): String = {
    if (stats == null) return "Data has not been pulled yet."

    if (date.isEmpty) return caseString(stats.latest())

    val parsedDate = Try(LocalDate.parse(date.get)).getOrElse(return s"Cannot parse date '${date.get}'")
    val cases = stats.findDayCount(parsedDate).getOrElse(return s"No data found for $parsedDate")
    caseString(DayRecord(parsedDate, cases))
  }

  private def caseString(rec: DayRecord): String = s"Cases for ${rec.date}: ${rec.count}"

  private def broadcastToday(stats: DayRecords): Unit = {
    stats
      .findDayCount(LocalDate.now())
      .foreach(r => CovidBot.ACTOR_TELEGRAM ! TelegramText(Telegram.broadcastId, s"There are ${r} new cases!"))
  }
}

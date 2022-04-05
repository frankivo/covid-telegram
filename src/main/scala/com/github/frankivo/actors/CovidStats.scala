package com.github.frankivo.actors

import akka.actor.Actor
import com.github.frankivo.CovidBot
import com.github.frankivo.messages._
import com.github.frankivo.model.{DayRecord, DayRecords, WeekRecord}
import com.github.frankivo.util.DateHelper.LocalDateExtender

import java.time.LocalDate
import scala.util.Try

object CovidStats {
  val ROLLING_DAYS: Int = 100
}

class CovidStats extends Actor {

  override def receive: Receive = onMessage(null)

  private def onMessage(stats: DayRecords): Receive = {
    case e: RequestAllTimeHigh =>
      CovidBot.ACTOR_TELEGRAM ! TelegramText(
        e.destination,
        allTimeHigh(e, stats)
      )
    case e: RequestCasesForDate =>
      CovidBot.ACTOR_TELEGRAM ! TelegramText(
        e.destination,
        getDayCount(e, stats)
      )
    case e: RefreshData => updateStats(e, stats)
  }

  private def updateStats(update: RefreshData, stats: DayRecords): Unit = {
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

  /** Creates a graph for each month. Every graph contains daily counts.
    *
    * @param stats
    *   All covid daily data.
    * @param isFirstRun
    *   Will generate all months if false. Otherwise only the current month.
    */
  private def graphMonths(stats: DayRecords, isFirstRun: Boolean): Unit = {
    val grouped = stats.data
      .groupBy(r => (r.date.getYear, r.date.getMonthValue))

    if (isFirstRun)
      grouped.foreach(m => CovidBot.ACTOR_GRAPHS ! CreateMonthGraph(m._2))
    else {
      val curMonth = (LocalDate.now().getYear, LocalDate.now().getMonthValue)
      CovidBot.ACTOR_GRAPHS ! CreateMonthGraph(grouped(curMonth))
    }
  }

  /** Creates a graph of the last N days.
    *
    * @param stats
    *   All covid daily data.
    */
  private def graphRolling(stats: DayRecords): Unit = {
    val data = stats.data
      .sortBy(_.date)
      .takeRight(CovidStats.ROLLING_DAYS)

    CovidBot.ACTOR_GRAPHS ! CreateRollingGraph(data)
  }

  /** Creates a graph with weekly counts summed.
    *
    * @param stats
    *   All covid daily data.
    */
  private def graphWeeks(stats: DayRecords): Unit = {
    val currentWeek = LocalDate.now.weekNumber
    val currentYear = LocalDate.now.getYear

    val weekData = stats.data
      .groupBy(d => (d.date.getYear, d.date.weekNumber))
      .map(x => {
        val count = {
          // Day average times seven for current week.
          if (currentWeek == x._1._2 && currentYear == x._1._1)
            (x._2.map(c => c.count).sum / x._2.length) * 7
          // Sum of week for other weeks.
          else x._2.map(c => c.count).sum
        }
        WeekRecord(year = x._1._1, weekOfYear = x._1._2, count = count)
      })
      .toSeq
    CovidBot.ACTOR_GRAPHS ! CreateWeeklyGraph(weekData)
  }

  private def getDayCount(
      req: RequestCasesForDate,
      stats: DayRecords
  ): String = {
    if (stats == null) return "Data has not been pulled yet."

    val date = req.date

    if (date.isEmpty)
      return caseString(stats.latest())

    val parsedDate = Try(LocalDate.parse(date.get))
      .getOrElse(return s"Cannot parse date '${date.get}'")
    val cases = stats
      .findDayCount(parsedDate)
      .getOrElse(return s"No data found for $parsedDate")
    caseString(DayRecord(parsedDate, cases))
  }

  private def caseString(rec: DayRecord): String =
    s"Cases for ${rec.date}: ${rec.count}"

  private def broadcastToday(stats: DayRecords): Unit = {
    stats
      .findDayCount(LocalDate.now())
      .foreach(r =>
        CovidBot.ACTOR_TELEGRAM ! TelegramText(
          Telegram.broadcastId,
          s"There are $r new cases!"
        )
      )
  }

  private def allTimeHigh(
      req: RequestAllTimeHigh,
      stats: DayRecords
  ): String = {
    if (stats == null) return "Data has not been pulled yet."

    val max = stats.max()
    s"'All time high' has been set on ${max.date}. Amount of cases on that day: ${max.count}."
  }
}

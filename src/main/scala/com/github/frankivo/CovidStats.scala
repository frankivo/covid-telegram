package com.github.frankivo

import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

import akka.actor.Actor

import scala.util.Try

case class GetCasesForDay(destination: Long, date: Option[String] = None)

case class RefreshData(data: Seq[CovidRecord], containsUpdates: Boolean)

class CovidStats extends Actor {

  override def receive: Receive = onMessage(null)

  private def onMessage(stats: Statistics): Receive = {
    case e: GetCasesForDay => CovidBot.ACTOR_TELEGRAM ! TelegramMessage(e.destination, getDayCount(stats, e.date))
    case e: RefreshData => updateStats(stats, e)
  }

  def updateStats(stats: Statistics, update: RefreshData): Unit = {
    val isFirstRun = stats == null

    val newStats = Statistics(update.data)
    context.become(onMessage(newStats))

    if (newStats != null) {
      graphMonths(newStats, isFirstRun)
      graphWeeks(newStats)
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
  def graphMonths(stats: Statistics, isFirstRun: Boolean): Unit = {
    val grouped = stats
      .data
      .groupBy(r => (r.date.getYear, r.date.getMonthValue))

    if (isFirstRun)
      grouped.foreach(m => CovidBot.ACTOR_GRAPHS ! MonthData(m._2))
    else {
      val curMonth = (LocalDate.now().getYear, LocalDate.now().getMonthValue)
      CovidBot.ACTOR_GRAPHS ! MonthData(grouped(curMonth))
    }
  }

  /**
   * Creates a graph with weekly average counts.
   *
   * @param stats All covid daily data.
   */
  def graphWeeks(stats: Statistics): Unit = {
    val weekData = stats
      .data
      .groupBy(d => weekNumber(d.date))
      .map(x => (x._1, x._2.map(c => c.count).sum / x._2.length))
      .toSeq
    CovidBot.ACTOR_GRAPHS ! WeekData(weekData)
  }

  def weekNumber(date: LocalDate): Int = date.get(WeekFields.of(Locale.GERMANY).weekOfYear())

  def getDayCount(stats: Statistics, date: Option[String]): String = {
    if (stats == null) return "Data has not been pulled yet."

    if (date.isEmpty) return caseString(stats.latest())

    val parsedDate = Try(LocalDate.parse(date.get)).getOrElse(return s"Cannot parse date '${date.get}'")
    val rec = stats.findDayCount(parsedDate).getOrElse(return s"No data found for $parsedDate")
    caseString(rec)
  }

  def caseString(rec: CovidRecord): String = s"Cases for ${rec.date}: ${rec.count}"

  def broadcastToday(stats: Statistics): Unit = {
    stats
      .findDayCount(LocalDate.now())
      .foreach(r => CovidBot.ACTOR_TELEGRAM ! TelegramMessage(Telegram.broadcastId, s"There are ${r.count} new cases!"))
  }
}

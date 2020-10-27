package com.github.frankivo

import java.time.LocalDate

import akka.actor.Actor

import scala.util.Try

case class Statistics(data: Seq[CovidRecord]) {
  def findDayCount(date: LocalDate): Option[CovidRecord] = data.find(_.date.isEqual(date))

  def findMaxDate(): LocalDate = data.map(_.date).max
}

case class GetCasesForDay(destination: Long, date: Option[String] = None)

class CovidStats() extends Actor {

  override def receive: Receive = onMessage(null)

  private def onMessage(stats: Statistics): Receive = {
    case e: GetCasesForDay => CovidBot.ACTOR_TELEGRAM ! TelegramMessage(e.destination, getDayCount(stats, e.date))
    case e: Statistics => updateStats(stats, e)
  }

  def updateStats(stats: Statistics, newStats: Statistics): Unit = {
    val isFirstRun = stats == null

    context.become(onMessage(newStats))

    if (newStats != null) {
      graphMonths(newStats, isFirstRun)
      broadcastToday(newStats)
    }
  }

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

  def getDayCount(stats: Statistics, date: Option[String]): String = {
    if (stats == null) return "Data has not been pulled yet."

    val parsedDate =
      if (date.isEmpty) stats.findMaxDate()
      else {
        val parsed = Try(LocalDate.parse(date.get)).toOption
        if (parsed.isEmpty) return s"Cannot parse date '${date.get}'"
        parsed.get
      }

    val data = stats.findDayCount(parsedDate)
    if (data.isEmpty) return s"No data found for $parsedDate"

    s"Cases for ${data.head.date}: ${data.head.count}"
  }

  def broadcastToday(stats: Statistics): Unit = {
    stats
      .findDayCount(LocalDate.now())
      .foreach(r => CovidBot.ACTOR_TELEGRAM ! TelegramMessage(Telegram.broadcastId, s"There are ${r.count} new cases!"))
  }

}

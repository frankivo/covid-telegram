package com.github.frankivo

import java.time.LocalDate

import akka.actor.Actor

import scala.util.Try

case class Statistics(data: Seq[CovidRecord])

case class GetCasesForDay(destination: Long, date: Option[String] = None)

class CovidStats() extends Actor {

  private var stats: Statistics = _

  def updateStats(data: Statistics): Unit = {
    val isFirstRun = stats == null

    stats = data
    graphMonths(isFirstRun)

    broadcastToday()
  }

  def graphMonths(isFirstRun: Boolean): Unit = {
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

  def findDayCount(date: LocalDate): Option[CovidRecord] = stats.data.find(_.date.isEqual(date))

  def findMaxDate(): LocalDate = stats.data.map(_.date).max

  def getDayCount(date: Option[String]): String = {
    if (stats == null) return "Data has not been pulled yet."

    val parsedDate =
      if (date.isEmpty) findMaxDate()
    else
    {
      val parsed = Try(LocalDate.parse(date.get)).toOption
      if (parsed.isEmpty) return s"Cannot parse date '${date.get}'"
      parsed.get
    }

    val data = findDayCount(parsedDate)
    if (data.isEmpty) return s"No data found for $parsedDate"

    s"Cases for ${data.head.date}: ${data.head.count}"
  }

  override def receive: Receive = {
    case e: GetCasesForDay => CovidBot.ACTOR_TELEGRAM ! TelegramMessage(e.destination, getDayCount(e.date))
    case e: Statistics => updateStats(e)
  }

  def broadcastToday(): Unit = {
    findDayCount(LocalDate.now())
      .foreach(r => CovidBot.ACTOR_TELEGRAM ! TelegramMessage(Telegram.broadcastId, s"There are ${r.count} new cases!"))
  }
}

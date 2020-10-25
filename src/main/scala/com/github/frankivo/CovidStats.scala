package com.github.frankivo

import java.time.LocalDate

import akka.actor.Actor
import play.api.libs.json._

import scala.util.Try

case class NewStats(stats: Seq[CovidRecord])

case class GetCasesForDay(chatId: Long, date: Option[String] = None)

class CovidStats extends Actor {

  private var stats: Seq[CovidRecord] = Seq()

  def getDayCount(date: Option[String]): String = {
    val parsedDate = if (date.isEmpty) LocalDate.now().minusDays(1) else {
      val parsed = Try(LocalDate.parse(date.get)).toOption
      if (parsed.isEmpty) return s"Cannot parse date '${date.get}'"
      parsed.get
    }

    val data = stats.find(r => r.date.isEqual(parsedDate))
    if (data.isEmpty) return s"No data found for ${parsedDate}"

    s"Cases for ${data.head.date}: ${data.head.count}"
  }

  override def receive: Receive = {
    case e: GetCasesForDay => sender() ! TelegramMessage(getDayCount(e.date), e.chatId)
    case e: NewStats => stats = e.stats
  }
}

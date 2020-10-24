package com.github.frankivo

import java.time.LocalDate

import akka.actor.Actor
import com.github.frankivo.CovidRecordHelper._
import play.api.libs.json._
import scalaj.http.Http

import scala.util.Try

case class GetCasesForDay(chatId: Long, date: Option[String] = None)

case class UpdateAll(chatId: Long)

class CovidStats extends Actor {

  private var stats: Seq[CovidRecord] = Seq()

  def download: JsValue = {
    val data = Http("https://api.covid19api.com/dayone/country/netherlands").asString.body
    Json.parse(data)
  }

  def filterCountry(json: JsValue): Seq[JsValue] = {
    json
      .as[JsArray]
      .value
      .filter(j => (j \ "Province").as[JsString].value.isEmpty)
      .toSeq
  }

  def getDayCount(chatId: Long, date: Option[String]): TelegramMessage = {
    val parsedDate = if (date.isEmpty) LocalDate.now().minusDays(1) else {
      val parsed = Try(LocalDate.parse(date.get)).toOption
      if (parsed.isEmpty) return TelegramMessage(s"Cannot parse date '${date.get}'", chatId)
      parsed.get
    }

    val data = stats.find(r => r.date.isEqual(parsedDate))
    if (data.isEmpty) return TelegramMessage(s"No data found for ${parsedDate}", chatId)

    val rec = data.get
    TelegramMessage(s"Cases for ${rec.date}: ${rec.count}", chatId)
  }

  def backFill(chatId: Long): TelegramMessage = {
    val json = download
    val filtered = filterCountry(json)
    val mapped = filtered.map(j => CovidRecord(getDate(j \ "Date"), getLong(j \ "Confirmed")))

    stats = mapped.getDailyCounts

    TelegramMessage("Got %s records!".format(stats.length), chatId)
  }

  def getDate(field: JsLookupResult): LocalDate = LocalDate.parse(field.as[JsString].value.substring(0, 10))

  def getLong(field: JsLookupResult): Long = field.as[JsNumber].value.toLong

  override def receive: Receive = {
    case e: GetCasesForDay => sender() ! getDayCount(e.chatId, e.date)
    case u: UpdateAll => sender() ! backFill(u.chatId)
  }
}

package com.github.frankivo

import java.time.LocalDate

import akka.actor.{Actor, ActorRef}
import com.github.frankivo.CovidRecordHelper.CovidSequence
import play.api.libs.json._
import scalaj.http.Http

case class UpdateAll(chatId: Long)

class Updater(stats: ActorRef) extends Actor {
  override def receive: Receive = {
    case u: UpdateAll => sender() ! TelegramMessage(backFill(), u.chatId)
  }

  def backFill(): String = {
    val json = download
    val filtered = filterCountry(json)
    val mapped = filtered.map(j => CovidRecord(getDate(j \ "Date"), getLong(j \ "Confirmed")))

    val update = mapped.getDailyCounts
    stats ! NewStats(update)

    "Got %s records!".format(update.length)
  }

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

  def getDate(field: JsLookupResult): LocalDate = LocalDate.parse(field.as[JsString].value.substring(0, 10))

  def getLong(field: JsLookupResult): Long = field.as[JsNumber].value.toLong
}

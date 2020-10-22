package com.github.frankivo

import java.time.LocalDate

import akka.actor.Actor
import play.api.libs.json.{JsArray, JsLookupResult, JsNumber, JsString, JsValue, Json}
import scalaj.http.Http
import CovidRecordHelper._

case class UpdateAll()

class CovidStats extends Actor {
  def download: JsValue = {
    val data = Http("https://api.covid19api.com/dayone/country/netherlands").asString.body
    Json.parse(data)
  }

  def yesterday: String = s"${LocalDate.now.minusDays(1).toString}T00:00:00Z"

  def updatedToday(json: JsValue): Boolean = {
    json
      .as[JsArray]
      .value
      .exists(j => (j \ "Date").as[JsString].value == yesterday)
  }

  def filterCountry(json: JsValue): Seq[JsValue] = {
    json
      .as[JsArray]
      .value
      .filter(j => (j \ "Province").as[JsString].value.isEmpty)
      .toSeq
  }

  def backFill(): Int = {
    val json = download
    val filtered = filterCountry(json)
    val mapped = filtered.map(j => CovidRecord(getDate(j \ "Date"), getLong(j \ "Confirmed")))
    val counts = mapped.getDailyCounts
    println(counts.last)

    mapped.length
  }

  def getDate(field: JsLookupResult): LocalDate = LocalDate.parse(field.as[JsString].value.substring(0, 10))

  def getLong(field: JsLookupResult): Long = field.as[JsNumber].value.toLong

  override def receive: Receive = {
    case _: UpdateAll => sender ! TelegramMessage("Got %s records!".format(backFill()))
  }
}

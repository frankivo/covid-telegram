package com.github.frankivo

import java.time.LocalDate

import akka.actor.Actor
import com.github.frankivo.CovidRecordHelper._
import play.api.libs.json._
import scalaj.http.Http

case class GetCasesForDay(date: LocalDate = LocalDate.now)

case class UpdateAll()

class CovidStats extends Actor {

  val db: Database = new Database

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

  def getDayCount(date: LocalDate): TelegramMessage = {
    val data = db.getDayCount(date)

    if (data.isEmpty) TelegramMessage("No data found")
    else {
      val rec = data.get
      TelegramMessage(s"Casses for ${rec.date}: ${rec.count}")
    }
  }

  def backFill(): TelegramMessage = {
    val json = download
    val filtered = filterCountry(json)
    val mapped = filtered.map(j => CovidRecord(getDate(j \ "Date"), getLong(j \ "Confirmed")))
    val counts = mapped.getDailyCounts

    db.clearDaily()
    counts.foreach(db.insertCovidRecord)

    TelegramMessage("Got %s records!".format(mapped.length))
  }

  def getDate(field: JsLookupResult): LocalDate = LocalDate.parse(field.as[JsString].value.substring(0, 10))

  def getLong(field: JsLookupResult): Long = field.as[JsNumber].value.toLong

  override def receive: Receive = {
    case e: GetCasesForDay => sender() ! getDayCount(e.date)
    case _: UpdateAll => sender() ! backFill()
  }
}

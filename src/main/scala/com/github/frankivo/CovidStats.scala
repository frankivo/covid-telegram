package com.github.frankivo

import java.time.LocalDate

import akka.actor.Actor
import com.github.frankivo.CovidRecordHelper._
import play.api.libs.json._
import scalaj.http.Http

import scala.util.Try

case class GetCasesForDay(date: Option[String] = None)

case class UpdateAll()

class CovidStats extends Actor {

  val db: Database = new Database

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

  def getDayCount(date: Option[String]): TelegramMessage = {
    val parsedDate = if (date.isEmpty) LocalDate.now().minusDays(1) else {
      val parsed = Try(LocalDate.parse(date.get)).toOption
      if (parsed.isEmpty) return TelegramMessage(s"Cannot parse date '${date.get}'")
      parsed.get
    }

    val data = db.getDayCount(parsedDate)

    if (data.isEmpty) return TelegramMessage(s"No data found for ${parsedDate}")

    val rec = data.get
    TelegramMessage(s"Casses for ${rec.date}: ${rec.count}")
  }

  def backFill(): TelegramMessage = {
    val json = download
    val filtered = filterCountry(json)
    val mapped = filtered.map(j => CovidRecord(getDate(j \ "Date"), getLong(j \ "Confirmed")))
    val counts = mapped.getDailyCounts

    db.clearDaily()
    db.insertDailyCounts(counts: _*)

    TelegramMessage("Got %s records!".format(mapped.length))
  }

  def getDate(field: JsLookupResult): LocalDate = LocalDate.parse(field.as[JsString].value.substring(0, 10))

  def getLong(field: JsLookupResult): Long = field.as[JsNumber].value.toLong

  override def receive: Receive = {
    case e: GetCasesForDay => sender() ! getDayCount(e.date)
    case _: UpdateAll => sender() ! backFill()
  }
}

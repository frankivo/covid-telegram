package com.github.frankivo

import java.time.temporal.ChronoUnit.{MINUTES, SECONDS}
import java.time.{LocalDate, LocalTime}

import akka.actor.{Actor, ActorRef}
import com.github.frankivo.CovidRecordHelper.CovidSequence
import play.api.libs.json._
import scalaj.http.Http

case class UpdateAll(chatId: Long)

class Updater(stats: ActorRef) extends Actor {

  var lastUpdated: LocalTime = LocalTime.MIN
  val MIN_AGE: Int = 15

  override def receive: Receive = {
    case u: UpdateAll => sender() ! TelegramMessage(refresh(), u.chatId)
  }

  private def refresh(): String = {
    if (LocalTime.now().isBefore(nextAllowedUpdate)) s"Refresh is not allowed for another ${countDown()}"
    else backFill()
  }

  private def nextAllowedUpdate : LocalTime = lastUpdated.plusMinutes(MIN_AGE)

  private def countDown() : String = {
    val now = LocalTime.now()

    val diffMinutes = MINUTES.between(now, nextAllowedUpdate)
    val diffSeconds = SECONDS.between(now, nextAllowedUpdate)
    if (diffMinutes > 0) s"$diffMinutes minutes"
    else s"$diffSeconds seconds"
  }

  private def resetTime() : Unit = lastUpdated = LocalTime.now()

  private def backFill(): String = {
    val json = download
    val filtered = filterCountry(json)
    val mapped = filtered.map(j => CovidRecord(getDate(j \ "Date"), getLong(j \ "Confirmed")))

    val update = mapped.getDailyCounts
    stats ! Statistics(update)

    resetTime()

    "Got %s records!".format(update.length)
  }

  private def download: JsValue = {
    val data = Http("https://api.covid19api.com/dayone/country/netherlands").asString.body
    Json.parse(data)
  }

  private def filterCountry(json: JsValue): Seq[JsValue] = {
    json
      .as[JsArray]
      .value
      .filter(j => (j \ "Province").as[JsString].value.isEmpty)
      .toSeq
  }

  private def getDate(field: JsLookupResult): LocalDate = LocalDate.parse(field.as[JsString].value.substring(0, 10))

  private def getLong(field: JsLookupResult): Long = field.as[JsNumber].value.toLong
}

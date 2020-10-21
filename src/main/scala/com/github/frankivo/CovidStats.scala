package com.github.frankivo

import java.time.LocalDate

import play.api.libs.json.{JsArray, JsString, JsValue, Json}
import scalaj.http.Http

object CovidStats {
  def getData(): Unit = {
    val data = download
    val json = toJson(data)

    println(updatedToday(json))
    
    val nl = filterCountry(json)
    println(nl.length)
  }

  def download: String = Http("https://api.covid19api.com/dayone/country/netherlands").asString.body

  def toJson(raw: String): JsValue = Json.parse(raw)

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
}

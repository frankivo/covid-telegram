package com.github.frankivo

import play.api.libs.json.{JsArray, JsString, JsValue, Json}
import scalaj.http.Http

object CovidStats {
  def getData(): Unit = {
    val data = download
    val json = toJson(data)
    val nl = filterCountry(json)

    println(nl.length)
  }

  def download: String = Http("https://api.covid19api.com/dayone/country/netherlands").asString.body

  def toJson(raw: String): JsValue = Json.parse(raw)

  def filterCountry(json: JsValue): Seq[JsValue] = {
    json
      .as[JsArray]
      .value
      .filter(j => (j \ "Province").as[JsString].value.isEmpty)
      .toSeq
  }
}

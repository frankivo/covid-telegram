package com.github.frankivo

import java.sql.Date
import java.time.LocalDate

import scala.concurrent.duration.DurationInt

object CovidBot {

  def main(args: Array[String]): Unit = {
//    Telegram.sendMessage("blaat")
//
//    while (true) {
//      CovidStats.getData()
//      Thread.sleep(30.minutes.toMillis)
//    }

    val db = new Database
    db.insert(CovidRecord(Date.valueOf(LocalDate.now), 1337))
    db.getAllData.foreach(println)
    println("ok")
  }

}

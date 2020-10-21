package com.github.frankivo

import scala.concurrent.duration.DurationInt

object CovidBot {

  def main(args: Array[String]): Unit = {
    Telegram.sendMessage("blaat")

    while (true) {
      CovidStats.getData()
      Thread.sleep(30.minutes.toMillis)
    }
  }

}

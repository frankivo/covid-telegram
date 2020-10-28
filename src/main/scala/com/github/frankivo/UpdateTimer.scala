package com.github.frankivo

import com.github.frankivo.messages.UpdateAll

import scala.concurrent.duration.{DurationInt, FiniteDuration}

class UpdateTimer {
  val UPDATE_INTERVAL_MIN: FiniteDuration = 30.minutes

  start()

  def start(): Unit = {
    while (true) {
      trigger()
      Thread.sleep(UPDATE_INTERVAL_MIN.toMillis)
    }
  }

  def trigger(): Unit = CovidBot.ACTOR_UPDATER ! UpdateAll(None)
}

package com.github.frankivo

import akka.actor.ActorRef

import scala.concurrent.duration.{DurationInt, FiniteDuration}

class UpdateTimer(updater: ActorRef) {
  val UPDATE_INTERVAL_MIN: FiniteDuration = 30.minutes

  start()

  def start(): Unit = {
    while (true) {
      trigger()
      Thread.sleep(UPDATE_INTERVAL_MIN.toMillis)
    }
  }

  def trigger(): Unit = updater ! UpdateAll()

}

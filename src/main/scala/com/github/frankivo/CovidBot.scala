package com.github.frankivo

import akka.actor.{ActorSystem, Props}

object CovidBot {

  def main(args: Array[String]): Unit = {
    val akka = ActorSystem()

    val db = akka.actorOf(Props(new Database))
    val stats = akka.actorOf(Props(new CovidStats(db)))

    val telegram = akka.actorOf(Props(new Telegram(stats)))
    telegram ! TelegramMessage("Hello, World!")
  }

}

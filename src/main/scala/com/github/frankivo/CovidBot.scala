package com.github.frankivo

import akka.actor.{ActorSystem, Props}

object CovidBot {

  def main(args: Array[String]): Unit = {
    val akka = ActorSystem()

    val stats = akka.actorOf(Props(new CovidStats))

    val telegram = akka.actorOf(Props(new Telegram(stats)))
    telegram ! TelegramMessage("Hello, World!")
  }

}

package com.github.frankivo

import akka.actor.{ActorSystem, Props}

object CovidBot {

  def main(args: Array[String]): Unit = {
    val akka = ActorSystem()

    val graphs = akka.actorOf(Props(new Graphs))
    val stats = akka.actorOf(Props(new CovidStats(graphs)))
    val updater = akka.actorOf(Props(new Updater(stats)))
    akka.actorOf(Props(new Telegram(stats, updater)))

    updater ! UpdateAll(force = true)
  }

}

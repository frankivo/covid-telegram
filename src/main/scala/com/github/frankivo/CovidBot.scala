package com.github.frankivo

import java.nio.file.{Path, Paths}

import akka.actor.{ActorSystem, Props}

object CovidBot {

  val DIR_BASE : Path = Paths.get(System.getProperty("user.home"), ".covidbot")

  def main(args: Array[String]): Unit = {
    val akka = ActorSystem()

    val graphs = akka.actorOf(Props(new Graphs))
    val stats = akka.actorOf(Props(new CovidStats(graphs)))
    val updater = akka.actorOf(Props(new Updater(stats)))
    akka.actorOf(Props(new Telegram(stats, updater)))

    updater ! UpdateAll()
  }

}

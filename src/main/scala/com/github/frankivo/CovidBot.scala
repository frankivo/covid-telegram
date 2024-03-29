package com.github.frankivo

import akka.actor
import akka.actor.{ActorRef, ActorSystem, Props}
import com.github.frankivo.actors.{CovidStats, Graphs, Telegram, Updater}

import java.nio.file.{Path, Paths}

object CovidBot {
  val DIR_BASE: Path = Paths.get(System.getProperty("user.home"), ".covidbot")

  val AKKA: ActorSystem = ActorSystem()
  val ACTOR_GRAPHS: ActorRef = AKKA.actorOf(Props(new Graphs))
  val ACTOR_STATS: ActorRef = AKKA.actorOf(Props(new CovidStats()))
  val ACTOR_UPDATER: ActorRef = AKKA.actorOf(Props(new Updater()))
  val ACTOR_TELEGRAM: actor.ActorRef = AKKA.actorOf(Props(new Telegram()))

  def main(args: Array[String]): Unit = {
    new UpdateTimer()
  }
}

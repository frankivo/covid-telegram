package com.github.frankivo.actors

import akka.actor.Actor
import com.github.frankivo.messages._
import com.github.frankivo.{CovidBot, Version}
import com.pengrad.telegrambot.model.{MessageEntity, Update}
import com.pengrad.telegrambot.request.{SendMessage, SendPhoto}
import com.pengrad.telegrambot.{TelegramBot, UpdatesListener}

import scala.jdk.CollectionConverters._
import scala.util.Try

/**
 * Handles Telegram environment variables.
 */
object Telegram {
  val apiKey: String = sys.env("TELEGRAM_APIKEY")

  val ownerId: Long = sys.env("TELEGRAM_OWNER").toLong

  val broadcastId: Long = sys.env("TELEGRAM_BROADCAST").toLong

  def registerUpdates: Boolean = sys.env.getOrElse("TELEGRAM_UPDATES", "true").toBoolean

  def isOwner(destination: Long): Boolean = ownerId == destination
}

/**
 * Connects to the Telegram API.
 * Sends out broadcast messages and handles requests from Telegram members.
 */
class Telegram extends Actor {

  case class Command(destination: Long, cmd: String, parameter: Option[String])

  val bot = new TelegramBot(Telegram.apiKey)
  send(TelegramText(Telegram.ownerId, s"Hello World.\n" + versionText()))
  println(versionText())

  listenForUpdates()

  override def receive: Receive = {
    case txt: TelegramText => send(txt)
    case img: TelegramImage => send(img)
  }

  private def listenForUpdates(): Unit = {
    if (!Telegram.registerUpdates) return

    bot.setUpdatesListener(updates => {
      try handleUpdates(updates.asScala.toSeq)
      catch {
        case e: Exception =>
          send(TelegramText(Telegram.ownerId, "Error occurred!"))
          send(TelegramText(Telegram.ownerId, e.toString))
          System.exit(1) // docker-compose should revive the service.
      }
      UpdatesListener.CONFIRMED_UPDATES_ALL
    })
  }

  private def handleUpdates(updates: Seq[Update]): Unit = {
    val commands = updates
      .flatMap(u => Some(u.message))
      .filter(u => u.entities != null)
      .filter(u => u.entities.exists(e => e.`type`.eq(MessageEntity.Type.bot_command)))
      .map(u => {
        val split = u.text.split(" ")
        Command(destination = u.chat().id(), cmd = split.head.toLowerCase, parameter = Try(split(1)).toOption)
      })
    handleCommands(commands)
  }

  private def handleCommands(commands: Seq[Command]): Unit = {
    commands
      .foreach(c => {
        c.cmd match {
          case "/ath" => CovidBot.ACTOR_STATS ! RequestAllTimeHigh(c.destination)
          case "/cases" => CovidBot.ACTOR_STATS ! RequestCasesForDate(c.destination, c.parameter)
          case "/graph" => CovidBot.ACTOR_GRAPHS ! RequestRollingGraph(c.destination)
          case "/hi" => send(TelegramText(c.destination, "Hi!"))
          case "/month" => CovidBot.ACTOR_GRAPHS ! RequestMonthGraph(c.destination, c.parameter)
          case "/refresh" => CovidBot.ACTOR_UPDATER ! UpdateAll(Some(c.destination))
          case "/source" => CovidBot.ACTOR_UPDATER ! RequestSource(c.destination)
          case "/version" => send(TelegramText(c.destination, versionText()))
          case "/weekly" => CovidBot.ACTOR_GRAPHS ! RequestWeekGraph(c.destination, c.parameter)

          case e => send(TelegramText(c.destination, s"Unknown command: $e"))
        }
      })
  }

  private def versionText(): String = {
    Seq(
      s"Running version: ${Version.VERSION_STRING}",
      s"Scala version: ${dotty.tools.dotc.config.Properties.versionString}",
      s"Java version: ${scala.util.Properties.javaVersion}",
    )
      .mkString("\n")
  }

  def send(txt: TelegramText): Unit = bot.execute(new SendMessage(txt.destination, txt.body))

  def send(img: TelegramImage): Unit = bot.execute(new SendPhoto(img.destination, img.file))

}

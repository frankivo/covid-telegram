package com.github.frankivo.actors

import java.io.File
import java.nio.file.Paths
import java.time.LocalDate

import akka.actor.Actor
import com.github.frankivo.CovidBot
import com.github.frankivo.messages.{RequestCasesForDate, RequestMonthGraph, RequestWeekGraph, TelegramImage, TelegramText, UpdateAll}
import com.pengrad.telegrambot.model.{MessageEntity, Update}
import com.pengrad.telegrambot.request.{SendMessage, SendPhoto}
import com.pengrad.telegrambot.{TelegramBot, UpdatesListener}

import scala.jdk.CollectionConverters._
import scala.util.Try

object Telegram {
  val apiKey: String = sys.env("TELEGRAM_APIKEY")

  val ownerId: Long = sys.env("TELEGRAM_OWNER").toLong

  val broadcastId: Long = sys.env("TELEGRAM_BROADCAST").toLong

  def registerUpdates: Boolean = sys.env.getOrElse("TELEGRAM_UPDATES", "true").toBoolean

  def isOwner(destination: Long): Boolean = ownerId == destination
}

class Telegram extends Actor {

  case class Command(destination: Long, cmd: String, parameter: Option[String])

  val bot = new TelegramBot(Telegram.apiKey)
  send(TelegramText(Telegram.ownerId, "Hello World"))

  override def receive: Receive = {
    case txt: TelegramText => send(txt)
    case img: TelegramImage => send(img)
  }

  if (Telegram.registerUpdates) {
    bot.setUpdatesListener(updates => {
      try handleUpdates(updates.asScala.toSeq)
      catch {
        case e: Exception =>
          send(TelegramText(Telegram.ownerId, "Error occurred!"))
          send(TelegramText(Telegram.ownerId, e.getMessage))
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
        Command(u.chat().id(), split.head, Try(split(1)).toOption)
      })
    handleCommands(commands)
  }

  private def handleCommands(commands: Seq[Command]): Unit = {
    commands
      .foreach(c => {
        c.cmd match {
          case "/hi" => send(TelegramText(c.destination, "Hi!"))
          case "/refresh" => CovidBot.ACTOR_UPDATER ! UpdateAll(Some(c.destination))
          case "/cases" => CovidBot.ACTOR_STATS ! RequestCasesForDate(c.destination, c.parameter)
          case "/graph" => CovidBot.ACTOR_GRAPHS ! RequestMonthGraph(c.destination, c.parameter)
          case "/weekly" => CovidBot.ACTOR_GRAPHS ! RequestWeekGraph(c.destination)

          case e => send(TelegramText(c.destination, s"Unknown command: $e"))
        }
      })
  }

  def send(txt: TelegramText): Unit = bot.execute(new SendMessage(txt.destination, txt.body))

  def send(img: TelegramImage): Unit = bot.execute(new SendPhoto(img.destination, img.file))

}

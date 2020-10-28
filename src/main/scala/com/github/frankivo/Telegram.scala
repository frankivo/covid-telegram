package com.github.frankivo

import java.io.File
import java.nio.file.Paths
import java.time.LocalDate

import akka.actor.Actor
import com.pengrad.telegrambot.model.{MessageEntity, Update}
import com.pengrad.telegrambot.request.{SendMessage, SendPhoto}
import com.pengrad.telegrambot.{TelegramBot, UpdatesListener}

import scala.jdk.CollectionConverters._
import scala.util.Try

case class TelegramMessage(destination: Long, body: String)

case class Command(destination: Long, cmd: String, parameter: Option[String])

object Telegram {
  val apiKey: String = sys.env("TELEGRAM_APIKEY")

  val ownerId: Long = sys.env("TELEGRAM_OWNER").toLong

  val broadcastId: Long = sys.env("TELEGRAM_BROADCAST").toLong

  def registerUpdates: Boolean = sys.env.getOrElse("TELEGRAM_UPDATES", "true").toBoolean

  def isOwner(destination: Long): Boolean = ownerId == destination
}

class Telegram extends Actor {
  val bot = new TelegramBot(Telegram.apiKey)
  send(TelegramMessage(Telegram.ownerId, "Hello World"))

  if (Telegram.registerUpdates)
    bot.setUpdatesListener(updates => handleUpdates(updates.asScala.toSeq))

  private def handleUpdates(updates: Seq[Update]): Int = {
    val commands = updates
      .flatMap(u => Some(u.message))
      .filter(u => u.entities != null)
      .filter(u => u.entities.exists(e => e.`type`.eq(MessageEntity.Type.bot_command)))
      .map(u => {
        val split = u.text.split(" ")
        Command(u.chat().id(), split.head, Try(split(1)).toOption)
      })
    handleCommands(commands)

    UpdatesListener.CONFIRMED_UPDATES_ALL
  }

  private def handleCommands(commands: Seq[Command]): Unit = {
    commands
      .foreach(c => {
        c.cmd match {
          case "/hi" => send(c.destination, "Hi!")
          case "/refresh" => CovidBot.ACTOR_UPDATER ! UpdateAll(Some(c.destination))
          case "/cases" => CovidBot.ACTOR_STATS ! GetCasesForDay(c.destination, c.parameter)
          case "/graph" => sendGraphMonthly(c.destination, c.parameter)
          case "/weekly" => sendGraphWeekly(c.destination)

          case e => send(TelegramMessage(c.destination, s"Unknown command: $e"))
        }
      })
  }

  def sendGraphMonthly(dest: Long, request: Option[String]): Unit = {
    val curYear = LocalDate.now().getYear
    val curMonth = LocalDate.now().getMonthValue

    try {
      val (year, month) = {
        if (request.isDefined) (curYear, request.get.toInt)
        else (curYear, curMonth)
      }

      val file = Paths.get(Graphs.DIR_MONTHS.toString, s"${year}_$month.png").toFile
      if (file.exists())
        send(dest, file)
      else
        send(dest, s"No file found for 'month/${year}_$month'")
    }
    catch {
      case e: Exception => send(dest, "Failed: " + e.getMessage)
    }
  }

  def sendGraphWeekly(dest: Long): Unit = {
    val file = Paths.get(Graphs.DIR_WEEKS.toString, "2020.png").toFile
    if (file.exists())
      send(dest, file)
    else
      send(dest, "File not found")
  }

  def send(msg: TelegramMessage): Unit = send(msg.destination, msg.body)

  def send(dest: Long, msg: String): Unit = bot.execute(new SendMessage(dest, msg))

  def send(dest: Long, photo: File): Unit = bot.execute(new SendPhoto(dest, photo))

  override def receive: Receive = {
    case msg: TelegramMessage => send(msg)
  }
}

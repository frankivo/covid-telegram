package com.github.frankivo

import java.io.File

import akka.actor.{Actor, ActorRef}
import com.pengrad.telegrambot.model.{MessageEntity, Update}
import com.pengrad.telegrambot.request.{SendMessage, SendPhoto}
import com.pengrad.telegrambot.{TelegramBot, UpdatesListener}

import scala.jdk.CollectionConverters._
import scala.util.Try

case class TelegramMessage(destination: Long, body: String)

case class Command(destination: Long, cmd: String, parameter: Option[String])

object Telegram {
  val ownerId: Long = sys.env("TELEGRAM_OWNER").toLong

  val apiKey: String = sys.env("TELEGRAM_APIKEY")

  def isOwner(destination: Long): Boolean = ownerId == destination
}

class Telegram(stats: ActorRef, updater: ActorRef) extends Actor {
  val bot = new TelegramBot(Telegram.apiKey)
  self ! TelegramMessage(Telegram.ownerId, "Hello World")

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
          case "/refresh" => updater ! UpdateAll(Telegram.isOwner(c.destination), Some(c.destination))
          case "/date" => stats ! GetCasesForDay(c.destination, c.parameter)
          case "/latest" => stats ! GetCasesForDay(c.destination)
          case "/graph" => handleGraph(c.destination)

          case e => self ! TelegramMessage(c.destination, s"Unknown command: $e")
        }
      })
  }

  def handleGraph(dest: Long): Unit = {
    val file = Graphs.tmpFile("month/2020_10.png").jfile
    send(dest, file)
  }

  def send(dest: Long, msg: String): Unit = bot.execute(new SendMessage(dest, msg))

  def send(msg: TelegramMessage): Unit = send(msg.destination, msg.body)

  def send(dest: Long, photo: File): Unit = bot.execute(new SendPhoto(dest, photo))

  override def receive: Receive = {
    case msg: TelegramMessage => send(msg)
  }
}

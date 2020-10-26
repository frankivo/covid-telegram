package com.github.frankivo

import java.io.File

import akka.actor.{Actor, ActorRef}
import com.pengrad.telegrambot.model.{MessageEntity, Update}
import com.pengrad.telegrambot.request.{SendMessage, SendPhoto}
import com.pengrad.telegrambot.{TelegramBot, UpdatesListener}

import scala.jdk.CollectionConverters._
import scala.util.Try

case class TelegramMessage(body: String, chatId: Long)

case class Command(cmd: String, chatId: Long, parameter: Option[String])

object Telegram {
  val ownerId: Long = sys.env("TELEGRAM_OWNER").toLong

  val apiKey: String = sys.env("TELEGRAM_APIKEY")

  def isOwner(chatId: Long): Boolean = ownerId == chatId
}

class Telegram(stats: ActorRef, updater: ActorRef) extends Actor {
  val bot = new TelegramBot(Telegram.apiKey)
  self ! TelegramMessage("Hello World", Telegram.ownerId)

  bot.setUpdatesListener(updates => handleUpdates(updates.asScala.toSeq))

  private def handleUpdates(updates: Seq[Update]): Int = {
    val commands = updates
      .flatMap(u => Some(u.message))
      .filter(u => u.entities != null)
      .filter(u => u.entities.exists(e => e.`type`.eq(MessageEntity.Type.bot_command)))
      .map(u => {
        val split = u.text.split(" ")
        Command(split.head, u.chat().id(), Try(split(1)).toOption)
      })
    handleCommands(commands)

    UpdatesListener.CONFIRMED_UPDATES_ALL
  }

  private def handleCommands(commands: Seq[Command]): Unit = {
    commands
      .foreach(c => {
        c.cmd match {
          case "/hi" => send(c.chatId, "Hi!")
          case "/refresh" => updater ! UpdateAll(Telegram.isOwner(c.chatId), Some(c.chatId))
          case "/date" => stats ! GetCasesForDay(c.chatId, c.parameter)
          case "/latest" => stats ! GetCasesForDay(c.chatId)
          case "/graph" => handleGraph(c.chatId)

          case e => self ! TelegramMessage(s"Unknown command: $e", c.chatId)
        }
      })
  }

  def handleGraph(dest: Long): Unit = {
    val file = Graphs.tmpFile("month/2020_10.png").jfile
    send(dest, file)
  }

  def send(dest: Long, msg: String): Unit = bot.execute(new SendMessage(dest, msg))

  def send(msg: TelegramMessage): Unit = send(msg.chatId, msg.body)

  def send(dest: Long, photo: File): Unit = bot.execute(new SendPhoto(dest, photo))

  override def receive: Receive = {
    case msg: TelegramMessage => send(msg)
  }
}

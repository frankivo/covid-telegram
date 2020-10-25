package com.github.frankivo

import akka.actor.{Actor, ActorRef}
import com.pengrad.telegrambot.model.{MessageEntity, Update}
import com.pengrad.telegrambot.request.SendMessage
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
          case "/hi" => self ! TelegramMessage("Hi!", c.chatId)
          case "/refresh" => updater ! UpdateAll(Telegram.isOwner(c.chatId), Some(c.chatId))
          case "/date" => stats ! GetCasesForDay(c.chatId, c.parameter)
          case "/latest" => stats ! GetCasesForDay(c.chatId)

          case e => self ! TelegramMessage(s"Unknown command: $e", c.chatId)
        }
      })
  }

  def send(msg: TelegramMessage): Unit = bot.execute(new SendMessage(msg.chatId, msg.body))

  override def receive: Receive = {
    case msg: TelegramMessage => send(msg)
  }
}

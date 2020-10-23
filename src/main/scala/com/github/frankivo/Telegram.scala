package com.github.frankivo

import akka.actor.{Actor, ActorRef}
import com.pengrad.telegrambot.model.{MessageEntity, Update}
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.{TelegramBot, UpdatesListener}

import scala.jdk.CollectionConverters._

case class TelegramMessage(body: String)

class Telegram(stats: ActorRef) extends Actor {
  val bot = new TelegramBot(apiKey)

  bot.setUpdatesListener(updates => handleUpdates(updates.asScala.toSeq))

  private def handleUpdates(updates: Seq[Update]): Int = {
    val commands = updates
      .flatMap(u => Some(u.message))
      .filter(u => u.entities != null)
      .filter(u => u.entities.exists(e => e.`type`.eq(MessageEntity.Type.bot_command)))
      .map(u => u.text)
    handleCommands(commands)

    UpdatesListener.CONFIRMED_UPDATES_ALL
  }

  private def handleCommands(commands: Seq[String]): Unit = {
    commands
      .foreach {
        case "/hi" => self ! TelegramMessage("Hi!")
        case "/today" => stats ! GetToday()
        case "/refresh" => stats ! UpdateAll()
        case e => self ! TelegramMessage(s"Unknown command: $e")
      }
  }

  def chatId: Long = sys.env("TELEGRAM_CHATID").toLong

  def apiKey: String = sys.env("TELEGRAM_APIKEY")

  override def receive: Receive = {
    case msg: TelegramMessage => bot.execute(new SendMessage(chatId, msg.body))
  }
}

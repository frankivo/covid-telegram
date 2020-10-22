package com.github.frankivo

import com.pengrad.telegrambot.model.{MessageEntity, Update}
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.{TelegramBot, UpdatesListener}

import scala.jdk.CollectionConverters._

class Telegram {
  val bot = new TelegramBot(apiKey)

  bot.setUpdatesListener(updates => handleUpdates(updates.asScala.toSeq))

  private def handleUpdates(updates: Seq[Update]): Int = {
    val commands = updates
      .filter(u => u.message.entities != null)
      .filter(u => u.message.entities.exists(e => e.`type`.eq(MessageEntity.Type.bot_command)))
      .map(u => u.message.text)
    handleCommands(commands)

    UpdatesListener.CONFIRMED_UPDATES_ALL
  }

  private def handleCommands(commands: Seq[String]): Unit = {
    commands
      .foreach {
        case "/hi" => sendMessage("hi!")
        case e => sendMessage(s"Unknown command: ${e}")
      }
  }

  def chatId: Long = sys.env("TELEGRAM_CHATID").toLong

  def apiKey: String = sys.env("TELEGRAM_APIKEY")

  def sendMessage(msg: String): Unit = bot.execute(new SendMessage(chatId, msg))

}

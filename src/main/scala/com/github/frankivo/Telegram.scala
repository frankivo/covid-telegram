package com.github.frankivo

import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.{TelegramBot, UpdatesListener}

import scala.jdk.CollectionConverters._

class Telegram {
  val bot = new TelegramBot(apiKey)

  bot.setUpdatesListener(updates => handleUpdates(updates.asScala.toSeq))

  private def handleUpdates(updates: Seq[Update]): Int = {
    println(updates)
    UpdatesListener.CONFIRMED_UPDATES_ALL
  }

  def chatId: Long = sys.env("TELEGRAM_CHATID").toLong

  def apiKey: String = sys.env("TELEGRAM_APIKEY")

  def sendMessage(msg: String): Unit = bot.execute(new SendMessage(chatId, msg))

}

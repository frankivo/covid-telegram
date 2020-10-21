package com.github.frankivo

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage

object Telegram {

  val bot = new TelegramBot(apiKey)

  def chatId: Long = sys.env("TELEGRAM_CHATID").toLong

  def apiKey: String = sys.env("TELEGRAM_APIKEY")

  def sendMessage(msg: String): Unit = bot.execute(new SendMessage(chatId, msg))

}

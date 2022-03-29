package com.github.frankivo.messages

/** Send a text message on Telegram.
  *
  * @param destination
  *   The recipient.
  * @param body
  *   The message.
  */
case class TelegramText(destination: Long, body: String)

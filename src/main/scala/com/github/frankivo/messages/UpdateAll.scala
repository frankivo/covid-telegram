package com.github.frankivo.messages

/** Requests to refresh data from source.
  *
  * @param destination
  *   Message result to this destination on Telegram.
  */
case class UpdateAll(destination: Option[Long])

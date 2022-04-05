package com.github.frankivo.messages

/** Requests to refresh data from source.
  *
  * @param destination
  *   Message result to this destination on Telegram.
  * @param forceAmount
  *   Deletes amount of files and downloads again.
  */
case class UpdateAll(destination: Option[Long], forceAmount: Long = 0)

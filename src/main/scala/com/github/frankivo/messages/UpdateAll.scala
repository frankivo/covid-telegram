package com.github.frankivo.messages

/** Requests to refresh data from source.
  *
  * @param destination
  *   Message result to this destination on Telegram.
  * @param force
  *   Deletes latest file and downloads it again.
  */
case class UpdateAll(destination: Option[Long], force: Boolean = false)

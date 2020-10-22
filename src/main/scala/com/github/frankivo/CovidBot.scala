package com.github.frankivo

object CovidBot {

  def main(args: Array[String]): Unit = {
    val telegram = new Telegram
    telegram.sendMessage("Hello, World!")
  }

}

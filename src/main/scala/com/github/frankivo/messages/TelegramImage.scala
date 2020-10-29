package com.github.frankivo.messages

import java.io.File

/**
 * Send an image on Telegram.
 *
 * @param destination The recipient.
 * @param image The image file.
 */
case class TelegramImage(destination: Long, image: File)

package com.github.frankivo.messages

import java.io.File

/**
 * Send an image on Telegram.
 *
 * @param destination The recipient.
 * @param file The image file.
 */
case class TelegramImage(destination: Long, file: File)

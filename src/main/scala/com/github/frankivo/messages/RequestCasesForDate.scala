package com.github.frankivo.messages

/**
 *
 * @param destination Send the result of this request to this recipient.
 * @param date        The date to request the cases for. Latest day if None.
 */
case class RequestCasesForDate(destination: Long, date: Option[String] = None)
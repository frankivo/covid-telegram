package com.github.frankivo.messages

/**
 * Request to fine the day with the highest amount of cases.
 *
 * @param destination Send the result of this request to this recipient.
 */
case class RequestAllTimeHigh(destination: Long)
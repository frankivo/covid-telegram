package com.github.frankivo.messages

/** Request an image for weekly stats.
  *
  * @param destination
  *   The requester.
  * @param year
  *   The year to request a graph for.
  */
case class RequestWeekGraph(destination: Long, year: Option[String])

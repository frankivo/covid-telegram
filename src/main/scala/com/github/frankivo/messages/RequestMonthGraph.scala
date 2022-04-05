package com.github.frankivo.messages

/** Request an image for a specified month.
  *
  * @param destination
  *   The requester.
  * @param month
  *   The month for the graph.
  */
case class RequestMonthGraph(destination: Long, month: Option[String])

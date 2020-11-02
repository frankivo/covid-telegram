package com.github.frankivo.messages

/**
 * Request an image for the last 100 days.
 *
 * @param destination The requester.
 */
case class RequestRollingGraph(destination: Long)

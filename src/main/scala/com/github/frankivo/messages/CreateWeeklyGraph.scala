package com.github.frankivo.messages

/**
 * Make graph for weekly data.
 * @param data Weeknumbers and counts.
 */
case class CreateWeeklyGraph(data: Seq[(Int, Long)])

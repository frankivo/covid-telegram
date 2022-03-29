package com.github.frankivo.messages

import com.github.frankivo.model.WeekRecord

/** Make graph for weekly data.
  *
  * @param data
  *   Week records.
  */
case class CreateWeeklyGraph(data: Seq[WeekRecord])

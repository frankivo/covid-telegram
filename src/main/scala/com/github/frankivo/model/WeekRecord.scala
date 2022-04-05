package com.github.frankivo.model

import com.github.frankivo.util.DateHelper.LocalDateExtender

import java.time.LocalDate

/** Covid cases per week.
  *
  * @param year
  *   The year for this data.
  * @param weekOfYear
  *   The week for this data.
  * @param count
  *   Weeks cases count.
  */
case class WeekRecord(year: Int, weekOfYear: Int, count: Long) {
  def isCurrentWeek: Boolean = {
    LocalDate.now.weekNumber == weekOfYear && LocalDate.now.getYear == year
  }

  override def clone(): AnyRef = super.clone()
}

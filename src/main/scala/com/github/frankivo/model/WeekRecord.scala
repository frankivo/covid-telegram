package com.github.frankivo.model

/**
 * Covid cases per week.
 * @param weekNumber The week for this data.
 * @param count Weeks cases count.
 */
case class WeekRecord(weekNumber: Int, count: Long) // Todo: YEAR

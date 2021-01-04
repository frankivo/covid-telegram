package com.github.frankivo.model

/**
 * Covid cases per week.
 *
 * @param year       The year for this data.
 * @param weekOfYear The week for this data.
 * @param count      Weeks cases count.
 */
case class WeekRecord(year: Int, weekOfYear: Int, count: Long)

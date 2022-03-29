package com.github.frankivo.util

import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

object DateHelper {
  implicit class LocalDateExtender(d: LocalDate) {

    /** Calculates the weeknumber.
      *
      * @return
      *   Weeknumber.
      */
    def weekNumber: Int = d.get(WeekFields.of(Locale.GERMANY).weekOfYear())
  }
}

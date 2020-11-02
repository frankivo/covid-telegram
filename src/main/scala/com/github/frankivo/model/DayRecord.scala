package com.github.frankivo.model

import java.time.LocalDate

/**
 * Covid data for ONE day.
 *
 * @param date  Record date.
 * @param count Cases for this day.
 */
case class DayRecord(date: LocalDate, count: Long)

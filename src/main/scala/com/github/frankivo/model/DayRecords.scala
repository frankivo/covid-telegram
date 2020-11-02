package com.github.frankivo.model

import java.time.LocalDate

/**
 * Covid data for a range of time.
 *
 * @param data Records.
 */
case class DayRecords(data: Seq[DayRecord]) {
  /**
   * Find case count for a specific date.
   *
   * @param date Date to find cases for.
   * @return Cases if date-data is present. None otherwise.
   */
  def findDayCount(date: LocalDate): Option[Long] = data.find(_.date.isEqual(date)).map(_.count)

  /**
   * Get latest record.
   *
   * @return Latest DayRecord.
   */
  def latest(): DayRecord = data.maxBy(_.date)
}

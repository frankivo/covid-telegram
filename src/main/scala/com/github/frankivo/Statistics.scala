package com.github.frankivo

import java.time.LocalDate

case class Statistics(data: Seq[CovidRecord]) {
  def findDayCount(date: LocalDate): Option[CovidRecord] = data.find(_.date.isEqual(date))

  def latest(): CovidRecord = data.maxBy(_.date)

  def findMaxDate(): LocalDate = data.map(_.date).max
}

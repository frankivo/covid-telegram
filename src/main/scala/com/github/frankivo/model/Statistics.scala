package com.github.frankivo.model

import java.time.LocalDate

case class Statistics(data: Seq[CovidRecord]) {
  def findDayCount(date: LocalDate): Option[CovidRecord] = data.find(_.date.isEqual(date))

  def latest(): CovidRecord = data.maxBy(_.date)
}

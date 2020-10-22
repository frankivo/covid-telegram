package com.github.frankivo

import java.time.LocalDate

case class CovidRecord(date: LocalDate, count: Long)

object CovidRecordHelper {

  implicit class CovidSequence(seq: Seq[CovidRecord]) {
    def withDayZero: Seq[CovidRecord] = Seq(CovidRecord(seq.head.date.minusDays(1), 0)) ++ seq

    def orderByDate: Seq[CovidRecord] = seq.sortBy(j => j.date)

    def getDailyCounts: Seq[CovidRecord] = {
      val zipped = seq.orderByDate.withDayZero.zipWithIndex

      for (i <- zipped.head._2 to zipped.last._2; if i > 0) yield {
        val prev: CovidRecord = zipped(i - 1)._1
        val curr = zipped(i)._1
        val diff = curr.count - prev.count
        CovidRecord(curr.date, diff)
      }
    }
  }

}

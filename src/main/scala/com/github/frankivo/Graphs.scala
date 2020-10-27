package com.github.frankivo

import java.nio.file.{Path, Paths}
import java.time.LocalDate

import akka.actor.Actor
import org.jfree.chart.{ChartFactory, ChartUtils}
import org.jfree.data.category.DefaultCategoryDataset

case class MonthData(data: Seq[CovidRecord])

object Graphs {
  val DIR_GRAPHS: Path = Paths.get(CovidBot.DIR_BASE.toString, "graphs")
  val DIR_MONTHS: Path = Paths.get(DIR_GRAPHS.toString, "month")
}

class Graphs extends Actor {

  override def receive: Receive = {
    case e: MonthData => createMonthGraph(e.data)
  }

  def mapData(data: Seq[CovidRecord]): DefaultCategoryDataset = {
    val dataset = new DefaultCategoryDataset

    data
      .sortBy(_.date)
      .foreach(s => dataset.setValue(s.count.toDouble, "Cases", s.date.getDayOfMonth))
    dataset
  }

  def createMonthGraph(data: Seq[CovidRecord]): Unit = {
    Graphs.DIR_MONTHS.toFile.mkdirs()

    val firstDate = data.head.date

    val imgFile = Paths.get(
      Graphs.DIR_MONTHS.toString,
      s"${firstDate.getYear}_${firstDate.getMonthValue}.png"
    ).toFile

    val doUpdate = !imgFile.exists() || isCurrentMonth(firstDate)

    if (doUpdate) {
      val barChart = ChartFactory.createBarChart(
        s"Cases ${camelCase(firstDate.getMonth.toString)} ${firstDate.getYear}",
        "Day",
        "Cases",
        mapData(data)
      )

      imgFile.delete()
      ChartUtils.saveChartAsPNG(imgFile, barChart, 800, 400)
    }
  }

  def camelCase(str: String): String = str.take(1).toUpperCase() + str.drop(1).toLowerCase()

  def isCurrentMonth(date: LocalDate): Boolean = {
    val now = LocalDate.now()
    date.getMonthValue == now.getMonthValue && date.getYear == now.getYear
  }
}

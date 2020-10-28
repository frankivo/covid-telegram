package com.github.frankivo

import java.nio.file.{Path, Paths}
import java.time.LocalDate

import akka.actor.Actor
import org.jfree.chart.{ChartFactory, ChartUtils}
import org.jfree.data.category.DefaultCategoryDataset

case class MonthData(data: Seq[CovidRecord])

case class WeekData(data: Seq[(Int, Long)])

object Graphs {
  val DIR_GRAPHS: Path = Paths.get(CovidBot.DIR_BASE.toString, "graphs")
  val DIR_MONTHS: Path = Paths.get(DIR_GRAPHS.toString, "month")
  val DIR_WEEKS: Path = Paths.get(DIR_GRAPHS.toString, "week")
}

class Graphs extends Actor {

  override def receive: Receive = {
    case e: MonthData => createMonthGraph(e.data)
    case e: WeekData => createWeeklyGraph(e.data)
  }

  def mapMonthData(data: Seq[CovidRecord]): DefaultCategoryDataset = {
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
        mapMonthData(data)
      )

      imgFile.delete()
      ChartUtils.saveChartAsPNG(imgFile, barChart, 1000, 400)
    }
  }

  def mapWeekData(data: Seq[(Int, Long)]): DefaultCategoryDataset = {
    val dataset = new DefaultCategoryDataset

    data
      .sortBy(_._1)
      .foreach(s => dataset.setValue(s._2.toDouble, "Cases", s._1))
    dataset
  }

  def createWeeklyGraph(data: Seq[(Int, Long)]): Unit = {
    Graphs.DIR_WEEKS.toFile.mkdirs()

    val imgFile = Paths.get(Graphs.DIR_WEEKS.toString, "2020.png").toFile

    val barChart = ChartFactory.createBarChart(
      s"Cases 2020 per week",
      "Week",
      "Cases",
      mapWeekData(data)
    )

    imgFile.delete()
    ChartUtils.saveChartAsPNG(imgFile, barChart, 1000, 400)
  }

  def camelCase(str: String): String = str.take(1).toUpperCase() + str.drop(1).toLowerCase()

  def isCurrentMonth(date: LocalDate): Boolean = {
    val now = LocalDate.now()
    date.getMonthValue == now.getMonthValue && date.getYear == now.getYear
  }
}

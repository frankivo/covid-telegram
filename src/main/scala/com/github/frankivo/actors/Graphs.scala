package com.github.frankivo.actors

import java.nio.file.{Path, Paths}
import java.time.LocalDate

import akka.actor.Actor
import com.github.frankivo.CovidBot
import com.github.frankivo.JFreeChart.{FirstDateAxis, FirstDatePainter}
import com.github.frankivo.messages._
import com.github.frankivo.model.{DayRecord, WeekRecord}
import org.jfree.chart.{ChartFactory, ChartUtils}
import org.jfree.data.category.DefaultCategoryDataset

object Graphs {
  val DIR_GRAPHS: Path = Paths.get(CovidBot.DIR_BASE.toString, "graphs")
  val DIR_MONTHS: Path = Paths.get(DIR_GRAPHS.toString, "month")
  val DIR_WEEKS: Path = Paths.get(DIR_GRAPHS.toString, "week")
}

class Graphs extends Actor {

  override def receive: Receive = {
    case e: CreateMonthGraph => createMonthGraph(e.data)
    case e: CreateRollingGraph => createRollingGraph(e.data)
    case e: CreateWeeklyGraph => createWeeklyGraph(e.data)
    case e: RequestMonthGraph => requestMonthGraph(e)
    case e: RequestWeekGraph => requestWeekGraph(e)
  }

  /**
   * Creates a graph over the last N dates.
   * Every first day of the month is being highlighted.
   *
   * @param data Covid data.
   */
  def createRollingGraph(data: Seq[DayRecord]): Unit = {
    Graphs.DIR_GRAPHS.toFile.mkdirs()

    val imgFile = Paths.get(Graphs.DIR_GRAPHS.toString, "rolling.png").toFile
    imgFile.delete()

    val dataset = new DefaultCategoryDataset
    data
      .foreach(s => dataset.setValue(s.count.toDouble, "Cases", s.date))

    val barChart = ChartFactory.createBarChart(
      s"Cases last 100 days",
      "Day",
      "Cases",
      dataset
    )

    barChart.getCategoryPlot.setDomainAxis(new FirstDateAxis)
    barChart.getCategoryPlot.setRenderer(new FirstDatePainter(data))
    barChart.removeLegend()

    ChartUtils.saveChartAsPNG(imgFile, barChart, 1000, 400)
  }

  def mapMonthData(data: Seq[DayRecord]): DefaultCategoryDataset = {
    val dataset = new DefaultCategoryDataset

    data
      .sortBy(_.date)
      .foreach(s => dataset.setValue(s.count.toDouble, "Cases", s.date.getDayOfMonth))
    dataset
  }

  def createMonthGraph(data: Seq[DayRecord]): Unit = {
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

  def mapWeekData(data: Seq[WeekRecord]): DefaultCategoryDataset = {
    val dataset = new DefaultCategoryDataset

    data
      .sortBy(_.weekNumber)
      .foreach(s => dataset.setValue(s.count.toDouble, "Cases", s.weekNumber))
    dataset
  }

  def createWeeklyGraph(data: Seq[WeekRecord]): Unit = {
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

  def requestMonthGraph(request: RequestMonthGraph): Unit = {
    val curYear = LocalDate.now().getYear
    val curMonth = LocalDate.now().getMonthValue

    try {
      val (year, month) = {
        if (request.month.isDefined) (curYear, request.month.get.toInt)
        else (curYear, curMonth)
      }

      val file = Paths.get(Graphs.DIR_MONTHS.toString, s"${year}_$month.png").toFile
      if (file.exists())
        CovidBot.ACTOR_TELEGRAM ! TelegramImage(request.destination, file)
      else {
        val msg = s"No file found for 'month/${year}_$month'"
        CovidBot.ACTOR_TELEGRAM ! TelegramText(request.destination, msg)
      }
    }
    catch {
      case _: Exception => CovidBot.ACTOR_TELEGRAM ! TelegramText(request.destination, "Failed!")
    }
  }

  def requestWeekGraph(request: RequestWeekGraph): Unit = {
    val file = Paths.get(Graphs.DIR_WEEKS.toString, "2020.png").toFile
    if (file.exists())
      CovidBot.ACTOR_TELEGRAM ! TelegramImage(request.destination, file)
    else
      CovidBot.ACTOR_TELEGRAM ! TelegramText(request.destination, "File not found")
  }
}

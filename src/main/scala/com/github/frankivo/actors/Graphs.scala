package com.github.frankivo.actors

import akka.actor.Actor
import com.github.frankivo.CovidBot
import com.github.frankivo.JFreeChart.{FifthWeekAxis, FirstDateAxis, FirstDateBarRenderer}
import com.github.frankivo.messages._
import com.github.frankivo.model.{DayRecord, WeekRecord}
import org.jfree.chart.{ChartFactory, ChartUtils}
import org.jfree.data.category.DefaultCategoryDataset

import java.io.File
import java.nio.file.{Path, Paths}
import java.time.LocalDate

object Graphs {
  val DIR_GRAPHS: Path = Paths.get(CovidBot.DIR_BASE.toString, "graphs")
  val DIR_MONTHS: Path = Paths.get(DIR_GRAPHS.toString, "month")
  val DIR_WEEKS: Path = Paths.get(DIR_GRAPHS.toString, "week")

  val IMG_WIDTH: Int = 1000
  val IMG_HEIGHT: Int = 400
}

/**
 * Create graphs on request.
 */
class Graphs extends Actor {

  override def receive: Receive = {
    case e: CreateMonthGraph => createMonthGraph(e.data)
    case e: CreateRollingGraph => createRollingGraph(e.data)
    case e: CreateWeeklyGraph => createWeeklyGraphs(e.data)
    case e: RequestMonthGraph => requestMonthGraph(e)
    case e: RequestRollingGraph => requestRollingGraph(e)
    case e: RequestWeekGraph => requestWeekGraph(e)
  }

  /**
   * Creates a graph over the last N dates.
   * Every first day of the month is being highlighted.
   *
   * @param data Covid data.
   */
  private def createRollingGraph(data: Seq[DayRecord]): Unit = {
    Graphs.DIR_GRAPHS.toFile.mkdirs()

    val imgFile = Paths.get(Graphs.DIR_GRAPHS.toString, "rolling.png").toFile
    imgFile.delete()

    val dataset = new DefaultCategoryDataset
    data
      .foreach(s => dataset.setValue(s.count.toDouble, "Cases", s.date))

    val barChart = ChartFactory.createBarChart(
      s"Cases last ${CovidStats.ROLLING_DAYS} days",
      "Day",
      "Cases",
      dataset
    )

    barChart.getCategoryPlot.setDomainAxis(new FirstDateAxis)
    barChart.getCategoryPlot.setRenderer(new FirstDateBarRenderer(data))
    barChart.removeLegend()

    ChartUtils.saveChartAsPNG(imgFile, barChart, Graphs.IMG_WIDTH, Graphs.IMG_HEIGHT)
  }

  private def createMonthGraph(data: Seq[DayRecord]): Unit = {
    Graphs.DIR_MONTHS.toFile.mkdirs()

    val firstDate = data.head.date

    val imgFile = Paths.get(
      Graphs.DIR_MONTHS.toString,
      s"${firstDate.getYear}_${firstDate.getMonthValue}.png"
    ).toFile

    val doUpdate = !imgFile.exists() || isCurrentMonth(firstDate)

    if (doUpdate) {
      val dataset = new DefaultCategoryDataset
      data
        .sortBy(_.date)
        .foreach(s => dataset.setValue(s.count.toDouble, "Cases", s.date.getDayOfMonth))

      val barChart = ChartFactory.createBarChart(
        s"Cases ${camelCase(firstDate.getMonth.toString)} ${firstDate.getYear}",
        "Day",
        "Cases",
        dataset
      )
      barChart.removeLegend()

      imgFile.delete()
      ChartUtils.saveChartAsPNG(imgFile, barChart, Graphs.IMG_WIDTH, Graphs.IMG_HEIGHT)
    }
  }

  private def createWeeklyGraphs(data: Seq[WeekRecord]): Unit = {
    Graphs.DIR_WEEKS.toFile.mkdirs()

    data
      .map(_.year)
      .distinct
      .foreach(year => createWeeklyWeekGraph(data.filter(_.year == year)))

    createWeeklyRollingGraph(data)
  }

  private def createWeeklyWeekGraph(data: Seq[WeekRecord]): Unit = {
    val year = data.head.year

    val imgFile = Paths.get(Graphs.DIR_WEEKS.toString, s"$year.png").toFile

    val dataset = new DefaultCategoryDataset
    data
      .sortBy(_.weekOfYear)
      .foreach(s => dataset.setValue(s.count.toDouble, "Cases", s.weekOfYear))

    val barChart = ChartFactory.createBarChart(
      s"Cases $year per week",
      "Week",
      "Cases",
      dataset
    )
    barChart.removeLegend()
    barChart.getCategoryPlot.setDomainAxis(new FifthWeekAxis)

    imgFile.delete()
    ChartUtils.saveChartAsPNG(imgFile, barChart, Graphs.IMG_WIDTH, Graphs.IMG_HEIGHT)
  }

  private def createWeeklyRollingGraph(data: Seq[WeekRecord]): Unit = {
    val imgFile = Paths.get(Graphs.DIR_WEEKS.toString, "rolling.png").toFile

    val dataset = new DefaultCategoryDataset
    data
      .sortBy(w => (w.year, w.weekOfYear))
      .takeRight(50)
      .foreach(s => dataset.setValue(s.count.toDouble, "Cases", s.weekOfYear))

    val barChart = ChartFactory.createBarChart(
      s"Cases per week",
      "Week",
      "Cases",
      dataset
    )
    barChart.removeLegend()
    barChart.getCategoryPlot.setDomainAxis(new FifthWeekAxis)

    imgFile.delete()
    ChartUtils.saveChartAsPNG(imgFile, barChart, Graphs.IMG_WIDTH, Graphs.IMG_HEIGHT)
  }

  private def camelCase(str: String): String = str.take(1).toUpperCase() + str.drop(1).toLowerCase()

  private def isCurrentMonth(date: LocalDate): Boolean = {
    val now = LocalDate.now()
    date.getMonthValue == now.getMonthValue && date.getYear == now.getYear
  }

  private def requestMonthGraph(request: RequestMonthGraph): Unit = {
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

  private def requestRollingGraph(request: RequestRollingGraph): Unit = {
    requestImage(request.destination, Paths.get(Graphs.DIR_GRAPHS.toString, "rolling.png").toFile)
  }

  private def requestWeekGraph(request: RequestWeekGraph): Unit = {
    val year = request.year.getOrElse(LocalDate.now.getYear.toString)
    requestImage(request.destination, Paths.get(Graphs.DIR_WEEKS.toString, s"$year.png").toFile)
  }

  private def requestImage(destination: Long, file: File): Unit = {
    if (file.exists())
      CovidBot.ACTOR_TELEGRAM ! TelegramImage(destination, file)
    else
      CovidBot.ACTOR_TELEGRAM ! TelegramText(destination, "File not found")
  }
}

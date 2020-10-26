package com.github.frankivo

import java.nio.file.Paths
import java.time.Month

import akka.actor.Actor
import org.jfree.chart.{ChartFactory, ChartUtils}
import org.jfree.data.category.DefaultCategoryDataset

import scala.reflect.io.{Directory, File}

case class MonthData(data: Seq[CovidRecord])

object Graphs {
  val tmpDir: File = new File(Paths.get(System.getProperty("java.io.tmpdir"), "covidbot").toFile)

  def tmpFile(name: String): File = {
    createTempDir()
    new File(Paths.get(tmpDir.toString, name).toFile)
  }

  def deleteDir(): Unit = new Directory(tmpDir.jfile).deleteRecursively()

  def createTempDir(): Unit = new Directory(tmpDir.jfile).createDirectory()
}

class Graphs extends Actor {
  override def receive: Receive = {
    case e: MonthData => createMonthGraph(e.data)
  }

  def mapData(data: Seq[CovidRecord]) : DefaultCategoryDataset = {
    val dataset = new DefaultCategoryDataset

    data.foreach(s => dataset.setValue(s.count, "Cases", s.date.getDayOfMonth))
    dataset
  }

  def createMonthGraph(data: Seq[CovidRecord]): Unit = {
    Graphs.tmpFile("month").createDirectory()

    val firstDate = data.head.date

    val barChart = ChartFactory.createBarChart(s"Cases ${camelCase(firstDate.getMonth.toString)} ${firstDate.getYear}", "Day", "Cases", mapData(data))
    val imgFile = Graphs.tmpFile(s"month/${firstDate.getYear}_${firstDate.getMonthValue}.png")
    
    ChartUtils.saveChartAsPNG(imgFile.jfile, barChart, 800, 400)
  }

  def camelCase(str: String): String = str.take(1).toUpperCase() + str.drop(1).toLowerCase()
}

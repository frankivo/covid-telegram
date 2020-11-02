package com.github.frankivo.JFreeChart

import java.awt.Graphics2D
import java.time.LocalDate

import org.jfree.chart.axis.CategoryAxis
import org.jfree.chart.text.{G2TextMeasurer, TextBlock, TextUtils}
import org.jfree.chart.ui.RectangleEdge

/**
 * A custom CategoryAxis.
 * Displays label only on first day of the month.
 */
class FirstDateAxis extends CategoryAxis {
  override def createLabel(category: Comparable[_], width: Float, edge: RectangleEdge, g2: Graphics2D): TextBlock = {
    TextUtils.createTextBlock(
      labelText(category),
      getTickLabelFont(category),
      getTickLabelPaint(category),
      200,
      1,
      new G2TextMeasurer(g2)
    )
  }

  private def labelText(category: Comparable[_]): String = {
    val date = category.asInstanceOf[LocalDate]
    if (date.getDayOfMonth == 1) category.toString else ""
  }
}

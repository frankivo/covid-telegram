package com.github.frankivo.JFreeChart

import java.awt.{Color, Paint}

import com.github.frankivo.model.DayRecord
import org.jfree.chart.renderer.category.BarRenderer

/**
 * A custom BarRenderer.
 * Paints every first day of the month in a different colour.
 * @param data Covid data.
 */
class FirstDatePainter(data: Seq[DayRecord]) extends BarRenderer {
  override def getItemPaint(row: Int, column: Int): Paint = {
    if (data(column).date.getDayOfMonth == 1) Color.RED
    else Color.ORANGE
  }
}
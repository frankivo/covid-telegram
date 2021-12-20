package com.github.frankivo.JFreeChart

import com.github.frankivo.model.DayRecord
import org.jfree.chart.renderer.category.BarRenderer

import java.awt.{Color, Paint}

/**
 * A custom BarRenderer.
 * Paints every first day of the month in a different colour.
 * @param data Covid data.
 */
case class FirstDateBarRenderer(data: Seq[DayRecord]) extends BarRenderer {
  override def getItemPaint(row: Int, column: Int): Paint = {
    if (data(column).date.getDayOfMonth == 1) Color.RED
    else Color.ORANGE
  }

  override def clone(): AnyRef = super.clone()
}

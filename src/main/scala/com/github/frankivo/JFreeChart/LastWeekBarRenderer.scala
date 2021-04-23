package com.github.frankivo.JFreeChart

import com.github.frankivo.model.WeekRecord
import org.jfree.chart.renderer.category.BarRenderer

import java.awt.{Color, Paint}

/**
 * A custom BarRenderer.
 * Paints the current week orange and all other weeks red.
 *
 * @param data Covid data.
 */
class LastWeekBarRenderer(data: Seq[WeekRecord]) extends BarRenderer {
  override def getItemPaint(row: Int, column: Int): Paint = {
    if (!data(column).isCurrentWeek) Color.RED
    else Color.ORANGE
  }
}

package com.github.frankivo.messages

import com.github.frankivo.model.DayRecord

/**
 * Make a rolling graph containing last N days.
 *
 * @param data Covid data.
 */
case class CreateRollingGraph(data: Seq[DayRecord])

package com.github.frankivo.messages

import com.github.frankivo.model.CovidRecord

/**
 * Make graph for month.
 *
 * @param data Data of one month.
 */
case class CreateMonthGraph(data: Seq[CovidRecord])
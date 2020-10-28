package com.github.frankivo.messages

import com.github.frankivo.CovidRecord

/**
 * Make graph for month.
 * @param data Data of one month.
 */
case class CreateMonthGraph(data: Seq[CovidRecord])
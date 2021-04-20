package com.github.frankivo.model

import java.time.LocalDate

/**
 * Covid data for al municipals for one day.
 *
 * @param date           Date of data.
 * @param municipalCount Map[Municipal, Count]
 */
case class MunicipalRecord(date: LocalDate, municipalCount: Map[String, Long])

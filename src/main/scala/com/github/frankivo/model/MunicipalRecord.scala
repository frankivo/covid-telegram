package com.github.frankivo.model

import java.time.LocalDate

case class MunicipalRecord(date: LocalDate, municipalCount: Map[String, Long])

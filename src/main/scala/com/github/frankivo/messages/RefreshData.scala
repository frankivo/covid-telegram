package com.github.frankivo.messages

import com.github.frankivo.model.DayRecord

/** Pushes new data.
  *
  * @param data
  *   The data.
  * @param containsUpdates
  *   True if any new data is present.
  */
case class RefreshData(data: Seq[DayRecord], containsUpdates: Boolean)

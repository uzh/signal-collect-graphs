package com.signalcollect.approx.flood

class Timer(tout: Long) extends Serializable {

  var startTime: Long = 0L
  var on: Boolean = false

  val timeout = tout
 
  def go = {
    startTime = System.currentTimeMillis
    on = true
  }
  def stopped: Boolean = {
    if ((on == false)||(System.currentTimeMillis - startTime > timeout)) {
      on = false
      true
    } else {
      false
    }
  }
}
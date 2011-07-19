/*
 *  @author Philip Stutz
 *  
 *  Copyright 2010 University of Zurich
 *      
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *         http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package com.signalcollect.graphproviders.synthetic

import scala.math._
import scala.util.Random

class Partitions(partitions: Int, vertices: Int, seed: Long, sigma: Double = 1, mu: Double = 3, symmetric: Boolean = false) extends Traversable[(Int, Int)] {

  def foreach[U](f: ((Int, Int)) => U) = {
    val r = new Random(seed)
    var i = 0
    while (i < vertices) {
      val from = i
      val outDegree: Int = exp(mu + sigma * (r.nextGaussian)).round.toInt //log-normal
      var j = 0
      while (j < outDegree) {
        val to = ((r.nextDouble * (vertices - 1))).round.toInt
        if (from != to && (to % partitions == from % partitions)) {
          f(from, to)
          if (symmetric)
        	f(to, from)
          j += 1
        }
      }
      i += 1
    }
  }

}
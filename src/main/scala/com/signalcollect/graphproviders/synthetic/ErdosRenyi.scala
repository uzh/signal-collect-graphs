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

/**
 *  Vertices are randomly connected, each possible edge gets chosen with probability edgeProbability
 *  See http://en.wikipedia.org/wiki/Erd%C5%91s%E2%80%93R%C3%A9nyi_model
 */
class ErdosRenyi(vertices: Int, edgeProbability: Double = 0.0001) extends Traversable[(Int, Int)] {

  def foreach[U](f: ((Int, Int)) => U) = {
    val r = new Random(0)
    for (from <- (0 to vertices).par) {
      for (to <- (0 to vertices).par) {
        if (from != to && r.nextDouble > edgeProbability) {
          f(from, to)
        }
      }
    }
  }

}
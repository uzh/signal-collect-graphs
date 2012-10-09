/*
 *  @author Daniel Strebel
 *  
 *  Copyright 2012 University of Zurich
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

import com.signalcollect._
import scala.util.Random
import scala.math._
import com.signalcollect.graphproviders.GraphProvider

class DistributedLogNormal(graphSize: Int, numberOfWorkers: Option[Int] = None, seed: Long = 0, sigma: Double = 1, mu: Double = 3) extends GraphProvider[Int] {
  def populate(graphEditor: GraphEditor, vertexBuilder: Int => Vertex[_, _], edgeBuilder: (Int, Int) => Edge[_]) {
    val r = new Random(seed)

    val workers = numberOfWorkers.getOrElse(24)

    //Load the vertices
    for (worker <- (0 until workers).par) {
      var vertexIdHint: Option[Int] = None
      if (numberOfWorkers.isDefined) {
        vertexIdHint = Some(worker)
      }
      for (vertexId <- worker.until(graphSize).by(workers)) {
        graphEditor.loadGraph(vertexIdHint, graph => {
          graphEditor.addVertex(vertexBuilder(vertexId))
        })
      }
    }

    //Load the edges
    for (worker <- (0 until workers).par) {
      var vertexIdHint: Option[Int] = None
      if (numberOfWorkers.isDefined) {
        vertexIdHint = Some(worker)
      }
      for (vertexId <- worker.until(graphSize).by(workers)) {
        graphEditor.loadGraph(vertexIdHint, graph => {
          val outDegree: Int = exp(mu + sigma * (r.nextGaussian)).round.toInt //log-normal
          var j = 0
          while (j < outDegree) {
            val to = ((r.nextDouble * (graphSize - 1))).round.toInt
            if (vertexId != to) {
              graph.addEdge(vertexId, edgeBuilder(vertexId, to))
              j += 1
            }
          }
        })
      }
    }
  }

  override def toString = "Distrubuted LogNormal(" + graphSize + ", " + numberOfWorkers + ", " + seed + ", " + sigma + ", " + mu + ")"

}
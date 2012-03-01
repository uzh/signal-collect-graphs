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

import com.signalcollect._
import com.signalcollect.graphproviders.GraphProvider

class FullyConnected(val vertices: Int) extends GraphProvider with Traversable[(Int, Int)] {

  def populateGraph(builder: GraphBuilder, vertexBuilder: (Any) => Vertex, edgeBuilder: (Any, Any) => Edge) = {
    val graph = builder.build
    for (id <- (0 to vertices).par) {
      graph.addVertex(vertexBuilder(id))
    }
    for (i <- 0 to vertices) {
      for (j <- 0 until i) {
        graph.addEdge(edgeBuilder(i, j))
        graph.addEdge(edgeBuilder(j, i))
      }
    }
    graph
  }

  def foreach[U](f: ((Int, Int)) => U) = {
    for (i <- 0 to vertices) {
      for (j <- 0 until i) {
        f((i, j))
        f((j, i))
      }
    }
  }
}
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

import com.signalcollect.graphproviders.GraphProvider
import com.signalcollect._

class Grid(val width: Int, height: Int) extends GraphProvider[Int, Any] {

  def populate(graphEditor: GraphEditor[Int, Any], vertexBuilder: Int => Vertex[Int, _], edgeBuilder: (Int, Int) => Edge[Int]) {
    val max = width * height

    for (id <- 1 to max) {
    	graphEditor.addVertex(vertexBuilder(id))
    }
    for (n <- 1 to max) {
      if (n + width <= max) {
        graphEditor.addEdge(n, edgeBuilder(n, n + width))
        graphEditor.addEdge(n + width, edgeBuilder(n + width, n))
      }
      if (n % height != 0) {
        graphEditor.addEdge(n, edgeBuilder(n, n + 1))
        graphEditor.addEdge(n + 1, edgeBuilder(n + 1, n))
      }
    }
  }

}
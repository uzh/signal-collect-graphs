/*
 *  @author Philip Stutz
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
import graphproviders.GraphProvider

class Chain(graphSize: Int, symmetric: Boolean = false) extends GraphProvider[Int, Any] {

  def populate(graphEditor: GraphEditor[Int, Any], vertexBuilder: Int => Vertex[Int, _], edgeBuilder: (Int, Int) => Edge[Int]) {
    for (id <- (0 until graphSize).par) {
      graphEditor.addVertex(vertexBuilder(id))
    }
    for (i <- (0 until graphSize - 1)) {
      graphEditor.addEdge(i, edgeBuilder(i, i + 1))
    }
  }

}
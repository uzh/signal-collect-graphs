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

class BinaryTree(val vertices: Int, inverted: Boolean = false) extends GraphProvider[Int, Any] {

  def populate(graphEditor: GraphEditor[Int, Any], vertexBuilder: Int => Vertex[Int, _, Int, Any], edgeBuilder: (Int, Int) => Edge[Int]) {
    for (j <- 0 until vertices) {
      graphEditor.addVertex(vertexBuilder(j))
    }
    var i = 1
    while (2 * i - 1 < vertices) {
      val sourceId = 2 * i - 1
      val targetId = i - 1
      if (inverted)
        graphEditor.addEdge(targetId, edgeBuilder(targetId, sourceId))
      else
        graphEditor.addEdge(sourceId, edgeBuilder(sourceId, targetId))
      if (2 * i < vertices) {
        val sourceId = i - 1
        val targetId = 2 * i
        if (inverted)
          graphEditor.addEdge(targetId, edgeBuilder(targetId, sourceId))
        else
          graphEditor.addEdge(sourceId, edgeBuilder(sourceId, targetId))
      }
      i += 1
    }
  }

  def foreach[U](f: ((Int, Int)) => U) = {
    var i = 1
    while (2 * i - 1 < vertices) {
      if (inverted)
        f((2 * i - 1, i - 1))
      else
        f((i - 1, 2 * i - 1))
      if (2 * i < vertices) {
        if (inverted)
          f((2 * i, i - 1))
        else
          f((i - 1, 2 * i))
      }

      i += 1
    }
  }
}
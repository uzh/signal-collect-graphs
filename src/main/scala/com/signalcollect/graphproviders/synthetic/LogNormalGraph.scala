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
import graphproviders.GraphProvider
import java.io.PrintWriter
import java.io.FileWriter

class LogNormalGraph(graphSize: Int, seed: Long = 0, sigma: Double = 1, mu: Double = 3) extends GraphProvider with Traversable[(Int, Int)] {

  def populateGraph(builder: GraphBuilder, vertexBuilder: (Any) => Vertex, edgeBuilder: (Any, Any) => Edge) = {
    val graph = builder.build
    for (id <- (0 until graphSize).par) {
      graph.addVertex(vertexBuilder(id))
    }

    val r = new Random(seed)

    for (i <- 0 until graphSize) {
      val from = i
      val outDegree: Int = exp(mu + sigma * (r.nextGaussian)).round.toInt //log-normal
      var j = 0
      while (j < outDegree) {
        val to = ((r.nextDouble * (graphSize - 1))).round.toInt
        if (from != to) {
          graph.addEdge(edgeBuilder(from, to))
          j += 1
        }
      }
    }
    graph
  }

  def foreach[U](f: ((Int, Int)) => U) = {
    val r = new Random(seed)
    var i = 0
    while (i < graphSize) {
      val from = i
      val outDegree: Int = exp(mu + sigma * (r.nextGaussian)).round.toInt //log-normal
      var j = 0
      while (j < outDegree) {
        val to = ((r.nextDouble * (graphSize - 1))).round.toInt
        if (from != to) {
          f(from, to)
          j += 1
        }
      }
      i += 1
    }
  }

  override def toString = "LogNormal(" + graphSize + ", " + seed + ", " + sigma + ", " + mu + ")"
}

object LogNormalGraph extends App {
  val numberOfVertices = 200000
  val sigma = 1
  val mu = 1
  val graph = new LogNormalGraph(numberOfVertices, 0, sigma, mu)
  val vertexWriter = new PrintWriter(new FileWriter("./lognormal-vertices" + numberOfVertices + "-sigma" + sigma + "-mu" + mu))
  for (i <- 0 until numberOfVertices) {
    vertexWriter.write(i + "\n")
  }
  vertexWriter.close
  val numberOfEdges = graph.size
  val edgeWriter = new PrintWriter(new FileWriter("./lognormal-edges" + numberOfEdges + "-sigma" + sigma + "-mu" + mu))
  graph.foreach(tuple => {
    tuple match {
      case (source, target) => {
        edgeWriter.write(source + "," + target + "\n")
      }
    }
  })
  edgeWriter.close
}
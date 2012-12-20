/*
 *  @author Daniel Strebel
 *  @author Philip Stutz
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
package com.signalcollect.dcopevaluation

import com.signalcollect.GraphBuilder
import com.signalcollect.ExecutionInformation
import com.signalcollect.graphproviders.synthetic._
import com.signalcollect.ExecutionConfiguration
import com.signalcollect.Graph
import com.signalcollect.evaluation.algorithms._
import com.signalcollect.configuration.ExecutionMode
import com.signalcollect.graphproviders.GraphProvider
import com.signalcollect.approx.flood._
import com.signalcollect.StateForwarderEdge
import com.signalcollect.dcopgraphproviders.AdoptFileGraphGenerator

class DSANEvaluationRun(
  graphBuilder: GraphBuilder[Any, Double] = new GraphBuilder[Any, Double](),
  graphProvider: GraphProvider[Any, Double],
  executionConfiguration: ExecutionConfiguration = ExecutionConfiguration(ExecutionMode.Synchronous).withSignalThreshold(0.01),
  jvmParams: String = "",
  reportMemoryStats: Boolean = false) extends EvaluationAlgorithmRun[Any, Double] {

  //var graph: Graph[Any, Double]

  def loadGraph = {
              val c12:  Constraint = Variable(1) != Variable(2)
          
          graph.addVertex(new DSANVertex(1, Array(c12), Array(0, 1)))
         	graph.addVertex(new DSANVertex(2, Array(c12), Array(0, 1)))
         
         	graph.addEdge(1, new StateForwarderEdge(2))
         	graph.addEdge(2, new StateForwarderEdge(1))
  }

  //TODO: Cut the AdoptFileGraphGenerator: creating a graph instance to use in buildGraph, and adding edges and vertices for loadGraph

  def buildGraph = {
       graph = new GraphBuilder[Any, Double].build

//    val outGraph = new AdoptFileGraphGenerator("data-sets/problems/Problem-GraphColor-8_3_2_0.4_r0")
//    print(outGraph)
//    outGraph.displayConstraintGraph
//    graph = outGraph.constraintGraph
    
    //val visualizer = new GraphVisualizer(new ComputeGraphInspector(outGraph.constraintGraph)).setVisible(true)
  } //graphBuilder

  def execute = {
    //        println("Printing vertex states")
    //    graph.foreachVertex(println(_))
    //    println("Done printing vertex states")
    val stats = graph.execute(executionConfiguration)
    //    println("Printing vertex states")
    //    graph.foreachVertex(println(_))
    //    println("Done printing vertex states")

    stats
  }

  override def postExecute: List[(String, String)] = {
    //val pseudoAggregate = 37 
  
    val pseudoAggregate = graph.aggregate(new GlobalUtility)
    List[(String, String)](("utility", pseudoAggregate.toString))
  }

  def algorithmName = "DSAN"

  def graphStructure = "Manual graph provider" //graphProvider.toString

  override def jvmParameters = jvmParams

  override def memoryStatsEnabled = reportMemoryStats

}

object DSANEvalR extends App {
  val out = new java.io.FileWriter("results.txt")
  val outTime = new java.io.FileWriter("resultsTime.txt")
  var startTime = System.nanoTime()
  val terminationCondition = new DSANGlobalTerminationCondition(out, outTime, startTime)

  val bla = new DSANEvaluationRun(graphProvider = null, executionConfiguration = ExecutionConfiguration().withExecutionMode(ExecutionMode.OptimizedAsynchronous).withGlobalTerminationCondition(terminationCondition).withTimeLimit(500), jvmParams = "-XX:+UseNUMA -XX:+UseCondCardMark -XX:+UseParallelGC", reportMemoryStats = true)
  bla.loadGraph
  println("loading complete. execute now")
  println(bla.execute)
  bla.shutdown
}

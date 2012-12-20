/*
 *  @author Philip Stutz, Mihaela Verman
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

/*
 * Implementation of Weighted Regret Monitoring with Inertia
 * ( Arslan, Marden, Shamma, 2007. "Autonomous vehicle-target assignment: a game theoretical formulation". 
 *  ASME Journal of Dynamic Systems, Measurement and Control 129, 584-596 )
 */

package com.signalcollect.approx.flood

import com.signalcollect._
import com.signalcollect.configuration._
import com.signalcollect.configuration.LoggingLevel._
import scala.util._
import scala.math
import com.signalcollect.interfaces.MessageBus

/**
 * Represents an Agent
 *
 *  @param id: the identifier of this vertex
 *  @param constraints: the set of constraints in which it is involved
 *  @param possibleValues: which values can the state take
 */
class WRMIVertex(id: Any, constraints: Iterable[Constraint], possibleValues: Array[Int]) extends DataGraphVertex(id, 0.0) {

  type Signal = Double
  var inertia: Double = 0.5 //time counter used for calculating temperature
  var oldState = possibleValues(0)
  var weightedAvgDiff: Array[Double] = Array.fill[Double](possibleValues.size)(0)
  var stateRegret: Array[Double] = Array.fill[Double](possibleValues.size)(0)
  var numberSatisfied: Int = 0 //number of satisfied constraints
  val fadingMemory: Double = 0.03 //constant rho for fading memory  - is 1 if we do not take into account memory and only current utility

  /**
   * The collect function chooses a new random state and chooses it if it improves over the old state,
   * or, if it doesn't it still chooses it (for exploring purposes) with probability decreasing with time
   */
  def collect(oldState: Double, mostRecentSignals: Iterable[Double]): Double = {

    //Update the weighted average regrets for each action 
    val neighbourConfigs = mostRecentSignalMap.map(x => (x._1,x._2)).toMap //neighbourConfigs must be immutable and mostRecentSignalMap is mutable, so we convert

    //we calculate the utility for our last state
    val configs = neighbourConfigs + (id -> oldState.toDouble)
    val utility = constraints.foldLeft(0.0)((a, b) => a + b.utility(configs))

    var normFactor: Double = 0

    for (i <- 0 to (possibleValues.size - 1)) {
      state = possibleValues(i)
      val possibleStatesConfigs = neighbourConfigs + (id -> state.toDouble)
      val possibleStatesConfigsUtility = constraints.foldLeft(0.0)((a, b) => a + b.utility(possibleStatesConfigs))
      val regret = possibleStatesConfigsUtility - utility
      weightedAvgDiff(i) = fadingMemory * regret + (1 - fadingMemory) * weightedAvgDiff(i)
      stateRegret(i) = if (weightedAvgDiff(i) < 0) 0 else weightedAvgDiff(i);

      normFactor += stateRegret(i)
    }

    //end Update weighted average diff and state regrets

    //Select one of the actions with maximum weighted average utilities as candidate State

    val r = new Random()
    val probabilitySel: Double = r.nextDouble()
    var candidateStateIndex: Int = -1

    var partialSumRegrets: Double = 0
    for (i <- 0 to (possibleValues.size - 1)) {
      if (candidateStateIndex == -1) {
        partialSumRegrets += stateRegret(i)
        if (probabilitySel * normFactor <= partialSumRegrets) {
          candidateStateIndex = i

        }

      }
    }

    val candidateState = possibleValues(candidateStateIndex)
    val candidateStateConfigs = neighbourConfigs + (id -> candidateState.toDouble)

    val probabilityAcc: Double = r.nextDouble()

    if (probabilityAcc > inertia) { // we adopt the new maximum state, else we do not change state
      println("Vertex: " + id + "; changed to state: " + candidateState + " of regret " + stateRegret(candidateStateIndex) + " instead of old state " + oldState + " with utility " + utility + "; prob = " + probabilityAcc + " > inertia =  " + inertia)
      numberSatisfied = constraints.foldLeft(0)((a, b) => a + b.satisfiesInt(candidateStateConfigs))
        //constraints map (_.satisfiesInt(candidateStateConfigs)) sum;
      return candidateState
    }
    numberSatisfied = constraints.foldLeft(0)((a, b) => a + b.satisfiesInt(configs))
      //constraints map (_.satisfiesInt(configs)) sum;
    return oldState

  } //end collect function

  override def scoreSignal: Double = {
    lastSignalState match {
      case Some(oldState) =>
        if ((oldState == state) && (numberSatisfied == constraints.size)) { //computation is allowed to stop only if state has not changed and the utility is maximized
          0
        } else {
          1
        }
      case other => 1

    }

  } //end scoreSignal

} //end WRMIVertex class

/** Builds an agents graph and executes the computation */
object WRMI extends App {

  val graph = GraphBuilder.withLoggingLevel(LoggingLevel.Debug).build

  println("From client: Graph built")

  //Simple graph with 2 vertices

  //    val c12:  Constraint = Variable(1) != Variable(2)
  //    
  //    graph.addVertex(new WRMIVertex(1, Array(c12), Array(0, 1)))
  //   	graph.addVertex(new WRMIVertex(2, Array(c12), Array(0, 1)))
  //   
  //   	graph.addEdge(new StateForwarderEdge(1, 2))
  //   	graph.addEdge(new StateForwarderEdge(2, 1))

  //Graph with 6 nodes 

  var constraints: List[Constraint] = List()

  constraints = (Variable(1) != Variable(2)) :: constraints
  constraints = (Variable(1) != Variable(3)) :: constraints
  constraints = (Variable(3) != Variable(2)) :: constraints
  constraints = (Variable(3) != Variable(4)) :: constraints
  constraints = (Variable(5) != Variable(4)) :: constraints
  constraints = (Variable(5) != Variable(3)) :: constraints
  constraints = (Variable(5) != Variable(6)) :: constraints
  constraints = (Variable(6) != Variable(2)) :: constraints

  for (i <- 1 to 6) {
    graph.addVertex(new WRMIVertex(i, constraints.filter(s => s.variablesList().contains(i)).toArray: Array[Constraint], Array(0, 1, 2)))
  }

  for (ctr <- constraints) {
    for (i <- ctr.variablesList()) {
      for (j <- ctr.variablesList()) {
        if (i != j)
          graph.addEdge(i, new StateForwarderEdge(j))
      }
    }
  }

  println("Begin")

  val stats = graph.execute(ExecutionConfiguration().withExecutionMode(ExecutionMode.Synchronous))
  println(stats)
  graph.foreachVertex(println(_))
  graph.shutdown
}

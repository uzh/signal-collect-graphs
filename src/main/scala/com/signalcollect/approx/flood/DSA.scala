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
 * Implementation of Distributed Stochastic Algorithm - B
 * ( Zhang, Wang, Wittenburg, 2002. "Distributed stochastic search for constraint satisfaction and optimization: Parallelism, phase transitions and performance". 
 *  In Proceedings  of AAAI-02 Workshop on Probabilistic Approaches in Search, 2002, pp. 53–59)
 */

package com.signalcollect.approx.flood

import com.signalcollect._
import com.signalcollect.configuration._
import com.signalcollect.configuration.LoggingLevel._
import scala.util._
import scala.math
import com.signalcollect.interfaces.MessageBus

object DSAVariant extends Enumeration {
  type DSAVariant = Value
  val A, B, C, D, E = Value
}

/**
 * Variants of DSA (citation from: Arshad, Silaghi, 2003. "Distributed Simulated Annealing and comparison to DSA")
 * DSA-A: whenever the current states can be improved, the change is made stochastically. Otherwise no change is mare
 * DSA-B: same as DSA-A, but agents also change their values stochastically if they know conflicts and changing the values does not increase the number of conflicts
 * DSA-C: same as DSA-B, but agents also change their values stochastically if there are no conflicts and they don't introduce any conflicts
 * DSA-D: same as DSA-B, but probability to move when state can be improved is 1. Can lead to cycling behavior!
 * DSA-E: same as DSA-C, but probability to move when state can be improved is 1. Can lead to cycling behavior!
 */

import DSAVariant._

/**
 * Represents an Agent
 *
 *  @param id: the identifier of this vertex
 *  @param constraints: the set of constraints in which it is involved
 *  @param possibleValues: which values can the state take
 */
class DSAVertex(id: Any, constraints: Iterable[Constraint], possibleValues: Array[Int], variant: DSAVariant) extends DataGraphVertex(id, 0.0) {

  type Signal = Double
  var oldState = possibleValues(0)
  var utility: Double = 0
  var numberSatisfied: Int = 0 //number of satisfied constraints
  var inertia: Double = 0.5 //probability to NOT change to the better state and keep the old state instead
  var maxDelta: Double = 0

  println("Variant is " + variant)
  /**
   * The collect function chooses a new random state and chooses it if it improves over the old state,
   * or, if it doesn't it still chooses it (for exploring purposes) with probability decreasing with time
   */
  def collect(oldState: Double, mostRecentSignals: Iterable[Double]): Double = {

    val neighbourConfigs = mostRecentSignalMap.map(x => (x._1,x._2)).toMap //neighbourConfigs must be immutable and mostRecentSignalMap is mutable, so we convert

    //Calculate utility and number of satisfied constraints for the current value
    val configs = neighbourConfigs + (id -> oldState.toDouble)
    utility = constraints.foldLeft(0.0)((a, b) => a + b.utility(configs))
    // (constraints map (_.utility(configs)) sum) // use foldLeft instead
    numberSatisfied = constraints.foldLeft(0)((a, b) => a + b.satisfiesInt(configs))
    //constraints map (_.satisfiesInt(configs)) sum

    //Initialize maximum Delta (difference between a new state utility and the current state utility) with 0
    maxDelta = 0
    var maxDeltaState = oldState

    //Calculate maximum Delta 

    //We search in all the states except the current one 
    for (newState <- possibleValues)
      if (newState != oldState) {

        val newconfigs = neighbourConfigs + (id -> newState.toDouble)
        val newStateUtility = constraints.foldLeft(0.0)((a, b) => a + b.utility(newconfigs))
        val newNumberSatisfied = constraints.foldLeft(0)((a, b) => a + b.satisfiesInt(newconfigs))
        val newNumberSatisfiedHard = constraints.foldLeft(0)((a, b) => a + b.satisfiesInt(newconfigs) * b.hardInt)

        if (newStateUtility - utility >= maxDelta) {
          maxDeltaState = newState
          maxDelta = newStateUtility - utility
        }
      } //if we would select randomly between multiple values with the same maximum Delta we would have the DSA-A,B,C,D or E _barred_, as in [Arshad, Silaghi, 2003]

    println("Vertex: " + id + "] " + state)

    // With some inertia we keep the last state even if it's not the best. Else, we update to the best new state. The exceptions are DSA-D and DSA-E, where we update with probability 1.
    val r = new Random()
    val probability: Double = r.nextDouble()

    if (variant == A) {
      if (!((maxDelta > 0) && (probability > inertia)))
        println("!!Vertex: " + id + "; NOT changed to state: " + maxDeltaState + " of new Delta " + maxDelta + " instead of old state " + oldState + " with utility " + utility + "; prob = " + probability + " > inertia =  " + inertia)
      if ((maxDelta > 0) && (probability > inertia)) {
        println("Vertex: " + id + "; changed to state: " + maxDeltaState + " of new Delta " + maxDelta + " instead of old state " + oldState + " with utility " + utility + "; prob = " + probability + " > inertia =  " + inertia)
        return maxDeltaState
      }
    }
    if (variant == B) {
      if (((maxDelta > 0) || ((maxDelta == 0) && (numberSatisfied != constraints.size))) && (probability > inertia)) {
        println("Vertex: " + id + "; changed to state: " + maxDeltaState + " of new Delta " + maxDelta + " instead of old state " + oldState + " with utility " + utility + "; prob = " + probability + " > inertia =  " + inertia)
        return maxDeltaState
      }
    }
    if (variant == C) {
      if ((maxDelta >= 0) && (probability > inertia)) {
        println("Vertex: " + id + "; changed to state: " + maxDeltaState + " of new Delta " + maxDelta + " instead of old state " + oldState + " with utility " + utility + "; prob = " + probability + " > inertia =  " + inertia)
        return maxDeltaState
      }
    }
    if (variant == D) {
      if ((maxDelta > 0) || ((maxDelta == 0) && (numberSatisfied != constraints.size) && (probability > inertia))) {
        println("Vertex: " + id + "; changed to state: " + maxDeltaState + " of new Delta " + maxDelta + " instead of old state " + oldState + " with utility " + utility + "; prob = " + probability + " > inertia =  " + inertia)
        return maxDeltaState
      }
    }
    if (variant == E) {
      if ((maxDelta > 0) || ((maxDelta == 0) && (probability > inertia))) {
        println("Vertex: " + id + "; changed to state: " + maxDeltaState + " of new Delta " + maxDelta + " instead of old state " + oldState + " with utility " + utility + "; prob = " + probability + " > inertia =  " + inertia)
        return maxDeltaState
      }

    }

    return oldState
  } //end collect function

  override def scoreSignal: Double = {
    lastSignalState match {
      case Some(oldState) =>
        if ((oldState == state) && (maxDelta == 0) /*(numberSatisfied == constraints.size)*/ ) { //computation is allowed to stop only if state has not changed and the utility is maximized
          0
        } else {
          1
        }
      case other => 1

    }

  } //end scoreSignal

} //end DSAVertex class

/** Builds an agents graph and executes the computation */
object DSA extends App {

  val graph = GraphBuilder. /*withLoggingLevel(LoggingLevel.Debug).*/ build

  println("From client: Graph built")

  //Simple graph with 2 vertices

  //    val c12:  Constraint = Variable(1) != Variable(2)
  //    
  //    graph.addVertex(new DSAVertex(1, Array(c12), Array(0, 1)))
  //   	graph.addVertex(new DSAVertex(2, Array(c12), Array(0, 1)))
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
    graph.addVertex(new DSAVertex(i, constraints.filter(s => s.variablesList().contains(i)).toArray: Array[Constraint], Array(0, 1, 2), A))
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

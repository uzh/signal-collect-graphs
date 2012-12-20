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
 * Implementation of Distributed Simulated Annealing 
 * ( Arshad, Silaghi, 2003. "Distributed Simulated Annealing and comparison to DSA". 
 *  In Proceedings of the 4th International Workshop on Distributed Contraint Reasoning, Acapulco, Mexico)
 */

package com.signalcollect.approx.flood

import com.signalcollect._
import com.signalcollect.configuration._
import com.signalcollect.configuration.LoggingLevel._
import scala.util._
import scala.math
import com.signalcollect.interfaces.MessageBus
import com.signalcollect.interfaces.SignalMessage
import com.signalcollect.dcopgraphproviders.AdoptFileGraphGenerator
import com.signalcollect.interfaces.AggregationOperation
import java.io.File
/**
 * Represents an Agent
 *
 *  @param id: the identifier of this vertex
 *  @param constraints: the set of constraints in which it is involved
 *  @param possibleValues: which values can the state take
 */
class DSANVertex(id: Any, csts: Iterable[Constraint], possibleValues: Array[Int]) extends DataGraphVertex(id, 0.0) {

  type Signal = Double
  var time: Int = 0 //time counter used for calculating temperature
  var oldState = possibleValues(0)

  val constraints: Iterable[Constraint] = csts
  var utility: Double = 0
  var numberHard: Int = constraints.foldLeft(0)((a, b) => a + b.hardInt) //number of hard constraints
  var numberSatisfied: Int = 0 //number of satisfied constraints
  var numberSatisfiedHard: Int = 0 //number of satisfied hard constraints
  val constTemp: Double = 100 //constant for calculating temperature

  val alarm: Timer = new Timer(100) //Seems unnecessary

  println(constraints)

  /**
   * The collect function chooses a new random state and chooses it if it improves over the old state,
   * or, if it doesn't it still chooses it (for exploring purposes) with probability decreasing with time
   */
  def collect(oldState: Double, mostRecentSignals: Iterable[Double]): Double = {

    time += 1

    // while (!alarm.stopped) //Seems unnecessary
    //{}

    val neighbourConfigs = mostRecentSignalMap.map(x => (x._1, x._2)).toMap //neighbourConfigs must be immutable and mostRecentSignalMap is mutable, so we convert

    //Calculate utility and number of satisfied constraints for the current value
    val configs = neighbourConfigs + (id -> oldState)
    //    println((oldState == state) +" " + (numberSatisfied == constraints.size)+" In collect " + id + " " + configs + " " + constraints)

    utility = constraints.foldLeft(0.0)((a, b) => a + b.utility(configs))
    numberSatisfied = constraints.foldLeft(0)((a, b) => a + b.satisfiesInt(configs))
    numberSatisfiedHard = constraints.foldLeft(0)((a, b) => a + b.satisfiesInt(configs) * b.hardInt)

    // Select randomly a value and adopt it with probability (e(delta/t_i)) when delta<=0 (to explore)
    // or with probability 1 otherwise
    val r = new Random()

    //Calculate utility and number of satisfied constraints for the new value
    val newStateIndex = r.nextInt(possibleValues.size)
    val newState = possibleValues(newStateIndex)
    val newconfigs = neighbourConfigs + (id -> newState.toDouble)
    val newStateUtility = constraints.foldLeft(0.0)((a, b) => a + b.utility(newconfigs))
    val newNumberSatisfied = constraints.foldLeft(0)((a, b) => a + b.satisfiesInt(newconfigs))
    val newNumberSatisfiedHard = constraints.foldLeft(0)((a, b) => a + b.satisfiesInt(newconfigs) * b.hardInt)

    // delta is the difference between the utility of the new randomly selected state and the utility of the old state. 
    // It is > 0 if the new state would lead to improvements
    val delta = newStateUtility - utility

    /**
     * The actual algorithm:
     * 	If new state does not improve the utility (delta<=0), select it for exploring purposes with probability (e(delta/t_i)) or else keep the old state
     * 	If new state does improve the utility (delta>0), select it instead of the old state
     * t_i is the temperature which has to be a decreasing function of time. For this particular case we chose t_i = constTemp/(time*time)
     */

    if (delta <= 0) { //The new state does not improve utility
      val adopt = r.nextDouble()
      if (adopt < math.exp(delta * time * time / constTemp)) { // We choose the new state (to explore) over the old state with probability (e(delta/t_i))

        //        if (oldState == newState) //We send a dummy value to self to avoid blocking - doesn't work in the Async version
        //          graphEditor.sendSignalToVertex(0.0, id)

        utility = newStateUtility
        numberSatisfied = newNumberSatisfied

        //println("Vertex: " + id + " at time " + time + "; Case DELTA=" + delta + "<= 0 and changed to state: " + newState + " instead of " + oldState + " with Adoption of new state prob =" + math.exp(delta * time * time / constTemp) + " ")
        //alarm.go //Seems unnecessary

        return newState

      } else { //With probability 1 - (e(delta/t_i)) we keep the old state which is better

        //graphEditor.sendSignalToVertex(0.0, id) //We send a dummy value to self to avoid blocking - doesn't work in the Async version
        //println("Vertex: " + id + " at time " + time + "; Case DELTA=" + delta + "<= 0 and NOT changed to state: " + newState + " instead of " + oldState + " with Adoption of new state prob =" + math.exp(delta * time * time / constTemp) + " ")
        //alarm.go //Seems unnecessary

        return oldState
      }
    } else { //The new state improves utility (delta>0), so we adopt the new state
      utility = newStateUtility
      numberSatisfied = newNumberSatisfied

      //println("Vertex: " + id + " at time " + time + "; Case DELTA=" + delta + "> 0 and changed to state: " + newState + " instead of " + oldState)
      ///alarm.go //Seems unnecessary

      return newState
    }

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

  //  override def scoreCollect(signals: Iterable[SignalMessage[_, _, _]]): Double = { //Modifying scoreCollect to include the alarm leads to problems in termination detection, even when modifying scoreSignal to be 0 only when alarm.stopped == true 
  // //   if (alarm.stopped == true)
  //      signals.size
  // //   else
  //  //    0.0
  //  
  //  } //end collectScore

  override def toString = {
    val stringResult = "Vertex ID: " + id + ", State: " + state + " Utility: " + utility + "/" + constraints.size +
      "\n Edges: " + this.outgoingEdges.size + //values + 
      "\n Possible values: " + possibleValues.mkString("; ") +
      "\n Constraints: " + csts.mkString("; ")

    stringResult
  }

} //end DSANVertex class

class GlobalUtility extends AggregationOperation[(Int, Double)] {
  val neutralElement = (0, 0.0)
  def extract(v: Vertex[_, _]): (Int, Double) = v match {
    case vertex: DSANVertex => (vertex.constraints.size, vertex.utility)
    case other => neutralElement
  }
  def reduce(elements: Stream[(Int, Double)]) = elements.foldLeft(neutralElement)(aggregate)
  def aggregate(a: (Int, Double), b: (Int, Double)): (Int, Double) = (a._1 + b._1, a._2 + b._2)
}

class DSANGlobalTerminationCondition(
  f: java.io.FileWriter,
  g: java.io.FileWriter,
  startTime: Long,
  aggregationOperation: AggregationOperation[(Int, Double)] = new GlobalUtility,
  aggregationInterval: Long = 5l) extends GlobalTerminationCondition[(Int, Double)](aggregationOperation, aggregationInterval) {
  def shouldTerminate(aggregate: (Int, Double)): Boolean = {
    if (aggregate._1 - aggregate._2 < 0.001) true
    else {
      f.write(aggregate._1 - aggregate._2 + " ")
      g.write((System.nanoTime() - startTime).toString + " ")
      print(aggregate._1 - aggregate._2 + " " + (System.nanoTime() - startTime).toString + "; ")

      false
    }
  }
}

/** Builds an agents graph and executes the computation */
object DSAN extends App {

  val myDirectory = new java.io.File("data-sets/problems/")
  val outS = new java.io.FileWriter("resultsS.txt")
  val outTimeS = new java.io.FileWriter("resultsTimeS.txt")
  val outA = new java.io.FileWriter("resultsA.txt")
  val outTimeA = new java.io.FileWriter("resultsTimeA.txt")

  for (file <- myDirectory.listFiles) {

    //Synchronous Execution
    val graphGenS = new AdoptFileGraphGenerator(file.getAbsolutePath())
    val graphS = graphGenS.constraintGraph

    println("From client -  Sync: Graph built from file: " + file.getName())
    outS.write(file.getName() + "S ")
    outTimeS.write(file.getName() + "S ")
    var startTime = System.nanoTime()
    val terminationConditionS = new DSANGlobalTerminationCondition(outS, outTimeS, startTime)
    val statsS = graphS.execute(ExecutionConfiguration().withExecutionMode(ExecutionMode.Synchronous).withGlobalTerminationCondition(terminationConditionS).withStepsLimit(1000))
    outS.write(statsS.aggregatedWorkerStatistics.numberOfOutgoingEdges.toString + "\n")
    outTimeS.write("\n")
    println("\n" + statsS.executionStatistics.computationTime)
    graphS.shutdown

    //Asynchronous Execution
    val graphGenA = new AdoptFileGraphGenerator(file.getAbsolutePath())
    val graphA = graphGenA.constraintGraph

    println("From client - Async: Graph built from file: " + file.getName())
    outA.write(file.getName() + "A ")
    outTimeA.write(file.getName() + "A ")
    startTime = System.nanoTime()
    val terminationConditionA = new DSANGlobalTerminationCondition(outA, outTimeA, startTime)
    val statsA = graphA.execute(ExecutionConfiguration().withExecutionMode(ExecutionMode.OptimizedAsynchronous).withGlobalTerminationCondition(terminationConditionA).withTimeLimit(500))
    outA.write(statsA.aggregatedWorkerStatistics.numberOfOutgoingEdges.toString + "\n")
    outTimeA.write("\n")
    println("\n" + statsA.executionStatistics.computationTime)
    graphA.shutdown

  }

  outS.close
  outTimeS.close
  outA.close
  outTimeA.close

}


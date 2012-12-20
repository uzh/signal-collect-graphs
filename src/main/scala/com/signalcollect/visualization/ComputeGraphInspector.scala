package com.signalcollect.visualization

import com.signalcollect._
import com.signalcollect.interfaces._
import java.util.LinkedList
import collection.JavaConversions._
import com.signalcollect.configuration.ExecutionMode

class ComputeGraphInspector(val cg: Graph[Any, _]) {

  def getMostRecentSignal(edgeId: EdgeId[_]): Any = {
    val signalOption: Option[_] = cg.forVertexWithId(edgeId.targetId, { v: Vertex[_, _] => v.asInstanceOf[DataGraphVertex[_, _]].getMostRecentSignal(edgeId) })
    if (signalOption.isDefined) {
      signalOption.get.asInstanceOf[Any]
    } else {
      null
    }
  }

  def getSuccessors(v: Vertex[_, _]): java.lang.Iterable[Vertex[_, _]] = {
    val result = new LinkedList[Vertex[_, _]]()
    val edges = getEdges(v)
    val successors = edges map (_.targetId)
    for (neighborId <- successors) {
      val neighbor = cg.forVertexWithId(neighborId, { v: Vertex[_, _] => v })
      result.add(neighbor)
    }
    result
  }

  def getEdges(v: Vertex[_, _]): java.lang.Iterable[Edge[_]] = {
    val result = new LinkedList[Edge[_]]()
    val edges = v.asInstanceOf[AbstractVertex[_, _]].outgoingEdges
    for (edge <- edges.values) {
      result.add(edge)
    }
    result
  }

  def isInt(s: String): Boolean = {
    try {
      s.toInt
      true
    } catch {
      case someProblem: Throwable => false
    }
  }

  def searchVertex(vertexId: String): java.lang.Iterable[Vertex[_, _]] = {
    if (isInt(vertexId)) {
      val vertex = getVertexWithId(vertexId.toInt)
      if (null != vertex) {
        val l = new LinkedList[Vertex[_, _]]()
        l.add(vertex)
        l
      } else {
        new LinkedList[Vertex[_, _]]()
      }
    } else {
      val aggr = new ModularAggregationOperation[LinkedList[Vertex[_, _]]] {
        def extract(v: Vertex[_, _]): LinkedList[Vertex[_, _]] = {
          val l = new LinkedList[Vertex[_, _]]()
          l.add(v)
          l
        }

        def aggregate(a: LinkedList[Vertex[_, _]], b: LinkedList[Vertex[_, _]]): LinkedList[Vertex[_, _]] = {
          a.addAll(b)
          a
        }

        val neutralElement: LinkedList[Vertex[_, _]] = new LinkedList[Vertex[_, _]]()
      }
      cg.aggregate(aggr)
    }
  }

  def getVertexWithId(id: Any): Vertex[_, _] = {
    cg.forVertexWithId(id, { v: Vertex[Any, _] => v })
  }

  def executeComputationStep {
    cg.execute(ExecutionConfiguration(executionMode = ExecutionMode.Synchronous, stepsLimit = Some(1), signalThreshold = 0.0))
  }

}
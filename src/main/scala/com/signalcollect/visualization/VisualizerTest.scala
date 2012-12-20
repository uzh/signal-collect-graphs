package com.signalcollect.visualization

import com.signalcollect.GraphBuilder
//import com.signalcollect.examples._
import com.signalcollect.approx.flood._
import com.signalcollect.StateForwarderEdge

object VisualizerTest extends App {
  
 

  val graph = GraphBuilder.build
val c12: Constraint = Variable(1) != Variable(2)
  val c13: Constraint = Variable(1) != Variable(3)
  val c23: Constraint = Variable(3) != Variable(2)
  val c34: Constraint = Variable(3) != Variable(4)
  val c45: Constraint = Variable(5) != Variable(4)
  val c35: Constraint = Variable(5) != Variable(3)
  val c56: Constraint = Variable(5) != Variable(6)
  val c26: Constraint = Variable(6) != Variable(2)

  graph.addVertex(new DSANVertex(1, Array(c12, c13), Array(0, 1, 2)))
  graph.addVertex(new DSANVertex(2, Array(c12, c23, c26), Array(0, 1, 2)))
  graph.addVertex(new DSANVertex(3, Array(c13, c23, c35, c34), Array(0, 1, 2)))
  graph.addVertex(new DSANVertex(4, Array(c34, c45), Array(0, 1, 2)))
  graph.addVertex(new DSANVertex(5, Array(c35, c45, c56), Array(0, 1, 2)))
  graph.addVertex(new DSANVertex(6, Array(c26, c56), Array(0, 1, 2)))

  graph.addEdge(1, new StateForwarderEdge(2))
  graph.addEdge(1, new StateForwarderEdge(3))
  graph.addEdge(2, new StateForwarderEdge(3))
  graph.addEdge(3, new StateForwarderEdge(4))
  graph.addEdge(4, new StateForwarderEdge(5))
  graph.addEdge(3, new StateForwarderEdge(5))
  graph.addEdge(5, new StateForwarderEdge(6))
  graph.addEdge(2, new StateForwarderEdge(6))

  graph.addEdge(2, new StateForwarderEdge(1))
  graph.addEdge(3, new StateForwarderEdge(1))
  graph.addEdge(3, new StateForwarderEdge(2))
  graph.addEdge(4, new StateForwarderEdge(3))
  graph.addEdge(5, new StateForwarderEdge(4))
  graph.addEdge(5, new StateForwarderEdge(3))
  graph.addEdge(6, new StateForwarderEdge(5))
  graph.addEdge(6, new StateForwarderEdge(2))

//  cg.addVertex(new Location(1, Some(0)))
//  cg.addVertex(new Location(2))
//  cg.addVertex(new Location(3))
//  cg.addVertex(new Location(4))
//  cg.addVertex(new Location(5))
//  cg.addVertex(new Location(6))
//  cg.addEdge(new Path(1, 2))
//  cg.addEdge(new Path(2, 3))
//  cg.addEdge(new Path(3, 4))
//  cg.addEdge(new Path(1, 5))
//  cg.addEdge(new Path(4, 6))
//  cg.addEdge(new Path(5, 6))
//  
  val visualizer = new GraphVisualizer(new ComputeGraphInspector(graph)).setVisible(true)
}
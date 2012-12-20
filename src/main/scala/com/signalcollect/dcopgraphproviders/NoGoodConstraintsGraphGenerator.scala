package com.signalcollect.dcopgraphproviders

import com.signalcollect._
import com.signalcollect.approx.flood._
import com.signalcollect.graphproviders._
import scala.io.Source
import com.signalcollect.visualization.GraphVisualizer
import com.signalcollect.visualization.ComputeGraphInspector


//
//class NoGoodConstraintsGraphGenerator(fileName: String, colors: Int = 0) {
//
//  val holmes = Source.fromFile(fileName).getLines.toList
//  val constraintGraphData = getFromText(holmes)
//  val constraintGraph = buildConstraintGraphFromData(constraintGraphData)
//
//  def getFromText(textLines: List[String]): ConstraintGraphData = {
//    textLines match {
//      case Nil => ConstraintGraphData(Map(), Nil, List())
//      case tl :: tls => {
//        val splitTextLine = tl.split("\\s+")
//        splitTextLine(0) match {
//          case "AGENT" => getFromText(tls) //lose it
//          case "VARIABLE" => {
//            val variableId = splitTextLine(1).toInt
//            val variablePossibleValues: Array[Int] = (0 to (if (colors==0) splitTextLine(3).toInt-1 else colors-1)).toArray
//            getFromText(tls).addPossibleValues(variableId, variablePossibleValues)
//          }
//          case "CONSTRAINT" => {
//
//            val noGoods = tls.takeWhile(x => x.split("\\s+")(0) == "NOGOOD")
//            val tlsRest = tls.dropWhile(x => x.split("\\s+")(0) == "NOGOOD")
//            if (noGoods == Nil) throw new Error("Constraint with no NOGOOD pairs")
//
//            val noGoodsListOfLists: List[List[Int]] = noGoods.map(x => (x.split("\\s+").drop(1).map(y => y.toInt)).toList)
//
//            if (splitTextLine.length == noGoodsListOfLists(0).length + 1) { //no utility value specified. default 1
//              val constraintVariables = splitTextLine.toList.drop(1) map (x => x.toInt)
//              val utility = 1
//
//              getFromText(tlsRest).addConstraint(SimpleNoGoodConstraint(constraintVariables, noGoodsListOfLists, utility))
//            } else {
//
//              val constraintVariables = splitTextLine.toList.drop(1).take(splitTextLine.length - 2) map (x => x.toInt)
//              val utility = splitTextLine(splitTextLine.length - 1).toInt
//
//              getFromText(tlsRest).addConstraint(SimpleNoGoodConstraint(constraintVariables, noGoodsListOfLists, utility))
//            }
//
//          }
//        }
//      }
//    }
//  }
//
//  def buildConstraintGraphFromData(cgd: ConstraintGraphData): Graph[Any, Double] = {
//    val graph = new GraphBuilder[Any, Double].build
//
//    for (id <- cgd.ids) {
//      graph.addVertex(new DSANVertex(id, cgd.csts.filter(s => s.variablesList().contains(id)).toArray: Array[Constraint], cgd.possibleValues(id)))
//    }
//
//    for (cst <- cgd.csts) {
//      for (i <- cst.variablesList()) {
//        for (j <- cst.variablesList()) {
//          if (i != j)
//            graph.addEdge(i, new StateForwarderEdge(j))
//        }
//      }
//    }
//
//    graph
//  }
//
//  def displayConstraintGraph = {
//    constraintGraph.foreachVertex(x => println(x))
//
//  }
//
//  override def toString = constraintGraphData.toString
//}
//
//object AdoptFileGenerator extends App {
//  val outGraph = new AdoptFileGraphGenerator("data-sets/problems/Problem-GraphColor-8_3_2_0.4_r0")
//  print(outGraph)
//  outGraph.displayConstraintGraph
//  val visualizer = new GraphVisualizer(new ComputeGraphInspector(outGraph.constraintGraph)).setVisible(true)
//
//}
package com.signalcollect.approx.flood

//import scala.collection.mutable._

case class SimpleUtilityConstraint(constraintVariables: List[Any], utilityFunction: Map[List[Double], Double], 
    hard: Boolean = false) extends Constraint { 
  
  def satisfies(configuration: Map[Any, Double]): Boolean = {
    utilityFunction.contains(constraintVariables map(variableName => configuration(variableName)))
  }
    
  def satisfiesInt(configuration: Map[Any, Double]): Int = if (satisfies(configuration)) 1 else 0
  
  def utility(configuration: Map[Any, Double]): Double = {
    utilityFunction(constraintVariables map(variableName => configuration(variableName)))
  }
  
  def variablesList(): List[Any] = {
    constraintVariables
  }
  
  
  
  def hardInt = if (hard) 1 else 0
    
  
  
}

//TODO: put inside the permitted values!
//TODO: verify the call for satisfies if the config is empty or doesn't have something 

case class SimpleNoGoodConstraint(constraintVariables: List[Any], noGoodFunction: List[List[Int]], 
    utilitySatisfied: Double = 1, utilityNonSatisfied: Double = 0, hard: Boolean = false) extends Constraint { 
  
  def satisfies(configuration: Map[Any, Double]): Boolean = 
    !noGoodFunction.contains(constraintVariables map(variableName => configuration.getOrElse(variableName, false)))
    // !noGoodFunction.contains(constraintVariables map(variableName => configuration(variableName)))
    
  def satisfiesInt(configuration: Map[Any, Double]): Int = if (satisfies(configuration)) 1 else 0
  
  def utility(configuration: Map[Any, Double]): Double = {
    if (satisfies(configuration)) utilitySatisfied else utilityNonSatisfied
  }
  
  def variablesList(): List[Any] = {
    constraintVariables
  }
  
  def hardInt = if (hard) 1 else 0
  
  override def toString = { " "/*
    "[\n   Variables = "+ constraintVariables + 
    "\n   NoGoodFunction =  "+noGoodFunction.mkString(",") + 
    " Utility satisfied = " + utilitySatisfied +
    "\n]"*/
    
  }
  
}
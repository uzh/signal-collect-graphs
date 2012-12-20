package com.signalcollect.approx.flood

//import scala.collection.mutable._
//TODO: weird fact: if i make it immutable, then i get problems in dsa etc.

object ConstraintExample extends App {

  //  import Expression._
  //  val fC: Constraint = Variable("Id3")*4+2==10
  //  println(fC.satisfies(Map("Id3"-> 2)))

}
//todo treat case when the index is not inside
/// todo add +, - , / operators
// todo add brackets
//todo ?? precedence?

object Expression {
  implicit def toExpression(x: Int): Expression = new IntExpression(x)
  implicit def toExpression(x: String): Expression = new Variable(x)

}

case class IntExpression(x: Int) extends Expression {
  def evaluate(configuration: Map[Any, Double]): Double = x
  def variablesList(): List[Any] = List()

  override def toString: String = {
    x.toString
  }
}

case class EqualsConstraint(lhs: Expression, rhs: Expression, utilitySatisfied: Double = 1, utilityNonSatisfied: Double = 0, hard: Boolean = false) extends Constraint {

  def satisfies(configuration: Map[Any, Double]): Boolean = lhs.evaluate(configuration) == rhs.evaluate(configuration)
  def satisfiesInt(configuration: Map[Any, Double]): Int = if (satisfies(configuration)) { return 1 } else { return 0 }
  def utility(configuration: Map[Any, Double]): Double = if (satisfies(configuration)) utilitySatisfied else utilityNonSatisfied

  def variablesList(): List[Any] = {
    lhs.variablesList() ::: rhs.variablesList()
  }

  def hardInt: Int = {
    if (hard == true)
      return 1
    return 0
  }

  override def toString: String = {
    lhs.toString + "==" + rhs.toString
  }
}

case class LessEqualsConstraint(lhs: Expression, rhs: Expression, utilitySatisfied: Double = 1, utilityNonSatisfied: Double = 0, hard: Boolean = false) extends Constraint {

  def satisfies(configuration: Map[Any, Double]): Boolean = lhs.evaluate(configuration) <= rhs.evaluate(configuration)
  def satisfiesInt(configuration: Map[Any, Double]): Int = if (satisfies(configuration)) { return 1 } else { return 0 }
  def utility(configuration: Map[Any, Double]): Double = if (satisfies(configuration)) utilitySatisfied else utilityNonSatisfied

  def variablesList(): List[Any] = {
    lhs.variablesList() ::: rhs.variablesList()
  }

  def hardInt: Int = {
    if (hard == true)
      return 1
    return 0
  }

  override def toString: String = {
    lhs.toString + "<=" + rhs.toString
  }
}

case class NotEqualsConstraint(lhs: Expression, rhs: Expression, utilitySatisfied: Double = 1, utilityNonSatisfied: Double = 0, hard: Boolean = false) extends Constraint {

  def satisfies(configuration: Map[Any, Double]): Boolean = {
    //println(this.toString() + " " + configuration)
    lhs.evaluate(configuration) != rhs.evaluate(configuration)
  }

  def satisfiesInt(configuration: Map[Any, Double]): Int =
    if (satisfies(configuration)) {
      return 1
    } else {
      return 0
    }

  def utility(configuration: Map[Any, Double]): Double =
    if (satisfies(configuration))
      utilitySatisfied
    else
      utilityNonSatisfied

  def variablesList(): List[Any] = {
    lhs.variablesList() ::: rhs.variablesList()
  }

  def hardInt: Int = {
    if (hard == true)
      return 1
    return 0
  }

  override def toString: String = {
    lhs.toString + "!=" + rhs.toString
  }
}


trait Constraint {
  def satisfies(configuration: Map[Any, Double]): Boolean
  def satisfiesInt(configuration: Map[Any, Double]): Int
  def utility(configuration: Map[Any, Double]): Double

  def variablesList(): List[Any]
  def hard: Boolean
  def hardInt: Int

}




case class Multiplication(e1: Expression, e2: Expression) extends Expression {
  def evaluate(configuration: Map[Any, Double]): Double = {
    e1.evaluate(configuration) * e2.evaluate(configuration)
  }

  def variablesList(): List[Any] = {
    e1.variablesList() ::: e2.variablesList()
  }

  override def toString: String = {
    e1.toString + "*" + e2.toString
  }
}

case class Addition(e1: Expression, e2: Expression) extends Expression {
  def evaluate(configuration: Map[Any, Double]): Double = {
    e1.evaluate(configuration) + e2.evaluate(configuration)
  }
  def variablesList(): List[Any] = {
    e1.variablesList() ::: e2.variablesList()
  }

  override def toString: String = {
    e1.toString + "+" + e2.toString
  }
}

case class Substraction(e1: Expression, e2: Expression) extends Expression {
  def evaluate(configuration: Map[Any, Double]): Double = {
    e1.evaluate(configuration) - e2.evaluate(configuration)
  }
  def variablesList(): List[Any] = {
    e1.variablesList() ::: e2.variablesList()
  }

  override def toString: String = {
    e1.toString + "-" + e2.toString
  }
}

case class Division(e1: Expression, e2: Expression) extends Expression {
  def evaluate(configuration: Map[Any, Double]): Double = {
    e1.evaluate(configuration) / e2.evaluate(configuration)
  }

  def variablesList(): List[Any] = {
    e1.variablesList() ::: e2.variablesList()
  }

  override def toString: String = {
    e1.toString + "/" + e2.toString
  }
}

trait Expression {
  def *(other: Expression): Expression = Multiplication(this, other)
  def +(other: Expression): Expression = Addition(this, other)
  def -(other: Expression): Expression = Substraction(this, other)
  def /(other: Expression): Expression = Division(this, other)

  def evaluate(configuration: Map[Any, Double]): Double
  def variablesList(): List[Any]

  def ==(other: Expression): Constraint = EqualsConstraint(this, other)
  def <=(other: Expression): Constraint = LessEqualsConstraint(this, other)
  def !=(other: Expression): Constraint = NotEqualsConstraint(this, other)

}

case class Variable(name: Any) extends Expression {
  def evaluate(configuration: Map[Any, Double]): Double = {
    //  println(name + " " + configuration)
    configuration(name)
  }
  def variablesList(): List[Any] = {
    val varList = List(name)
    varList
  }

  override def toString: String = {
    name.toString()
  }
}


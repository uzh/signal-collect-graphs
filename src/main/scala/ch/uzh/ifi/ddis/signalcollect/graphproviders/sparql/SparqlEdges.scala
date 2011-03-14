/*
 *  @author Philip Stutz
 *  
 *  Copyright 2010 University of Zurich
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

package ch.uzh.ifi.ddis.signalcollect.graphproviders

import scala.collection.mutable.ArrayBuffer

case class RdfId(val iris: scala.collection.immutable.List[String]) {
  override val hashCode: Int = iris.hashCode
}

class SparqlEdges(
  val db: SparqlAccessor,
  val edgeQueries: List[String],
  val sourceBindingNames: List[String] = List("source"),
  val targetBindingNames: List[String] = List("target")
) extends Traversable[(RdfId, RdfId)] {
  def foreach[U](f: ((RdfId, RdfId)) => U) = {
    for (query <- edgeQueries) {
      val bindings = db.execute(query)
      for (binding <- bindings) {
        val sourceRdfTerms: List[String] = sourceBindingNames.flatMap(binding.get(_) match {
          case Some(binding) => List(binding)
          case None => List()
          })
        val targetRdfTerms: List[String] = targetBindingNames.flatMap(binding.get(_) match {
          case Some(binding) => List(binding)
          case None => List()
          })
        if (targetRdfTerms != Nil && sourceRdfTerms != Nil) {
          //println(sourceRdfTerms + "->" + targetRdfTerms)
          f((RdfId(sourceRdfTerms), RdfId(targetRdfTerms)))
        } else {
          //logger.warn("That's a silly triplestore. Returned nonesense, will ignore this binding: " + binding)
        }
      }
    }
  }

  def materialized: Traversable[(RdfId, RdfId)] = {
    val edges = new ArrayBuffer[(RdfId, RdfId)]
    foreach(edges.append(_))
    edges
  }

}


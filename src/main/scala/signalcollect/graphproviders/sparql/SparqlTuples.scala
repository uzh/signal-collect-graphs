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

package signalcollect.graphproviders

import scala.collection.JavaConversions._

object SparqlTuples {
  val defaultSourceVariableName = "source"
  val defaultTargetVariableName = "target"
	
//	  implicit def scalaIterableToJavaIterable[T](scalaIterable: java.lang.Iterable[T]): scala.collection.Iterable[T] = new SJIterable(jit);

//  def javaIterable: java.lang.Iterable[(String, String)] = new java.lang.Iterable[(String, String)] {
//    def iterator = self.iterator
//  }
}

class SparqlTuples(
  db: SparqlEndpoint,
  query: String,
  sourceBindingName: String = SparqlTuples.defaultSourceVariableName,
  targetBindingName: String = SparqlTuples.defaultTargetVariableName) extends Iterable[(String, String)] {
  self =>
  // for Java access to replace default parameters
  def this(db: SparqlEndpoint, query: String) = this(db, query, SparqlTuples.defaultSourceVariableName, SparqlTuples.defaultTargetVariableName)

  def iterator = new Iterator[(String, String)] {
    val bindings: Iterator[Bindings] = db.execute(query).iterator
    def next: (String, String) = {
      val binding = bindings.next
      val sourceRdfTerm: String = binding.get(sourceBindingName) match {
        case Some(binding) => binding
        case None => throw new Exception("Source RDF term not bound: " + sourceBindingName)
      }
      val targetRdfTerm: String = binding.get(targetBindingName) match {
        case Some(binding) => binding
        case None => throw new Exception("Target RDF term not bound: " + targetBindingName)
      }
      (sourceRdfTerm, targetRdfTerm)
    }
    def hasNext: Boolean = bindings.hasNext
  }

  def javaIterable: java.lang.Iterable[(String, String)] = new java.lang.Iterable[(String, String)] {
    def iterator = self.iterator
  }

}
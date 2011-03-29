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

package signalcollect.graphproviders.sparql

import com.hp.hpl.jena.query.QuerySolution
import com.hp.hpl.jena.query.ResultSet
import com.hp.hpl.jena.query.QueryExecutionFactory

class JenaSparql(val endpointUrl: String = "http://localhost:8080/openrdf-sesame") extends SparqlEndpoint {

  override def execute(query: String): Iterable[Bindings] = {
    val queryExecution = QueryExecutionFactory.sparqlService(endpointUrl, query)
    new JenaResultAdapter(queryExecution.execSelect)
  }
}

class JenaResultAdapter(val i: ResultSet) extends Iterable[Bindings] {
  def iterator = new Iterator[Bindings] {
    def next = new JenaBindingsAdapter(i.next)
    def hasNext = i.hasNext
  }
}

class JenaBindingsAdapter(val i: QuerySolution) extends Bindings {
  def get(s: String) = {
    val b = i.get(s)
    if (b != null)
      Some(b.toString)
    else
      None
  }

  override def toString: String = i.toString
}
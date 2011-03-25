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

import org.openrdf.query.BindingSet
import org.openrdf.query.TupleQueryResult
import org.openrdf.query.QueryLanguage
import org.openrdf.query.resultio.TupleQueryResultFormat
import org.openrdf.repository.http.HTTPRepository

class SesameSparql(serverUrl: String = "http://localhost:8080/openrdf-sesame", repositoryString: String = null) extends SparqlAccessor {

  var repository: HTTPRepository = null
  if (repositoryString != null)
    repository = new HTTPRepository(serverUrl, repositoryString)
  else
    repository = new HTTPRepository(serverUrl)
  if (repositoryString == null)
    repository.setPreferredTupleQueryResultFormat(TupleQueryResultFormat.SPARQL)
  else
    repository.setPreferredTupleQueryResultFormat(TupleQueryResultFormat.BINARY)
  repository.initialize

  override def execute(query: String): Traversable[Bindings] = {
    val connection = repository.getConnection
    val tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query)
    new SesameResultAdapter(tupleQuery.evaluate)
  }
}


//String endpointURL = "http://dbpedia.org/sparql";
//HTTPRepository dbpediaEndpoint = 
//         new HTTPRepository(endpointURL, "");
//dbpediaEndpoint.initialize();
//
//RepositoryConnection conn = 
//         dbpediaEndpoint.getConnection();
//try {
//  String sparqlQuery = 
//         " SELECT * WHERE {?X ?P ?Y} LIMIT 10 ";
//  TupleQuery query = conn.prepareTupleQuery(SPARQL, query);
//  TupleQueryResult result = query.evaluate();
//
//  while (result.hasNext()) {
//      ... // do something linked and open
//  }
//}
//finally {
//  conn.close();
//}

class SesameResultAdapter(val i: TupleQueryResult) extends Traversable[Bindings] {
  def foreach[U](f: (Bindings) => U) = while (i.hasNext) f(new SesameBindingsAdapter(i.next))
}

class SesameBindingsAdapter(val i: BindingSet) extends Bindings {
  def get(s: String) = {
    val b = i.getValue(s)
    if (b != null)
      Some(b.toString)
    else
      None
  }

  override def toString: String = i.toString
}
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

package signalcollect.graphproviders.synthetic

class Grid(val width: Int, height: Int) extends Traversable[(Int, Int)] {

  def foreach[U](f: ((Int, Int)) => U) = {
	  val max = width*height
	  for (n <- 1 to max) {
	 	if (n + width <= max) {
	 		f(n, n+width)
	 		f(n+width, n)
	 	}
	 	if (n % height != 0) {
	 		f(n, n+1)
	 		f(n+1, n)
	 	}
	  }
  }
}
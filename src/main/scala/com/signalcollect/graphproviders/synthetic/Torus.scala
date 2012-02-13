/*
 *  @author Philip Stutz
 *  
 *  Copyright 2012 University of Zurich
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

package com.signalcollect.graphproviders.synthetic

class Torus(val width: Int, height: Int) extends Traversable[(Int, Int)] {

  def foreach[U](f: ((Int, Int)) => U) = {
    val max = width * height
    for (y <- 0 until height) {
      for (x <- 0 until width) {
        val flattenedCurrentId = flatten((x, y), width)
        println(flattenedCurrentId)
        for (neighbor <- neighbors(x, y, width, height).map(flatten(_, width))) {
          f(flattenedCurrentId, neighbor)
        }
      }
    }
  }

  //  def neighbors(x: Int, y: Int, width: Int, height: Int): List[(Int, Int)] = {
  //    List(
  //      (decrease(x, width), decrease(y, height)), (x, decrease(y, height)), (increase(x, width), decrease(y, height)),
  //      (decrease(x, width), y), (increase(x, width), y),
  //      (decrease(x, width), increase(y, height)), (x, increase(y, height)), (increase(x, width), increase(y, height)))
  //  }

  def neighbors(x: Int, y: Int, width: Int, height: Int): List[(Int, Int)] = {
    List(
      (x, decrease(y, height)),
      (decrease(x, width), y), (increase(x, width), y),
      (x, increase(y, height)))
  }

  def decrease(counter: Int, limit: Int): Int = {
    if (counter - 1 >= 0) {
      counter - 1
    } else {
      width - 1
    }
  }

  def increase(counter: Int, limit: Int): Int = {
    if (counter + 1 >= width) {
      0
    } else {
      counter + 1
    }
  }

  def flatten(coordinates: (Int, Int), width: Int): Int = {
    coordinates._1 + coordinates._2 * width
  }
}
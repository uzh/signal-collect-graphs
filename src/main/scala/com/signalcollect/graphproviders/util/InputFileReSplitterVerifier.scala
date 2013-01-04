/*
 *  @author Daniel Strebel
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
package com.signalcollect.graphproviders.util

import java.io._
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer
import java.util.zip.GZIPInputStream
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Reads the number of vertices and edges contained in a single split.
 */
object InputFileReSplitterVerifier extends App {
  val numberOfSplits = 2880
  val inputFileDirectory = "/home/user/strebel/webgraph/webgraph_bin_zip_2880"
  val outputFileDirectory = "/home/user/strebel/webgraph/webgraph_bin_" + numberOfSplits
  val statusLoggerFile: Option[String] = Some("/home/user/strebel/status.txt")
  
  val splitToVerify = "input_pt_2879.txt.gz"
  
  var verticesRead = 0
  var verticesWriten = 0
  var edgesWritten= 0l
  var edgesRead = 0l

  /**
   * ********
   * Script
   * ********
   */

  def logStatus(msg: String) {
    if (statusLoggerFile.isDefined) {
      val timeFormat = new SimpleDateFormat("HH:mm:ss")
      val logFileWiter = new FileWriter(statusLoggerFile.get, true)
      val logger = new BufferedWriter(logFileWiter)
      logger.write(timeFormat.format(new Date) + " - " + msg + "\n")
      logger.close
    }

  }

  val splitterCriteria: Int => Int = (id) => { //determines to which split a line in the input file belongs
    id % numberOfSplits
  }

  def writeBuffer(buffer: Array[ArrayBuffer[Array[Int]]]) {
    for (i <- 0 until numberOfSplits) {
      val fileOut = new FileOutputStream(new File(outputFileDirectory + "/input_pt_" + i + ".txt"), true)
      val bufferedOut = new BufferedOutputStream(fileOut)
      val writer = new DataOutputStream(bufferedOut)
      for (entry <- buffer(i)) {
        verticesWriten+=1
        writer.writeInt(entry(0))
        val numberOfLinks = entry(1)
        writer.writeInt(numberOfLinks)
        for (link <- 0 until numberOfLinks) {
          writer.writeInt(entry(link + 2))
        }
        edgesWritten +=numberOfLinks
      }
      buffer(i).clear
      writer.flush
      writer.close
    }
    logStatus("vertices written: " + verticesWriten + " edges: " + edgesWritten)
  }

  val splitBuffer = new Array[ArrayBuffer[Array[Int]]](numberOfSplits)
  for (i <- 0 until numberOfSplits) {
    splitBuffer(i) = new ArrayBuffer[Array[Int]]()
  }

  val inputFolder = new File(inputFileDirectory)
  
  var count = 0

  for (inputFile <- inputFolder.listFiles.filter(file => file.getName.startsWith(splitToVerify))) {

    val gzipIn = new GZIPInputStream(new FileInputStream(inputFile))
    val bufferedInput = new BufferedInputStream(gzipIn)
    val in = new DataInputStream(bufferedInput)

    try {

      while (true) {
        
        val id = in.readInt
        val numberOfLinks = in.readInt
        val entry = new Array[Int](numberOfLinks+2)
        for (i <- 0 until numberOfLinks) {
          in.readInt
        }
        verticesRead+=1
        edgesRead+=numberOfLinks
      }


    } catch {
      case e: EOFException => {} //Reached end of file.
      case exception: Exception => exception.printStackTrace()
    }
    in.close
    logStatus(inputFile.getAbsolutePath() + " read")
    count +=1

    logStatus("Read " + count  + " splits, " + verticesRead + " vertices " + edgesRead + " edges")

  }
  
  logStatus("++++++++++Done+++++++++++++")
}
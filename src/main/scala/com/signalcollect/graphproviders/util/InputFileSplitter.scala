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
import scala.sys.process._
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Simple script to partition input files according to some splitting condition
 *
 * Output files are named outputFilePrefix + "_pt_" + NUMBER_OF_SPLIT + outputFileExtension
 * The temporary
 */
object InputFileSplitter extends App {

  /**
   * ********
   * Config
   * ********
   */
  val inputfileDirectory = "/path/to/input/folder"
  val outputFileDirectory = "/path/to/output/folder"
  val outputFilePrefix = "input"
  val outputFileExtension = ".txt"
  val tempDir = None : Option[String]//Some("/path/to/temp/folder")
  val numberOfSplits = 3840
  val statusLoggerFile: Option[String] = Some("/path/to/status/.txt")
  
  val splitterCriteria: (String) => Int = (inputString) => { //determines to which split a line in the input file belongs
    val splittedLine = inputString.split(" ")
    val id = Integer.valueOf(splittedLine(0))
    id % numberOfSplits
  }

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

  //Initialize output writers

  logStatus("start: create writers")

  var outputFolderPath = outputFileDirectory

  if (tempDir.isDefined) {
    outputFolderPath = tempDir.get + System.getProperty("file.separator") + "out"
  }
  val writers = new Array[BufferedWriter](numberOfSplits)
  val output = new File(outputFolderPath)

  if (!output.exists) {
    println("creating" + output.getAbsolutePath())
    output.mkdir
  }

  for (i <- 0 until numberOfSplits) {
    val fwriter = new FileWriter(outputFolderPath + System.getProperty("file.separator") + outputFilePrefix + "_pt_" + i + outputFileExtension)
    writers(i) = new BufferedWriter(fwriter)
  }

  logStatus("done: create writers")

  //copy input file
  var inputFolderPath = inputfileDirectory

  if (tempDir.isDefined) {
    logStatus("start: copy input files")

    val inputFolder = new File(inputfileDirectory)
    if (!inputFolder.exists) {
      logStatus(inputfileDirectory + " does not exist!")
    }

    val tempInputFolder = new File(tempDir.get + System.getProperty("file.separator") + "in")
    if (!tempInputFolder.exists) {
      tempInputFolder.mkdir
    }

    inputFolderPath = inputFolder.getAbsolutePath

    for (inputFile <- inputFolder.listFiles) {
      val copyInputFiles = "cp " + inputFile.getAbsolutePath + " " + tempInputFolder.getAbsolutePath
      logStatus(copyInputFiles)
      copyInputFiles!!
    }

    logStatus("content of temp input folder: " + tempInputFolder.list.toString)

    logStatus("done: copy input files")
  }

  var totalRead = 0

  val inputFiles = new File(inputFolderPath).list
  for (inputFile <- inputFiles) {

    logStatus("start: splitting file " + inputFile)

    //initialize input reader
    val fstream = new FileInputStream(inputFolderPath + System.getProperty("file.separator") + inputFile)
    val in = new DataInputStream(fstream)
    val input = new BufferedReader(new InputStreamReader(in))

    //read input file line by line
    var readCurrentFile = 0
    var line = input.readLine
    while (line != null) {
      writers(splitterCriteria(line)).write(line + "\n")
      readCurrentFile += 1
      if (readCurrentFile % 10000000 == 0) {
        logStatus(inputFile + " read so far: " + readCurrentFile)
      }
      line = input.readLine
    }

    in.close
    fstream.close

    totalRead += readCurrentFile
    logStatus("done: splitting file " + inputFile + " read: " + readCurrentFile)

  }

  logStatus("done splitting files read total: " + totalRead)

  //close all streams
  for (i <- 0 until numberOfSplits) {
    writers(i).close
  }

  if (tempDir.isDefined) {
    logStatus("start: copy output files")

    //move stuff to final output directory
    val outputFolder = new File(outputFolderPath)
    for (splitFile <- outputFolder.listFiles) {
      val copyOutputFiles = "cp " + splitFile.getAbsolutePath + " " + outputFileDirectory
      logStatus(copyOutputFiles)
      copyOutputFiles!!
    }

    logStatus("done: copy output files")

    "rm -rdf " + tempDir.get !!

  }
  
  logStatus("+++DONE+++")

}
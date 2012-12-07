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

import com.signalcollect.nodeprovisioning.torque.TorqueJobSubmitter
import com.signalcollect.nodeprovisioning.torque.TorquePriority


/**
 * Run the ReSplitter on a kraken node
 */
object RunSplitter extends App {
  val submitter = new TorqueJobSubmitter("strebel", 
      "strebel@ifi.uzh.ch", "kraken.ifi.uzh.ch", System.getProperty("user.home") + System.getProperty("file.separator") + ".ssh" + System.getProperty("file.separator") + "id_rsa")
  submitter.copyFileToCluster("target/signal-collect-graphs-assembly-2.0.0-SNAPSHOT.jar", "splitter.jar")
  submitter.runOnClusterNode("inputsplitter", "splitter.jar", "com.signalcollect.graphproviders.util.InputFileReSplitter", TorquePriority.fast, "-Xmx55G -Xms55G")
}
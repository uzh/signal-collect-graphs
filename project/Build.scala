import sbt._
import Keys._

object GraphsBuild extends Build {
   lazy val scCore = ProjectRef(file("../signal-collect"), id = "signal-collect")
   lazy val torqueDeployment = ProjectRef(file("../signal-collect-torque"), id = "signal-collect-torque")
   val scGraphs = Project(id = "signal-collect-graphs",
                         base = file(".")) dependsOn(scCore) dependsOn(torqueDeployment)
}
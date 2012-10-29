import sbt._
import Keys._

object GraphsBuild extends Build {
   lazy val scCore = ProjectRef(file("../signal-collect"), id = "signal-collect")
   val scGraphs = Project(id = "signal-collect-graphs",
                         base = file(".")) dependsOn(scCore)
}
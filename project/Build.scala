import sbt._
import Keys._

object HelloBuild extends Build {
      lazy val scCore = ProjectRef(file("../signal-collect"), id = "signal-collect-core")
   val scGraphs = Project(id = "signal-collect-graphs",
                         base = file(".")) dependsOn(scCore)
}
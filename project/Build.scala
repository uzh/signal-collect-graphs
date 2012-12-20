import sbt._
import Keys._

object DcopsBuild extends Build {
   lazy val scCore = ProjectRef(file("../signal-collect"), id = "signal-collect")
   lazy val scGraphs = ProjectRef(file("../signal-collect-graphs"), id = "signal-collect-graphs")
   lazy val scEval = ProjectRef(file("../signal-collect-evaluation"), id = "signal-collect-evaluation")
   val scDcops = Project(id = "signal-collect-dcops",
                         base = file(".")) dependsOn(scGraphs, scEval, scCore)
}
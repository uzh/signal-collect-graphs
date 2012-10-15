/** Project */
name := "signal-collect-graphs"

version := "2.0.0-SNAPSHOT"

organization := "com.signalcollect"

scalaVersion := "2.10.0-M7"

/** Dependencies */
libraryDependencies ++= Seq(
 "org.scala-lang" % "scala-library" % "2.10.0-M7"  % "compile",
 "com.hp.hpl.jena" % "arq" % "2.8.5"  % "compile",
 "com.signalcollect" % "signal-collect-core" % "2.0.0-SNAPSHOT" 
  )


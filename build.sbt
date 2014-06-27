import AssemblyKeys._ 
assemblySettings

/** Project */
name := "signal-collect-graphs"

version := "2.1.0-SNAPSHOT"

organization := "com.signalcollect"

scalaVersion := "2.11.1"

EclipseKeys.withSource := true

/** Dependencies */
libraryDependencies ++= Seq(
 "org.scala-lang" % "scala-library" % "2.11.1"  % "compile",
 "org.apache.jena" % "apache-jena-libs" % "2.11.1" % "compile"
  )

import AssemblyKeys._ 
assemblySettings

/** Project */
name := "signal-collect-graphs"

version := "2.0.0-SNAPSHOT"

organization := "com.signalcollect"

scalaVersion := "2.10.0-RC1"

resolvers += "Typesafe Snapshot Repository" at "http://repo.typesafe.com/typesafe/snapshots/"

EclipseKeys.withSource := true

/** Dependencies */
libraryDependencies ++= Seq(
 "org.scala-lang" % "scala-library" % "2.10.0-RC1"  % "compile",
 "org.apache.jena" % "jena-arq" % "2.9.3"
  )
name := """marktplaats-show-and-tell"""
organization := "nl.wwbakker.mst"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "org.typelevel" %% "cats-core" % "1.3.1"
libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.16" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.16" % Test

fork in Test := false

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "nl.wwbakker.mst.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "nl.wwbakker.mst.binders._"

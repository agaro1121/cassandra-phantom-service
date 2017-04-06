name := """cassandra-phantom-service"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

val phantomVersion = "2.6.2"
libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "com.outworkers"  %% "phantom-dsl" % phantomVersion,
  "com.outworkers"   %% "phantom-streams" % phantomVersion
)


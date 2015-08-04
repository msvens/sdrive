name := "sdrive"

version := "1.0-SNAPSHOT"
organization := "org.mellowtech"

scalaVersion := "2.11.5"

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.3.9"

//google dependencies
libraryDependencies ++= Seq(
	"com.google.apis" % "google-api-services-drive" % "v2-rev158-1.19.1"
	)


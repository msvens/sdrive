name := "sdrive"

version := "1.1-SNAPSHOT"
organization := "org.mellowtech"

scalaVersion := "2.11.7"

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
libraryDependencies += "org.mellowtech" %% "mpoi" % "0.1-SNAPSHOT"



// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.3.9"

//google dependencies
libraryDependencies ++= Seq(
	"com.google.apis" % "google-api-services-drive" % "v2-rev182-1.20.0",
	"com.google.gdata" % "core" % "1.47.1").map(
				_.exclude("org.mortbay.jetty", "jetty")
						.exclude("org.mortbay.jetty", "servlet-api")
						.exclude("org.mortbay.jetty", "jetty-util")
			)


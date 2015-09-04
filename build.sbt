name := "workshop"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Local Maven Repository" at "file:///"+Path.userHome+"/.m2/repository"

libraryDependencies += "com.google.guava" % "guava" % "11.0.1"

libraryDependencies += "junit" % "junit" % "4.10" % "test"


javacOptions ++= Seq("-source", "1.8", "-target", "1.8")


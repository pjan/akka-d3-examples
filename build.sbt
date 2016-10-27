
name := "akka-d3-examples"
version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.8"
scalacOptions ++= Seq("-deprecation", "-feature")

resolvers += 
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "io.pjan" %% "akka-d3" % "0.1.0"
)

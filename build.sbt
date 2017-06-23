name := "akka-d3-examples"
version := "0.4.3"

scalaVersion := "2.12.1"
scalacOptions ++= Seq("-deprecation", "-feature")

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "io.pjan" %% "akka-d3" % "0.4.3",
  "io.pjan" %% "akka-d3-query-inmemory" % "0.4.3",
  "io.pjan" %% "akka-d3-readside-cassandra" % "0.4.3",
  "org.slf4j" % "slf4j-simple" % "1.7.7"
)

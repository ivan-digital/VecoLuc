name := "VecoLuc"

version := "0.1"

scalaVersion := "2.13.14"
// --add-modules jdk.incubator.vector
libraryDependencies ++= Seq(
  "org.apache.lucene" % "lucene-core" % "9.11.1",
  "com.typesafe.akka" %% "akka-http" % "10.5.3",
  "com.typesafe.akka" %% "akka-actor-typed" % "2.8.6",
  "com.typesafe.akka" %% "akka-stream" % "2.8.6",
  "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",
  "io.circe" %% "circe-generic" % "0.14.9",
  "io.circe" %% "circe-parser" % "0.14.9",
  "ch.qos.logback" % "logback-classic" % "1.5.6",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.scalatestplus" %% "mockito-5-8" % "3.2.17.0" % Test
)
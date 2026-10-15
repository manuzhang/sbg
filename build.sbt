scalaVersion     := "2.13.8"
version          := "0.1.0-SNAPSHOT"
organization     := "io.github.manuzhang"

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "os-lib" % "0.8.1",
  "com.lihaoyi" %% "scalatags" % "0.8.6",
  "com.vladsch.flexmark" % "flexmark-all" % "0.64.0",
  "org.rogach" %% "scallop" % "6.0.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % "1.2.11"
)


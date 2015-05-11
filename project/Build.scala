import sbt._
import sbt.Keys._
import spray.revolver.RevolverPlugin._
import sbtassembly.Plugin._
import AssemblyKeys._
import scala.util.Properties

object Version {
  val gtAdmin = "0.1.0"
  val scala = "2.10.4"
}

object Build extends Build {
  val defaultAssemblySettings =
    assemblySettings ++
  Seq(
    test in assembly := {},
    mergeStrategy in assembly <<= (mergeStrategy in assembly) {
      (old) => {
        case "reference.conf" => MergeStrategy.concat
        case "application.conf" => MergeStrategy.concat
        case "META-INF/MANIFEST.MF" => MergeStrategy.discard
        case "META-INF\\MANIFEST.MF" => MergeStrategy.discard
        case _ => MergeStrategy.first
      }
    }
  )

  override lazy val settings =
    super.settings ++
  Seq(
    shellPrompt := { s => Project.extract(s).currentProject.id + " > " },
    version := Version.gtAdmin,
    scalaVersion := Version.scala,
    organization := "com.azavea.geotrellis",

    scalacOptions ++=
      Seq("-deprecation",
        "-unchecked",
        "-Yinline-warnings",
        "-language:implicitConversions",
        "-language:reflectiveCalls",
        "-language:higherKinds",
        "-language:postfixOps",
        "-language:existentials",
        "-feature")

  )
 
  def either(environmentVariable: String, default: String): String =
    Properties.envOrElse(environmentVariable, default)
 
  lazy val hadoopVersion = either("SPARK_HADOOP_VERSION", "2.4.1")

  // Project: root
  lazy val root =
    Project("gt-admin", file("."))
      .aggregate(ingest, server)
  
  lazy val server = Project(
    id = "server",
    base = file("server"),
    settings = Project.defaultSettings ++ Seq(
      name := "gt-admin-server",
      organization := "com.azavea",
      version := "0.1.0-SNAPSHOT",
      libraryDependencies ++= {
        val akkaV = "2.3.9"
        val sprayV = "1.3.2"

        val geotrellisV = "0.10.0-SNAPSHOT"
        Seq(
          "io.spray"            %%   "spray-can"     % sprayV,
          "io.spray"            %%   "spray-routing" % sprayV,
          "io.spray"            %%  "spray-json"    % "1.3.1",
          "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
          "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
          "com.quantifind" %% "sumac" % "0.3.0",
 
          "org.slf4j"                 %   "slf4j-log4j12"   % "1.7.7",
          "org.clapper"               %%  "grizzled-slf4j"  % "1.0.2",
          "org.apache.logging.log4j"  %   "log4j"           % "2.0-rc2",
 
          "com.azavea.geotrellis" %% "geotrellis-spark"   % geotrellisV,
          "com.azavea.geotrellis" %% "geotrellis-raster"  % geotrellisV,
          "com.azavea.geotrellis" %% "geotrellis-vector"  % geotrellisV,
          "com.azavea.geotrellis" %% "geotrellis-proj4"   % geotrellisV,
 
          "org.apache.spark" %% "spark-core" % "1.2.1" % "provided",
 
          "com.github.nscala-time" %% "nscala-time" % "0.8.0",
          "org.scalatest" % "scalatest_2.10" % "2.1.0" % "test",
          "org.apache.hadoop" % "hadoop-client" % hadoopVersion % "provided"
        )
      }
    ) ++ Revolver.settings ++ defaultAssemblySettings  ++ net.virtualvoid.sbt.graph.Plugin.graphSettings
  )

  lazy val ingest = Project(
    id = "ingest",
    base = file("ingest"),
    settings = Project.defaultSettings ++ Seq(
      name := "gt-admin-ingest",
      organization := "com.azavea",
      version := "0.1.0-SNAPSHOT",
      libraryDependencies ++= {
        val akkaV = "2.3.9"
        val sprayV = "1.3.2"
        val geotrellisV = "0.10.0-SNAPSHOT"
        Seq(
          "com.quantifind" %% "sumac" % "0.3.0",
 
          "org.slf4j"                 %   "slf4j-log4j12"   % "1.7.7",
          "org.clapper"               %%  "grizzled-slf4j"  % "1.0.2",
          "org.apache.logging.log4j"  %   "log4j"           % "2.0-rc2",
 
          "com.azavea.geotrellis" %% "geotrellis-spark"   % geotrellisV,
          "com.azavea.geotrellis" %% "geotrellis-raster"  % geotrellisV,
          "com.azavea.geotrellis" %% "geotrellis-vector"  % geotrellisV,
          "com.azavea.geotrellis" %% "geotrellis-proj4"   % geotrellisV,
 
          "org.apache.spark" %% "spark-core" % "1.2.1" % "provided",
 
          "com.github.nscala-time" %% "nscala-time" % "0.8.0",
          "org.scalatest" % "scalatest_2.10" % "2.1.0" % "test",
          "org.apache.hadoop" % "hadoop-client" % hadoopVersion % "provided"
        )
      }
    ) ++ Revolver.settings ++ defaultAssemblySettings ++ net.virtualvoid.sbt.graph.Plugin.graphSettings
  )
}

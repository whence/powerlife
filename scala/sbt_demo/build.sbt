name := "sbt_demo"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % Test,
  "org.scalactic" % "scalactic_2.11" % "2.2.4",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % Test
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .aggregate(sbt_demo_core, sbt_demo_services)
  .dependsOn(sbt_demo_core, sbt_demo_services)

lazy val sbt_demo_core = (project in file("core"))

lazy val sbt_demo_services = (project in file("services"))
  .dependsOn(sbt_demo_core, sbt_demo_repositories)

lazy val sbt_demo_repositories = (project in file("repositories"))
  .dependsOn(sbt_demo_core)

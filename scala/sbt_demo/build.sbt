lazy val commonSettings = Seq(
  version := "1.0",
  scalaVersion := "2.11.7",
  libraryDependencies ++= Seq(
    "org.scalatest" % "scalatest_2.11" % "2.2.4" % Test,
    "org.scalactic" % "scalactic_2.11" % "2.2.4",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % Test
  )
)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "sbt_demo",
    libraryDependencies ++= Seq(
      "org.scalatestplus" %% "play" % "1.4.0-M3" % Test
    )
  )
  .enablePlugins(PlayScala)
  .settings(
    routesGenerator := InjectedRoutesGenerator
  )
  .aggregate(core, services, repositories)
  .dependsOn(core, services)

lazy val core = project
  .settings(commonSettings: _*)
  .settings(
    name := "sbt_demo_core"
  )

lazy val services = project
  .settings(commonSettings: _*)
  .settings(
    name := "sbt_demo_services"
  )
  .dependsOn(core, repositories)

lazy val repositories = project
  .settings(commonSettings: _*)
  .settings(
    name := "sbt_demo_repositories"
  )
  .dependsOn(core)

lazy val root = (project in file(".")).
  settings(
    name := "sbt_demo",
    version := "1.0",
    scalaVersion := "2.11.7",
    libraryDependencies ++= Seq(
      "org.scalatest" % "scalatest_2.11" % "2.2.4" % Test,
      "org.scalactic" % "scalactic_2.11" % "2.2.4",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % Test
    )
  )

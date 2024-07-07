import scala.collection.immutable.Seq

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.3"

lazy val root = (project in file("."))
  .settings(
    name := "tuier3",

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "com.olvind.tui" %% "tui" % "0.0.7"

    )
  )
  .aggregate(dbAccess)
  .dependsOn(dbAccess)

lazy val dbAccess = (project in file("dbAccess")).settings(
  name := "dbAccess",
  libraryDependencies ++= Seq(
    "org.tpolecat" %% "doobie-core" % "1.0.0-RC5",
    "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC5",
    "com.zaxxer" % "HikariCP" % "5.1.0",
    "org.xerial" % "sqlite-jdbc" % "3.46.0.0",
    "org.slf4j" % "slf4j-api" % "2.1.0-alpha1",
    "org.slf4j" % "slf4j-nop" % "2.1.0-alpha1",

    "org.yaml" % "snakeyaml" % "2.2",
    "io.circe" %% "circe-core" % "0.14.7",
    "io.circe" %% "circe-yaml" % "1.15.0",
    "io.circe" %% "circe-generic" % "0.14.7",
    "io.circe" %% "circe-parser" % "0.14.7"
  )
)
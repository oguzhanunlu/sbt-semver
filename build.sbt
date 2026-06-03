import Dependencies._

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / organization         := "dev.unlu"
ThisBuild / organizationName     := "Oguzhan Unlu"
ThisBuild / organizationHomepage := Some(url("https://unlu.dev"))
ThisBuild / homepage             := Some(url("https://github.com/oguzhanunlu/sbt-semver"))
ThisBuild / startYear            := Some(2026)
ThisBuild / licenses             := Seq(
  "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")
)
ThisBuild / developers := List(
  Developer(
    id = "oguzhanunlu",
    name = "Oguzhan Unlu",
    email = "oguzhan@unlu.dev",
    url = url("https://unlu.dev")
  )
)
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/oguzhanunlu/sbt-semver"),
    "scm:git:git@github.com:oguzhanunlu/sbt-semver.git"
  )
)

ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := V.Scala212

lazy val root = (project in file("."))
  .aggregate(core, plugin)
  .settings(
    name           := "sbt-semver-root",
    publish / skip := true
  )

lazy val core = (project in file("modules/core"))
  .settings(
    name                                  := "semver-core",
    scalaVersion                          := V.Scala212,
    crossScalaVersions                    := AllScalaVersions,
    libraryDependencies += Libraries.Munit % Test
  )

lazy val plugin = (project in file("modules/plugin"))
  .enablePlugins(SbtPlugin)
  .dependsOn(core)
  .settings(
    name                                  := "sbt-semver",
    scalaVersion                          := V.Scala212,
    crossScalaVersions                    := Seq(V.Scala212),
    pluginCrossBuild / sbtVersion         := V.Sbt1,
    libraryDependencies += Libraries.Munit % Test
  )

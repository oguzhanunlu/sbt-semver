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

ThisBuild / scalacOptions += "-release:8"

addCommandAlias("fmt", "scalafmtAll; scalafmtSbt")
addCommandAlias("fmtCheck", "scalafmtCheckAll; scalafmtSbtCheck")

lazy val root = (project in file("."))
  .aggregate(core, cats, plugin, docs)
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

lazy val cats = (project in file("modules/cats"))
  .dependsOn(core)
  .settings(
    name               := "semver-cats",
    scalaVersion       := V.Scala212,
    crossScalaVersions := AllScalaVersions,
    libraryDependencies ++= Seq(
      Libraries.Cats,
      Libraries.Munit % Test
    )
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

lazy val docs = (project in file("docs"))
  .enablePlugins(MdocPlugin)
  .dependsOn(core, cats)
  .settings(
    name           := "sbt-semver-docs",
    scalaVersion   := V.Scala212,
    publish / skip := true,
    mdocIn         := (LocalRootProject / baseDirectory).value / "docs" / "README.md",
    mdocOut        := (LocalRootProject / baseDirectory).value / "README.md"
  )

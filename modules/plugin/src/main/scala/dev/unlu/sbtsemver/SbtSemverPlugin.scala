package dev.unlu.sbtsemver

import sbt._

object SbtSemverPlugin extends AutoPlugin {

  override def trigger  = allRequirements
  override def requires = sbt.plugins.JvmPlugin

  object autoImport {
    type SemVer = dev.unlu.semver.SemVer
    val SemVer = dev.unlu.semver.SemVer
  }
}

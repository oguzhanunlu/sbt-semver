package dev.unlu.semver.cats

import _root_.cats.{Order, Show}

import dev.unlu.semver.SemVer

trait SemVerInstances {
  implicit val semverShow: Show[SemVer]   = Show.show(_.render)
  implicit val semverOrder: Order[SemVer] = Order.fromOrdering(SemVer.ordering)
}

object instances extends SemVerInstances

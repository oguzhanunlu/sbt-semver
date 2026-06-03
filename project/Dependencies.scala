import sbt._

object Dependencies {

  object V {
    val Scala212 = "2.12.20"
    val Scala213 = "2.13.15"
    val Scala3   = "3.3.4"

    val Sbt1 = "1.12.11"

    val Munit = "1.3.2"
  }

  val AllScalaVersions: Seq[String] = Seq(V.Scala212, V.Scala213, V.Scala3)

  object Libraries {
    val Munit = "org.scalameta" %% "munit" % V.Munit
  }
}

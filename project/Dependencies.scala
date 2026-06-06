import sbt._

object Dependencies {

  object V {
    val Scala212 = "2.12.21"
    val Scala213 = "2.13.18"
    val Scala3   = "3.3.7"

    val Sbt1 = "1.12.11"

    val Cats  = "2.13.0"
    val Munit = "1.3.2"
  }

  val AllScalaVersions: Seq[String] = Seq(V.Scala212, V.Scala213, V.Scala3)

  object Libraries {
    val Cats  = "org.typelevel" %% "cats-core" % V.Cats
    val Munit = "org.scalameta" %% "munit"     % V.Munit
  }
}

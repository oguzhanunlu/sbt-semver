package dev.unlu.semver

/** Represents a semantic version per SemVer 2.0.0.
  *
  * Numeric components are non-negative integers without leading zeros. Pre-release and build strings are dot-separated
  * identifier lists; pre-release identifiers reject leading zeros on numeric forms, build identifiers do not.
  *
  * The primary constructor is package-private. Use [[SemVer.of]] for validated construction; [[SemVer.unsafe]] throws
  * on invalid input.
  *
  * Numeric components are stored as `Int`. Values larger than `Int.MaxValue` (2,147,483,647) are rejected at parse
  * time, even though the SemVer spec sets no upper bound on these integers.
  *
  * @param major
  *   non-negative major version
  * @param minor
  *   non-negative minor version
  * @param patch
  *   non-negative patch version
  * @param preRelease
  *   optional pre-release identifier list (e.g. `"rc.1"`)
  * @param build
  *   optional build metadata identifier list (e.g. `"sha.abc"`)
  * @see
  *   https://semver.org/spec/v2.0.0.html
  * @see
  *   https://semver.org/#backusnaur-form-grammar-for-valid-semver-versions
  */
final case class SemVer private[semver] (
    major: Int,
    minor: Int,
    patch: Int,
    preRelease: Option[String] = None,
    build: Option[String] = None
) {
  def render: String = {
    val base = s"$major.$minor.$patch"
    val pre  = preRelease.fold("")("-" + _)
    val bld  = build.fold("")("+" + _)
    base + pre + bld
  }

  override def toString: String = render
}

object SemVer {

  private val Pattern =
    """^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-([0-9A-Za-z\-.]+))?(?:\+([0-9A-Za-z\-.]+))?$""".r

  private val AlphanumericPattern =
    """^[0-9A-Za-z\-]*[A-Za-z\-][0-9A-Za-z\-]*$""".r

  private val BuildIdPattern = """^[0-9A-Za-z\-]+$""".r

  def of(
      major: Int,
      minor: Int,
      patch: Int,
      preRelease: Option[String] = None,
      build: Option[String] = None
  ): Either[String, SemVer] =
    for {
      _ <- nonNegative(major, "major")
      _ <- nonNegative(minor, "minor")
      _ <- nonNegative(patch, "patch")
      _ <- preRelease.fold[Either[String, Unit]](Right(()))(validatePreRelease)
      _ <- build.fold[Either[String, Unit]](Right(()))(validateBuild)
    } yield new SemVer(major, minor, patch, preRelease, build)

  def of(s: String): Either[String, SemVer] = s match {
    case Pattern(maj, min, pat, pre, bld) =>
      for {
        majI <- parseInt(maj, "major")
        minI <- parseInt(min, "minor")
        patI <- parseInt(pat, "patch")
        v    <- of(majI, minI, patI, Option(pre), Option(bld))
      } yield v
    case _ =>
      Left(s"invalid SemVer string: $s")
  }

  def unsafe(
      major: Int,
      minor: Int,
      patch: Int,
      preRelease: Option[String] = None,
      build: Option[String] = None
  ): SemVer =
    of(major, minor, patch, preRelease, build)
      .fold(e => throw new IllegalArgumentException(e), identity)

  def unsafe(s: String): SemVer =
    of(s).fold(e => throw new IllegalArgumentException(e), identity)

  private def nonNegative(v: Int, name: String): Either[String, Unit] =
    if (v >= 0) Right(()) else Left(s"$name must be >= 0, got $v")

  private def parseInt(s: String, name: String): Either[String, Int] =
    try Right(s.toInt)
    catch { case _: NumberFormatException => Left(s"$name component overflows Int: $s") }

  private def validatePreRelease(s: String): Either[String, Unit] =
    if (s.isEmpty) Left("pre-release must not be empty")
    else
      s.split("\\.", -1)
        .foldLeft[Either[String, Unit]](Right(()))((acc, id) => acc.flatMap(_ => validatePreReleaseId(id)))

  private def validatePreReleaseId(s: String): Either[String, Unit] =
    if (s.isEmpty) Left("pre-release identifier must not be empty")
    else if (s.forall(_.isDigit))
      if (s.length > 1 && s.startsWith("0"))
        Left(s"numeric pre-release identifier must not have leading zeros: $s")
      else Right(())
    else if (AlphanumericPattern.pattern.matcher(s).matches) Right(())
    else Left(s"invalid alphanumeric identifier: $s")

  private def validateBuild(s: String): Either[String, Unit] =
    if (s.isEmpty) Left("build metadata must not be empty")
    else
      s.split("\\.", -1)
        .foldLeft[Either[String, Unit]](Right(()))((acc, id) => acc.flatMap(_ => validateBuildId(id)))

  private def validateBuildId(s: String): Either[String, Unit] =
    if (BuildIdPattern.pattern.matcher(s).matches) Right(())
    else Left(s"invalid build identifier: $s")
}

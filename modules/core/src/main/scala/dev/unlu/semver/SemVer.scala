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
) extends Ordered[SemVer] {
  def compare(that: SemVer): Int = SemVer.ordering.compare(this, that)

  def nextMajor: SemVer = copy(major = Math.addExact(major, 1), minor = 0, patch = 0, preRelease = None, build = None)
  def nextMinor: SemVer = copy(minor = Math.addExact(minor, 1), patch = 0, preRelease = None, build = None)
  def nextPatch: SemVer = copy(patch = Math.addExact(patch, 1), preRelease = None, build = None)

  def render: String =
    s"$major.$minor.$patch${preRelease.fold("")("-" + _)}${build.fold("")("+" + _)}"

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

  // Per spec §11. Build metadata is excluded from precedence.
  implicit val ordering: Ordering[SemVer] = (x, y) => {
    val cMajor = Integer.compare(x.major, y.major)
    if (cMajor != 0) cMajor
    else {
      val cMinor = Integer.compare(x.minor, y.minor)
      if (cMinor != 0) cMinor
      else {
        val cPatch = Integer.compare(x.patch, y.patch)
        if (cPatch != 0) cPatch
        else comparePreRelease(x.preRelease, y.preRelease)
      }
    }
  }

  private def comparePreRelease(a: Option[String], b: Option[String]): Int =
    (a, b) match {
      case (None, None)         => 0
      case (None, Some(_))      => 1
      case (Some(_), None)      => -1
      case (Some(av), Some(bv)) => comparePreReleaseStrings(av, bv)
    }

  private def comparePreReleaseStrings(a: String, b: String): Int = {
    val aIds = a.split("\\.", -1)
    val bIds = b.split("\\.", -1)
    aIds.iterator
      .zip(bIds.iterator)
      .map { case (x, y) => compareIdentifier(x, y) }
      .find(_ != 0)
      .getOrElse(Integer.compare(aIds.length, bIds.length))
  }

  private def compareIdentifier(a: String, b: String): Int =
    (a.forall(_.isDigit), b.forall(_.isDigit)) match {
      case (true, true)   => Integer.compare(Integer.parseInt(a), Integer.parseInt(b))
      case (true, false)  => -1
      case (false, true)  => 1
      case (false, false) => a.compareTo(b)
    }

  private def nonNegative(v: Int, name: String): Either[String, Unit] =
    if (v >= 0) Right(()) else Left(s"$name must be >= 0, got $v")

  private def parseInt(s: String, name: String): Either[String, Int] =
    try Right(Integer.parseInt(s))
    catch { case _: NumberFormatException => Left(s"$name overflows Int: $s") }

  private def traverseIds(s: String, emptyMsg: String, validate: String => Either[String, Unit]): Either[String, Unit] =
    if (s.isEmpty) Left(emptyMsg)
    else
      s.split("\\.", -1).foldLeft[Either[String, Unit]](Right(()))((acc, id) => acc.flatMap(_ => validate(id)))

  private def validatePreRelease(s: String): Either[String, Unit] =
    traverseIds(s, "pre-release must not be empty", validatePreReleaseId)

  private def validatePreReleaseId(s: String): Either[String, Unit] = {
    val allDigits = s.forall(_.isDigit)
    s match {
      case ""                                                  => Left("pre-release identifier must not be empty")
      case _ if allDigits && s.length > 1 && s.startsWith("0") =>
        Left(s"numeric pre-release identifier must not have leading zeros: $s")
      case _ if allDigits =>
        parseInt(s, "numeric pre-release identifier").map(_ => ())
      case _ if AlphanumericPattern.pattern.matcher(s).matches => Right(())
      case _                                                   => Left(s"invalid alphanumeric identifier: $s")
    }
  }

  private def validateBuild(s: String): Either[String, Unit] =
    traverseIds(s, "build metadata must not be empty", validateBuildId)

  private def validateBuildId(s: String): Either[String, Unit] =
    if (BuildIdPattern.pattern.matcher(s).matches) Right(())
    else Left(s"invalid build identifier: $s")
}

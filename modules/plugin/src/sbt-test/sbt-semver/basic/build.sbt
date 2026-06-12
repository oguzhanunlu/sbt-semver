// Mirrors the "In build.sbt" section of the README. SemVer is auto-imported
// by the plugin; this file compiles without an import statement.

version := SemVer.unsafe("1.4.0-rc.1").render

val akkaVersion = SemVer.unsafe("2.6.20")
libraryDependencies += {
  if (akkaVersion >= SemVer.unsafe("2.6.0")) "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion.render
  else "com.typesafe.akka" %% "akka-actor" % akkaVersion.render
}

val printNextMinor = taskKey[Unit]("Print the next minor version")
printNextMinor := println(SemVer.unsafe(version.value).nextMinor.render)

val check = taskKey[Unit]("Verify the README claims")
check := {
  assert(version.value == "1.4.0-rc.1", s"unexpected version: ${version.value}")
  assert(
    SemVer.unsafe(version.value).nextMinor.render == "1.5.0",
    s"unexpected next minor: ${SemVer.unsafe(version.value).nextMinor.render}"
  )
  assert(
    libraryDependencies.value.exists(m => m.name == "akka-actor-typed" && m.revision == "2.6.20"),
    s"version comparison picked the wrong branch: ${libraryDependencies.value}"
  )
  assert(SemVer.unsafe("1.0.0-rc.1") < SemVer.unsafe("1.0.0"), "pre-release must sort below release")
}

import sbt.Keys.libraryDependencies

name := "wson"

organization := "net.woggioni"

maintainer := "oggioni.walter@gmail.com"

version := "1.0"
resolvers += Resolver.mavenLocal

crossPaths := false

autoScalaLibrary := false

scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-opt:l:inline", "-opt-inline-from",
    "-target:jvm-1.8",
    "-encoding", "UTF-8"
)

git.useGitDescribe := true
//javaOptions in Test += "-Xmx14G"
javacOptions in (Compile, compile) ++= Seq("--release", "8")
//scalafmtOnCompile := true
libraryDependencies += "org.projectlombok" % "lombok" % Versions.lombok % Provided
libraryDependencies += "net.woggioni" % "jwo" % "1.0" % Compile

Compile / packageBin / packageOptions +=
  Package.ManifestAttributes("Automatic-Module-Name" -> "net.woggioni.wson")

libraryDependencies += "net.aichler" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test
libraryDependencies += "org.junit.jupiter" % "junit-jupiter-params" % JupiterKeys.junitJupiterVersion.value % Test
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % Versions.jackson % Test
libraryDependencies += "org.tukaani" % "xz" % Versions.xz % Test

enablePlugins(Delombok)
enablePlugins(DelombokJavadoc)
enablePlugins(JupiterPlugin)

testOptions += Tests.Argument(jupiterTestFramework, "-q", "-a")

lazy val testUtils = (project in file("test-utils"))

dependsOn(testUtils % "test")

lazy val worthAntlr = (project in file("antlr"))
  .dependsOn(LocalRootProject)
  .dependsOn(testUtils % "test")
  .enablePlugins(Antlr4Plugin)

lazy val cli = (project in file("cli"))
  .dependsOn(LocalRootProject)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(UniversalPlugin)
  .enablePlugins(JlinkPlugin)

lazy val benchmark = (project in file("benchmark"))
  .dependsOn(LocalRootProject)
  .dependsOn(worthAntlr)
  .dependsOn(testUtils)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(UniversalPlugin)

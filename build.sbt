name := "worth"

organization := "net.woggioni"

version := "1.0"
resolvers += Resolver.mavenLocal

scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-opt:l:inline", "-opt-inline-from",
    "-target:jvm-1.8",
    "-encoding", "UTF-8"
)

git.useGitDescribe := true
fork := true
//javaOptions in Test += "-Xmx14G"
//scalafmtOnCompile := true
libraryDependencies += "org.projectlombok" % "lombok" % "1.18.8"

val testDependencies = Seq("com.novocode" % "junit-interface" % "0.11" % Test,
                           "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.6" % Test,
                           "org.tukaani" % "xz" % "1.8" % Test)
libraryDependencies ++= testDependencies
testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-a")

val antlrVersion = "4.7.2"
lazy val worthAntlr = (project in file("antlr")).settings(
    organization := (organization in LocalRootProject).value,
    name := "worth-antlr",
    version := (version in LocalRootProject).value,
    resourceDirectory := (resourceDirectory in(LocalRootProject, Test)).value,
    antlr4Version in Antlr4 := antlrVersion,
    antlr4PackageName in Antlr4 := Some("net.woggioni.worth.antlr"),
    skip in publish := true,
    unmanagedClasspath in Test += (classDirectory in (LocalRootProject, Test)).value,
    libraryDependencies += "org.antlr" % "antlr4" % antlrVersion % Compile,
    libraryDependencies += "org.antlr" % "antlr4-runtime" % antlrVersion,
    libraryDependencies += "org.projectlombok" % "lombok" % "1.18.8",
    libraryDependencies ++= testDependencies
).dependsOn(LocalRootProject).enablePlugins(Antlr4Plugin)

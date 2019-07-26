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
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.6" % Test

libraryDependencies += "org.antlr" % "antlr4" % "4.7.1" % Test
libraryDependencies += "org.antlr" % "antlr4-runtime" % "4.7.1" % Test
libraryDependencies += "org.tukaani" % "xz" % "1.8" % Test

enablePlugins(Antlr4Plugin)
antlr4Version in Antlr4 := "4.7.1"
antlr4PackageName in Antlr4 := Some("net.woggioni.worth.antlr")

testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-a")
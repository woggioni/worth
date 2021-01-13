organization := (organization in LocalRootProject).value
name := "worth-antlr"
version := (version in LocalRootProject).value
resourceDirectory := (resourceDirectory in(LocalRootProject, Test)).value
antlr4Version in Antlr4 := Versions.antlr
antlr4PackageName in Antlr4 := Some("net.woggioni.worth.antlr")
skip in publish := true

libraryDependencies += "org.projectlombok" % "lombok" % Versions.lombok % Provided

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % Versions.jackson % Test
libraryDependencies += "org.tukaani" % "xz" % Versions.xz % Test
libraryDependencies += "org.antlr" % "antlr4" % Versions.antlr % Test
libraryDependencies += "org.antlr" % "antlr4-runtime" % Versions.antlr % Test
libraryDependencies += "net.aichler" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test
libraryDependencies += "org.junit.jupiter" % "junit-jupiter-params" % JupiterKeys.junitJupiterVersion.value % Test

enablePlugins(JupiterPlugin)
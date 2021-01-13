organization := (organization in LocalRootProject).value
name := "worth-cli"
version := (version in LocalRootProject).value
resourceDirectory := (resourceDirectory in(LocalRootProject, Test)).value
skip in publish := true
mainClass := Some("net.woggioni.worth.cli.Main")
maintainer := "oggioni.walter@gmail.com"
libraryDependencies += "com.beust" % "jcommander" % Versions.jcommander

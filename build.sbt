name := "skillGC"

version := "0.1"

scalaVersion := "2.12.4"

javacOptions ++= Seq("-encoding", "UTF-8")

compileOrder := CompileOrder.JavaThenScala

libraryDependencies ++= Seq(
	"junit" % "junit" % "4.12" % "test",
	"org.scalatest" %% "scalatest" % "3.0.4" % "test"
)

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    assemblyJarName in assembly := "skillgc.jar",
    test in assembly := {},
    exportJars := true,

    mainClass := Some("de.ust.skill.gc.CommandLine"),

    (testOptions in Test) += Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/tests"),

    libraryDependencies += "commons-lang" % "commons-lang" % "2.6",

    libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.0",

    libraryDependencies += "com.github.scopt" %% "scopt" % "3.7.0"


      ).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "de.ust.skill",
  )


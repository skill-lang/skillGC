package de.ust.skill.gc

import scala.collection.mutable.ArrayBuffer
import java.io.File

object CommandLine {
  case class GCConfig(
    targets : ArrayBuffer[File] = new ArrayBuffer,
    roots : ArrayBuffer[String] = new ArrayBuffer,
    dryRun : Boolean = false,
    progress : Boolean = false,
    statistics : Boolean = false);

  val argumentParser = new scopt.OptionParser[GCConfig]("skillGC") {

    opt[Unit]('d', "dry-run").optional().action((x, c) ⇒
      c.copy(dryRun = true)).text("do not write results")

    opt[String]('p', "progress").unbounded().action((x, c) ⇒
      c.copy(progress = true)).text("print progress while collecting")

    opt[String]('r', "root").unbounded().action((x, c) ⇒
      c.copy(statistics = true)).text("add a type as gc root")

    opt[Unit]('s', "statistics").optional().action((x, c) ⇒
      c.copy(statistics = true)).text("print garbage statistics")

    help("help").text("prints this usage text")

    arg[File]("<file>...").unbounded().action { (x, c) ⇒
      c.targets += x; c
    }.text("target files")
  }

  def main(args : Array[String]) : Unit = {
    val opts = GCConfig()
    argumentParser.parse(args, opts)

    process(opts)
  }

  private def process(opts : GCConfig) {
    // TODO
  }
}
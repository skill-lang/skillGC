package de.ust.skill.gc

import java.io.File

import scala.collection.mutable.ArrayBuffer
import de.ust.skill.gc.api.SkillFile
import de.ust.skill.common.scala.api.Read
import de.ust.skill.common.scala.api.ReadOnly
import de.ust.skill.common.scala.api.Write
import scala.collection.mutable.HashSet

final object CommandLine {
  case class GCConfig(
    targets : ArrayBuffer[File] = new ArrayBuffer,
    roots : HashSet[String] = new HashSet,
    dumpGraph : String = null,
    dryRun : Boolean = false,
    progress : Boolean = false,
    statistics : Boolean = false);

  val argumentParser = new scopt.OptionParser[GCConfig]("skillGC") {

    opt[Unit]('d', "dry-run").optional().action((x, c) ⇒
      c.copy(dryRun = true)).text("do not write results")

    opt[Unit]('p', "progress").unbounded().action((x, c) ⇒
      c.copy(progress = true)).text("print progress while collecting")

    opt[String]('r', "root").unbounded().action { (x, c) ⇒
      c.roots += x.toLowerCase; c
    }.text("add a type as gc root")

    opt[Unit]('s', "statistics").optional().action((x, c) ⇒
      c.copy(statistics = true)).text("print garbage statistics")

    help("help").text("prints this usage text")

    arg[File]("<file>...").unbounded().action { (x, c) ⇒
      c.targets += x; c
    }.text("target files")
  }

  def main(args : Array[String]) : Unit = {
    argumentParser.parse(args, GCConfig()).foreach(process)
  }

  private def process(opts : GCConfig) {
    for (f ← opts.targets) {
      println(s"processing $f...")
      val sf = SkillFile.open(f, Read,
        if (opts.dryRun) ReadOnly
        else Write
      );

      val begin = System.nanoTime()

      new GCRun(sf, opts.roots, opts.progress, opts.statistics)

      if (!opts.dryRun)
        sf.close

      if (opts.statistics) {
        println(s" finished in ${(System.nanoTime() - begin) * 1e-9} sec")
      }

      println("-done-")
    }
  }
}
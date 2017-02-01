package de.ust.skill.gc

import java.io.PrintStream
import java.io.File

/**
 * util for heap graph dumping
 */
class GraphDump {
  val out = new PrintStream(new File("heap.dot"))
  out.append("digraph {\n");

  def edge(from : String, to : String) {
    out.append(s""" "${repr(from)}" -> "${repr(to)}"
""")
  }

  private def repr(s : String) : String = {
    {
      if (s.length() > 30) s.substring(0, 29) + "..."
      else s
    }.replace("unknown(", "").replace(")#", "#")
  }

  def close {
    out.append("\n}");
    out.close
  }
}
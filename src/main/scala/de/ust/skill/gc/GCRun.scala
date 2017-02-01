package de.ust.skill.gc

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashSet

import de.ust.skill.common.scala.api.SkillObject
import de.ust.skill.gc.api.SkillFile
import de.ust.skill.common.scala.api.Access
import de.ust.skill.common.scala.api.FieldType
import de.ust.skill.common.scala.internal.fieldTypes.SingleBaseTypeContainer
import de.ust.skill.common.scala.internal.fieldTypes.MapType
import scala.collection.mutable.HashMap

/**
 * state of a garbage collection run
 */
final class GCRun(
    val sf : SkillFile,
    val roots : HashSet[String],
    val printProgress : Boolean,
    val printStatistics : Boolean) {
  val seen = new HashSet[AnyRef]
  val todo = new HashSet[SkillObject]

  val types : Map[String, Access[_]] = sf.map(t ⇒ (t.name, t)).toMap

  lazy val totalNodes = sf.filter(_.superName.isEmpty).map(_.size).sum + sf.String.size

  // PHASE 1: add root instances
  addRoots

  // PHASE 2: process todo until it's empty
  processNodes

  // PHASE 3: remove unreachable objects
  removeDeadNodes

  // implementation \\

  private def addRoots {
    for (t ← sf) {
      if (roots.contains(t.name)) {
        todo ++= t.all
      }
    }
    // our initial todo list is the set of root objects 
    seen ++= todo
    if (printStatistics) {
      println(s"  total nodes: $totalNodes")
      println(s"  roots: ${seen.size}")
    }
  }

  private def processNodes {
    if (printProgress)
      print("collecting")

    var processedNodes = 0
    while (!todo.isEmpty) {
      val node = todo.head
      todo -= node

      if (printProgress) {
        processedNodes += 1
        if (0 == processedNodes % (totalNodes / 10))
          print(".")
      }

      // visit all fields of that node
      var t = types(node.getTypeName)

      for (f ← t.allFields if !ignoreType(f.t)) {
        val v = f.getR(node).asInstanceOf[AnyRef];
        if (null != v) {
          processObject(f.t, v)
        }
      }
    }

    if (printProgress)
      println("done")
  }

  private def processObject(t : FieldType[_], x : AnyRef) {
    val id = t.typeID
    if (14 == id) {
      // string
      seen += x

    } else if (15 <= id && id <= 19) {
      // linear collection
      val bt = t.asInstanceOf[SingleBaseTypeContainer[_, _]].groundType
      for (i ← x.asInstanceOf[Iterable[_ <: AnyRef]])
        processObject(bt, i)

    } else if (20 == id) {
      // map
      val mt = t.asInstanceOf[MapType[_, _]]
      val followKey = !ignoreType(mt.keyType)
      val followVal = !ignoreType(mt.valueType)
      for (i ← x.asInstanceOf[HashMap[_, _]]) {
        if (followKey) processObject(mt.keyType, i._1.asInstanceOf[AnyRef])
        if (followVal) processObject(mt.valueType, i._2.asInstanceOf[AnyRef])
      }

    } else {
      // ref
      if (!seen(x)) {
        seen += x
        if (x.isInstanceOf[SkillObject])
          todo += x.asInstanceOf[SkillObject]
      }
    }
  }

  private def removeDeadNodes {
    if (printStatistics) {
      println(s"  reachable: ${seen.size}")
    }

    for (t ← sf) {
      for (x ← t) {
        if (!seen(x)) {
          println("delete: " + x)
          sf.delete(x)
        }
      }
    }
    for (s ← sf.String) {
      if (!seen(s)) {
        println("delete string (TODO): " + s)

      }
    }
  }

  private def ignoreType(t : FieldType[_]) : Boolean = {
    val id = t.typeID;
    if (id < 14 && id != 5) {
      return true;
    } else if (15 <= id && id <= 19) {
      return ignoreType(t.asInstanceOf[SingleBaseTypeContainer[_, _]].groundType)
    } else if (20 == id) {
      return ignoreType(t.asInstanceOf[MapType[_, _]].keyType) && ignoreType(t.asInstanceOf[MapType[_, _]].valueType)
    } else
      return false
  }
}
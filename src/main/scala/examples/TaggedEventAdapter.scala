package examples

import akka.actor.ExtendedActorSystem
import akka.persistence.journal.{EventAdapter, EventSeq, Tagged}

class TaggedEventAdapter(system: ExtendedActorSystem)
  extends EventAdapter {

  override def manifest(event: Any): String = ""

  override def toJournal(event: Any): Any = event match {
    case e: InvoiceEvent ⇒
      Tagged(e, Set("invoice"))
  }

  override def fromJournal(event: Any, manifest: String): EventSeq =
    EventSeq.single(event)

}

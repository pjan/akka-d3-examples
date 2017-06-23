package examples

import akka.NotUsed
import akka.actor.ActorSystem
import akka.contrib.d3._
import akka.contrib.d3.readside.{CassandraOffsetStore, CassandraSession}
import akka.persistence.query.{NoOffset, Offset}
import akka.stream.ThrottleMode
import akka.stream.scaladsl.Source

import scala.concurrent.Future
import scala.concurrent.duration._

class ReadSideApp(system: ActorSystem) {
  import system.dispatcher

  val session = CassandraSession(system, "invoices-readside")
  val offsetStore = CassandraOffsetStore(system, session, 10.seconds)

  val invoiceEventProcessor = new InvoiceEventProcessor(system, session, offsetStore)

  def run(): Unit = {
    ReadSide(system).register[InvoiceEvent](invoiceEventProcessor)

    ReadSide(system).status("invoices").foreach(println)

    Thread.sleep(5000)

    ReadSide(system).start("invoices")

    Thread.sleep(5000)

    ReadSide(system).status("invoices").foreach(println)

    Thread.sleep(5000)

    ReadSide(system).stop("invoices")

    Thread.sleep(2500)

    ReadSide(system).status("invoices").foreach(println)

    Thread.sleep(2500)

    ReadSide(system).rewind("invoices", NoOffset)

    ReadSide(system).status("invoices").foreach(println)

    Thread.sleep(3000)

    ReadSide(system).start("invoices")

    Thread.sleep(10000)
  }

}

class InvoiceEventProcessor(
  system: ActorSystem,
  session: CassandraSession,
  offsetStore: CassandraOffsetStore
) extends ReadSideProcessor[InvoiceEvent] {
  override def name: String = "invoices"

  override def tag: Tag = Tag("invoice")

  override def eventStreamFactory(tag: Tag, fromOffset: Offset): Source[EventStreamElement[InvoiceEvent], NotUsed] =
    Domain(system).eventStream[InvoiceEvent](tag, fromOffset, "invoices")
      .throttle(10, 1.second, 10, ThrottleMode.shaping)

  override def buildHandler(): ReadSideProcessor.Handler[InvoiceEvent] =
    ReadSide(system).cassandra(session, offsetStore).builder[InvoiceEvent](name, tag)
      .setEventHandler[InvoiceEvent.Created] { e => println(s"!!!!!!!! $e"); Future.successful(Vector.empty) }
      .setEventHandler[InvoiceEvent.Closed] { e =>  println(s"XXXXXXXX $e"); Future.successful(Vector.empty) }
      .build()
}

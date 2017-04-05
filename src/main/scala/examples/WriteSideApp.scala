package examples

import akka.Done
import akka.actor.ActorSystem
import akka.contrib.d3._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util._

class WriteSideApp(
  system: ActorSystem
) {
  implicit private val ec = system.dispatcher
  implicit private val timeout = akka.util.Timeout(10.seconds)

  private val domain = Domain(system)
    .register[InvoiceEntity](
    entityFactory = (id: Invoice.Id) => InvoiceEntity(id),
    name = "invoices"
  )

  def run(): Future[Done] = {
    val aggregates = List.fill(50) {
      Random.nextInt(1000000)
    }.map { id ⇒ domain.aggregateRef[InvoiceEntity](Invoice.Id(s"$id")) }

    val result = Future.sequence {
      aggregates.map(aggregate ⇒
        for {
          e1 ← aggregate ? InvoiceCommand.Create(Invoice.Amount(BigDecimal(200)))
          s1 ← aggregate.state
          e2 ← aggregate ? InvoiceCommand.Close("paid")
          s2 ← aggregate.state
          q3 ← aggregate.isInitialized
        } yield (e1, s1, e2, s2, q3))
    }

    result.map { l =>
      l.foreach {
        case (e1, s1, e2, s2, q3) ⇒
          println(
            s"""
             |1. events:       $e1
             |   state:        $s1
             |2. events:       $e2
             |   state:        $s2
             |4. initialized?: $q3
           """.stripMargin
        )
      }
      Done
    }
  }

}

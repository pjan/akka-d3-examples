package examples

import akka.actor._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.util.{Failure, Success}

object Local extends App {

  val config = ConfigFactory.parseString(
    s"""
       |akka.loglevel = "INFO"
       |akka.actor.provider = "local"
       |akka.contrib.d3.topology = "local"
       |akka.contrib.d3.writeside.journal.plugin = "cassandra-journal"
       |akka.contrib.d3.writeside.invoices.passivation-timeout = 5 s
       |akka.contrib.d3.readside.processor.invoices.auto-start = false
       |akka.contrib.d3.query.provider = "cassandra"
       |akka.contrib.d3.query.invoices.read-journal.plugin = "cassandra-invoice-query-journal"
     """.stripMargin
  ).withFallback(ConfigFactory.load())

  implicit val system = ActorSystem("LocalTest", config)
  implicit val dispatcher = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val startupApp = new StartupApp(system)
  val writeSideApp = new WriteSideApp(system)
  val readSideApp = new ReadSideApp(system)

  val result = for {
    _ <- startupApp.run()
    res <- writeSideApp.run()
  } yield res

  result.onComplete {
    case Success(_) =>
      readSideApp.run()
      Thread.sleep(3000)

      system.terminate()

    case Failure(exception) =>
      println(s"Something went wrong: ${exception.getMessage}")
      Thread.sleep(3000)
      system.terminate()
  }

}

package examples

import akka.actor._
import akka.cluster.Cluster
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.util.{Failure, Success}

object Clustered extends App {

  val config = ConfigFactory.parseString(
    s"""
       |akka.loglevel = "INFO"
       |akka.actor.provider = "cluster"
       |akka.cluster.roles = [invoices]
       |akka.contrib.d3.topology = "cluster"
       |akka.contrib.d3.writeside.journal.plugin = "cassandra-journal"
       |akka.contrib.d3.writeside.invoices.passivation-timeout = 5 s
       |akka.contrib.d3.query.provider = "cassandra"
       |akka.contrib.d3.query.invoices.read-journal.plugin = "cassandra-invoice-query-journal"
     """.stripMargin
  ).withFallback(ConfigFactory.load())

  implicit val system = ActorSystem("ClusterTest", config)
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher
  val cluster = Cluster(system)
  cluster.join(cluster.selfAddress)

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

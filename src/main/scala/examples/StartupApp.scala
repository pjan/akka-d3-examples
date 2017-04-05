package examples

import akka.Done
import akka.actor.ActorSystem
import akka.contrib.d3.utils.StartupTasks

import scala.concurrent.Future
import scala.concurrent.duration._

class StartupApp(
  system: ActorSystem
) {
  implicit val ec = system.dispatcher

  val startupTasks = StartupTasks(system)
  implicit val timeout = akka.util.Timeout(10.seconds)

  def run(): Future[Done] = {
    val startupTask = startupTasks.create(
      "boom",
      () => Future { Thread.sleep(2000); akka.Done },
      5.seconds,
      5.seconds,
      30.seconds,
      0.5
    )
    Thread.sleep(3000)

    startupTask.execute().onComplete { res => println(res) }
    startupTask.execute().onComplete { res => println(res) }
    startupTask.execute().onComplete { res => println(res) }
    startupTask.execute()
  }

}

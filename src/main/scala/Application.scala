import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends App{
  val system = ActorSystem("AuctionSystem")

  val titles: List[String] = List("Nexus 5", "BRAND NEW Xiaomi", "iPhone 7", "Nokia 3310")

  val auctionSearch = system.actorOf(Props[AuctionSearch], "auctionSearch")
  val seller = system.actorOf(Props(new Seller(titles)), "seller")

  system.scheduler.scheduleOnce(1 seconds) {
    val buyer1 = system.actorOf(Props(new Buyer("nexus", 100)), "buyer1")
    val buyer2 = system.actorOf(Props(new Buyer("3310", 150)), "buyer2")
    val buyer3 = system.actorOf(Props(new Buyer("brand", 200)), "buyer3")
  }

  Await.result(system.whenTerminated, Duration.Inf)
}
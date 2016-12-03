import akka.actor._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import akka.pattern.ask

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends App{
  val config = ConfigFactory.load()

  val titles: List[String] = List("Nexus 5", "BRAND NEW Xiaomi", "iPhone 7", "Nokia 3310")

  val auctionpublishersystem = ActorSystem("AuctionSystem", config.getConfig("auctionpublishersystem").withFallback(config))
  val auctionPublisher = auctionpublishersystem.actorOf(Props[AuctionPublisher], "auctionPublisher")

  auctionPublisher ! AuctionPublisher.Init
  val auctionSystem = ActorSystem("AuctionSystem", config.getConfig("auctionsystem").withFallback(config))

  val notifier = auctionSystem.actorOf(Props[Notifier], "notifier")

  notifier ! Notifier.Init
  val masterSearch = auctionSystem.actorOf(Props[MasterSearch], "masterSearch")
  val auctionSearch = auctionSystem.actorOf(Props[AuctionSearch], "auctionSearch")
  val seller = auctionSystem.actorOf(Props(new Seller(titles)), "seller")

  auctionSystem.scheduler.scheduleOnce(1 seconds) {
    val buyer1 = auctionSystem.actorOf(Props(new Buyer("nexus", 100)), "buyer1")
    val buyer2 = auctionSystem.actorOf(Props(new Buyer("3310", 150)), "buyer2")
    val buyer3 = auctionSystem.actorOf(Props(new Buyer("brand", 200)), "buyer3")
  }

//  notifier ! Notifier.Done

//  auctionpublishersystem.terminate()

  Await.result(auctionSystem.whenTerminated, Duration.Inf)
  Await.result(auctionpublishersystem.whenTerminated, Duration.Inf)
}
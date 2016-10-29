import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


//object Seller {
//  case object Auctions
//}
//
//class Seller(items: List[String]) extends Actor{
//  def receive = LoggingReceive {
//
//  }
//}

object Application extends App{
  val system = ActorSystem("AuctionSystem")

  val auction1 = system.actorOf(Props[Auction], "auction1")
  val auction2 = system.actorOf(Props[Auction], "auction2")

  val auctions = List(auction1, auction2)

  val buyer1 = system.actorOf(Props(new Buyer(auctions)), "buyer1")
  val buyer2 = system.actorOf(Props(new Buyer(auctions)), "buyer2")
  val buyer3 = system.actorOf(Props(new Buyer(auctions)), "buyer3")

  auction1 ! Auction.Created
  auction2 ! Auction.Created

  buyer1 ! Buyer.Created
  buyer2 ! Buyer.Created
  buyer3 ! Buyer.Created

  Await.result(system.whenTerminated, Duration.Inf)
}
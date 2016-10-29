import akka.actor._
import akka.event.LoggingReceive

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Auction {
  case object Created
  case object Active
  case object Ignored
  case object Sold
  case object Inactive
}

class Auction extends Actor {

  var actualBid = BigInt(0)
  var winner = ActorRef.noSender

  def receive = LoggingReceive {
    case Auction.Created =>
      context.system.scheduler.scheduleOnce(2 seconds, self, Auction.Inactive)
      context become created
  }

  def created: Receive = LoggingReceive {
    case Buyer.bid(from, newBid) if actualBid <= newBid =>
      println("Bid success")
      actualBid = newBid
      winner = from
      sender ! Auction.Active
      context become activated

    case Auction.Inactive =>
      context.system.scheduler.scheduleOnce(1 seconds, self, Auction.Ignored)
      context become inactive
  }

  def activated: Receive = LoggingReceive {
    case Buyer.bid(from, newBid) if actualBid <= newBid =>
      println("Bid success")
      actualBid = newBid
      winner = from
      sender ! Auction.Active

    case Auction.Inactive =>
      context.system.scheduler.scheduleOnce(1 seconds, self, Auction.Sold)
      context become inactive
  }

  def inactive: Receive = LoggingReceive {
    case Auction.Ignored =>
      println("Timer for auction " + self.path + " stopped")
      println("Auction " + self.path + " ignored")
      context stop self

    case Auction.Sold =>
      println("Timer for auction " + self.path + " stopped")
      println("Auction " + self.path + " sold to " + winner.path + " for " + actualBid)
      winner ! Auction.Sold
  }
}

object Buyer {
  case object Created
  case class bid(from: ActorRef, bid: BigInt)
}

class Buyer(auctions: List[ActorRef]) extends Actor {

  val r = scala.util.Random
  var newBid = 0

  def receive = LoggingReceive {
    case Buyer.Created =>
      newBid = r.nextInt(100)
      println("Buyer bid to " + newBid)
      auctions.head ! Buyer.bid(self, newBid)
      context become awaitBid
  }

  def awaitBid: Receive = LoggingReceive {
    case Auction.Active =>
      newBid = r.nextInt(100)
      println("Buyer bid to " + newBid)
      auctions.head ! Buyer.bid(self, newBid)

    case Auction.Sold =>
      context.system.terminate()
  }
}

object Seller {
  case object Auctions
}

class Seller(items: List[String]) extends Actor{

}

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
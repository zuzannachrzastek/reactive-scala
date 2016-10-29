import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Zuzanna on 29.10.2016.
  */
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

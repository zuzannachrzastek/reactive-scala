import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Zuzanna on 29.10.2016.
  */
object Auction {
  case object Created
  case object Active
  case object Ignored
  case object Sold
  case object Inactive
  case object Deleted
  case class Offer(price: Double)
  case class YouWon(item: String, price: Double)
  case class Sold(item: String)
  case class Beaten(price: Double)
  case class NotEnough(price: Double)
}

class Auction(itemName: String) extends Actor {

  var actualBid: Double = 0
  var winner = ActorRef.noSender

  def receive = LoggingReceive {
    case Auction.Created =>
      context.system.scheduler.scheduleOnce(2 seconds, self, Auction.Inactive)
      context become activated
  }

  def activated: Receive = LoggingReceive {
    case Buyer.offer(from, newBid) if actualBid <= newBid =>
      println("Beaten")
      actualBid = newBid
      winner = from
      sender ! Auction.Beaten(actualBid)

    case Buyer.offer(from, _) =>
      sender ! Auction.NotEnough(actualBid)

    case Buyer.offer(from, newBid) =>
      println("auction ends")
      actualBid = newBid
      winner = from
      sender ! Auction.YouWon(itemName, newBid)
      context.system.scheduler.scheduleOnce(6 seconds, self, Auction.Deleted)
      context become sold

    case Auction.Inactive =>
      context.system.scheduler.scheduleOnce(1 seconds, self, Auction.Ignored)
      context become ignored
  }

  def ignored: Receive = LoggingReceive {

    case Auction.Ignored =>
      println("Timer for auction " + self.path + " stopped")
      println("Auction " + self.path + " ignored")
      context stop self
  }

  def sold: Receive = LoggingReceive {
    case Auction.Deleted =>
      context.parent ! Seller.AuctionEnds(sold = false)

    case Auction.Sold =>
      println("Timer for auction " + self.path + " stopped")
      println("Auction " + self.path + " sold to " + winner.path + " for " + actualBid)
      winner ! Auction.Sold
  }
}


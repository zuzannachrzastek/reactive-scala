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
  case object Deleted
  case object OK
  case class Offer(price: Double)
  case class YouWon(item: String, price: Double)
  case class ItemSold(item: String)
  case class Beaten(price: Double)
  case class NotEnough(price: Double)
  case object TimeEnd
}

class Auction(itemName: String) extends Actor {

  var actualBid: Double = 0
  var winner = ActorRef.noSender

  def receive = LoggingReceive {
    case Auction.Created =>
      sender ! Auction.OK
      context.system.scheduler.scheduleOnce(7 seconds, self, Auction.TimeEnd)
      context become activated
  }

  def activated: Receive = LoggingReceive {
    case Buyer.offer(from, newBid) if actualBid <= newBid =>
      println("Beaten")
      actualBid = newBid
      winner = from
      sender ! Auction.Beaten(actualBid)

    case Buyer.offer(from, newBid)if actualBid > newBid =>
      sender ! Auction.NotEnough(actualBid)

    case Auction.TimeEnd =>
      println("auction ends")
      winner = sender
      sender ! Auction.YouWon(itemName, actualBid)
      context.system.scheduler.scheduleOnce(1 seconds, self, Auction.Deleted)
      context become sold
  }

  def ignored: Receive = LoggingReceive {

    case Auction.Deleted =>
      println("Timer for auction " + self.path + " stopped")
      println("Auction " + self.path + " ignored")
      context.parent ! Seller.AuctionEnds(sold = false)
      context stop self
  }

  def sold: Receive = LoggingReceive {
    case Auction.Deleted =>
      context.parent ! Seller.AuctionEnds(sold = true)

    case Auction.Sold =>
      println("Timer for auction " + self.path + " stopped")
      println("Auction " + self.path + " sold to " + winner.path + " for " + actualBid)
      winner ! Auction.ItemSold(itemName)
  }
}


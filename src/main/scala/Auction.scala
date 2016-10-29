import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive

import scala.concurrent.Await
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


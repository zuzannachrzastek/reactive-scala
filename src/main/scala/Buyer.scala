import AuctionSearch.Search
import akka.actor.{Actor, ActorRef, PoisonPill}
import akka.event.LoggingReceive

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Zuzanna on 29.10.2016.
  */
object Buyer {
  case class offer(from: ActorRef, bid: Double)
}

class Buyer(target: String, maxPrice: Double) extends Actor {

  val auctionSearch = context.actorSelection("user/AuctionSearch")

  auctionSearch ! Search(target)

  val r = scala.util.Random
  var newBid = 0
  var items: List[ActorRef] = _
  var bought = 0

  def receive = LoggingReceive {
    case AuctionSearch.SearchResults(auctions) =>
      items = auctions
      items.foreach(_ ! Buyer.offer(self, newBid))
      context become inProgress

//    case Buyer.Created =>
//      newBid = r.nextInt(100)
//      println("Buyer bid to " + newBid)
//      auctions.head ! Buyer.bid(self, newBid)
//      context become awaitBid
//  }

  def inProgress: Receive = LoggingReceive {
//    case Auction.Active =>
//      newBid = r.nextInt(100)
//      println("Buyer bid to " + newBid)
//      auctions.head ! Buyer.offer(self, newBid)

//    case Auction.Sold =>
//      context.system.terminate()

    case Auction.Beaten(price) =>
      offerInAuction(price, sender())

    case Auction.NotEnough(price) =>
      offerInAuction(price, sender())

    case Auction.Sold(item) =>
      println(s"I not bought $item")
      bought += 1
      stopIfEnd()

    case Auction.YouWon(item, price) =>
      println(s"I bought $item for ${price}DC")
      bought += 1
      stopIfEnd()
  }

      def offerInAuction(actualBid: Double, auction: ActorRef): Unit ={
        maxPrice - actualBid match {
          case farFromMax if farFromMax > 1 =>
            sendOfferToAuction(auction, actualBid + r.nextInt(100))
          case notFarFromMax if notFarFromMax > 0 =>
            sendOfferToAuction(auction, maxPrice)
          case moreThanMax =>
            bought += 1
            println(s"I not bought from ${auction.path.name} because price is to high")
            stopIfEnd()
        }
      }

      def sendOfferToAuction(auction: ActorRef, price:Double): Unit ={
        context.system.scheduler.scheduleOnce(2 seconds) {
          auction ! Buyer.offer(self, price)
        }
      }

      def stopIfEnd() = if (items.length == bought) {
        println(s"${self.path.name} die.")
        self ! PoisonPill
      }
  }

}

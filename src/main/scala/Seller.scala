import AuctionSearch.{Register, Unregister}
import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import akka.event.LoggingReceive

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Zuzanna on 29.10.2016.
  */

object Seller {
  case class AuctionEnds(sold: Boolean)
}

class Seller (titles: List[String]) extends Actor {

  val auctionSearch = context.actorSelection("/user/auctionSearch").resolveOne(1 seconds)

  var auctions = titles.map(title => {
    val auction = context.actorOf(Props(classOf[Auction], title, self), s"""${title.replaceAll(" ", "_")}_auction""")
    auctionSearch.map(_.tell(Register(title), auction))
    auction ! Auction.Created
    auction -> title
  }).toMap

  def receive = LoggingReceive {
    case Auction.OK =>
      println("Auction created")
      context become created
  }

  def created = LoggingReceive {
    case Seller.AuctionEnds(sold) =>
      sold match {
        case true => println(s"${sender().path.name} sold item")
        case false => println(s"${sender().path.name} didn't sold")
      }
      auctions -= sender
      auctionSearch.map(_.tell(Unregister(auctions(sender())), sender()))

      if (auctions.size == 0) {
        println(s"no auctions left, shutdown")
        self ! PoisonPill
      }
  }
}

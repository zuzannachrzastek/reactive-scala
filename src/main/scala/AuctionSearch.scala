import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive

import scala.collection.mutable

/**
  * Created by Zuzanna on 29.10.2016.
  */

object AuctionSearch {
  case class Search(phase: String)
  case class SearchResults(auctions: List[ActorRef])
  case class Unregister(auctionName: String)
  case class Register(auctionName: String)
}

class AuctionSearch extends Actor {

  val auctions = mutable.Map[String, ActorRef]()
  println(s"${self.path} AuctionSearch created")

  def receive = LoggingReceive {
    case AuctionSearch.Register(name) =>
      println(s"registering auction $name by actor ${sender()}")
      auctions += name.toLowerCase -> sender

    case AuctionSearch.Search(searchPhase) =>
      println(s"received search for $searchPhase request")
      val phase = searchPhase.toLowerCase
      sender ! AuctionSearch.SearchResults(auctions.filterKeys(_.contains(phase)).values.toList)

    case AuctionSearch.Unregister(auctionName) =>
      println(s"unregistering auction $auctionName by actor ${sender()}")
      auctions -= auctionName
  }

}

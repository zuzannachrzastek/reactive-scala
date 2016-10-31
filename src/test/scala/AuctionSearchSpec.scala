import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

/**
  * Created by Zuzanna on 30.10.2016.
  */
class AuctionSearchSpec extends TestKit(ActorSystem("AuctionSearchSpec"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  "An AuctionSearch" must {
    "receive search request" in {
      val probe = TestProbe()
      val auction1 = TestProbe("nexus_6_auction")
      val auction2 = TestProbe("iphone_7_auction")
      val auction3 = system.actorOf(Props(classOf[Auction], "pixel_auction", probe.ref))
      val auctionSearch = system.actorOf(Props[AuctionSearch], "auctionSearch")

      auctionSearch ! AuctionSearch.Register(auction1.ref.path.name)
      auctionSearch ! AuctionSearch.Register(auction2.ref.path.name)
      auctionSearch.tell(AuctionSearch.Register("pixel"), auction3)

      auctionSearch ! AuctionSearch.Search("pixel")
      expectMsg(AuctionSearch.SearchResults(List(auction3)))
    }

//    "registers and unregisters auctions" in {
//      val auctionSearch = system.actorOf(Props[AuctionSearch], "auctionSearch")
//
//      auctionSearch ! AuctionSearch.Register
//
//    }
  }

}

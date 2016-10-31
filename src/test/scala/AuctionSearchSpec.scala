import Auction.Unregistered
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

/**
  * Created by Zuzanna on 30.10.2016.
  */
class AuctionSearchSpec extends TestKit(ActorSystem("AuctionSearchSpec"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate
  }

  val auctionSearch = system.actorOf(Props[AuctionSearch], "auctionSearch")

  "An AuctionSearch" must {
    "receive search request" in {
      val auction1 = TestProbe("nexus_6_auction")
      val auction2 = TestProbe("iphone_7_auction")
      val auction3 = TestProbe("pixel_auction")

      auctionSearch.tell(AuctionSearch.Register("nexus_6_auction"), auction1.ref)
      auctionSearch.tell(AuctionSearch.Register("iphone_7_auction"), auction2.ref)
      auctionSearch.tell(AuctionSearch.Register("pixel_pixel"), auction3.ref)

      auctionSearch ! AuctionSearch.Search("iphone")
      expectMsg(AuctionSearch.SearchResults(List(auction2.ref)))

    }

    "registers and unregisters auctions" in {
      val auction = TestProbe("nexus_6_auction")

      auctionSearch.tell(AuctionSearch.Register("nexus_6_auction"), auction.ref)
      auctionSearch.tell(AuctionSearch.Unregister("nexus_6_auction"), auction.ref)
      auction.expectMsg(Unregistered)
    }
  }

}

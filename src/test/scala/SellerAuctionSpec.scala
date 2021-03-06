import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}


/**
  * Created by Zuzanna on 31.10.2016.
  */
class SellerAuctionSpec extends TestKit(ActorSystem("SellerSpec"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate
  }

  "An Auction (child)" should {
    "be tested without Seller (parent)" in {
      val probe = TestProbe()
      val auction = system.actorOf(Props(classOf[Auction], "item", probe.ref))

      auction ! Auction.Created
      expectMsg(Auction.OK)
    }
  }

  "A Seller (parent)" should {
    "test Auction's (child) responses" in {
      //      val seller = system.actorOf(Props(new Seller(List("auction1", "auction2", "auction3"))), "seller")

      val auction = TestProbe()
      val seller = system.actorOf(Props(new Seller(List("auction1"))), "seller")

      seller ! Auction.OK
      seller ! Seller.AuctionEnds(false)

      expectMsg(Auction.AuctionEnded)
    }
  }
}

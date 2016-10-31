import akka.actor.{ActorSystem, Props, Scheduler}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Zuzanna on 30.10.2016.
  */
class AuctionSpec extends TestKit(ActorSystem("AuctionSpec"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate
  }

  "An Auction" must {

    val actualBid = 10

    "be created" in {
      val probe = TestProbe()
      val auction = system.actorOf(Props(classOf[Auction], "item", probe.ref))

      auction ! Auction.Created
      expectMsg(Auction.OK)
    }

    "get too small amount of money" in {
      val probe = TestProbe()
      val auction = system.actorOf(Props(classOf[Auction], "item", probe.ref))

      auction ! Auction.Created
      expectMsg(Auction.OK)

      auction ! Buyer.offer(self, 5)
      expectMsg(Auction.NotEnough(actualBid))
    }

    "be beaten" in {
      val probe = TestProbe()
      val auction = system.actorOf(Props(classOf[Auction], "item", probe.ref))

      val newBid = 15
      auction ! Auction.Created
      expectMsg(Auction.OK)

      auction ! Buyer.offer(self, newBid)
      expectMsg(Auction.Beaten(newBid))
    }

    "ends with no buyers" in {
      val probe = TestProbe()
      val auction = system.actorOf(Props(classOf[Auction], "item", probe.ref))

      auction ! Auction.Created
      expectMsg(Auction.OK)

      auction ! Auction.TimeEnd
      expectMsg(Auction.Deleted)
    }

    "ends with buyers" in {
      val probe = TestProbe()
      val auction = system.actorOf(Props(classOf[Auction], "item", probe.ref))

      val newBid1 = 15
      val newBid2 = 20
      val newBid3 = 19
      val newBid4 = 40

      auction ! Auction.Created
      expectMsg(Auction.OK)

      auction ! Buyer.offer(self, 5)
      expectMsg(Auction.NotEnough(actualBid))

      auction ! Buyer.offer(self, newBid1)
      expectMsg(Auction.Beaten(newBid1))

      auction ! Buyer.offer(self, newBid2)
      expectMsg(Auction.Beaten(newBid2))

      auction ! Buyer.offer(self, newBid3)
      expectMsg(Auction.NotEnough(newBid2))

      auction ! Buyer.offer(self, newBid4)
      expectMsg(Auction.Beaten(newBid4))

      auction ! Auction.TimeEnd
      expectMsg(Auction.YouWon("item", newBid4))
    }
  }
}

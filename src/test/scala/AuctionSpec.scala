import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

/**
  * Created by Zuzanna on 30.10.2016.
  */
class AuctionSpec extends TestKit(ActorSystem("AuctionSpec"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender{

  override def afterAll(): Unit = {
    system.terminate
  }

  "An Auction" must {
    "be created" in {
      val auction = system.actorOf(Props(new Auction("hehe")))

      auction ! Auction.Created
      expectMsg(Auction.OK)
    }
  }
}

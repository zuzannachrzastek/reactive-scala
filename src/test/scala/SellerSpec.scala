import akka.actor.{ActorSystem, Props}
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

/**
  * Created by Zuzanna on 30.10.2016.
  */
class SellerSpec extends TestKit(ActorSystem("SellerSpec"))
with WordSpecLike with BeforeAndAfterAll{

  "A Seller (parent)" should {
    "test Auction's (child) responses" in {
      val seller = system.actorOf(Props(new Seller("auction1", "auction2", "auction3")), "seller")


    }
  }
}

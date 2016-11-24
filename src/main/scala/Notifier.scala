import akka.actor.Actor
import akka.event.LoggingReceive
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by Zuzanna on 22.11.2016.
  */

object Notifier {
  case object Init
  case object Done
}

class Notifier extends Actor{

  val auctionPublisher = context.actorSelection("akka.tcp://AuctionSystem@127.0.0.1:2553/user/auctionPublisher")

  def receive = LoggingReceive {
    case Notifier.Init =>
      println("Notifier initiated")

    case Auction.Notify(title, buyer, price) =>
      println("I got the NOTIFY message: ", title, " ", buyer, " ", price)

      implicit val timeout = Timeout(3 seconds)
      val future = auctionPublisher ? AuctionPublisher.Publish(title, buyer, price)
      val result = Await.result(future, timeout.duration)
//      auctionPublisher ! AuctionPublisher.Publish

    case AuctionPublisher.Thanks =>
      println("No problem")

    case Notifier.Done =>
      implicit val timeout = Timeout(3 seconds)
      val future = auctionPublisher ? AuctionPublisher.Done
      val result = Await.result(future, timeout.duration)
      context.system.terminate()
  }

}

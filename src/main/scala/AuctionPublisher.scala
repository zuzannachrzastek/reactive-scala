import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive

/**
  * Created by Zuzanna on 23.11.2016.
  */

object AuctionPublisher{
  case object Init
  case object Done
  case class Publish(title: String, buyer: ActorRef, price: Double)
  case object Thanks
  case object OK
}

class AuctionPublisher extends Actor{
  def receive = LoggingReceive {
    case AuctionPublisher.Init =>
      val notifier = context.actorSelection("akka.tcp://AuctionSystem@127.0.0.1:2552/user/notifier")
      println("AUCTION PUBLISHER INITIATED")

    case AuctionPublisher.Publish(title: String, buyer: ActorRef, price: Double) =>
      println("\n--AUCTION PUBLISHER-- ", title, " ", buyer, " ", price, "\n")
      sender ! AuctionPublisher.Thanks

    case AuctionPublisher.Done =>
      sender ! AuctionPublisher.OK
      context.system.terminate()
  }

}

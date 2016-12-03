import akka.actor.{Actor, ActorRef, Props}
import akka.event.LoggingReceive
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, RoundRobinRoutingLogic, Router}

/**
  * Created by Zuzanna on 03.12.2016.
  */

object MasterSearch {
  case class Register(auctionName: String)
  case class Unregister(auctionName: String)
  case class Terminated(a: ActorRef)
}

class MasterSearch extends Actor {

  val nbOfroutees: Int = 5

  val routees = Vector.fill(nbOfroutees) {
    val r = context.actorOf(Props[AuctionSearch])
    context watch r
    ActorRefRoutee(r)
  }

  var registerRouter = {
    Router(BroadcastRoutingLogic(), routees)
  }

  var searchRouter = {
    Router(RoundRobinRoutingLogic(), routees)
  }

  def receive = LoggingReceive {
    case MasterSearch.Register(name) =>
      registerRouter.route(MasterSearch.Register(name), sender())
      println()

    case MasterSearch.Terminated(a) =>
      registerRouter = registerRouter.removeRoutee(a)
      if(registerRouter.routees.size == 0)
        context.system.terminate()
  }
}

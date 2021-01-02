package org.example.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.example.message.AskedMessageUnitOfWork;
import org.example.message.AssemblingMessage;
import org.example.message.MessageUnitOfWork;
import scala.concurrent.Await;
import scala.concurrent.Future;

/**
 * A Parent Actor is responsible for identifying subtasks
 * to be executed by the worker actors.
 *
 * The Parent actor should have a reference to a pool of actors he can
 * enqueue too.
 *
 * Finally the Parent actor will need be able to tell the worker actors how
 * to forward the completed message to the final build step.
 */
public class ParentActor extends AbstractActor {

  private final boolean useAskPattern;
  private final ActorRef finalActor;
  private final List<ActorRef> nextActors;
  private final Queue<ActorRef> queueOfActorsToSendTo;
  private final Router router;

  public ParentActor(List<ActorRef> nextActors, ActorRef finalActor, boolean useAskPattern) {
    this.nextActors = nextActors;
    this.useAskPattern = useAskPattern;
    this.finalActor = finalActor;

    List<Routee> routeeList = new ArrayList<>();

    // Ask pattern  seems to be quite clumsy.
    queueOfActorsToSendTo = new LinkedList();
    nextActors.forEach( r ->
        {
          routeeList.add(new ActorRefRoutee(r));
          queueOfActorsToSendTo.add(r);
        }
    );

    router = new Router(RoundRobinRoutingLogic.apply(), routeeList);
  }

  /**
   * Factory Way To Create Actor for ActorSystem.
   *
   * @param nextActors The Next actors to send to
   * @param finalActor The ending actor of the topology. A sink.
   * @param useAskPattern Whether or not to use the ask pattern.
   * @return
   */
  public static Props getProps(List<ActorRef> nextActors, ActorRef finalActor, boolean useAskPattern) {
    return Props.create(ParentActor.class, () -> new ParentActor(nextActors, finalActor, useAskPattern));
  }

  @Override
  public Receive createReceive() {
    return ReceiveBuilder
        .create()
        .match(AssemblingMessage.class, this::processAssemblingMessage)
        .build();
  }

  /**
   * Process an message
   * @param message a message to process
   *
   * @throws InterruptedException
   * @throws ExecutionException
   * @throws TimeoutException
   */
  public void processAssemblingMessage(AssemblingMessage message)
      throws InterruptedException, ExecutionException, TimeoutException {

    List<Future> futures = new ArrayList<>();

    for (Integer work: message.getPayload()) {

      // Round Robin func
      if (useAskPattern) {
        MessageUnitOfWork unitOfWork = new AskedMessageUnitOfWork(message::isWorkDone, work, message);
      futures.add(akka.pattern.Patterns.ask(queueOfActorsToSendTo.peek(), unitOfWork, 1000L));
        queueOfActorsToSendTo.add(queueOfActorsToSendTo.poll());
      } else {
        MessageUnitOfWork unitOfWork = new MessageUnitOfWork(message::isWorkDone, work, message);
        router.route(unitOfWork, getSelf());
      }
    }

    // if using ask, then lets wait for all actors to finish their work
    // then finish our "work"
    if (useAskPattern) {
      for (Future f : futures) {
        Await.result(f, scala.concurrent.duration.Duration.apply(10, "seconds"));
      }
      finalActor.tell(message, getSelf());
    }

  }
}

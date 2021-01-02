package org.example.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.example.message.AskedMessageUnitOfWork;
import org.example.message.AssemblingMessage;
import org.example.message.MessageUnitOfWork;

/**
 * A Worker actor does some IO bound logic that takes some time,
 * but it also caches the work it's done. Imagine this worker is doing
 * a get request.
 */
public class WorkerActor extends AbstractActor {

  /**
   * Our fake cache
   */
  Set<Integer> cachedValues = new HashSet<>();

  private ActorRef nextActor;

  public WorkerActor(ActorRef nextActor) {
    this.nextActor = nextActor;
  }

  public static Props getProps(ActorRef nextActor) {
    return Props.create(WorkerActor.class, () -> new WorkerActor(nextActor));
  }

  @Override
  public Receive createReceive() {
    return ReceiveBuilder
        .create()
        .match(AskedMessageUnitOfWork.class, this::processAsk)
        .match(MessageUnitOfWork.class, this::processTell)
        .build();
  }

  /**
   * Process an asked message, and tell the sender some stuff.
   *
   * @param subtask work to be processed.
   * @throws InterruptedException
   */
  private void processAsk(AskedMessageUnitOfWork subtask) throws InterruptedException {
    processUnitOfWork(subtask);

    getSender().tell(subtask, getSelf());
  }

  /**
   * Process a told message, and mark the work as done.
   * If work is done, then refer to final actor.
   *
   * @param subtask
   * @throws InterruptedException
   */
  private void processTell(MessageUnitOfWork subtask) throws InterruptedException {
    processUnitOfWork(subtask);

    subtask.getFullTask().workDone(subtask.getWork());
    if (subtask.isWorkDone.get()) {
      nextActor.tell(subtask.getFullTask(), getSelf());
    }
  }

  /**
   * Common code between ask and tell processing.
   * @param subtask
   * @throws InterruptedException
   */
  private void processUnitOfWork(MessageUnitOfWork subtask) throws InterruptedException {
    if (!cachedValues.contains(subtask.getWork())) {
      Thread.sleep(100);
      cachedValues.add(subtask.getWork());
    }
  }
}

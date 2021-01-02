package org.example.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.routing.Router;
import java.util.function.Consumer;
import java.util.logging.Logger;
import org.example.message.AssemblingMessage;

/**
 * The Final Actor, due to is namesake, is the final actor
 * in our actor topology. He just logs, and he also will consume
 * in its construction a function that we will use as a callback
 * for saying, that this Test Application is complete.
 */
public class FinalActor extends AbstractActor {

  /**
   * Logger for our class
   */
  private static Logger LOGGER = Logger.getLogger(FinalActor.class.getName());

  /**
   * A callback to run on a successful message process
   */
  private final Consumer<Integer> onMessageProcess;

  /**
   * The Number of Processed Messages
   */
  int numberOfProcessedMessages = 0;

  /**
   * Constructor
   *
   * @param onMessageProcess
   */
  public FinalActor(Consumer<Integer> onMessageProcess) {
    this.onMessageProcess = onMessageProcess;
  }

  /**
   * Expose Factory like Akka way to create a Final Actor
   * with a callback
   *
   * @param onMessageProcess callback to process on successful message
   * @return
   */
  public static Props getProps(Consumer<Integer> onMessageProcess) {
    return Props.create(FinalActor.class, () -> new FinalActor(onMessageProcess));
  }

  @Override
  public Receive createReceive() {
    return ReceiveBuilder
        .create()
        .match(AssemblingMessage.class, this::processAssemblingMessage)
        .build();
  }

  /**
   * Method to invoke, when we receive an AssemblingMessage
   *
   * @param message a message to process
   */
  private void processAssemblingMessage(AssemblingMessage message) {
    numberOfProcessedMessages++;
    onMessageProcess.accept(numberOfProcessedMessages);
  }
}

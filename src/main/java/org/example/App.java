package org.example;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.ActorRefRoutee;
import akka.routing.Group;
import akka.routing.Pool;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import org.example.actor.FinalActor;
import org.example.actor.ParentActor;
import org.example.actor.WorkerActor;
import org.example.message.AssemblingMessage;

/**
 * Application Class of Fork, Join, Assemble.
 */
public class App {

  private static final String ACTOR_SYSTEM_TERMINATED_LOG = "Actor System Terminated.";

  private static Logger logger = Logger.getLogger(App.class.getName());

  private static final String ACTOR_SYSTEM_START_LOG = "Starting Fork, Join, Assemble";

  private static final String ACTOR_SYSTEM_NAME = "Fork-Join-Assemble";

  private static Long startTime;

  private static Integer numberOfMessagesEmitted = 100;

  private static ActorSystem actorSystem;

  private static Random random = new Random();

  private static boolean trialIsRunning;

  public static void main(String[] args) throws InterruptedException {
    logger.info(ACTOR_SYSTEM_START_LOG);

    // Run trial without ask, then with ask.
    runTrial(constructTrialAndProvideEntryActor(false));

    while (trialIsRunning) {
      Thread.sleep(100L);
    }
    runTrial(constructTrialAndProvideEntryActor(true));
  }

  public static void runTrial(ActorRef parentActor) {
    for (int i = 0; i < numberOfMessagesEmitted; i++) {
      AssemblingMessage message = createMessage();
      parentActor.tell(message, Actor.noSender());
    }
    startTime = new Date().getTime();
  }
  public static ActorRef constructTrialAndProvideEntryActor(boolean useAsk) {
    trialIsRunning = true;
    actorSystem = ActorSystem.create(ACTOR_SYSTEM_NAME);

    logger.info(String.format("Using Ask Pattern: %s", useAsk));

    actorSystem.registerOnTermination(
        () -> {
          logger.info(ACTOR_SYSTEM_TERMINATED_LOG);
        }
    );

    // Final Actor who just logs.
    ActorRef finalActor = actorSystem.actorOf(
        FinalActor.getProps(App::onSuccessProcessing)
    );

    // Lets Construct some Working Actors that do the actual work
    List<ActorRef> actorRefArrayList = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      ActorRef workerActor = actorSystem.actorOf(
          WorkerActor.getProps(finalActor)
      );
      actorRefArrayList.add(workerActor);
    }

    // The Actor who Forks. He has a reference to a pool
    // of worker actors.
    ActorRef parentActor = actorSystem.actorOf(
        ParentActor.getProps(actorRefArrayList, finalActor, useAsk)
    );

    return parentActor;
  }

  private static AssemblingMessage createMessage() {

    Set<Integer> payload = new HashSet<>();

    for (int i = 0; i < 100; i++) {
      payload.add(random.nextInt(100));
    }

    return new AssemblingMessage(payload);
  }

  public static void onSuccessProcessing(Integer numberOfMessagesProcessed) {

    if (!numberOfMessagesProcessed.equals(numberOfMessagesEmitted)) {
      return;
    }

    Long time = new Date().getTime();
    logger.info("Took This amount of time to process: " + (time - startTime));
    actorSystem.terminate();
    trialIsRunning = false;
  }
}

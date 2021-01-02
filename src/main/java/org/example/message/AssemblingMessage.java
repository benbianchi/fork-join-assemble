package org.example.message;

import java.util.HashSet;
import java.util.Set;

/**
 * A message we are tossing through the actor system.
 * Contains work to be done.
 */
public class AssemblingMessage {
  
  Set<Integer> workToBeDone = new HashSet<>();
  Set<Integer> payload = new HashSet<>();
  
  public AssemblingMessage(Set<Integer> payload){
    this.payload = payload;
    workToBeDone.addAll(payload);
  }


  /**
   * Mark work to be done for message
   *
   * @param work work that was completed
   */
   synchronized public void workDone(Integer work){
    this.workToBeDone.remove(work);
   }

  /**
   * Has this assembling message's work been completed?
   * @return whether or not this message's subwork was done.
   */
  public Boolean isWorkDone() {
    return workToBeDone.isEmpty();
   }

  public Set<Integer> getPayload() {
    return payload;
  }
}

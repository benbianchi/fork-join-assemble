package org.example.message;

import java.util.function.Supplier;

public class MessageUnitOfWork {

  private final Integer unitOfWork;
  public final Supplier<Boolean> isWorkDone;
  private final AssemblingMessage fullTask;

  public MessageUnitOfWork(Supplier<Boolean> isWorkDone, Integer unitOfWork, AssemblingMessage fullTask) {
    this.isWorkDone = isWorkDone;
    this.unitOfWork = unitOfWork;
    this.fullTask = fullTask;
  }

  public Integer getWork() {
    return unitOfWork;
  }

  public AssemblingMessage getFullTask() {
    return fullTask;
  }
}

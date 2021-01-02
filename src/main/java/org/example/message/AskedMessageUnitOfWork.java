package org.example.message;

import java.util.function.Supplier;

/**
 * Defined an Asked Variation of {@link MessageUnitOfWork} to make
 * receive easier.
 */
public class AskedMessageUnitOfWork extends MessageUnitOfWork {

  public AskedMessageUnitOfWork(Supplier<Boolean> isWorkDone,
      Integer unitOfWork, AssemblingMessage fullTask) {
    super(isWorkDone, unitOfWork, fullTask);
  }
}

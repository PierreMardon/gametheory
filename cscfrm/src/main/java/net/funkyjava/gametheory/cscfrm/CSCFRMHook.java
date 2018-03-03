package net.funkyjava.gametheory.cscfrm;

public abstract class CSCFRMHook {

  private final boolean oneTime;

  public CSCFRMHook(final boolean oneTime) {
    this.oneTime = oneTime;
  }

  public boolean isOneTime() {
    return oneTime;
  }

  public abstract void action();
}

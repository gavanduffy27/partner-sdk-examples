package com.genkey.abisclient.examples;

public class StopWatch {

  private static final long NanoSecondsInMilli = 1000000;

  long start = 0;

  public StopWatch() {
    reset();
  }

  public void reset() {
    start = System.nanoTime();
  }

  public long getDurationNano() {
    return System.nanoTime() - start;
  }

  public double getDurationMs() {
    return (double) getDurationNano() / NanoSecondsInMilli;
  }

  public void sleep(long delay) {
    try {
      Thread.sleep(delay);
    } catch (Exception e) {

    }
  }
}

package com.genkey.partner.utils.concurrency;

/**
 * Core interface for stress test class.
 *
 * <p>Note the ConcurrentTaskThread knows how to replicate itself and manages assignment of a shared
 * TaskController.
 *
 * @author Gavan
 */
public interface ConcurrentTaskThread extends Runnable {

  /**
   * Set the controller
   *
   * @param controller
   */
  void setTaskController(TaskController controller);

  /**
   * Generate a new thread instance
   *
   * @param controller
   * @return
   */
  ConcurrentTaskThread generateTaskThread();

  /** Request thread to stop */
  void stop();

  boolean isStopped();

  /**
   * Execute task
   *
   * @param task
   */
  void executeTask(TestTask task);

  /** Wait for task to finish */
  void waitFinish();

  /**
   * Return true if task is finished
   *
   * @return
   */
  boolean isFinished();

  /**
   * Set threadID for display in messages
   *
   * @param threadID
   */
  void setThreadID(long threadID);
}

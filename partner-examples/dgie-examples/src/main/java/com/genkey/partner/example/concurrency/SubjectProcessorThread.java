package com.genkey.partner.example.concurrency;

import com.genkey.abisclient.service.ABISServiceModule;
import com.genkey.abisclient.service.GenkeyABISService;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.partner.example.PartnerExample;
import com.genkey.partner.utils.EnrollmentUtils;
import com.genkey.partner.utils.concurrency.AbstractTestThread;
import com.genkey.partner.utils.concurrency.ObjectWrapperTask;
import com.genkey.partner.utils.concurrency.SubjectListController;
import com.genkey.partner.utils.concurrency.TaskController;
import com.genkey.partner.utils.concurrency.TestTask;
import com.genkey.platform.utils.CollectionUtils;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.FormatUtils;
import java.util.List;

/**
 * Base class for construction of runnable tasks that execute within the TaskController concurrency
 * framework that has been provided.
 *
 * <p>The unit tasks are based on subject task queues specified by numeric-id.
 *
 * <p>The cyclic shift property in combination with the subject-id is used to construct the specific
 * biometric content for each processed task.
 *
 * @author Gavan
 */
public abstract class SubjectProcessorThread extends AbstractTestThread {

  List<Integer> samples;

  String domainName = PartnerExample.getPartnerDomainName();

  boolean nonBlocking = false;

  static boolean simulationMode = false;

  ConcurrencyTest concurrencyTest = null;

  int cyclicShift = 0;

  public SubjectProcessorThread() {}

  public SubjectProcessorThread(
      TaskController controller, List<Integer> samples, String domainName) {
    super.setTaskController(controller);
    this.setSamples(samples);
  }

  public ConcurrencyTest getConcurrencyTest() {
    return concurrencyTest;
  }

  public void setConcurrencyTest(ConcurrencyTest concurrencyTest) {
    this.concurrencyTest = concurrencyTest;
  }

  public static boolean isSimulationMode() {
    return simulationMode;
  }

  public static void setSimulationMode(boolean simulationMode) {
    SubjectProcessorThread.simulationMode = simulationMode;
  }

  public SubjectListController getSubjectController() {
    TaskController controller = this.getTaskController();
    if (controller == null) {
      this.setTaskController(new SubjectListController());
    }
    return (SubjectListController) getTaskController();
  }

  public void initializeSubject(Iterable<Long> subjects) {
    SubjectListController controller = getSubjectController();
    controller.setSubjectList(subjects);
    this.setTaskController(controller);
  }

  public void initializeSubject(long start, int nSubjects, int iterationCount, int step) {
    SubjectListController controller = getSubjectController();
    controller.setSubjectList(start, nSubjects, step);
    controller.setIterationCount(iterationCount);
  }

  public List<Integer> getSamples() {
    if (samples == null) {
      setSample(1);
    }
    return samples;
  }

  public void setSamples(List<Integer> samples) {
    this.samples = samples;
  }

  public void setSample(int sample) {
    this.samples = CollectionUtils.singleValueList(sample);
  }

  public int getCyclicShift() {
    return cyclicShift;
  }

  public void setCyclicShift(int cyclicShift) {
    this.cyclicShift = cyclicShift;
  }

  public String getDomainName() {
    return domainName;
  }

  public void setDomainName(String domainName) {
    this.domainName = domainName;
  }

  @Override
  public SubjectProcessorThread generateTaskThread() {
    SubjectProcessorThread task = null;
    try {
      task = this.getClass().newInstance();
      task.setConcurrencyTest(this.getConcurrencyTest());
      task.setTaskController(this.getTaskController());
      task.setDomainName(this.getDomainName());
      task.setSamples(this.getSamples());
      task.setNonBlocking(this.isNonBlocking());
      task.setCyclicShift(this.getCyclicShift());
    } catch (Exception e) {
      PartnerExample.handleException(e);
    }
    return task;
  }

  @Override
  public void executeTask(TestTask task) {
    ObjectWrapperTask<Number> wrapperTask = (ObjectWrapperTask<Number>) (task);
    long subject = wrapperTask.getValue().longValue();
    executeSubjectTask(subject);
  }

  /**
   * Default handler for a subject-id task that is specified by a number
   *
   * @param subject
   */
  protected void executeSubjectTask(long subject) {
    Thread.yield();
    if (this.isLocked()) {
      this.getConcurrencyTest().waitIfLocked();
    }
    this.incrementCounter();
    this.printMessage("Executing for " + subject);
    for (int sample : this.getSamples()) {
      SubjectEnrollmentReference ref =
          EnrollmentUtils.accessEnrollmentRecord(
              subject, sample, this.getDomainName(), this.getCyclicShift());
      if (isSimulationMode()) {
        simulateTask(subject, sample, this.getDomainName());
      } else {
        executeSubjectTask(ref, subject, sample);
      }
    }
  }

  protected void incrementCounter() {
    this.getConcurrencyTest().incrementCounter();
  }

  /**
   * Wait until lock is released
   *
   * @return
   */
  public boolean waitIfLocked() {
    return waitIfLocked(0, 3000);
  }

  public boolean waitIfLocked(long timeOut, long interval) {
    int maxCount = (int) (timeOut / interval);
    int count = 0;
    boolean exit = false;
    while (isLocked() && !exit) {
      Commons.waitMillis(interval);
      if (maxCount > 0 && count >= maxCount) {
        break;
      }
    }
    return isLocked();
  }

  /**
   * Call this to block all other tasks such as when forcing a system reset
   *
   * @return
   */
  public boolean lockAllThreads() {
    return this.getConcurrencyTest().lockSystem();
  }

  /**
   * Release lock on all threads
   *
   * @return
   */
  public boolean unlockAllThreads() {
    return this.getConcurrencyTest().unlockSystem();
  }

  private boolean isLocked() {
    return this.getConcurrencyTest().isLocked();
  }

  private void simulateTask(long subject, int sample, String domainName) {
    FormatUtils.nl();
    FormatUtils.println(
        Commons.classShortName(this)
            + " simulated test for "
            + subject
            + "@"
            + domainName
            + "/"
            + sample);
  }

  /**
   * Base functional work to be implemented by the subclass
   *
   * @param ref
   * @param subject
   * @param sample
   */
  abstract void executeSubjectTask(SubjectEnrollmentReference ref, long subject, int sample);

  protected GenkeyABISService getABISService() {
    return ABISServiceModule.getABISService();
  }

  public boolean isNonBlocking() {
    return nonBlocking;
  }

  public void setNonBlocking(boolean nonBlocking) {
    this.nonBlocking = nonBlocking;
  }

  @Override
  protected String getMessageContext() {
    return super.getMessageContext() + "::(" + this.getConcurrencyTest().getCounter() + ")";
  }
}

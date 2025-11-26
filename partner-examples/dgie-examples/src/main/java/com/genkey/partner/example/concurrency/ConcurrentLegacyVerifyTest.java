package com.genkey.partner.example.concurrency;

import org.junit.Test;

/**
 * Concurrent verifications against legacy image data
 *
 * @author Gavan
 */
public class ConcurrentLegacyVerifyTest extends ConcurrencyTest {

  private static final int Concurrency = 16;
  private static final int SubjectCount = 20;
  private static final int IterationCount = 50;
  private static final int CyclicShift = 4;

  public static void main(String[] args) {
    ConcurrentLegacyVerifyTest test = new ConcurrentLegacyVerifyTest();
    test.runTestCase4();
  }

  @Override
  protected void setUp() {
    super.setUp();
    super.setConcurrency(Concurrency);
    super.setSubjectCount(SubjectCount);
    super.setIterationCount(IterationCount);
    super.setCyclicShift(CyclicShift);
    super.checkLegacySubjectsExist();
  }

  @Override
  protected void tearDown() {
    super.tearDown();
    super.deleteCreatedLegacySubjects();
  }

  @Override
  protected void runAllExamples() {
    stressTest();
  }

  @Test
  public void stressTest() {
    LegacyVerifyThread taskThread = new LegacyVerifyThread();
    taskThread.setCyclicShift(CyclicShift);
    super.executeThreadTest(taskThread);
  }
}

package com.genkey.partner.example.concurrency;

import org.junit.Test;

/**
 * Concurrent biometric image fetch tests
 *
 * @author Gavan
 */
public class ConcurrencyFetchTest extends ConcurrencyTest {

  public static final int Concurrency = 10;
  public static final int SubjectCount = 10;
  public static final int IterationCount = 20;
  private static final int CyclicShift = 5;
  private static final int StartSubject = 1020;

  public static void main(String[] args) {
    ConcurrencyFetchTest test = new ConcurrencyFetchTest();
    test.runTestCase4();
  }

  @Override
  protected void setUp() {
    super.setUp();
    super.setStartSubject(StartSubject);
    super.setConcurrency(Concurrency);
    super.setSubjectCount(SubjectCount);
    super.setIterationCount(IterationCount);
    super.setCyclicShift(CyclicShift);
    super.checkSubjectsExist();
  }

  @Override
  public void tearDown() {
    super.tearDown();
    super.deleteCreatedSubjects();
  }

  @Override
  protected void runAllExamples() {
    stressTest();
  }

  @Test
  public void stressTest() {
    ImageFetchThread taskThread = new ImageFetchThread();
    super.executeThreadTest(taskThread);
  }
}

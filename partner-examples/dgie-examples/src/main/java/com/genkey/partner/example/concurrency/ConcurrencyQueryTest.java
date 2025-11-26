package com.genkey.partner.example.concurrency;

import org.junit.Test;

/**
 * Concurrent biometric query tests
 *
 * @author Gavan
 */
public class ConcurrencyQueryTest extends ConcurrencyTest {

  private static final int QueryConcurrency = 10;
  private static final int QuerySubjectCount = 20;
  private static final int QueryIterationCount = 25;
  private static final int StartSubject = 1050;
  private static final int CyclicShift = 5;

  public static void main(String[] args) {
    ConcurrencyQueryTest test = new ConcurrencyQueryTest();
    test.runTestCase4();
  }

  @Override
  protected void setUp() {
    super.setUp();
    super.setStartSubject(StartSubject);
    super.setConcurrency(QueryConcurrency);
    super.setSubjectCount(QuerySubjectCount);
    super.setIterationCount(QueryIterationCount);
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
    QueryTestThread taskThread = new QueryTestThread();
    super.executeThreadTest(taskThread);
  }
}

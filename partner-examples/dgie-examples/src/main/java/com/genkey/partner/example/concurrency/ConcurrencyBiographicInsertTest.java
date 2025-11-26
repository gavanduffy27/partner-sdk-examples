package com.genkey.partner.example.concurrency;

import org.junit.After;
import org.junit.Test;

/**
 * Concurrent biographic insert test
 *
 * @author Gavan
 */
public class ConcurrencyBiographicInsertTest extends ConcurrencyTest {

  private static final int Concurrency = 4;
  private static final int SubjectCount = 40;
  private static final int IterationCount = 4;

  private static final int CyclicShift = 2;

  public static void main(String[] args) {
    ConcurrencyBiographicInsertTest test = new ConcurrencyBiographicInsertTest();
    test.runTestCase4();
  }

  @Override
  protected void setUp() {
    super.setUp();
    super.setConcurrency(Concurrency);
    super.setSubjectCount(SubjectCount);
    super.setIterationCount(IterationCount);
    super.setCyclicShift(CyclicShift);
    deleteBiographicSubjects();
    //		super.checkBiographicSubjectsExist();
  }

  @Override
  protected void runAllExamples() {
    stressTest();
  }

  @Test
  public void checkSubjectsDeleted() {
    for (int ix = 0; ix < 2; ix++) {
      setCyclicShift(ix);
      deleteBiographicSubjects();
    }
  }

  @Test
  public void stressTest() {
    BiographicInsertThread taskThread = new BiographicInsertThread();
    super.executeThreadTest(taskThread);
  }

  @After
  public void tearDown() {
    super.checkBiographicSubjectsDeleted();
  }
}

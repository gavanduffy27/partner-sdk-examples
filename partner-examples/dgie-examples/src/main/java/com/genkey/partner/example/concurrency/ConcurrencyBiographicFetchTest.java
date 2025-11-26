package com.genkey.partner.example.concurrency;

import org.junit.After;
import org.junit.Test;

/**
 * Pregenerates a set of subjects and then performs concurrent fetching
 *
 * @author Gavan
 */
public class ConcurrencyBiographicFetchTest extends ConcurrencyTest {
  /*
  public static final int Concurrency = 6;
  public static final int SubjectCount =20;
  public static final int IterationCount=5;
  */
  public static final int Concurrency = 4;
  public static final int SubjectCount = 20;
  public static final int IterationCount = 20;

  private static final int CyclicShift = 2;

  public static void main(String[] args) {
    ConcurrencyBiographicFetchTest test = new ConcurrencyBiographicFetchTest();
    test.runTestCase4();
  }

  @Override
  protected void setUp() {
    super.setUp();
    super.setConcurrency(Concurrency);
    super.setSubjectCount(SubjectCount);
    super.setIterationCount(IterationCount);
    super.setCyclicShift(CyclicShift);
    super.deleteBiographicSubjects();
    super.checkBiographicSubjectsExist();
  }

  @Override
  protected void runAllExamples() {
    stressTest();
  }

  @Test
  public void stressTest() {
    BiographicFetchThread taskThread = new BiographicFetchThread();
    super.executeThreadTest(taskThread);
  }

  @After
  public void tearDown() {
    super.checkBiographicSubjectsDeleted();
  }
}

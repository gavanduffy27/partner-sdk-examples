package com.genkey.partner.example.concurrency;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Concurrent biometric enrol tests
 *
 * @author Gavan
 */
public class ConcurrencyEnrolTest extends ConcurrencyTest {

  /*
  static int TestConcurrency=4;
  static int startSubject=1000;
  static int nSubjects=100;
  */
  static int TestConcurrency = 8;
  static int startSubject = 1000;
  static int nSubjects = 100;

  boolean tearDownRequired = false;

  public static void main(String[] args) {
    ConcurrencyEnrolTest test = new ConcurrencyEnrolTest();
    test.setConcurrency(TestConcurrency);
    test.runTestCase4();
    //		test.runTestExamples();
  }

  @BeforeClass
  public static void initTests() {}

  @Override
  protected void setUp() {
    super.setUp();
    super.setConcurrency(TestConcurrency);
    super.setStartSubject(startSubject);
    super.setSubjectCount(nSubjects);
    super.checkSubjectsNotPresent();
    tearDownRequired = true;
  }

  public void tearDown() {
    super.tearDown();
    if (tearDownRequired) {
      super.deleteCreatedSubjects();
      tearDownRequired = false;
    } else {
      println("Second call to tearDown");
    }
  }

  @Override
  protected void runAllExamples() {
    stressTest();
  }

  @Test
  public void stressTest() {
    EnrollmentTestThread taskThread = new EnrollmentTestThread();
    super.executeThreadTest(taskThread);
  }
}

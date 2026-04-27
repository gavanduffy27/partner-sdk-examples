package com.genkey.partner.workshop;

public class QueryWithBiographicTest extends IncrementalEnrolTests {

  public static void main(String[] args) {
    QueryWithBiographicTest test = new QueryWithBiographicTest();
    test.processCommandLine(args);
  }

  @Override
  protected void runAllExamples() {
    queryWithBiographics();
  }

  public void queryWithBiographics() {
	  super.performQuery(TestSubjectID, EnrollmentStep.Face, true);
  }
}

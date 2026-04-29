package com.genkey.partner.workshop;

public class QueryWithBiographicTest extends IncrementalEnrolTests {

  public static void main(String[] args) {
    QueryWithBiographicTest test = new QueryWithBiographicTest();
    test.processCommandLine(args);
  }

  @Override
  protected void runAllExamples() {
    queryWithBiographics();
    insertIfNoDuplicates();
  }

  public void insertIfNoDuplicates() { // TODO Auto-generated method stub
//    super.insertIfNoDuplicates(TestSubjectID, EnrollmentStep.Face, true);
    super.insertIfNoDuplicates(TestSubjectID2, EnrollmentStep.Face, true);
  }

  public void queryWithBiographics() {
//    super.performQuery(TestSubjectID, EnrollmentStep.Face, true);
    super.performQuery(TestSubjectID2, EnrollmentStep.Face, true);
  }
}

package com.genkey.abisclient.examples.transport;

import com.genkey.abisclient.examples.ExampleSuite;

public class TransportTestSuite extends ExampleSuite {

  @Override
  protected void initExampleSuite() {
    this.addExample(new FingerEnrollmentExample());
    this.addExample(new SubjectEnrollmentExample());
    this.addExample(new AuthenticationExample());
  }
}

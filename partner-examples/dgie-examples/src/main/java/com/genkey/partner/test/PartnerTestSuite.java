package com.genkey.partner.test;

import com.genkey.platform.test.framework.GKTestSuite;

public class PartnerTestSuite extends GKTestSuite {

  public PartnerTestSuite(String name) {
    super(name);
  }

  @Override
  protected void addTests() {
    this.addTestSuite(ErrorConnectionTests.class);
  }
}

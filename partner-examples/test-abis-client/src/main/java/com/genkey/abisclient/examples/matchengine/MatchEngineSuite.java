package com.genkey.abisclient.examples.matchengine;

import com.genkey.abisclient.examples.ExampleSuite;

public class MatchEngineSuite extends ExampleSuite {

  @Override
  protected void initExampleSuite() {
    this.addExample(new MatchEngineConfigTests());
    this.addExample(new MatchEngineLoadExample());
    this.addExample(new MatchEngineSearchExample());
    this.addExample(new SingleFingerSearchExample());
  }
}

package com.genkey.abisclient.examples.matchengine;

import com.genkey.abisclient.examples.ExampleModule;
import com.genkey.abisclient.matchengine.LocalMatchEngine;
import com.genkey.abisclient.matchengine.MatchEngineConfiguration;
import com.genkey.abisclient.matchengine.MatchResult;
import com.genkey.abisclient.matchengine.MatchResult.MatchResultDetail;
import com.genkey.abisclient.matchengine.Subject;
import java.util.List;

public abstract class MatchEngineExample extends ExampleModule {

  protected static void showConfiguration() {
    PrintMessage(LocalMatchEngine.getDescriptionConfig());
  }

  protected static void showConfiguration(MatchEngineConfiguration config) {
    PrintMessage(config.toString());
  }

  public void showResults(Subject probeSubject, List<MatchResult> results, boolean showDetails) {
    PrintHeader("Match results for " + probeSubject.getSubjectID());
    int index = 1;
    for (MatchResult result : results) {
      printIndexResult("Result", index++, result);
      if (showDetails) {
        int matchIndex = 1;
        for (MatchResultDetail detail : result.getMatchDetails()) {
          printIndexResult("FingerPair", matchIndex++, detail);
        }
      }
    }
  }
}

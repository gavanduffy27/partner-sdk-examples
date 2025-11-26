package com.genkey.abisclient.examples.matchengine;

import com.genkey.abisclient.matchengine.LocalMatchEngine;
import com.genkey.abisclient.matchengine.MatchResult;
import com.genkey.abisclient.matchengine.Subject;
import com.genkey.abisclient.matchengine.Subset;
import java.util.List;

public class SingleFingerSearchExample extends MatchEngineExample {

  @Override
  protected void runAllExamples() {
    testSingleFingerMatchAndEnroll();
  }

  public void testSingleFingerMatchAndEnroll() {
    int[] fingers = {3};
    testMatchAndEnrol(1, 10, fingers);
  }

  /// <summary>
  ///     performs a simulated enrolment using sample (1) for specified subjects and finger profile
  ///     followed by an iterated match on a second set that is generated based on sample(2) for
  // same
  ///     subjects and finger profile
  /// </summary>
  /// <param name="start">First subject</param>
  /// <param name="last">Last subject</param>
  /// <param name="fingers">Fingers to be used for enrolment</param>
  public void testMatchAndEnrol(long start, long last, int[] fingers) {
    LocalMatchEngine engine = new LocalMatchEngine();
    Subset cacheSubset = new Subset();
    engine.setSubset(cacheSubset);

    // see below
    doSimulatedEnrolment(engine, start, last, fingers, 1, false);

    // load testSubset for the same subjects and fingers but using sample-2
    Subset testSubset = MatchEngineTestUtils.loadSubset(start, last, fingers, 2, false);
    int nSubjects = engine.getSubset().size();

    // begin interation
    testSubset.beginIteration();
    while (testSubset.hasNextSubject()) {
      Subject testSubject = testSubset.nextSubject();
      List<MatchResult> results = engine.findDuplicates(testSubject, 60, 10, true);
      if (results.size() > 0) {
        // we expect a match for each subject
        showResults(testSubject, results, true);
      }
    }
  }

  /// <summary>
  /// Performs a simulated enrolment using specified subject range, fingers and sample index
  /// </summary>
  /// <param name="engine"></param>
  /// <param name="start"></param>
  /// <param name="last"></param>
  /// <param name="fingers"></param>
  /// <param name="sampleIndex"></param>
  /// <param name="useCache">if true then allow the subset to be saved and accessed from file-based
  // cache</param>
  /// <returns></returns>
  public Subset doSimulatedEnrolment(
      LocalMatchEngine engine,
      long start,
      long last,
      int[] fingers,
      int sampleIndex,
      boolean useCache) {

    // first load the subsets using simulated enrolment, but with no interaction on match engine
    Subset subset = MatchEngineTestUtils.loadSubset(start, last, fingers, sampleIndex, useCache);

    // Initialize a new iteraction
    subset.beginIteration();

    // iterate through subjects, matching first against the match-engine and addint to subset if not
    // present
    // for normal testing no matches are expected
    while (subset.hasNextSubject()) {
      Subject subject = subset.nextSubject();
      List<MatchResult> results = engine.findDuplicates(subject, 60, 10, true);
      if (results.size() > 0) {
        // really not expected
        showResults(subject, results, true);
      } else {
        // normal case - load the subject into match-engine after duplicate search
        engine.getSubset().addSubject(subject);
      }
    }
    return subset;
  }
}

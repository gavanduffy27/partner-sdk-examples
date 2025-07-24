package com.genkey.abisclient.examples.matchengine;

import java.util.ArrayList;
import java.util.List;

import com.genkey.abisclient.matchengine.LocalMatchEngine;
import com.genkey.abisclient.matchengine.MatchEngineConfiguration;

import com.genkey.abisclient.matchengine.MatchEngineConfiguration;
import com.genkey.abisclient.matchengine.MatchEngineEnums.GKMatcherSwitchParameter;
import com.genkey.abisclient.matchengine.MatchEngineEnums.GKMatcherThresholdParameter;
import com.genkey.abisclient.matchengine.MatchResult;
import com.genkey.abisclient.matchengine.Subject;
import com.genkey.abisclient.matchengine.Subset;

public class MatchEngineSearchExample extends MatchEngineExample {

	static boolean UseCache=false;
	
	@Override
	protected void runAllExamples() {
		simpleDetectionTest();
		doDuplicateDetectionTest();
		testLargeScaleMatch();
		testSubjectIteration();
		/*
		simpleCrashTest();		
		simpleDetectionTest();
		testSubjectIteration();
		testLargeScaleMatch();
		*/
	}
	
	/*
	@Override
	protected void setUp() {
		super.showConfiguration();
		MatchEngineConfiguration.getInstance().setUseStandardConfig(true);
	        MatchEngineConfiguration config = LocalMatchEngine.getConfiguration();		
	        boolean useLog10 = config.getSwitchParameter(GKMatcherSwitchParameter.GKSwitchUseLog10ScoreScheme);
	        printResult("useLog10", useLog10);
	        config.setSwitchParameter(GKMatcherSwitchParameter.GKSwitchUseLog10ScoreScheme, true);
	        useLog10 = config.getSwitchParameter(GKMatcherSwitchParameter.GKSwitchUseLog10ScoreScheme);
	        printResult("useLog10 2", useLog10);
	}
	*/

	public void simpleCrashTest() {
	        LocalMatchEngine engine = new LocalMatchEngine();
        Subset subset = MatchEngineTestUtils.loadSubset(1,4,MatchEngineTestUtils.DefaultFingers, 1, false);
        subset.clear();
	}	

	
	public void simpleDetectionTest() {
		simpleDetectionTest(4, 0, false);
	}
	


    public void simpleDetectionTest(int nSubjects , int nClones , boolean usePositionIndependent)
    {
        simpleDetectionTest(MatchEngineTestUtils.DefaultFingers, nSubjects, nClones, usePositionIndependent);
    }

    public void simpleDetectionTest(int[] fingers, int nSubjects, int nClones, boolean usePositionIndependent)
    {
        int firstSubject = 1;
        int[] testFingers = fingers; // MatchEngineTestUtils.DefaultFingers;
        LocalMatchEngine engine = new LocalMatchEngine();
        Subset subset = MatchEngineTestUtils.loadSubset(firstSubject, firstSubject + nSubjects - 1, fingers, 1, UseCache);
        engine.setSubset(subset);

        if (nClones > 0)
        {
            Subject matchSubject = MatchEngineTestUtils.enrolSubject(10, fingers, 2);
            MatchEngineTestUtils.extendSubsetWithClones(subset, matchSubject, 1000, nClones);
        }

        LocalMatchEngine.setThresholdParameter(GKMatcherThresholdParameter.GKThresholdPrefilterTriggerSize, 1);

        //int[] fingers = { 3, 4, 7, 8 };
        int sampleIndex = 2;
        for (int ix = 0; ix < nSubjects ; ix++)
        {
            int index = firstSubject + ix;
            Subject testSubject = MatchEngineTestUtils.enrolSubject(index, testFingers, sampleIndex);
            List<MatchResult> results1 = engine.findDuplicates(testSubject, 60, 3, usePositionIndependent);
            showResults(testSubject, results1, true);
        }
    }


    public void doDuplicateDetectionTest()
    {

        LocalMatchEngine engine = new LocalMatchEngine();
        Subset subset = MatchEngineTestUtils.loadSubset(1, 10, 1, UseCache);
        engine.setSubset(subset);

        int [] fingers = {3,4,7,8};
        int sampleIndex=2;
        for (int ix = 1; ix < 10; ix++)
        {
            Subject testSubject = MatchEngineTestUtils.enrolSubject(ix, fingers, sampleIndex);
            List<MatchResult> results1 = engine.findDuplicates(testSubject, 60, 3, false);
            showResults(testSubject, results1, true);

        }
    }


    public void testSubjectIteration()
    {
        Subset subset = MatchEngineTestUtils.loadSubset(1, 10, 1, UseCache);

        // Must make this call to initialise the iteration. 
        subset.beginIteration();
        int index=0;
        List<String> subjectNames = new ArrayList<String>();

        // Loop condition checks for whether any more subjects available
        while (subset.hasNextSubject())
        {
            // Access next subject in the iteration
            Subject subject = subset.nextSubject();
            printIndexResult("Iterated Subject", index++, subject.getSubjectID());

            // Store the name for next part of the example.
            subjectNames.add(subject.getSubjectID());
        }

        // Alternatively we can retrieve by name.  Requires that you know the names
        // up front to use these for an iteration
        index = 0;
        for(String subjectName :subjectNames)
        {
            Subject subject = subset.getSubjectByName(subjectName);
            printIndexResult("Lookup Subject", index++, subject.getSubjectID());
        }
    
    }


    public void testLargeScaleMatch()
    {
        testLargeScaleMatch(100);
    }

    public  void testLargeScaleMatch(int nClones)
    {
        LocalMatchEngine engine = new LocalMatchEngine();

        // Set this to use multiple phase matching (default is just minutia and is slow)
        LocalMatchEngine.setSwitchParameter(GKMatcherSwitchParameter.GKSwitchUsePrefilter, true);

        // Set the score schema to use -10log10(FAR).  A score of 60 equates to a FAR of 1E-6.  
        // A threshold of 50 equates to an FAR of 1E-5. Scale is continuous.
        LocalMatchEngine.setSwitchParameter(GKMatcherSwitchParameter.GKSwitchUseLog10ScoreScheme, true);

        // If there are less than 20 subjects the prefilter is not used, but all subjects are examined
        // with the slower more accurate minutia matcher
        LocalMatchEngine.setThresholdParameter(GKMatcherThresholdParameter.GKThresholdPrefilterTriggerSize, 20);

        // Default setting is that no more than 1% of subjects examined from first stage will be promoted to subsequent matching
        LocalMatchEngine.setThresholdParameter(GKMatcherThresholdParameter.GKThresholdPrefilterEfficiency, 0.01f);

        // Absolute cap on the number of candidate subjects that are permitted to be examined on the second stage
        LocalMatchEngine.setThresholdParameter(GKMatcherThresholdParameter.GKThresholdPrefilterMaxResults, 50);

        // Only subjects with a FAR score of lower than 1E-6 will be designated as duplicates 
        LocalMatchEngine.setThresholdParameter(GKMatcherThresholdParameter.GKThresholdFARScore, 60);

        showConfiguration();


        Subset subset = MatchEngineTestUtils.loadSubset(1, 10, 1, UseCache);
        engine.setSubset(subset);

        //int nClones = 5000;

        // Now we add 12000 subjects to our subset
        int[] fingers = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        //int[] fingers2 = { 3, 4, 7, 8 };
        int [] fingers4 = MatchEngineTestUtils.DefaultFingers;
        Subject matchSubject = MatchEngineTestUtils.enrolSubject(1, fingers4, 1);
        MatchEngineTestUtils.extendSubsetWithClones(subset, matchSubject, 1000, nClones);
        PrintMessage("Database is loaded");

        //int [] fingers2 = { 3, 4, 7, 8 };
        int sampleIndex = 2;
        for (int ix = 2; ix < 10; ix++)
        {
            Subject testSubject = MatchEngineTestUtils.enrolSubject(ix, fingers4, sampleIndex);
            List<MatchResult> results = engine.findDuplicates(testSubject, 60, 3, true);
            showResults(testSubject, results, true);
        }

    }
	

}

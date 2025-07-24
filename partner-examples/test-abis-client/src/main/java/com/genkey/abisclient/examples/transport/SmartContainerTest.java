package com.genkey.abisclient.examples.transport;

import java.util.List;
import java.util.Map;

import com.genkey.abisclient.Enums.GKXmlSchema;
import com.genkey.abisclient.examples.utils.ExampleSettings;
import com.genkey.abisclient.transport.FingerEnrollmentReference;
import com.genkey.abisclient.transport.SubjectEnrollmentReference;
import com.genkey.abisclient.transport.SubjectEnrollmentReference.ReferenceConsistencyState;
import com.genkey.abisclient.transport.SubjectEnrollmentReference.ReferenceMatchValues;
import com.genkey.platform.utils.Commons;
import com.genkey.platform.utils.FileUtils;

/**
 * Use of SubjectEnrollmentReference as smart container configured by quality threshold and target sample count and used
 * as a control object for data driven enrollment.
 * @author Gavan
 *
 */
public class SmartContainerTest extends TransportExampleModule{
	
	@Override
	protected void runAllExamples() {
		internalMatchTest();
		incrementalEnrolmentExample();
	}
	
	
    public void incrementalEnrolmentExample() {
    	ExampleSettings settings = ExampleSettings.getInstance();
    	int qualityThreshold = settings.getQualityThreshold();
    	int targetSampleCount = settings.getTargetSampleCount();
    	int testSubject=1;
    	int [] fingers = Commons.generateRangeV(3,4,7,8);
    	incrementalEnrolmentExample(testSubject, fingers, qualityThreshold, targetSampleCount);
	}


	private void incrementalEnrolmentExample(int testSubject, int[] fingers, int qualityThreshold, int targetSampleCount)
    {
        SubjectEnrollmentReference subjectReference = new SubjectEnrollmentReference(testSubject);
        subjectReference.setQualityThreshold(qualityThreshold);
        subjectReference.setTargetSampleCount(targetSampleCount);
        subjectReference.setTargetFingers(fingers);

        int maxSamples = 5;
        int sampleIndex = 0;

        // Start the enrollment terminating when the Subject Reference is complete or if we have no more samples available
        showEnrollmentReference("InitialState", subjectReference);

        while (!subjectReference.isComplete() && sampleIndex < maxSamples)
        {
            sampleIndex++;

            int[] requiredFingers = subjectReference.getFingersIncomplete();

            printResult("Required Fingers", requiredFingers);

         
            super.enrollSubjectBlob(subjectReference, requiredFingers, sampleIndex, false, true);
            printObject("Subject reference", subjectReference.printReferenceState());
        }

        if (sampleIndex < 5)
        {
            // One more for good luck
            sampleIndex++;
            enrollSubjectBlob(subjectReference, fingers, sampleIndex, false, true);
        }

        PrintHeader("Post enrollment processing");
        PrintMessage("Sort the references");
        subjectReference.sortReferences();
        printObject("Sorted Subject reference", subjectReference.printReferenceState());

        PrintMessage("Purge unwanted low quality samples only ");
        subjectReference.purgeSamples(false);
        printObject("Purged Quality Subject reference", subjectReference.printReferenceState());

        PrintMessage("Purge so that we strictly do not exceed target sample count for any finger");
        subjectReference.purgeSamples(true);
        printObject("Purged Strict Subject reference", subjectReference.printReferenceState());

        PrintMessage("Export as XML document");
        String xmlContent = subjectReference.toXml(GKXmlSchema.ABIS4);
        String testFile = getTestFile("incrementalEnrolment_" + targetSampleCount, "xml");
        PrintMessage("Saving XML in schema " + GKXmlSchema.ABIS4 + " to file " + testFile);
        try {
        	FileUtils.stringToFile(xmlContent, testFile);
        } catch (Exception e) {
        	super.handleException(e);
        }
    }

	private void showEnrollmentReference(String header, SubjectEnrollmentReference subjectReference) {
		printObject(header, subjectReference.printReferenceState());
	}

	public void internalMatchTest() {
		internalMatchTest(true);
	}
	
    public void internalMatchTest(boolean applyFix)
    {
        int []fingers = { 3, 4, 7, 8 };
        int subjectId = 1;
        SubjectEnrollmentReference subjectReference = new SubjectEnrollmentReference(String.valueOf(subjectId));
        SubjectEnrollmentReference.setDefaultConsistencyThresholds(60, 60);
        subjectReference.setTargetFingers(fingers);
        subjectReference.setTargetSampleCount(2);
        performCheckedEnrollment(subjectReference, 1, 1, applyFix); 
    }

    private void performCheckedEnrollment(SubjectEnrollmentReference subjectReference, int shift, int startSample, 
    		boolean applyConflictFix)
    {
        int [] fingers = subjectReference.getTargetFingers();
        int subjectId = Integer.parseInt(subjectReference.getSubjectID());

        int qualityThreshold = subjectReference.getQualityThreshold();
        int targetSampleCount = subjectReference.getTargetSampleCount();
        int maxSamples = 5;

        int sampleIndex = 1;
        while (!subjectReference.isComplete() && sampleIndex <= maxSamples)
        {
            printHeader("Initiating sample capture for sample " + sampleIndex, '#');
            printObject("Subject state [" + sampleIndex + "]", subjectReference.printReferenceState());
            int [] fingersRequired = subjectReference.getFingersIncomplete();


            //		enrollSubjectBlob(subjectReference, fingersRequired, sampleIndex);
            //foreach(int finger, fingersRequired) {
            for (int ix = 0; ix < fingersRequired.length; ix++)
            {
                int finger = fingersRequired[ix];
                FingerEnrollmentReference fingerReference = generateFingerBlob(subjectId, finger, sampleIndex, true, false);
                if (shift > 0 && sampleIndex > startSample)
                {

                    int newFinger = shiftFinger(ix, fingersRequired, shift++);
                    fingerReference.setFingerID(newFinger);
                    int checkFinger = fingerReference.getFingerID();
                    PrintMessage("Finger assigned from " + finger + " to " + newFinger + " and read back as " + checkFinger);
                }
                if (!subjectReference.addChecked(fingerReference)) {
                    printObject("Subject State", subjectReference.printReferenceState());
                    printHeader("Add checked failure for finger " + finger + " ==> " + fingerReference.getFingerID());

                    // Access state information
                    ReferenceConsistencyState matchState = subjectReference.checkReferenceConsistent(fingerReference); 

                    // Show consistency information	
                    printResult("Consistent", matchState.isConsistent());
                    printResult("Self Consistent", matchState.isSelfConsistent());
                    
                    if (! matchState.isSelfConsistent()) {
                    	// Therefore we failed to match on at least one existing sample
                    	List<Integer> sampleMatches = matchState.getMatchConflictSamples();                    	
                        printResult("Unexpected sample match failures", sampleMatches);
                    }
                    
                    // See if there were any matches against other fingers
                    List<Integer> matchFingers = matchState.getMatchFingers();
                    if (matchFingers.size()> 0) {
                        printResult("Unexpected Matched Fingers", matchFingers);                    	
                    }


                    // Access match value data for the inconsistent reference
                    ReferenceMatchValues matchValues = subjectReference.checkReferenceMatchValues(fingerReference); 

                    boolean consistent = matchValues.isConsistent();
                 
                	if (! consistent) {

                		// Match scores for existing samples in storage sequence
                		List<Integer> sampleMatchScores = matchValues.getSampleMatchScores();

                		//Match scores across all fingers present
                		Map<Integer, Integer> fingerScoreMap = matchValues.getFingerScoreMap();

                		printResult("Sample size/ MatchFinger count", sampleMatchScores.size()+ "/" + fingerScoreMap.size());
                		printResult("Self Consistent", matchValues.isSelfConsistent());
                		printResult("Sample scores", sampleMatchScores);

                		printObject("Finger score map", fingerScoreMap);

                		if (applyConflictFix)
                		{
                			printObject("Before Fix", subjectReference.printReferenceState());
                			subjectReference.removeInconsistentDependencies(fingerReference);
                			printObject("After Fix", subjectReference.printReferenceState());
                		}
                	}


                }
                else {
                    PrintMessage("Successfully added sample for " + fingerReference.getFingerID());
                }
            }
            sampleIndex++;
        }

        subjectReference.purgeSamples(true);

    }

    private static int shiftFinger(int index, int[] fingers, int shift)
    {
        int newIndex = (index + shift) % fingers.length;
        return fingers[newIndex];
    }    
	

}

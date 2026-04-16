# List of changes

	-	Extended CaptureResult to MultipleFingerCaptureResult for calls to capture multiple finger images
	-	Added captureMultiple to FingerprintScanner interface
	-	Added additional call to CaptureService for captureMultipleFingerImage
	- 	renamed previous implementation of captureMultipleFingers to captureMultipleFingersOld
	-   re-implemented captureMultipleFingers to use above extensions alongside SDK image segmentation

	-	CaptureUtils - holds static functions which could be a simple method of the CaptureResult class
	-	FingerprintDataUtils - holds static function(s) that could be a simple method of FingerprintData class
	-	EnrolmentUtils - holds a set of helper routines for ingesting the following object types into an instance of SubjectEnrolmentReference.  We could do this as a decorated class which would be easier to use.

## Observations

 
The functions for consuming CaptureResult and MultipleCaptureResult into SubjectEnrolmentReference can
be managed at component layer. This has following impacts:
 	
 	- this logic becomes more accessible for unit tests
 	- the code at the service layer is streamlined and simplified
 	- subjectEnrolmentReference is more easily managed as a durable session object that 
 	  	  can be maintained over multiple calls that implement a scenario
 	
 There is some level of logical equivalence between:
 
 	- CaptureResult (single finger)
 	- FingerprintData
 	- FingerEnrolmentReference
 
The initial approach of segmenting MultipleFingerCaptureResult into a collection of CaptureResult is pragmatic
    as a least change to what was there before.  However it is not a good strategic solution.
    
 -  Instead for now I have created a module EnrolmentUtils	
 
 -	There is no control loop in the initial implementation relating to the declarative targets of the 
    SubjectEnrolmentReference.  It is easier to do this after the initial refactoring
    
    
 
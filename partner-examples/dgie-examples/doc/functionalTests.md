# Functional tests

## Prerequisites

Check server connectivity

-	runSimpleTest  [ no args ]

Check access to test data

-	runDiagnosticTest testConfigAccess


## Functional test configuration and domains

##Managing configuration settings from a file

Note partnerExample.ini

<code>
abis.partner.hostName=10.22.74.51

abis.partner.hostName.legacy=10.22.74.51

abis.partner.hostName.dr=10.22.74.51

abis.partner.port=8091

abis.partner.domainName=EnrollmentSDK
</code>		

Most important is to set the domainName for the test.

The use of partnerExample.ini is a default configuration file but is subject to source control

Better is to use a private location and use the runtime VM args to set a property as in:

<code>
	-Dabis.partner.settings_file=MyFileName.ini
</code>

Or easier to set a global environment variable as in:

	setP ABIS_PARTNER_SETTINGS_FILE=./test/myFile.ini


Multiple test scenarios can be run in different domains


## Test sequence for roundtrip on a domain

-	runFunctionalTest  enrollExample
-	runFunctionalTest  verifyExample	
-	runFunctionalTest  identifyExample	
-	runFunctionalTest  deleteSubjectExample

Cleanup command:
-	runFunctionalTest  deleteDomainOnly
 	
Note the last test can also be run first if there is a need to clean up from a previous test

## Limitations on the delete function

The system delete function is not intended for reuse.  It is provided as a once-only cancellation of a previous
enrolment.  The limitations are based on security and data-integrity considerations, since there can be a history
of duplicates for a deleted subject and this history is not intended to be forgotten.  What the DELETE function
does is to erase all biometric data for a subject from the system such that it cannot be matched or verified.

There is therefore no hard delete function that eliminates all state.

However in test-mode the system will permit a future INSERT on a deleted subject which works 
by transforming the INSERT to an UPDATE and re-establishing biometric data.  From the external system
this appears to have the effect of hard-delete followed by reinsertion.

However the INSERT of a deleted subject is not calling the true INSERT function and is not appropriate
for formal tests.  

For strict testing it is best to delete the old domain, and perform the next round of tests in a new
test-domain



 	


# Workshop tests

## Next steps

	1	Prepare to make code example environment runnable
	2	Review the code examples
	3	Run the code examples
	4	For each use case :
		- analyse how we integrate in BMS
		- perform the BMS integration
		
		

# Preparation tasks for Code examples

	1	Access to sources    ** DONE **
	2	Installation and access to native libraries  ** DONE **
	3	Installation and access of Java Jar libraries from maven
	4	Access to test-data from ist_config.zip
	5	Access to runtime WIBU license
	6	Validation test
		
		

## Access to sources

Sources are available on Git Hub

	https://github.com/gavanduffy27/partner-sdk-examples.git


To create a local version :

	git clone https://github.com/gavanduffy27/partner-sdk-examples.git


Note this documentation file is updated and maintained from the git sources:

	partner-examples/dgie-examples/doc/workshopTests.md


## Additional native library dependencies files 

	abisClientDLL.zip


- unpack zip file to folder
- Windows ensure the folder is added to system path

To test that these can be accessed open a command window:

	where ABISClientJava.dll



## JAR file dependencies

-	abis-client-dgie-6.3-SNAPSHOT.jar
-	abis-client-testframework-6.3-SNAPSHOT.jar

Note only the first JAR is required for BMS system integration.

Both JAR files are required for the partner examples.

## Installing Jar file dependencies under Maven

	mvn install:install-file 
				-Dfile=jar/sdk/abis-client-dgie-6.3-SNAPSHOT.jar 
				-DgroupId=com.genkey.partnersdk 
				-DartifactId=abis-client-dgie 
				-Dversion=6.3-SNAPSHOT 
				-Dpackaging=jar 
				-DcreateChecksum=true

	mvn install:install-file 
				-Dfile=jar/sdk/abis-client-testframework-6.3-SNAPSHOT.jar 
				-DgroupId=com.genkey.partnersdk 
				-DartifactId=abis-client-testframework 
				-Dversion=6.3-SNAPSHOT 
				-Dpackaging=jar 
				-DcreateChecksum=true

The partner examples use a maven build file that assumes the above are installed with the above groupID/artifactId and with version compatible to the partner-examples/

An example script files for Windows and Linux are at this location:

	partner-examples-sdk/partner-examples/etc


## Test images

-	ist_config.zip

unpack the zip file

for example if the zip file is unloaded to:

	D:\releases\partnerSDK\ist_config

Then this will contain a sub-folder

	D:\releases\partnerSDK\ist_config\images\abisClientBMP

To access the images from example programs then define the variable IST_CONFIG_HOME to
point to the root folder where ist-config.zip is unpacked

	IST_CONFIG_HOME=D:\releases\partnerSDK\ist_config	

## Codemeter license availability

### Codemeter software availability and install

Check for codemeter on system. You can check for this by attempting to run:

	Codemeter Control Center

Or you can check for the following file on Windows

	"C:\Program Files (x86)\CodeMeter"\Runtime\bin\cmu32.exe 
	
If not available then install the CodeMeter runtime which is available online from

	https://www.wibu.com/support/user/user-software.html#CodeMeter_User_Download
	
	
Also in workshop files as:

	-	CodeMeterRuntime.exe


Run the installer if required


### Checking for available licenses:

For local installed licenses:

	"C:\Program Files (x86)\CodeMeter"\Runtime\bin\cmu32.exe -x

For network installed licenses:

	"C:\Program Files (x86)\CodeMeter"\Runtime\bin\cmu32.exe -kx


## Building the examples

From the partner-examples folder:

	mvn clean install
 
If the build runs successfully then the maven dependencies are correctly installed


## Install validation	


	
	
	
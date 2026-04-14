# Workshop tests

## Access to sources

https://github.com/gavanduffy27/partner-sdk-examples.git


Note this file is updated and maintained from:

	partner-examples/dgie-examples/doc/workshopTests.md


## Additional native library dependencies files 

	abisClientDLL.zip


- unpack zip file to folder
- Windows ensure the folder is added to system path



## JAR file dependencies

-	abis-client-dgie-6.3-SNAPSHOT.jar
-	abis-client-testframework-6.3-SNAPSHOT.jar

Note only the first JAR is required for normal system integration.

The second JAR file is required for the partner examples.


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

## Install validation	

.. updates to follow
	
	
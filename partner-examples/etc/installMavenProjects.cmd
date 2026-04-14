set VERSION=6.2.2-RELEASE
set JAR_PATH=jar\sdk
set GROUP_ID=com.genkey.partnersdk 
call packageExternalJar %GROUP_ID% abis-client-complete %VERSION% %JAR_PATH%
call packageExternalJar %GROUP_ID%  abis-client-extended %VERSION% %JAR_PATH%
call packageExternalJar %GROUP_ID%  abis-client-dgie %VERSION% %JAR_PATH%
call packageExternalJar %GROUP_ID%  abis-client-testframework %VERSION% %JAR_PATH%
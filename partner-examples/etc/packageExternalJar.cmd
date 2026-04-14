set GROUP_ID=%1
set ARTIFACT_ID=%2
set VERSION=%3
set JAR_PATH=%4
mvn install:install-file -Dfile=%JAR_PATH%\%ARTIFACT_ID%-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dversion=%VERSION% -Dpackaging=jar  -DcreateChecksum=true
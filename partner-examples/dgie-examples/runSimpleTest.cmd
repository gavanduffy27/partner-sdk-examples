set MAIN_CLASS=SimpleConnectionTest
SET VM_ARGS=-Djava.net.preferIPv4Stack=true 
set CMD_ARGS="%%1"
::mvn exec:java -Dexec.mainClass=com.genkey.partner.example.%MAIN_CLASS% -Dexec.classpathScope=runtime -Dexec.commandlineArguments=%VM_ARGS% -Dexec.args="%classpath %%2"
mvn exec:java -Dexec.mainClass=com.genkey.partner.example.%MAIN_CLASS% -Dexec.classpathScope=runtime -Dexec.commandlineArguments=%VM_ARGS% -Dexec.args="%classpath %%1"
#!/bin/sh
MAIN_CLASS=functional.DiagnosticExample
VM_ARGS=-Djava.net.preferIPv4Stack=true 
CMD_ARGS=$*
mvn exec:java -Dexec.mainClass=com.genkey.partner.example.%MAIN_CLASS% -Dexec.classpathScope=runtime -Dexec.commandlineArguments=%VM_ARGS% -Dexec.args="%classpath %%1"
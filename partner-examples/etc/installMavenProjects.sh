#!/bin/sh

# Current version
VERSION=6.3-SNAPSHOT

# Path to JAR files sent by henkey
JAR_PATH=jar/sdk

# Group ID
GROUP_ID=com.genkey.partnersdk 

# Math to Maven runtime
MAVEN=$MVN_HOME/bin/mvn


packageExternalJar()
{
	local group_id=$1
	local artifact_id=$2
	local version=$3
	local jar_path=$4
	local jar_file=${jar_path}/${artifact_id}-${version}.jar
	echo $MAVEN install:install-file -Dfile=${jar_file} -DgroupId=${group_id} -DartifactId=${artifact_id} -Dversion=${version} -Dpackaging=jar  -DcreateChecksum=true
}

packageArtifacts()
{
	for artifact in $*
	do
		packageExternalJar $GROUP_ID $artifact $VERSION $JAR_PATH
	done
}

# List of artifacts to install
ARTIFACT_LIST="abis-client-dgie abis-client-testframework"

# run the packaging
packageArtifacts $ARTIFACT_LIST

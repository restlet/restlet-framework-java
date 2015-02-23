#! /bin/bash

# Restlet Framework source dir: first argument of the script, given by Jenkins, matches to jenkins workspace
RF_SOURCE_DIR=$1

# Release type (snapshot, milestone, rc)
RELEASE_TYPE=$2

# Version (eg: 2.3)
VERSION=$3

# Build tag: will be used to create bundles artifact
BUILD_TAG=$5

# Clean build directories
rm -rf build/editions

cd build

# Run test suite
ant -Deditions=jse -Dverify=true 

# Build RF OSGI version for APISpark 
ant -Deditions=osgi -Dverify=false -Djavadoc=false -Dmaven=false -Dnsis=false -Dpackage=false -Declipse-pde=true -Declipse-pde-optional-dependencies=true -Dp2=true

# Build  RF JSE version for agent and create maven artefacts
ant -Deditions=jse -Dverify=false -Djavadoc=false -Dmaven=true -Dnsis=false -Dpackage=false -Declipse-pde=true -Declipse-pde-optional-dependencies=true -Dp2=true

#!/bin/bash


#run tests
#ant -Deditions=jse -Dverify=true -Djavadoc=false -Dmaven=false -Dnsis=false -Dpackage=false -Declipse-pde=true -Declipse-pde-optional-dependencies=true -Dp2=true


#build OSGI
#ant -Deditions=osgi -Dverify=false -Djavadoc=false -Dmaven=false -Dnsis=false -Dpackage=false -Declipse-pde=true -Declipse-pde-optional-dependencies=true -Dp2=true

RF=~/workspaces/restlet/restlet-framework-java
RF_BUILD=$RF/build/editions/osgi/dist/p2/restlet-osgi-*/plugins
AS=~/workspaces/apispark/apispark
AS_LIB=$AS/libraries

cd $AS_LIB
for DIR in org.restlet*
do
  cd $DIR
  for JAR in *.jar
  do
    echo -e "\nReplace $DIR/$JAR"
    NEW_JAR=$RF_BUILD/${JAR%.jar}_*.jar
    if [ -f $NEW_JAR ]; then
      cp $NEW_JAR ./$JAR
    else
      echo "MISSING! $NEW_JAR"
      exit 1
    fi
  done
  cd $AS_LIB
done

#build JSE for agent
#ant -Deditions=jse -Dverify=true -Djavadoc=false -Dmaven=true -Dnsis=false -Dpackage=false -Declipse-pde=true -Declipse-pde-optional-dependencies=true -Dp2=true
#cp -r editions/jse/dist/maven2/restlet-jse-2.3.0-apispark-3.2rc2/org ~/.m2/repository/

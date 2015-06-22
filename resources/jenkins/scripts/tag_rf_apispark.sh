#! /bin/bash

CURRENT_BRANCH=$1

TAG=$2

git checkout $CURRENT_BRANCH
git fetch origin $CURRENT_BRANCH
git reset --hard origin/$CURRENT_BRANCH

BUILD_PROPERTIES='build/build.properties'

# Check wc of initial build.properties
INIT_WC=`wc $BUILD_PROPERTIES`

# Save build.properties initial configuration
INIT_RELEASE_TYPE_LINE=`grep "meta.release-type:" $BUILD_PROPERTIES`
INIT_RELEASE_NUMBER_LINE=`grep "release-number:" $BUILD_PROPERTIES`

INIT_RELEASE_TYPE=`echo $INIT_RELEASE_TYPE_LINE | sed 's/meta.release-type: //' | sed 's/.$//'`

# Create new tag properties for build.properties
if [[ $TAG =~ (.+)\.(.+)m(.+) ]]; then
  NEW_RELEASE_TYPE="milestone"
elif [[ $TAG =~ (.+)\.(.+)\.(.+) ]]; then
  NEW_RELEASE_TYPE="final"
elif [[ $TAG =~ (.+)\.(.+)rc(.+) ]]; then
  NEW_RELEASE_TYPE="rc"
else
  echo "Wrong tag pattern"
  exit 1
fi

NEW_RELEASE_TYPE_LINE="meta.release-type: $NEW_RELEASE_TYPE"
NEW_RELEASE_NUMBER_LINE="release-number: apispark-$TAG"

# Replace lines
sed -i "s,$INIT_RELEASE_TYPE_LINE,$NEW_RELEASE_TYPE_LINE," $BUILD_PROPERTIES
sed -i "s,$INIT_RELEASE_NUMBER_LINE,$NEW_RELEASE_NUMBER_LINE," $BUILD_PROPERTIES

# Add and commit
git add $BUILD_PROPERTIES
git commit -m "Released version $TAG"

# Create and push tag
TAG_NAME=apispark-v$TAG
git tag $TAG_NAME
git push origin $TAG_NAME

# Restore build.properties
sed -i "s,$NEW_RELEASE_TYPE_LINE,$INIT_RELEASE_TYPE_LINE," $BUILD_PROPERTIES
sed -i "s,$NEW_RELEASE_NUMBER_LINE,$INIT_RELEASE_NUMBER_LINE," $BUILD_PROPERTIES

# Check if the file is identical to the original
FINAL_WC=`wc $BUILD_PROPERTIES`
if [ "$INIT_WC" != "$FINAL_WC" ]; then
  echo "build.properties has not been well restored"
  git reset HEAD~1
  exit 1
fi

# Add and commit
git add $BUILD_PROPERTIES
git commit -m "Released version $TAG"

# Push modifications
git push origin $CURRENT_BRANCH

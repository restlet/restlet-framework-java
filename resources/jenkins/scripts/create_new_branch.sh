#! /bin/bash

NEW_BRANCH=apispark-$1

git checkout -b $NEW_BRANCH
git push origin $NEW_BRANCH

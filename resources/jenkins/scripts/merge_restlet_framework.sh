#!/bin/bash
set -e

SOURCE=$1
DEST=$2

git fetch origin "$DEST"
git fetch origin "$SOURCE"

git checkout "$SOURCE"
git reset --hard "origin/$SOURCE"

git checkout "$DEST"
git reset --hard "origin/$DEST"

echo "merge, no conflicts expected"
MERGE_MESSAGE=$(git merge --no-ff --no-commit "$SOURCE")
echo $MERGE_MESSAGE

if [[ $MERGE_MESSAGE = "Already up-to-date." ]]; then
	exit 0
fi

# if release-number is not null, replace
if ! [[ -n "$(grep -E '^release-number:$' build/build.properties)" ]]; then
	echo "replacing relase-number"
	sed -i -r 's/(^release-number:).*?$/\1/' build/build.properties
fi

## if release-type is not snapshot, replace
if ! [[ -n "$(grep -E '^meta.release-type: snapshot$' build/build.properties)" ]]; then
	echo "replacing relase-type"
	sed -i -r 's/(^meta.release-type:).*?$/\1 snapshot/' build/build.properties
fi

git add build/build.properties

git commit --no-edit

git push origin "$DEST"

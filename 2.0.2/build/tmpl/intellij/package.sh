#!/bin/sh

proj=project
/bin/rm -f ${proj}.zip
wd=`pwd`
(cd ../../..; find . -name \*.ipr -exec zip ${wd}/${proj} {} \;)
(cd ../../..; find . -name \*.iml -exec zip ${wd}/${proj} {} \;)

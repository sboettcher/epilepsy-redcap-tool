#!/bin/bash

if [ $# -eq 0 ]
  then
    exit 1
fi

TIME=$(date +%Y%m%d_%H%M%S)
VERSION=$(git show-ref /HEAD --head --hash=7)

sed -i -r '/buildtime/s/(\".*\")/\"'"$TIME"'\"/' $1
sed -i -r '/buildversion/s/(\".*\")/\"'"$VERSION"'\"/' $1


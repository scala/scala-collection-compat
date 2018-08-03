#!/usr/bin/env bash

# set -x

HERE="`dirname $0`"
DST="$HERE/../.git/hooks/pre-commit"

if [ ! -f $DST ]; then
  cp "$HERE/pre-commit" $DST
fi

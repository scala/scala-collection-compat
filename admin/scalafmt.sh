#!/usr/bin/env bash

# set -x

HERE="`dirname $0`"
VERSION="3.7.4"
COURSIER="$HERE/.coursier"
SCALAFMT="$HERE/.scalafmt-$VERSION"

if [ ! -f $COURSIER ]; then
  # note that the launch script we're using here is considered antiquated and was removed by
  # https://github.com/coursier/coursier/pull/1565
  curl -L -o $COURSIER 'https://github.com/coursier/coursier/blob/483c980e784cf33168be94cfc1e5682f56d71142/coursier?raw=true'
  chmod +x $COURSIER
fi

if [ ! -f $SCALAFMT ]; then
  $COURSIER bootstrap org.scalameta:scalafmt-cli_2.12:$VERSION --main org.scalafmt.cli.Cli -o $SCALAFMT
  chmod +x $SCALAFMT
fi

$SCALAFMT "$@"

#!/usr/bin/env bash

pushd /tmp

  git clone git://github.com/MasseGuillaume/scalafix.git
  pushd scalafix
    git checkout v0-denot2
    git fetch --tags
    sbt "+publishLocal"
  popd

  git clone git://github.com/MasseGuillaume/sbt-scalafix.git
  pushd sbt-scalafix
    git checkout fix6
    git fetch --tags
    sbt "+publishLocal"
  popd

popd

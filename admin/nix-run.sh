#!/usr/bin/env bash

HERE="`dirname $0`"

nix-shell $HERE/scala-native.nix -A clangEnv
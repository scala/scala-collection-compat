#!/usr/bin/env bash

# Copied from the output of genKeyPair.sh
K=$encrypted_8c7005201bb0_key
IV=$encrypted_8c7005201bb0_iv
openssl aes-256-cbc -K $K -iv $IV -in admin/secring.asc.enc -out admin/secring.asc -d

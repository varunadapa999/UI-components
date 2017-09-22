#!/usr/bin/env bash

set -e

if [ "$TEST_TYPE" == "unit" ]; then
  echo "Starting unit tests..."
  ./gradlew assemble lint build test -PdisablePreDex;
elif [ "$TEST_TYPE" == "instrumentation" ]; then
  echo "Assembling instrumentation tests..."
  ./gradlew assemble -PdisablePreDex;
  echo "Waiting for emulator setup..."
  travis_wait android-wait-for-emulator
  adb shell input keyevent 82 &
  # Avoid having it lock itself again.
  adb shell svc power stayon true
  echo "Actually running instrumentation tests..."
  travis_wait ./gradlew connectedCheck -PdisablePreDex;
else
  echo "Unknown test type"
  exit 1
fi

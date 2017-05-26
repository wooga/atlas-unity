#!/usr/bin/env bash

brew tap wooga/unityversions
brew update
brew cask install unity@5.6.0f3

ln -s /Applications/Unity-5.6.0f3 /Applications/Unity

/Applications/Unity/Unity.app/Contents/MacOS/Unity -quit -batchmode -serial $UNITY_SERIAL -username $UNITY_USER -password $UNITY_PASSWORD
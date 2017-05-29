#!/usr/bin/env bash

#sudo apt-get -qq update
#supo apt-get -qq -y install lib32stdc++6
#
#DEB_NAME=unity-editor_amd64-5.6.1xf1Linux.deb
#DEB_LOCATION=.travis_linux/$DEB_NAME
#if [ ! -f $DEB_LOCATION ]; then
#    wget -P .travis_linux http://beta.unity3d.com/download/6a86e542cf5c/$DEB_NAME
#fi
#sudo dpkg -i DEB_LOCATION
#
#echo "install unity"
#sudo apt-get -qq -y -f install
#
#echo "activate link unity"
#/opt/Unity/Editor/Unity -quit -batchmode -serial $UNITY_SERIAL -username $UNITY_USER -password $UNITY_PASSWORD
#!/bin/bash


PREVTAG=$1
PREVTAG=${PREVTAG:4:3}
TAG=$((PREVTAG+1))
APP_VERSION_INTERNAL='1.1.'$TAG
FINAL_PROFILE=$4
optional_server=$5
optional_port=$6


if [ $FINAL_PROFILE == "main" ]
then
   FINAL_PROFILE="MobilisDemo"
else
   FINAL_PROFILE=$4
fi
export FINAL_PROFILE

if [ ! -z "$optional_server" ]
then
   DESIRED_SERVER_IP=$optional_server
else
   DESIRED_SERVER_IP=$2
fi

if [ ! -z "$optional_port"  ]
then
   DESIRED_PORT=$optional_port
else
   DESIRED_PORT=$3
fi
export TAG
export PREVTAG
export DESIRED_SERVER_IP
export DESIRED_PORT
export APP_VERSION_INTERNAL

echo 'TAG:' $TAG
echo 'PREVTAG:' $PREVTAG
echo 'DESIRED_SERVER_IP:' $DESIRED_SERVER_IP
echo 'DESIRED_PORT:' $DESIRED_PORT
echo 'APP_VERSION_INTERNAL:' $APP_VERSION_INTERNAL
echo 'FINAL_PROFILE:' $FINAL_PROFILE

sed -e "s/@string\/APP_VERSION_INTERNAL/$APP_VERSION_INTERNAL/g" MMWalletAndroid/AndroidManifest.xml > MMWalletAndroid/AndroidManifest.xml.tmp && mv MMWalletAndroid/AndroidManifest.xml.tmp MMWalletAndroid/AndroidManifest.xml
sed -e "s/@integer\/APP_VERSION_CODE/$TAG/g" MMWalletAndroid/AndroidManifest.xml > MMWalletAndroid/AndroidManifest.xml.tmp && mv MMWalletAndroid/AndroidManifest.xml.tmp MMWalletAndroid/AndroidManifest.xml
sed "s/.*APP_VERSION_INTERNAL.*/<string name=\"APP_VERSION_INTERNAL\">$APP_VERSION_INTERNAL<\/string>/g" MMWalletAndroid/res/values/donottranslate.xml > MMWalletAndroid/res/values/donottranslate.xml.tmp && mv MMWalletAndroid/res/values/donottranslate.xml.tmp MMWalletAndroid/res/values/donottranslate.xml
sed "s/.*APP_VERSION_CODE.*/<integer name=\"APP_VERSION_CODE\">$TAG<\/integer>/g" MMWalletAndroid/res/values/donottranslate.xml > MMWalletAndroid/res/values/donottranslate.xml.tmp && mv MMWalletAndroid/res/values/donottranslate.xml.tmp MMWalletAndroid/res/values/donottranslate.xml
sed "s/.*name=\"SERVER_IP\".*/<string name=\"SERVER_IP\">$DESIRED_SERVER_IP<\/string>/g" MMWalletAndroid/res/values/donottranslate.xml > MMWalletAndroid/res/values/donottranslate.xml.tmp && mv MMWalletAndroid/res/values/donottranslate.xml.tmp MMWalletAndroid/res/values/donottranslate.xml
sed "s/.*name=\"SERVER_PORT\".*/<string name=\"SERVER_PORT\">$DESIRED_PORT<\/string>/g" MMWalletAndroid/res/values/donottranslate.xml > MMWalletAndroid/res/values/donottranslate.xml.tmp && mv MMWalletAndroid/res/values/donottranslate.xml.tmp MMWalletAndroid/res/values/donottranslate.xml

search_server_ip='SERVER_IP'
search_server_port='SERVER_PORT'

i=0; 

for file in $(grep --include=\*.xml -l -R 'SERVER_IP' ./MMWalletAndroid/src/)
do
  sed "s/.*name=.*\"SERVER_IP\".*/<string name=\"SERVER_IP\">$DESIRED_SERVER_IP<\/string>/g" $file > tempfile.tmp && mv tempfile.tmp $file
  sed "s/.*name=.*\"SERVER_PORT\".*/<string name=\"SERVER_PORT\">$DESIRED_PORT<\/string>/g" $file > tempfile.tmp && mv tempfile.tmp $file
  let i++;
  #sed '/<!--/,/-->/d' $file > tempfile.tmp && mv tempfile.tmp $file 
  #sed '/<!--/,/-->/d' $file > tempfile.tmp && mv tempfile.tmp $file
  echo "Modified: " $file
done
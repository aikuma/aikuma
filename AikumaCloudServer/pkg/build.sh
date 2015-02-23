#! /bin/bash

REPO=hleeldc/aikuma-cloud-server
VER=1.0.0
TAG=$REPO:$VER
TAG_LATEST=$REPO:latest

docker build -t $REPO:$VER .
docker tag -f $TAG $TAG_LATEST


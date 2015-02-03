#! /bin/bash

REPO=hleeldc/aikuma-index-server
VER=1.0.1
TAG=$REPO:$VER
TAG_LATEST=$REPO:latest

docker build -t $REPO:$VER .
docker tag -f $TAG $TAG_LATEST


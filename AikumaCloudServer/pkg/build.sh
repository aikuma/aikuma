#! /bin/bash

REPO=aikuma/aikuma
VER=1.2.0
TAG=$REPO:$VER
TAG_LATEST=$REPO:latest

docker build -t $REPO:$VER .
docker tag -f $TAG $TAG_LATEST


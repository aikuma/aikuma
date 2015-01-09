#! /bin/bash

set -e

case $0 in
    /*) thisdir=`dirname $0` ;;
    *)  thisdir=`pwd`/`dirname $0` ;;
esac

cd $thisdir/..

tarpath() {
    # compute the path of the latest distribution tar file
    find build/distributions -name "aikuma-copy-server-*.tar" | sort | tail -n 1
}

dockerfile() {
    # generate dockerfile by filling in placeholder
    cat $thisdir/Dockerfile.template | sed -r "s@%TAR%@$TARPATH@"
}

TARPATH=`tarpath`
v=`echo $TARPATH | sed -r 's/.*-(.*).tar/\1/'`  # version

. $thisdir/build-config.sh
TAG=$DOCKER_USER/$DOCKER_REPO:$v
TAG_LATEST=$DOCKER_USER/$DOCKER_REPO:latest

gradle distTar           # build distribution tar file
dockerfile >Dockerfile   # generate dockerfile
docker build -t $TAG .   # bulid docker image
docker tag $TAG $TAG_LATEST
rm -f Dockerfile


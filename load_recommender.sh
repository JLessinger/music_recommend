#!/usr/bin/env bash
PROJ=spotify_hack
VERSION=1.0-SNAPSHOT
JAR=$PROJ-$VERSION.jar
PKG=spotify_hack

CURDIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
CURDIR=${CURDIR%%/}
LIBCP=${HOME%%/}/.m2/repository/com/google/guava/guava/18.0/*
CP=$LIBCP:$CURDIR/target/$JAR


java -ea -Xmx6g -cp $CP $PKG.NearestSongsMain "$@"

#!/usr/bin/env bash
PROJ=recommender
VERSION=1.0-SNAPSHOT
PROJJAR=$PROJ-$VERSION.jar
PKG=recommender

CURDIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
CURDIR=${CURDIR%%/}
LIBCP=lib/*:${HOME%%/}/.m2/repository/com/google/guava/guava/18.0/*
CP=$LIBCP:$CURDIR/target/$PROJJAR


java -ea -Xmx6g -cp $CP $PKG.NearestSongsMain "$@"

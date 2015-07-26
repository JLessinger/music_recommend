#!/usr/bin/env bash
PROJ=recommender
VERSION=1.0-SNAPSHOT
PROJJAR=$PROJ-$VERSION.jar
PKG=recommender

CURDIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
CURDIR=${CURDIR%%/}
LIBDIR=lib/*
MVNDIR=${HOME%%/}/.m2/repository
MVNLIB=$MVNDIR/com/google/guava/guava/18.0/*:$MVNDIR/org/json/json/20090211/*
CP=$LIBDIR:$MVNLIB:$CURDIR/target/$PROJJAR

java -ea -Xmx6g -cp $CP $PKG.NearestSongsMain "$@"

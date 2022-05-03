#!/bin/sh
#!/bin/bash
# more info at https://gist.github.com/mcgivrer/a31510019029eba73edf5721a93c3dec
# Copyright 2020 Frederic Delorme (McGivrer) fredericDOTdelormeATgmailDOTcom
#
export PROGRAM_NAME=monoclass2
export PROGRAM_VERSION=1.0.1
export PROGRAM_TITLE=MonoClass2
export AUTHOR_NAME='Frédéric Delorme'
export VENDOR_NAME=frederic.delorme@gmail.com
export MAINCLASS=com.demoing.app.Application
# pathes
export SRC=src
export LIBS=lib
export TARGET=target
export BUILD=$TARGET/build
export CLASSES=$TARGET/classes
export RESOURCES=$SRC/main/resources
export COMPILATION_OPTS=--enable-preview -Xlint:deprecation -Xlint:preview
export JAR_OPTS=--enable-preview

function manifest() {
  mkdir $TARGET
  echo "|_ 0. clear build directory"
  rm -Rf $TARGET/*
  touch $TARGET/manifest.mf
  # build manifest
  echo "|_ 1. Create Manifest file '$TARGET/manifest.mf'"
  echo 'Manifest-Version: 1.0' >$TARGET/manifest.mf
  echo "Created-By: $JAVA_VERSION ($VENDOR_NAME)" >>$TARGET/manifest.mf
  echo "Main-Class: $MAINCLASS" >>$TARGET/manifest.mf
  echo "Implementation-Title: $PROGRAM_TITLE" >>$TARGET/manifest.mf
  echo "Implementation-Version: $PROGRAM_VERSION-build_$GIT_COMIT_ID" >>$TARGET/manifest.mf
  echo "Implementation-Vendor: $VENDOR_NAME" >>$TARGET/manifest.mf
  echo "Implementation-Author: $AUTHOR_NAME" >>$TARGET/manifest.mf
  echo "   |_ done"
}
#
function compile() {
  echo "compile sources "
  echo "> from : $SRC"
  echo "> to   : $CLASSES"
  # prepare target
  mkdir -p $CLASSES
  # Compile class files
  rm -Rf $CLASSES/*
  echo "|_ 2. compile sources from '$SRC' ..."
  find $SRC -name '*.java' >$LIBS/sources.lst
  javac $COMPILATION_OPTS @$LIBS/options.txt @$LIBS/sources.lst -cp $CLASSES
  echo "   done."
}
#
function createJar() {
  echo "|_ 3. package jar file '$TARGET/$PROGRAM_NAME-$PROGRAM_VERSION.jar'..."
  if ([ $(ls $CLASSES | wc -l | grep -w "0") ]); then
    echo 'No compiled class files'
  else
    # Build JAR
    jar -cfmv $TARGET/$PROGRAM_NAME-$PROGRAM_VERSION.jar $TARGET/manifest.mf -C $CLASSES . -C $RESOURCES .
  fi

  echo "   |_ done."
}
#
function wrapJar() {
  # create runnable program
  echo "|_ 4. create run file '$BUILD/$PROGRAM_NAME-$PROGRAM_VERSION.run'..."
  mkdir -p $BUILD
  cat $LIBS/stub.sh $TARGET/$PROGRAM_NAME-$PROGRAM_VERSION.jar >$BUILD/$PROGRAM_NAME-$PROGRAM_VERSION.run
  chmod +x $BUILD/$PROGRAM_NAME-$PROGRAM_VERSION.run
  echo "   |_ done."
}
#
function executeJar() {
  if [ ! -f "$TARGET/$PROGRAM_NAME-$PROGRAM_VERSION.jar" ]; then
    manifest
    compile
    createJar
  fi
  echo "|_ 5.Execute just created JAR $TARGET/$PROGRAM_NAME-$PROGRAM_VERSION.jar"
  java $JAR_OPTS -jar $TARGET/$PROGRAM_NAME-$PROGRAM_VERSION.jar
}
#
function sign() {
  # must see here: https://docs.oracle.com/javase/tutorial/security/toolsign/signer.html
  echo "not already implemented... sorry"
}
#
function help() {
  echo "build2 command line usage :"
  echo "---------------------------"
  echo "$> build2 [options]"
  echo "where:"
  echo " - a|A|all     : perform all following operations"
  echo " - c|C|compile : compile all sources project"
  echo " - j|J|jar     : build JAR with all resources"
  echo " - w|W|wrap    : Build and wrap jar as a shell script"
  echo " - s|S|sign    : Build and wrap signed jar as a shell script"
  echo " - r|R|run     : execute (and build if needed) the created JAR"
  echo ""
  echo " (c)2022 MIT License Frederic Delorme (@McGivrer) fredericDOTdelormeATgmailDOTcom"
  echo " --"
}
#
function run() {
  echo "Build of program '$PROGRAM_NAME-$PROGRAM_VERSION' ..."
  echo "-----------"
  case $1 in
  a | A | all)
    manifest
    compile
    createJar
    wrapJar
    ;;
  c | C | compile)
    manifest
    compile
    ;;
  j | J | jar)
    createJar
    ;;
  w | W | wrap)
    wrapJar
    ;;
  s | S | sign)
    sign $2
    ;;
  r | R | run)
    executeJar
    ;;
  h | H | ? | *)
    help
    ;;
  esac
  echo "-----------"
  echo "... done".
}
#
run $1

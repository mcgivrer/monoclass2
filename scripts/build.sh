#!/bin/bash
# more info at https://gist.github.com/mcgivrer/a31510019029eba73edf5721a93c3dec
# Copyright 2020 Frederic Delorme (McGivrer) fredericDOTdelormeATgmailDOTcom
# Your program build definition
export PROGRAM_NAME=monoclass2
export PROGRAM_VERSION=1.0.3
export PROGRAM_TITLE=MonoClass2
export AUTHOR_NAME='Frédéric Delorme'
export VENDOR_NAME=frederic.delorme@gmail.com
export MAIN_CLASS=com.demoing.app.core.Application
export JAVADOC_CLASSPATH="com.demoing.app.core com.demoing.app.scenes"
export SOURCE_VERSION=17
export SRC_ENCODING=UTF-8
# the tools and sources versions
export GIT_COMMIT_ID=$(git rev-parse HEAD)
export JAVA_BUILD=$(java --version | head -1 | cut -f2 -d' ')
#
# Paths
export SRC=src
export LIBS=lib
export LIB_TEST="./lib/test/junit-platform-console-standalone-1.8.2.jar"
export TARGET=target
export BUILD=$TARGET/build
export CLASSES=$TARGET/classes
export RESOURCES=$SRC/main/resources
export TESTRESOURCES=$SRC/test/resources
export COMPILATION_OPTS="--enable-preview -Xlint:preview"
export JAR_NAME=$PROGRAM_NAME-$PROGRAM_VERSION.jar
# -Xlint:unchecked -Xlint:preview"
export JAR_OPTS=--enable-preview
#
function manifest() {
  mkdir $TARGET
  echo "|_ 0. clear build directory"
  rm -Rf $TARGET/*
  touch $TARGET/manifest.mf
  # build manifest
  echo "|_ 1. Create Manifest file '$TARGET/manifest.mf'"
  echo 'Manifest-Version: 1.0' >$TARGET/manifest.mf
  echo "Created-By: $JAVA_BUILD ($VENDOR_NAME)" >>$TARGET/manifest.mf
  echo "Main-Class: $MAIN_CLASS" >>$TARGET/manifest.mf
  echo "Implementation-Title: $PROGRAM_TITLE" >>$TARGET/manifest.mf
  echo "Implementation-Version: $PROGRAM_VERSION-build_${GIT_COMMIT_ID:0:8}" >>$TARGET/manifest.mf
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
  echo "|_ 2. compile sources from '$SRC/main' ..."
  find $SRC/main -name '*.java' >$TARGET/sources.lst
  javac $COMPILATION_OPTS @$LIBS/options.txt @$TARGET/sources.lst -cp $CLASSES
  echo "   done."
}
#
function generatedoc(){
echo "generate Javadoc "
  echo "> from : $SRC"
  echo "> to   : $TARGET/javadoc"
  # prepare target
  mkdir -p $TARGET/javadoc
  # Compile class files
  rm -Rf $TARGET/javadoc/*
  echo "|_ 2-5. generate javadoc from '$JAVADOC_CLASSPATH' ..."
  #java -jar ./lib/tools/markdown2html-0.3.1.jar <README.md >$TARGET/javadoc/overview.html
  javadoc $JAR_OPTS -source $SOURCE_VERSION \
  #  -overview $TARGET/javadoc/overview.html \
    -quiet -author -use -version \
    -doctitle "<h1>$PROGRAM_TITLE</h1>" \
    -d $TARGET/javadoc \
    -sourcepath $SRC/main/java $JAVADOC_CLASSPATH  >> target/build.log
  echo "   done."

}
#
function executeTests(){
  echo "execute tests"
  echo "> from : $SRC/test"
  echo "> to   : $TARGET/test-classes"
  mkdir -p $TARGET/test-classes
  rm -Rf $TARGET/test-classes/*
  echo "copy test resources"
  cp -r ./src/test/resources/* $TARGET/test-classes
  echo "compile test classes"
  #list test sources
  find ./src/test -name '*.java' >$TARGET/test-sources.lst
  javac -source 17 -encoding $SRC_ENCODING $COMPILATION_OPTS -cp "$LIB_TEST;$CLASSES;." -d $TARGET/test-classes @$TARGET/test-sources.lst
  echo "execute tests through JUnit"
  java $JAR_OPTS -jar "$LIB_TEST" --class-path "$CLASSES;$TARGET/test-classes;$SRC/test/resources;" --scan-class-path
  echo "done."
}
#
function createJar() {
  echo "|_ 3. package jar file '$TARGET/$JAR_NAME'..."
  if ([ $(ls $CLASSES | wc -l | grep -w "0") ]); then
    echo 'No compiled class files'
  else
    # Build JAR
    jar -cfmv $TARGET/$JAR_NAME $TARGET/manifest.mf -C $CLASSES . -C $RESOURCES .
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
  manifest
  compile
  createJar
  echo "|_ 5.Execute just created JAR $TARGET/$PROGRAM_NAME-$PROGRAM_VERSION.jar"
  java $JAR_OPTS -jar $TARGET/$PROGRAM_NAME-$PROGRAM_VERSION.jar "$@"
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
  echo " - d|D|doc     : generate javadoc for project"
  echo " - t|T|test    : execute JUnit tests"
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
    executeTests
    generatedoc
    createJar
    wrapJar
    ;;
  c | C | compile)
    manifest
    compile
    ;;
  d | D | doc)
    manifest
    compile
    generatedoc
    ;;
  t | T | test)
    manifest
    compile
    executeTests
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
run "$1"

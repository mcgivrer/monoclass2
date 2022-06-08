#!/bin/bash
# more info at https://gist.github.com/mcgivrer/a31510019029eba73edf5721a93c3dec
# Copyright 2020 Frederic Delorme (McGivrer) fredericDOTdelormeATgmailDOTcom

# ---- Project definition (Manifest entries)
export PROGRAM_NAME=monoclass2
export PROGRAM_VERSION=1.0.5
export PROGRAM_TITLE=MonoClass2
# Entry point for your program (used to create mainClass manifest entry)
export MAIN_CLASS=com.demoing.app.core.Application
# A dirty list of package to be build (TODO add automation on package detection)
export JAVADOC_CLASSPATH="com.demoing.app.core com.demoing.app.scenes"
export JAVADOC_GROUPS="-group \"Core package\" com.demoing.app.core -group \"Demo\" com.demoing.app.demo"
# ---- Author
export AUTHOR_NAME='Frédéric Delorme'
export VENDOR_NAME=frederic.delorme@gmail.com

# ---- Java JDK version and file encoding
export SOURCE_VERSION=18
export SOURCE_ENCODING=UTF-8

# ---- CheckStyle
# Uncomment the one to be used
export CHECKRULES=sun
#export CHECKRULES=google

# ---- JDK and sources versions (mainly for manifest generator)
export JAVA_BUILD=$(java --version | head -1 | cut -f2 -d' ')
export GIT_COMMIT_ID=$(git rev-parse HEAD)
#
# Paths
export SRC=src
export LIBS=lib
export LIB_TEST=$LIBS/test/junit-platform-console-standalone-1.8.2.jar
export LIB_CHECKSTYLES=$LIBS/tools/checkstyle-10.3-all.jar
export TARGET=./target
export BUILD=$TARGET/build
export CLASSES=$TARGET/classes
export TESTCLASSES=$TARGET/test-classes
export RESOURCES=$SRC/main/resources
export TESTRESOURCES=$SRC/test/resources
export JAR_NAME=$PROGRAM_NAME-$PROGRAM_VERSION.jar
# ---- to enforce preview compatibility use the --enable-preview mode,
# ---- for more information, see https://docs.oracle.com/en/java/javase/18/language/preview-language-and-vm-features.html
export COMPILATION_OPTS="--enable-preview"
#export COMPILATION_OPTS="--enable-preview -Xlint:preview"
#export COMPILATION_OPTS="--enable-preview -Xlint:unchecked -Xlint:preview"
# ---- to execute JAR one JDK preview, add the same attribute on JAR execution command line
export JAR_OPTS=--enable-preview
# ---- define the checkstyle rule set file
export CHECK_RULES_FILE=$LIBS/tools/rules/${CHECKRULES}_checks.xml

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
  # Compilation via JavaC with some debug options to add source, lines and vars in the compiled classes.
  javac $COMPILATION_OPTS \
   -d $CLASSES \
   -g:source,lines,vars \
   -source $SOURCE_VERSION \
   -target $SOURCE_VERSION \
   -classpath $CLASSES @$TARGET/sources.lst -cp $CLASSES
  echo "   done."
}
function checkCodeStyleQA() {
  echo "check code quality against rules $CHECK_RULES"
  echo "> explore sources at : $SRC"
  find $SRC/main -name '*.java' >$TARGET/sources.lst
  java $JAR_OPTS -cp "$LIB_CHECKSTYLES:$CLASSES:." \
   -jar $LIB_CHECKSTYLES \
   -c $CHECK_RULES_FILE \
   -f xml \
   -o $TARGET/checkstyle_errors.xml \
   @$TARGET/sources.lst
  echo "   done."
}
#
function generatedoc() {
  echo "generate Javadoc "
  echo "> from : $SRC"
  echo "> to   : $TARGET/javadoc"
  # prepare target
  mkdir -p $TARGET/javadoc
  # Compile class files
  rm -Rf $TARGET/javadoc/*
  echo "|_ 2-5. generate javadoc from '$JAVADOC_CLASSPATH' ..."
  java -jar ./lib/tools/markdown2html-0.3.1.jar <README.md >$TARGET/javadoc/overview.html
  javadoc $JAR_OPTS -source $SOURCE_VERSION \
    -author -use -version \
    -doctitle "<h1>$PROGRAM_TITLE</h1>" \
    -d $TARGET/javadoc \
    -sourcepath $SRC/main/java $JAVADOC_CLASSPATH \
    -overview $TARGET/javadoc/overview.html \
    $JAVADOC_GROUPS
  echo "   done." >>target/build.log
}

#
function executeTests() {
  echo "execute tests"
  echo "> from : $SRC/test"
  echo "> to   : $TARGET/test-classes"
  mkdir -p $TARGET/test-classes
  echo "copy test resources"
  cp -r ./src/main/resources/* $TESTCLASSES
  cp -r ./src/test/resources/* $TESTCLASSES
  echo "compile test classes"
  #list test sources
  find $SRC/main -name '*.java' >$TARGET/sources.lst
  find $SRC/test -name '*.java' >$TARGET/test-sources.lst
  javac -source $SOURCE_VERSION -encoding $SOURCE_ENCODING $COMPILATION_OPTS -cp ".;$LIB_TEST" -d $TESTCLASSES @$TARGET/sources.lst @$TARGET/test-sources.lst
  echo "execute tests through JUnit"
  java $JAR_OPTS -jar $LIB_TEST --class-path "$CLASSES;$TESTCLASSES;." --scan-class-path
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
function generateEpub() {
  rm -Rf $TARGET/book/
  mkdir $TARGET/book
  cat docs/*.yml >$TARGET/book/book.mdo
  cat docs/chapter-*.md >>$TARGET/book/book.mdo
  mv $TARGET/book/book.mdo $TARGET/book/book.md
  pandoc $TARGET/book/book.md --resource-path=./docs -t epub3 -o $TARGET/book/book-$PROGRAM_NAME-$PROGRAM_VERSION.epub
  echo "|_ 6. generate ebook to $TARGET/book/book-$PROGRAM_NAME-$PROGRAM_VERSION.epub"
}
# TODO https://www.toptal.com/docker/pandoc-docker-publication-chain
function generatePDF() {
  rm -Rf $TARGET/book/
  mkdir $TARGET/book
  cat docs/*.yml >$TARGET/book/book.mdo
  cat docs/chapter-*.md >>$TARGET/book/book.mdo
  mv $TARGET/book/book.mdo $TARGET/book/book.md
  # see https://stackoverflow.com/questions/29240290/pandoc-for-windows-pdflatex-not-found
  pandoc $TARGET/book/book.md --resource-path=./docs --pdf-engine=xelatex -o $TARGET/book/book-$PROGRAM_NAME-$PROGRAM_VERSION.pdf
  echo "|_ 6. generate pdf book to $TARGET/book/book-$PROGRAM_NAME-$PROGRAM_VERSION.pdf"
}
#
function sign() {
  # must see here: https://docs.oracle.com/javase/tutorial/security/toolsign/signer.html
  echo "not already implemented... sorry"
}
#
function help() {
  echo "$0 command line usage :"
  echo "---------------------------"
  echo "> $0 [options]"
  echo "where:"
  echo " - a|A|all     : perform all following operations"
  echo " - c|C|compile : compile all sources project"
  echo " - d|D|doc     : generate javadoc for project"
  echo " - e|E|epub    : generate *.epub file as docs for project (require pandoc : https://pandoc.org )"
  echo " - k|K|check   : check code source quality againt rules set (sun or google: see in build.sh for details)"
  echo " - t|T|test    : execute JUnit tests"
  echo " - j|J|jar     : build JAR with all resources"
  echo " - w|W|wrap    : Build and wrap jar as a shell script"
  echo " - p|P|pdf     : generate *.pdf file as docs for project (require pandoc: https://pandoc.org and miktex: https://miktex.org/download)"
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
    checkCodeStyleQA
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
  e | E | epub)
    generateEpub
    ;;
  k | K | check)
    checkCodeStyleQA
    ;;
  j | J | jar)
    createJar
    ;;
  t | T | test)
    manifest
    compile
    executeTests
    ;;
  w | W | wrap)
    wrapJar
    ;;
  p | P | pdf)
    generatePDF
    ;;
  r | R | run)
    executeJar
    ;;
  s | S | sign)
    sign $2
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

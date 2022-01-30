#!/bin/bash

# synopsis: toRDF PepperImporter source-dir-file-or-zip [-split] [-conll]
# we write to stdout

MYHOME=`dirname $(realpath $0)`
PEPPER=$MYHOME/pepper-wrapper
PAULA=$MYHOME/../paula
CONLL=$PAULA/conll-rdf

importer=$1
shift

split=false
conll=false
if echo $* | sed s/'\s'/'\n'/g | grep -i '^\-split$' >&/dev/null; then
  split="true"
fi;

if echo $* | sed s/'\s'/'\n'/g | grep -i '^\-conll$' >&/dev/null; then
  conll="true"
fi;

# init
if [ ! -e $PEPPER ]; then
  git clone https://github.com/acoli-repo/pepper-wrapper;
  cd $PEPPER;
  make;
fi;

if [ ! -e $CONLL ]; then
    cd $PAULA;
    make;
fi;

# transform
for src in $@; do
  if [ -e $src ]; then \

    if echo $src  | egrep -i '.*7z' >&/dev/null; then
      dir=`echo $src | sed s/'\.*7[zZ]$'//`;
      if [ -e $dir ]; then
        echo warning: use existing directory $dir, instead of $src 1>&2
      else
        mkdir $dir
        cd $dir;
        7z x ../`basename $src`
        cd -
      fi;
      src=$dir
    fi;

    src=`realpath $src`
    base=$src/
    paula=/tmp/`basename $src`/paula

    if [ $importer = "PaulaImporter" ]; then
      # our own Paula converter is more robust than the Pepper one
      if [ -d $src ]; then
        paula=$src
      else
        if [ -e $paula ]; then
          paula=$paula.`ls $paula* | wc -l`;
        fi;
        mkdir -p $paula
        if unzip $src -d $paula >&/dev/null; then
          echo ok >&/dev/null
        else
          echo unzip $src -d $paula failed 1>&2
          exit 2
        fi
      fi;
    else
      while [ -e $paula ]; do
        paula=$paula.`ls $paula*|wc -l`
      done;
      mkdir -p $paula
      bash -e $PEPPER/convert.sh $importer PaulaExporter $src $paula/;
      if find $paula | grep 'xml$' | egrep . >& /dev/null; then
        echo ok >&/dev/null;
      else
        echo Pepper error for file $src with importer $importer 1>&2
        exit 1
      fi;
    fi;

    if [ $conll = "true" ]; then
      if [ $split = "true" ]; then
        echo $PAULA/paula2conll.sh -split $paula 1>&2
        $PAULA/paula2conll.sh -split $paula
      else
        echo $PAULA/paula2conll.sh $paula 1>&2
        $PAULA/paula2conll.sh $paula
      fi;
    else
      echo python3 $PAULA/paula2rdf.py $base $paula 1>&2
      python3 $PAULA/paula2rdf.py $base $paula | \
      egrep -v '^#' | egrep '[^\s]' | \
      if [ $split = "true" ]; then
        $CONLL/run.sh CoNLLRDFUpdater -custom -updates $PAULA/induce-nif-sentences.sparql |\
        $CONLL/run.sh CoNLLRDFFormatter
      else
        cat
      fi;
    fi;
  fi;
done;

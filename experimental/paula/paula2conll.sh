#!/bin/bash
# convert PAULA directory to CoNLL

#
# init
########

MYHOME=`dirname $0`

if [ ! -e $MYHOME/conll-rdf ]; then
  cd $MYHOME;
  make;
fi;

UPDATE=$MYHOME/conll-rdf/run.sh' CoNLLRDFUpdater -custom -updates'
WRITE=$MYHOME/conll-rdf/run.sh' CoNLLRDFFormatter -conll'

#
# config
###########

split=""
if echo $1 | egrep -i '^-split$' >& /dev/null; then split=$MYHOME/induce-nif-sentences.sparql; shift; fi;

#
# help
###########

if [ $# -eq 0 -o ! -e $1 ]; then
  echo synopsis: $0" [-split] DATA[1..n]" 1>&2
  echo '  DATAi  ZIP file or folder that contains one or multiple PAULA projects, expect one annoset per (sub-) directory' 1>&2
  echo '  -split heuristic sentence splitting, will slow down conversion' 1>&2
  exit 1
fi;

#
# processing
##############

while test $# -gt 0; do
  DIR=$1
  echo '# '$DIR;
  echo;
  if [ -f $DIR ] ; then
      DIR=/tmp/`basename $1`;
      while [ -e $DIR ]; do
        DIR=$DIR.`ls $DIR* | wc -l`;
      done;
      mkdir -p $DIR;
      echo unzipping $1 to $DIR 1>&2;
      unzip $1 -d $DIR >&/dev/null
  fi;

  if [ ! -e $DIR ]; then
      echo did not find directory $DIR 1>&2;
      exit 2;
  fi

  for dir in `find $DIR | grep 'xml$' | sed s/'\/[^\/]*$'// | uniq | sort -u`; do
    tmpDir=/tmp/`basename $dir`;
    while [ -e $tmpDir ]; do
      tmpDir=$tmpDir.`ls $tmpDir* | wc -l`
    done;
    mkdir -p $tmpDir;
    ttl=$tmpDir/tmp.ttl
    echo '# '$dir
    echo

#
# PAULA 2 POWLA
###################
    echo $dir '->' $ttl 1>&2;
    time (\
      python3 $MYHOME/paula2rdf.py '/' $dir | \
      egrep -v '^#' | egrep '[^\s]' | \
      $UPDATE \
          $split \
          $MYHOME/powla2conll.sparql \
          $MYHOME/remove-powla.sparql  > $ttl ) 1>&2; \
    echo 1>&2

#
# POWLA 2 CoNLL
##################
    cols=`sed s/'\s'/'\n'/g $ttl | grep 'conll:' | sort -u | sed s/'conll:'// | egrep -v '^(ID|WORD|HEAD)$'`
    # note that the PAULA converter doesn't produce depenency annotations, but only aggregate dependencies, so we can filter out HEAD
    cols="ID WORD "$cols
    echo $ttl '-> conll with columns '$cols 1>&2
    time (
      cat $ttl | \
      $WRITE $cols
    ) ;\
    echo;
    echo 1>&2

#
# prune & iterate
####################
    rm -rf $tmpDir;
  done;

  if echo $1 | egrep -i 'zip$'; then
    rm -rf $DIR;
  fi;

  echo;
  shift;
done;

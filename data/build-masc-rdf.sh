#!/bin/bash
# retrieve the masc corpus from the web
HOME=..
TOOLS=$HOME/tools
CORPUS=MASC-1.0.3

echo compiling converter 1>&2;
$TOOLS/convert.sh -compile;
if [ -e $CORPUS ]; 
then echo found $CORPUS 1>&2;
else 
  if [ -e $CORPUS.tgz ];
  then echo found $CORPUS.tgz;
  else 
    echo retrieving corpus $CORPUS 1>&2;
    wget http://www.anc.org/MASC/download/$CORPUS.tgz;
  fi;
  echo decompressing corpus $CORPUS 1>&2;
  tar -xvf $CORPUS.tgz;
fi;
for file in $CORPUS/data/*/*.anc; do
	echo convert $file 1>&2;
	$TOOLS/convert.sh -f GrAF -t POWLA $file > $file.rdf
done;
echo conversion done 1>&2;

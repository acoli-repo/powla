#!/bin/bash
# synopsis: convert.sh [-compile] -f SOURCE_FORMAT -t TARGET_FORMAT FILE [FILE1 [FILE2 ...]] > OUT_FILE
# argument files should not contain whitespace or special characters

####################
# define classpath #         (depending on where this file was called from)
####################

LOCAL_DIR=`echo $0 | sed s/'\/[^\/]*$'//`;
if echo $LOCAL_DIR | egrep '[a-zA-Z0-9]' >& /dev/null; 
then echo >&/dev/null;
else if [ -e ./$0 ]; then LOCAL_DIR=./; else LOCAL_DIR=`whereis $0 | grep -m 1 $0 | sed -e s/'^[^\/]*'// -e s/'\/[^\/]*$'/'\/'/`; fi;
fi;

CLASSPATH_SEPARATOR=":"; # un*x
if echo $OSTYPE | egrep -i 'cygwin' >& /dev/null; then CLASSPATH_SEPARATOR=";";fi;
CLASSPATH=`for file in $(find $LOCAL_DIR/java/lib | egrep 'jar$|zip$' | grep -v '\.svn'); do echo $file; done | \
perl -e 'while(<>) { s/\n/'$CLASSPATH_SEPARATOR'/gs; print; }'; echo $LOCAL_DIR/java/bin;`;

############################
# -compile: compile        #
############################
if echo $* | egrep "\-compile" >&/dev/null;
then
    echo compile java converter 1>&2;
    mkdir $LOCAL_DIR/java/bin >&/dev/null;
    javac -classpath $CLASSPATH -sourcepath $LOCAL_DIR/java/src -d $LOCAL_DIR/java/bin `find $LOCAL_DIR/java/src/ | grep 'java$'`;
fi;

############################
# -h, -?, etc.: print help #
############################

if echo $* | egrep -i "\-h|\-\?" >&/dev/null; 
then
	echo "synopsis:" 1>&2;
	echo "   "$0" -help|-?|-h" 1>&2;
	echo "   "$0" -f SOURCE_FORMAT -t TARGET_FORMAT FILE [FILE1 ...] > OUT_FILE" 1>&2;
	echo "  -help             print this text" 1>&2;
	echo "  -f SOURCE_FORMAT  supported formats: GrAF (more to come)" 1>&2;
	echo "  -t TARGET_FORMAT  supported formats: POWLA" 1>&2;
	echo "  FILE [FILE1 ...]  files to be read from (*.anc for GrAF)" 1>&2;
	echo "  OUT_FILE          output file" 1>&2;
else

############################
# identify argument files  #
############################

ARGFILES=`for arg in $*; do
	if [ -e $arg ]; then echo $arg; fi;
done;`;

echo check arguments $* 1>&2;
echo found $ARGFILES 1>&2

#####################################
# -f GrAF: convert from GrAF to RDF #
#####################################

if echo $* | grep -i '\-f GrAF' >& /dev/null; then
	echo "convert GrAF to RDF" 1>&2;
	java -classpath $CLASSPATH powla.convert.DisambiguateGrAFIDs $ARGFILES;
	FILE_NAME=`echo $ARGFILES | sed -e s/'[ \t].*'// -e s/'.*[^\/\\]'//g`;
	java -Xmx1000m -classpath $CLASSPATH powla.convert.GrAF2POWLA graf=http://www.anc.org/graf/$FILE_NAME $ARGFILES;

# else: print help
else if echo $* | grep -v -i "\-compile" >&/dev/null; then
    bash $0 -help;
fi;
fi;
fi;

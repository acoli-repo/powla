#!/bin/bash
# synopsis: convert.sh -f SOURCE_FORMAT -t TARGET_FORMAT FILE [FILE1 [FILE2 ...]] > OUT_FILE
# argument files should not contain whitespace or special characters

####################
# define classpath #         (depending on where this file was called from)
####################

LOCAL_DIR=`echo $0 | sed s/'\/[^\/]*$'//`;
if echo $LOCAL_DIR | egrep '[a-zA-Z0-9]' >& /dev/null; 
then echo >&/dev/null;
else if [-e ./$0 ]; then LOCAL_DIR=./; else LOCAL_DIR=`whereis $0 | grep -m 1 $0 | sed -e s/'^[^\/]*'// -e s/'\/[^\/]*$'/'\/'/`; fi;
fi;

CLASSPATH_SEPARATOR=":"; # un*x
if echo $OSTYPE | egrep -i 'cygwin' >& /dev/null; then CLASSPATH_SEPARATOR=";";
CLASSPATH=`for file in $(find $LOCAL_DIR/java/lib | egrep 'jar$|zip$'); do echo $file; done | \
perl -e 'while(<>) { s/\n/'$CLASSPATH_SEPARATOR'/gs; print; }'; echo $LOCAL_DIR/java/bin;`;

############################
# -h, -?, etc.: print help #
############################

if echo $* | grep -i '\-h\|\-?' >&/dev/null; 
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

#####################################
# -f GrAF: convert from GrAF to RDF #
#####################################

if echo $* | grep -i '\-f GrAF' >& /dev/null; then
	echo "convert GrAF to RDF" 1>&2;
	java -Xmx1000m -classpath $CLASSPATH powla.convert.GrAF2POWLA graf=http://www.anc.org/graf/ $ARGFILES 

# else: print help
else bash $0 -help;

fi;
fi;
fi;
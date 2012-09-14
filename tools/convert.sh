#!/bin/bash
# synopsis: convert.sh [-compile] -f SOURCE_FORMAT -t TARGET_FORMAT FILE [FILE1 [FILE2 ...]] [-o OUT_FILE] [-e LOG_FILE]
# argument files should not contain whitespace or special characters

####################
# global variables #
####################
CORPUS=MASC-1.0.3;

#########################
# place debug info here #
#########################

####################
# define classpath #         (depending on where this file was called from)
####################

LOCAL_DIR=`echo $0 | sed s/'\/[^\/]*$'//`;
if echo $LOCAL_DIR | egrep '[a-zA-Z0-9]' >& /dev/null; then 
	echo >&/dev/null;
else 
	if [ -e ./$0 ]; then 
		LOCAL_DIR=./; 
	else 
		LOCAL_DIR=`whereis $0 | grep -m 1 $0 | sed -e s/'^[^\/]*'// -e s/'\/[^\/]*$'/'\/'/`; 
	fi;
fi;

CLASSPATH_SEPARATOR=":"; # un*x
if echo $OSTYPE | egrep -i 'cygwin' >& /dev/null; then 
	CLASSPATH_SEPARATOR=";";
fi;
CLASSPATH=`for file in $(find $LOCAL_DIR/java/lib | egrep 'jar$|zip$' | grep -v '\.svn'); do echo $file; done | \
perl -e 'while(<>) { s/\n/'$CLASSPATH_SEPARATOR'/gs; print; }'; echo $LOCAL_DIR/java/bin;`;

############################
# -compile: compile        #
############################
if echo $* | egrep "\-compile" >&/dev/null; then
    echo compile java converter 1>&2;
    mkdir $LOCAL_DIR/java/bin >&/dev/null;
    javac -classpath $CLASSPATH -sourcepath $LOCAL_DIR/java/src -d $LOCAL_DIR/java/bin `find $LOCAL_DIR/java/src/ | grep 'java$'`;
fi;

############################
# -h, -?, etc.: print help #
############################

if echo $* | egrep -i "\-h|\-\?" >&/dev/null; then
	echo "synopsis:" 1>&2;
	echo "   "$0" -help|-?|-h" 1>&2;
	echo "   "$0" -f SOURCE_FORMAT -t TARGET_FORMAT FILE [FILE1 ...] [-o OUT_FILE] [-e LOG_FILE]" 1>&2;
	echo "  -help             print this text" 1>&2;
	echo "  -f SOURCE_FORMAT  supported formats: GrAF (more to come)" 1>&2;
	echo "  -t TARGET_FORMAT  supported formats: POWLA" 1>&2;
	echo "  FILE [FILE1 ...]  files to be read from (*.anc for GrAF)" 1>&2;
	echo "  OUT_FILE          output file (default: stdout)" 1>&2;
	echo "  LOG_FILE          log file (default: stderr)" 1>&2;
else

##############################
# redirect stdout and stderr #
##############################

	if echo $* | egrep -i "\-e " >&/dev/null; then 
		STDERR=`echo $* | sed -e s/'.*\-e\s*'// -e s/'\s.*'//`; 
		echo redirect stderr to $STDERR 1>&2;
		rm $STDERR >&/dev/null;
	fi;
	if echo $* | egrep -i "\-o " >&/dev/null; then 
		STDOUT=`echo $* | sed -e s/'.*\-o\s*'// -e s/'\s.*'//`; 
		echo redirect stdout to $STDOUT 1>&2;
		rm $STDOUT >&/dev/null;
	fi;

############################
# identify argument files  #
############################

	ARGFILES=`for arg in $*; do
		if [ -e $arg ]; then echo $arg | grep 'anc$'; fi;
	done;`;

	echo check arguments $* 1>&2;
	echo found $ARGFILES 1>&2

#####################################
# -f GrAF: convert from GrAF to RDF #
#####################################

	MEMFLAGS=-Xmx5g
	if echo $* | grep -i '\-f GrAF' >& /dev/null; 
	then echo "convert GrAF to RDF" 1>&2;
		java -classpath $CLASSPATH powla.convert.DisambiguateGrAFIDs $ARGFILES;
		FILE_NAME=`echo $ARGFILES | sed s/'[ \t]'/'\n'/g | egrep -m 1 '\/' | sed s/'.*\/'//g`; 
		URI=http://www.anc.org/graf/$CORPUS/$FILE_NAME;
		echo using URI $URI 1>&2;
		if [ -n $STDERR ]; then
			if [ -n $STDOUT ]; then
				echo 'java '$MEMFLAGS' -classpath '$CLASSPATH' powla.convert.GrAF2POWLA graf='$URI $ARGFILES '2>'$STDERR '| grep -v "in ch.qos.logback." >'$STDOUT 1>&2;
				java $MEMFLAGS -classpath $CLASSPATH powla.convert.GrAF2POWLA graf=$URI $ARGFILES 2>$STDERR | grep -v "in ch.qos.logback." >$STDOUT;
			else
				echo 'java '$MEMFLAGS' -classpath '$CLASSPATH' powla.convert.GrAF2POWLA graf='$URI $ARGFILES '2>'$STDERR 1>&2;
				java $MEMFLAGS -classpath $CLASSPATH powla.convert.GrAF2POWLA graf=$URI $ARGFILES 2>$STDERR;
			fi;
		else 
			if [-n $STDOUT ]; then
				echo 'java '$MEMFLAGS' -classpath '$CLASSPATH' powla.convert.GrAF2POWLA graf='$URI $ARGFILES '| grep -v "in ch.qos.logback." >'$STDOUT 1>&2;
				java $MEMFLAGS -classpath $CLASSPATH powla.convert.GrAF2POWLA graf=$URI $ARGFILES | grep -v "in ch.qos.logback." >$STDOUT;
			else 
				echo 'java '$MEMFLAGS' -classpath '$CLASSPATH' powla.convert.GrAF2POWLA graf='$URI $ARGFILES 1>&2;
				java $MEMFLAGS -classpath $CLASSPATH powla.convert.GrAF2POWLA graf=$URI $ARGFILES
			fi;
		fi;

	# else: print help
	else 
		if echo $* | grep -v -i "\-compile|\-e |\-o " >&/dev/null; then
			bash $0 -help;
		fi;
	fi;
fi;
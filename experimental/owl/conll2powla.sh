#!/bin/bash
# reads a CoNLL file from base uri
# provide CoNLL parameters as args
# produces CoNLL-RDF
# convert to POWLA

# 0. prep JAVA
##############
# set classpath manually if you know what you're doing

# determines the classpath, updates class files if necessary and runs the specified java class with the provided arguments
HOME=`echo $0 | sed s/'[^\/]*$'//`./; 
CLASSPATH=$HOME":"`find $HOME/lib | perl -pe 's/\n/:/g;' | sed s/':$'//`;
if [ $OSTYPE = "cygwin" ]; then
	CLASSPATH=`echo $CLASSPATH | sed s/':'/';'/g;`;
fi;
JAVA="java -Dfile.encoding=UTF8 -classpath "$CLASSPATH;

if
	# 1. read data from base uri
	############################
	
	w3m -dump_source $1 | \
	\
	# 2. parse CoNLL to CoNLL-RDF
	#############################
	$JAVA org.acoli.conll.rdf.CoNLLStreamExtractor $* | \
	\
	# 3. load the conllrdf mapping and add powla data structures
	############################################################
	$JAVA org.acoli.conll.rdf.CoNLLRDFUpdater \
		-custom \
		-model conllrdf.owl http://purl.org/powla/ 	\
		-updates \
			sparql/infer-powla.sparql \
			sparql/conllrdf2powla.sparql | \
	\
	# 4. (optional) remove redundant conll data structures
	#####################################################
	$JAVA org.acoli.conll.rdf.CoNLLRDFUpdater \
		-custom \
		-model conllrdf.owl http://purl.org/powla/ 	\
		-updates \
			sparql/prune-powla.sparql \
			sparql/shrink-powla.sparql

then
	echo done 1>&2;
else
	echo CoNLL RDF failed, get it from https://github.com/acoli-repo/conll-rdf and add to the classpath 1>&2
fi;

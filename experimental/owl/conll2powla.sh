#!/bin/bash
# reads a CoNLL file from base uri
# provide CoNLL parameters as args
# produces CoNLL-RDF
# convert to POWLA

# 0. setup
##########

HOME=`realpath $(dirname $0)`
cd $HOME;
if not make >& make.log; then
	cat make.log 1>&2;
	exit 1
fi;

LOAD='conll-rdf/run.sh CoNLLStreamExtractor'
UPDATE='conll-rdf/run.sh CoNLLRDFUpdater -custom'

	if
		# 1. read data from base uri
		############################

		w3m -dump_source $1 | \
		\
		# 2. parse CoNLL to CoNLL-RDF
		#############################
		$LOAD $* | \
		\
		# 3. load the conllrdf mapping and add powla data structures
		############################################################
		$UPDATE -model conllrdf.owl http://purl.org/powla/ 	\
			-updates \
				sparql/infer-powla.sparql \
				sparql/conllrdf2powla.sparql | \
		\
		# 4. (optional) remove redundant conll data structures
		#####################################################
		$UPDATE -model conllrdf.owl http://purl.org/powla/ 	\
			-updates \
				sparql/prune-powla.sparql \
				sparql/shrink-powla.sparql

	then
		echo done 1>&2;
	else
		echo CoNLL RDF failed, get it from https://github.com/acoli-repo/conll-rdf and add to the classpath 1>&2
	fi;

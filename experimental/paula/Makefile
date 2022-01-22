all: paula-xml data samples

samples: conll-rdf
		for dir in `find data | grep 'xml$$' | sed s/'\/[^\/]*$$'// | sort -u`; do \
			tgt=`echo $$dir | sed s/'data'/samples/`;\
			tgtdir=`dirname $$tgt`;\
			if [ ! -e $$tgtdir ]; then mkdir -p $$tgtdir ; fi; \
			python3 paula2rdf.py $$dir > $$tgt.ttl;\
			# python3 paula2rdf.py $$dir -conll | \
			# egrep '.' | grep -v '^#' | \
			# conll-rdf/run.sh CoNLLRDFFormatter > $$tgt.conll.ttl;\
		done;

conll-rdf:
	# clone CoNLL-RDF
	# NOTE: to avoid having multiple copies, create conll-rdf as a symlink to an
	#       existing installation before calling make
	git clone https://github.com/acoli-repo/conll-rdf

data:
	mkdir data/
	cd data/;\
	wget https://corpus-tools.org/corpora/pcc2_PAULA.zip;\
	unzip pcc2_PAULA.zip

paula-xml:
	git clone https://github.com/korpling/paula-xml

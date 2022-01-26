all: paula-xml data samples

samples: samples/DDB_paula.ttl samples/pcc2_v6_PAULA

samples/DDB_paula.ttl: conll-rdf data
	# example workflow for phrase structure syntax
	# also illustrates sentence boundary induction
	# this is reasonably fast
	for dir in `find data/DDB* | grep 'xml$$' | sed s/'\/[^\/]*$$'/'\/'/ | sort -u`; do \
		tgt=`echo $$dir | sed -e s/'data'/samples/ -e s/'\/*$$'//;`;\
		tgtdir=`dirname $$tgt`;\
		echo $$tgt.ttl; \
		if [ ! -e $$tgtdir ]; then mkdir -p $$tgtdir ; fi; \
		# (a) convert PAULA to POWLA \
		python3 paula2rdf.py $$dir | tee $$tgt.ttl | \
		# (b) export POWLA as CoNLL-RDF \
		egrep '[^\s]' | grep -v '^#'| \
		# strip empty lines \
		conll-rdf/run.sh CoNLLRDFUpdater -custom \
				-updates \
					induce-nif-sentences.sparql \
				powla2conll.sparql  \
				remove-powla.sparql | \
		conll-rdf/run.sh CoNLLRDFFormatter | \
		# this step is necessary only for visualizing $$tgt.conll.ttl \
		tee $$tgt.conll.ttl | \
		conll-rdf/run.sh CoNLLRDFFormatter \
			-conll \
						ID WORD \
						LEMMA POS MORPH \
						FUNC \
						MERGED TIGER \
						REFERENTIALITY TYPE ANAPHOR_TYPE AMBIGUITY GRAMMATICAL_ROLE NP_FORM COMPLEX_NP DIR_SPEECH PHRASE_TYPE   \
						NP PP SENT HEADING INF_STAT TOPIC FOCUS_NEWINF \
						RST \
						;\
	done;

samples/pcc2_v6_PAULA: conll-rdf data
		# example workflow for multi-layer annotations, induction of sentence boundaries disabled
		# with so many layers of annotations, this is terribly slow, can be >10 min
		# for real-world applications, process annotation layers individually
		for dir in `find data/pcc* | grep 'xml$$' | sed s/'\/[^\/]*$$'/'\/'/ | sort -u`; do \
			tgt=`echo $$dir | sed -e s/'data'/samples/ -e s/'\/*$$'//;`;\
			tgtdir=`dirname $$tgt`;\
			echo $$tgt.ttl; \
			if [ ! -e $$tgtdir ]; then mkdir -p $$tgtdir ; fi; \
			# (a) convert PAULA to POWLA \
			python3 paula2rdf.py $$dir | tee $$tgt.ttl | \
			# (b) export POWLA as CoNLL-RDF \
			egrep '[^\s]' | grep -v '^#'| \
			# strip empty lines \
# #			conll-rdf/run.sh CoNLLRDFUpdater -custom \
#				-updates \
#					induce-nif-sentences.sparql | \
#			# the preceding step is optional, and will induce sentence boundaries *between trees*, not applicable to PCC2 data\
#			# because this provides RST trees that include the entire text \
			conll-rdf/run.sh CoNLLRDFUpdater -custom \
				-updates \
					powla2conll.sparql  \
				  remove-powla.sparql | \
			conll-rdf/run.sh CoNLLRDFFormatter | \
			# this step is necessary only for visualizing $$tgt.conll.ttl \
			tee $$tgt.conll.ttl | \
			conll-rdf/run.sh CoNLLRDFFormatter \
				-conll \
						  ID WORD \
							LEMMA POS MORPH \
							FUNC \
							MERGED TIGER \
							REFERENTIALITY TYPE ANAPHOR_TYPE AMBIGUITY GRAMMATICAL_ROLE NP_FORM COMPLEX_NP DIR_SPEECH PHRASE_TYPE   \
							NP PP SENT HEADING INF_STAT TOPIC FOCUS_NEWINF \
							RST \
							;\
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

	if [! -e DDB_paula.zip ]; then wget https://www.laudatio-repository.org/download/format/5/43/1.0 -O DDB_paula.zip; fi;
	mkdir data/DDB_paula;
	unzip -j DDB_paula.zip -d data/DDB_paula;
	unzip -j data/DDB_paula/*zip -x '*Spec*' '*FNHD*' '*Hench[^I]*' '*HenchI[IVX]*' '*exmaralda*' -d data/DDB_paula;

paula-xml:
	git clone https://github.com/korpling/paula-xml

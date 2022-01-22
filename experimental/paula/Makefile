all: paula-xml data samples

samples:
		for dir in `find data | grep 'xml$$' | sed s/'\/[^\/]*$$'// | sort -u`; do \
			tgt=`echo $$dir | sed s/'data'/samples/`;\
			tgtdir=`dirname $$tgt`;\
			if [ ! -e $$tgtdir ]; then mkdir -p $$tgtdir ; fi; \
			python3 paula2rdf.py $$dir > $$tgt.ttl;\
		done;

data:
	mkdir data/
	cd data/;\
	wget https://corpus-tools.org/corpora/pcc2_PAULA.zip;\
	unzip pcc2_PAULA.zip

paula-xml:
	git clone https://github.com/korpling/paula-xml

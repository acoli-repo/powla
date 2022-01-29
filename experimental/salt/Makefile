all: samples
	cd ../paula; make

samples:
	mkdir -p samples/tmp

	# TreeTagger
	wget https://www.laudatio-repository.org/download/format/9/31/1.0 -O samples/tmp/maerchenkorpus.zip
	unzip -j samples/tmp/maerchenkorpus.zip -d samples/tmp/treetagger
	mv samples/tmp/treetagger/*zip samples/maerchen.zip

	# CoraXML
	if [ ! -e samples/tmp/rem.zip ]; then wget https://www.laudatio-repository.org/download/format/20/36/1.0 -O samples/tmp/rem.zip; fi
	unzip -j samples/tmp/rem.zip -d samples/tmp/coralled
	unzip -j samples/tmp/coralled/*zip rem-corralled-20161222/M543-N1.xml rem-corralled-20161222/cora-xml.dtd -d samples/rem
	mv samples/rem/M543-N1.xml samples/rem/M543-N1.xml

	# Exmaralda
	if [ ! -e samples/tmp/swc.zip ]; then wget https://www.laudatio-repository.org/download/format/6/15/1.0 -O samples/tmp/swc.zip; fi
	unzip -j samples/tmp/swc.zip -d samples/tmp/exm
	mv samples/tmp/exm/*zip samples/swc.zip

	# Excel
	if [ ! -e samples/tmp/tuniz.zip ]; then wget https://www.laudatio-repository.org/download/format/3/101/1.0 -O samples/tmp/tuniz.zip; fi
	unzip -j samples/tmp/tuniz.zip -d samples/tmp/xls
	mv samples/tmp/xls/*zip samples/tuniz.zip

	# Gate
	if [ ! -e samples/tmp/germanc.zip ]; then wget https://www.laudatio-repository.org/download/format/26/55/1.0 -O samples/tmp/gate.zip; fi
	unzip -j samples/tmp/gate.zip -d samples/tmp/gate
	mv samples/tmp/gate/*7z samples/germanc.7z

	rm -rf samples/tmp

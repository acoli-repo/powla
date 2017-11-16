POWLA: Portable Linguistic Annotation with OWL

OWL2/DL vocabulary for linguistic annotations, grounded in the ISO TC37/SC4 Linguistic Annotation Framework and the PAULA XML standoff format.

POWLA is intended to represent any kind of linguistic data structures in an LOD/OWL-compliant way. It does *not* aim to model textual data nor the anchoring of annotations in textual data. Instead, it complements existing vocabularies such as Web Annotation/Open Annotation, NIF, CoNLL-RDF or mechanisms such as RDFa.

At the moment, POWLA is being updated for compliance with NIF 2.0, CoNLL-RDF and the Web Annotation Data Model

contents
========
owl/
	POWLA 1.0 ontology
data/
	sample data for POWLA 1.0 conversions
tools/
	converters to produce and validate POWLA 1.0 data
doc/
	documentation and publications
experimental/
	working drafts for POWLA 2.0
	
history
=======
* PAULA: XML standoff format (Dipper et al. 2007, Chiarcos et al. 2008), an implementation of ISO TC37/SC4 drafts
* POWLA 1.0: reconstruction of the PAULA data model in OWL/DL (Chiarcos 2012abc)
* POWLA 2.0: adaptation of POWLA 1.0 for compliance with Web Annotation/Open Annotation, NIF 2.0 and CoNLL-RDF

references for POWLA and its XML predecessors
==========
Christian Chiarcos, Stefanie Dipper, Michael Götze, Ulf Leser, Anke Lüdeling, Julia Ritz, and Manfred Stede (2008). A Flexible Framework for Integrating Annotations from Different Tools and Tagsets, TAL (Traitement automatique des langues) 49 (2).

Christian Chiarcos (2012a), POWLA: Modeling linguistic corpora in OWL/DL, In: E. Simperl et al. (eds.) Proceedings of the 9th Extended Semantic Web Conference (ESWC 2012). Springer, Heidelberg, Heraklion, Crete, May 2012 (LNCS 7295), 225-239. 

Christian Chiarcos 2012b), A Generic Formalism to Represent Linguistic Corpora in RDF and OWL/DL, In: 8th International Conference on Language Resources and Evaluation (LREC-2012). Istanbul, Turkey, May 2012, 3205-3212.

Christian Chiarcos (2012c), Interoperability of Corpora and Annotations, In: C. Chiarcos, S. Nordhoff, and S. Hellmann (eds.) Linked Data in Linguistics. Representing and Connecting Language Data and Language Metadata. Springer, Heidelberg.

Stefanie Dipper, Michael Götze, Uwe Küssner, and Manfred Stede (2007). Representing and querying standoff XML. In G. Rehm, A. Witt, and L. Lemnitzer, editors, Data Structures for Linguistic Resources and Applications, pages 337–346. Narr, Tübingen.

see also
========
http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core/nif-core.html
https://github.com/acoli-repo/conll-rdf
https://www.w3.org/TR/annotation-model/
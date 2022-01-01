# POWLA: Portable Linguistic Annotation with OWL

OWL2/DL vocabulary for linguistic annotations, grounded in the ISO TC37/SC4 Linguistic Annotation Framework.

POWLA is designed to represent any kind of linguistic data structures in an LOD/OWL-compliant way. It does *not* aim to model textual data nor the anchoring of annotations in textual data. Instead, it complements existing vocabularies such as Web Annotation/Open Annotation, NIF, CoNLL-RDF or mechanisms such as RDFa.

## contents

- [`owl/`](owl) POWLA 1.0 ontology
- [`data/`](data) sample data for POWLA 1.0 conversions
- [`tools/`](tools) converters to produce and validate POWLA 1.0 data
- [`doc/`](doc) documentation and publications
- [`experimental/`](experimental) working drafts for POWLA 2.0
	
## history

* 2007 PAULA/XML 0.9: XML standoff format (Dipper et al. 2007), an implementation of early (2004) ISO TC37/SC4 drafts
* 2008 PAULA 1.0: Development of a formal data model and minor revision of the XML standoff format (Chiarcos et al. 2008)
* 2012 POWLA 1.0: reconstruction of the PAULA data model in OWL/DL (Chiarcos 2012abc), published via [Sourceforge](https://sourceforge.net/projects/powla/)
* 2020 POWLA 2.0: adaptation of POWLA 1.0 for compliance with Web Annotation/Open Annotation, NIF 2.0 and CoNLL-RDF, originally published via [Sourceforge](https://sourceforge.net/projects/powla/)
* 2021-01-01: migration to GitHub

## POWLA and its XML predecessors

* POWLA 1.0:
	- Christian Chiarcos (2012a), POWLA: Modeling linguistic corpora in OWL/DL, In: E. Simperl et al. (eds.) Proceedings of the 9th Extended Semantic Web Conference (ESWC 2012). Springer, Heidelberg, Heraklion, Crete, May 2012 (LNCS 7295), 225-239.
	- Christian Chiarcos (2012b), Interoperability of Corpora and Annotations, In: C. Chiarcos, S. Nordhoff, and S. Hellmann (eds.) Linked Data in Linguistics. Representing and Connecting Language Data and Language Metadata. Springer, Heidelberg.
	- Christian Chiarcos 2012c), A Generic Formalism to Represent Linguistic Corpora in RDF and OWL/DL, In: 8th International Conference on Language Resources and Evaluation (LREC-2012). Istanbul, Turkey, May 2012, 3205-3212.

* XML predecessor: PAULA
	* PAULA is a data model that has been developed to represent (a) any type of linguistic annotation applicable to textual data, and (b) any combination of annotation layers. PAULA was originally serialized in a standoff XML format (PAULA XML) and as an RDBMS schema (relANNIS), it defines the semantics of the corpus query language [ANNIS-QL](https://corpus-tools.org/annis/), it has been used for NLP workflows [(MOTS)](https://link.springer.com/chapter/10.1007/978-3-642-22613-7_2) and format conversion [(Pepper, based on its Java implementation SALT)](https://github.com/korpling/pepper).
	* PAULA/XML 0.9:
		- Stefanie Dipper, Michael Götze, Uwe Küssner, and Manfred Stede (2007). Representing and querying standoff XML. In G. Rehm, A. Witt, and L. Lemnitzer, editors, Data Structures for Linguistic Resources and Applications, pages 337–346. Narr, Tübingen.
	* PAULA 1.0:
		- Christian Chiarcos, Stefanie Dipper, Michael Götze, Ulf Leser, Anke Lüdeling, Julia Ritz, and Manfred Stede (2008). A Flexible Framework for Integrating Annotations from Different Tools and Tagsets, TAL (Traitement automatique des langues) 49 (2).
	* PAULA 1.1/SALT:
		- After POWLA has been created, PAULA continued to be developed. The current XML format is described at the [PAULA XML page](https://github.com/korpling/paula-xml). The generic PAULA 1.0 data model has now been replaced by different implementation-specific variants [for XML serialization (PAULA XML data model)](https://github.com/korpling/paula-xml), and as [Java object model (SALT)](https://github.com/korpling/salt). POWLA 1.0 is yet another successor of the PAULA 1.0 data model. POWLA 2.0 is a reduced variant of POWLA 1.0 that is designed to complement other community standards for linguistic annotation on the web and that eliminates redundancies and incompatibilities with these.

* POWLA 2.0:
	- No reference publication for POWLA 2.0 has been published so far. The following papers describe the current status, but details may be updated until the formal release
	- Chiarcos, C., & Glaser, L. (2020, May). A Tree Extension for CoNLL-RDF. In Proceedings of the 12th Language Resources and Evaluation Conference (pp. 7161-7169).
	- Modelling Linguistic Annotations. Chap. 6 of Cimiano P., Chiarcos C., McCrae J.P., Gracia J. (2020) Linguistic Linked Data. Springer, Cham. https://doi.org/10.1007/978-3-030-30225-2_6

## Related vocabularies

POWLA 1.0 was designed as a stand-alone vocabulary to facilitate corpus queries in OWL and RDF. POWLA 2.0 is better integrated in the existing landscape of community standards for linguistic annotations on the web and primarily used for their extension with formal data structures for linguistic annotations. Designated host vocabularies include:

- [NIF](http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core/nif-core.html)
- [CoNLL-RDF](https://github.com/acoli-repo/conll-rdf)
- [Web Annotation](https://www.w3.org/TR/annotation-model/)

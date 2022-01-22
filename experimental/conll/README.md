# Towards POWLA 2.0 (experimental)

POWLA 2.0 is a slightly revised subset of POWLA 1.0, designed with the goal to complement other standards, especially CoNLL-RDF with linguistic data structures.
Numerous aspects of POWLA 1.0 were specifically designed to facilitate querying (inverse properties, navigational properties) and addressing primary text (offsets).
Both aspects will be moved into a separate ontology (powla-corpus).

## usage

setup with

  $> make

requires a Unix-style working environment, tested for Ubuntu 20.04L

demo with

  $> bash -e ./conll2powla-demo.sh

(converts a CoNLL file to POWLA, see that script how to process files on your own)

## concept (May 2018)

read CoNLL-RDF, write POWLA

this involves:
- nif:Sentence => powla:Nonterminal and powla:Root
- nif:Word => powla:Terminal
- nif:nextWord => powla:nextNode
- nif:nextSentence => powla:nextNode
- conll:HEAD => powla:hasParent
- keep everything else, do not create layers or document nodes (tbc: as blank nodes?)

idea:
we separate the mapping from CoNLL-RDF to POWLA into
- OWL: a mapping (rdfs:subPropertyOf/rdfs:subClassOf) defined in an ontology, i.e., conllrdf.owl,
- GENERIC SPARQL: a generic SPARQL transformation that replaces these classes and properties with their powla super classes/properties
- FORMAT-SPECIFIC SPARQL: CoNLL-specific treatment of format specifics, e.g., object properties in conll as produced for SRL

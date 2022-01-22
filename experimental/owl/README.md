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

## POWLA status (late 2021)

we list the vocabulary elements known to be used by existing tools and in publications:

- [CoNLL-RDF tree extension](https://github.com/acoli-repo/conll-rdf/blob/master/src/main/java/org/acoli/conll/rdf/CoNLLBrackets2RDF.java)
  - powla:next, powla:hasParent, powla:Node
- Cimiano et al. (2020, Chap. 6):
  - obligatory: powla:next [todo: should be recommended, not obligatory]
  - recommended (Node): powla:Node, powla:hasParent, powla:hasLayer
  - recommended (Terminal): powla:end, powla:start, powla:string
  - recommended (Relation): powla:Relation, powla:hasTarget, powla:hasSource
  - optional reflexives: powla:hasChild (= ^powla:hasParent), powla:isSourceOf (= ^powla:hasSource), powla:isTargetOf (= ^powla:hasTarget), powla:previous (= ^powla:next)
  - optional Node classes: powla:Terminal, powla:Nonterminal, powla:Root
  - optional corpus structure: powla:DocumentLayer sub powla:Layer, powla:Corpus sub powla:Document
  - changes: powla:hasStringValue => powla:string

Our vocabulary version is the one of Cimiano et al. 2020.

## defining profiles

- three different profiles
  - core vocabulary for information exchange
    - powla:hasTarget, powla:hasSource, powla:Relation, powla:Node, powla:start, powla:string
    - NOTE: if these concepts overlap with that of host vocabularies, these should be defined as subclasses and subproperties of POWLA concepts and properties, e.g., for CoNLL-RDF:
      - nif:Word sub powla:Terminal (sub powla:Node)
      - conll:ID sub powla:start
      - conll:WORD, conll:FORM sub powla:string
  - querying
    - powla:Terminal (can be inferred from the absence of children)
    - powla:Root (can be inferred from the absence of parents)
    - powla:Nonterminal (powla:Node that is neither terminal nor root)
    - powla:next of terminals (can be inferred from ^parent and start)
    - powla:end of terminals (in information exchange, this can be extrapolated from powla:start and powla:next)
    - powla:end and powla:start for nonterminals (facilitates inclusion queries)
  - corpus management
    - obligatory: powla:hasLayer, powla:DocumentLayer, powla:Layer, powla:hasDocument, powla:Document
- automated conversion:
  - core > query (expansion)
  - query > core (filtering)
  - core+corpus > querying+corpus (corpus extensions untouched)
  - query+corpus > core+corpus (-"-)

## simplifications for POWLA 2.0

- rename properties
  - powla:hasTarget => powla:target
  - powla:hasSource => powla:source
  - powla:hasParent => powla:parent
  - powla:hasChild => powla:child (?)
  - powla:hasLayer => powla:layer
  - etc.
- rename classes
  - powla:Document => powla:DataSet, with Document a subclass
  - powla:hasSubDocument => powla:subSet

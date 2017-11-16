experimental:
- improve integration with / import from CoNLL-RDF, Web Annotation and NIF

this *may* require adjustments to the ontology, hence, owl/ contains both initial converter prototypes and a copy of powla.owl

so far, powla.owl is left unchanged

adjustments for POWLA 2.0:
- deprecate subclasses of powla:Relation
- powla:hasStringValue => powla:string
- powla:nextNode => powla:next
- powla:endPosition => powla:end
- powla:startPosition => powla:start
- powla:firstTerminal => powla:first
- powla:lastTerminal => powla:last
- organize embedding via first/last and start/end: subclasses of Nonterminals
- split ontology in core ontology (remove inverse and transitive props, no begin/end, first/last etc.) and extended ontology (incl. inverses, transitives, start/end OR first/last)
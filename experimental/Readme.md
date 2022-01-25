# preparations for POWLA 2.0

## experimental
- improve integration with / import from CoNLL-RDF
- Web Annotation and NIF to come
- no mapping-specifics in the code:
  mapping RDFS-inferred from ontologies that formalize the dependencies between formats as rdfs:subClassOf, etc.

this *may* require adjustments to the ontology, hence, owl/ contains both initial converter prototypes and a copy of powla.owl

so far, powla.owl is left unchanged

## adjustments for POWLA 2.0
- deprecate subclasses of powla:Relation
- powla:hasStringValue => powla:string
- powla:nextNode => powla:next
- powla:endPosition => powla:end
- powla:startPosition => powla:start
- powla:firstTerminal => powla:first
- powla:lastTerminal => powla:last
- organize embedding via first/last and start/end: subclasses of Nonterminals
- split ontology in core ontology (remove inverse and transitive props, no begin/end, first/last etc.) and extended ontology (incl. inverses, transitives, start/end OR first/last)

## open questions
- hierarchically organized layers?
  - we could introduce `powla:subLayer`
  - PRO: these occur in PAULA annotations, e.g., the `mmax` layer of PCC2, in the distinction between layers and files. In the available data, this distinction is limited to a single layer with multiple files.
    - CON: they are not encoded in the format, but indirectly via the file system. The (implicit!) PAULA distinction is currently preserved as (implicit!) information in POWLA URIs.
  - PRO: The formalism for PAULA annosets allows to express hierarchies over multiple layers.
    - CON: No sample data at hand, reconsider upon request.
  - PRO: ELAN has a similar notion, but with different semantics: a sub-layer inherits the segmentation of its super-layer).
    - CON: No real-world sample data at hand, reconsider upon request.
  - decision: no action unless requested and/or real-world sample data is provided.

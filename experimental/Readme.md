# preparations for POWLA 2.0

## experimental
- improve integration with / import from CoNLL-RDF
- Web Annotation and NIF to come
- no mapping-specifics in the code:
  mapping RDFS-inferred from ontologies that formalize the dependencies between formats as rdfs:subClassOf, etc.
- converters to demonstrate expressive equivalence with PAULA (`paula/`), SALT (`salt/`), etc.
- CoNLL generation to debug demonstrators

This *may* require adjustments to the ontology, hence, `owl/` contains an updated version of powla.owl (must be compared with ../powla.owl before publication, to synchronize comments)

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
- keep, but deprecate powla:MarkableLayer, etc. In CoNLL export, these are very helpful temporary categories to orgaize the input

## open questions
- hierarchically organized layers?
  - we could introduce `powla:subLayer`
  - PRO: these occur in PAULA annotations, e.g., the `mmax` layer of PCC2, in the distinction between layers and files. In the available data, this distinction is limited to a single layer with multiple files.
    - CON: they are not encoded in the format, but indirectly via the file system. The (implicit!) PAULA distinction is currently preserved as (implicit!) information in POWLA URIs, so there is no information loss in the current modelling.
  - PRO: The formalism for PAULA annosets allows to express hierarchies over multiple layers.
    - CON: No sample data at hand, reconsider upon request.
  - PRO: ELAN has a similar notion, but with different semantics: a sub-layer inherits the segmentation of its super-layer).
    - CON: No real-world sample data at hand, reconsider upon request.
  - ACTION: none; no action unless requested and/or real-world sample data is provided.
- for performant querying, aggregation and transformation, we need more coarse-grained navigational segments equivalent to `nif:Sentence` in CoNLL-RDF. Suggestion: `powla:Segment` (this is to be preferred over `powla:Sentence` because it is technically motivated [limiting search space], not linguistically [beiing sentential]). A `nif:Sentence` as in CoNLL-RDF would then be `rdfs:subClassOf powla:Segment`.

## problems

### sequential order by properties

The experiments here follow CoNLL-RDF in modelling sequential order exclusively by means of `powla:nextTerm`, resp. `nif:nextWord`. However, for the processing of corpora, this is problematic, as corpus-wide search for transitive succession queries becomes quickly intractable. Moreover, offsets cannot be easily derived from `powla:nextTerm`, either, because already aggregate counts may time out.

In fact for many sample data in the `salt` directory, CoNLL export failed for that reason (with a stack overflow error).

There are two possible solutions:

- `powla:start` with integer offsets (can be problematic if empty elements are being inserted, also, for every change, we need to recalculate all indices; also, this would not be downward-compatible with CoNLL-RDF)
- splitting the input, e.g., into `nif:Sentence`s, and querying for order locally only (that would be compatible with current CoNLL-RDF practices)

Note that such a secondary split does not have to coincide with sentence breaks on other layers (e.g., tree annotations). (Unlike CoNLL, there are no structural constraints.) However, a CoNLL export may contain unexpected information then.

As a possible way out, we can create different POWLA profiles and converters between them, say, a document profile (roughly equivalent with CoNLL-RDF, no obligatory corpus overhead, sequential order by properties), and a corpus profile (roughly equivalent with relANNIS, using an indexing scheme to encode sequential organization).

Yet another possibility is a splitter functionality which inserts segment boundaries for tokens with specific string value. This would be equivalent with `nif:Sentence`, but to stay agnostic about the linguistic interpretation of these structures, we should better call them `nif:Segment`. The order of `nif:Segment`s can be encoded with `powla:next`, as these should be top-level elements in their respective layer.

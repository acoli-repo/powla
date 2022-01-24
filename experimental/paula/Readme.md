# PAULA conversion

setup and demo:

    $> make

see samples/ for converted data

To convert a directory containing PAULA files, say, my-paula-dir/, run

    $> python3 paula2rdf.py http://put-your-base-uri.here/ my-paula-dir > output.ttl

If only one argument is provided, it must be directory. Then, it will also be used as base URI.

By default, we produce "pure" POWLA. Alternatively, you can create CoNLL-RDF:

    $> python3 paula2rdf.py http://put-your-base-uri.here/ my-paula-dir --conll_rdf > output.ttl

## PAULA to POWLA

Conversion for marks, structs, feats, rels and toks, tested on PCC2 corpus.

- we map tokens to powla:Terminals, structs and markables to powla:Nodes with powla:hasParent relations
- layer structure is only preserved if an explicit annoset is provided.
- we do not preserve the difference between markable layer, token layer, text layer, struct layer. However, this can be recovered from the structure of annotations within a layer (struct layers are recursive or have labelled edges, mark layers are spans over tokens, tok layer contains only terminals)
- we provide feats as datatype properties, for each feat, we create a `powla:hasAnnotation` sub-property in the `paula:` namespace. Note if different annotation layers use the same feat name, these are not distinguished in POWLA (however, they may be distinguishable from the types of elements they pertain to)

remarks:
- we provide core information only, derivable information is omitted
- XPointer interpretation is not thoroughly tested. The full scope of XPointer support in PAULA is unclear. Note that XPointer is not a spec, but only a draft, and there seem to be some PAULA extensions (parentheses).
- we do not preserve PAULA namespaces, as these are mere naming conventions for individual files. However, the file names are preserved in URIs, so this information can be recovered.
- in annotations, we replace `"` by `&quot;` and `&` by `&amp;`
- PAULA is not limited to sentences as data types. When CoNLL is converted to PAULA and converted back, there is no reliable way of recovering sentence structure (unless you define a source-specific pattern). POWLA can represent sentences, but we do not attempt to recover them from arbitrary PAULA input.

## Limitations

- we require that all PAULA files reside in the same directory. no subdirectories supported
- tested only for PCC2
- the CoNLL-RDF export generates only nif:Word and nif:nextWord, but no nif:Sentence, because these are not explicitly annotated in PAULA
- the usage of XPointer in PAULA-XML is not fully consistent, we tried to make our XPointer parser robust, but it does not cover the full language
  as an example, PCC2 contains *sequences of* xpointer fragments like

      (#xpointer(id('tok_30')/range-to(id('tok_33'))),#xpointer(id('tok_35')/range-to(id('tok_38'))),#xpointer(id('tok_40')/range-to(id('tok_48'))))

  PAULA issues:
  - there should not be a parenthesis around fragment identifiers
  - are comma-separated xpointers in line with the XPointer specification?
  - the comma-separated xpointers include the `#` prefix. this is incorrect, the `#` should go before all xpointers
  - we have workarounds for these issues, but it is neither clear whether we cover the full XPointer fragment of PAULA nor whether valid XPointer fragments will break because of these PAULA-specific workarounds

## POWLA to CoNLL

This is mostly for validation of conversion results.

- we infer optional POWLA structures from recommended POWLA structures
- we infer TokenLayer, StructLayer and MarkLayer and encode them differently
  - TokenLayer: each annotation as string values in a separate column, using PAULA XML attribute names
  - MarkLayer: each annotation as IOBES-encoded string values in separate columns, using PAULA XML attribute names
  - StructLayer: each layer encoded as PTB-style tree in a single column, column name is derived from `rdfs:label` of StructLayer or from filename (as preserved in URI); seondary edges are processed like dependencies (RelLayer).
  - RelLayer: one or multiple pairs of `HEAD` and relation (as in CoNLL-U `DEPS`). Not implemented yet
- treat every original PAULA annoset as a single sentence (no principled way to split it from PAULA input)

remarks:
- we generate PTB-style trees, but we do not validate whether powla:hasParent relations constitute a single tree. For discontinuous elements, these are silently expanded to the full extent.
- we provide annotations of POWLA nodes as `|`-separated set (unsorted) of attribute-value pairs. As we explicitly encode the attribute, this is more verbose than conventional PTB trees in CoNLL that only provide the value.
- PTB encoding in CoNLL cannot distinguish node and relation annotations. both as presented as node-level annotations, here.
- if multiple sets of structs overlap in their nodes (including tokens!), all annotations on these shared nodes will be provided in both trees, because the original annotation layer of individual `powla:hasAnnotation` subproperties is not tracked. To keep them separate, avoid node sharing and/or annotating tokens with struct-specific annotations. 

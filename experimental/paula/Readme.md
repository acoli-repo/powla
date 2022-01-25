# PAULA conversion

setup

    $> make

demo (PAULA parsing and CoNLL generation, *slow*):

    $> make samples

see `samples/` for converted data

To convert a directory containing PAULA files, say, `my-paula-dir/` to POWLA/RDF, run

    $> python3 paula2rdf.py http://put-your-base-uri.here/ my-paula-dir > output.ttl

If only one argument is provided, it must be directory. Then, it will also be used as base URI.

By default, we produce "pure" POWLA. Alternatively, you can create CoNLL-RDF:

    $> python3 paula2rdf.py http://put-your-base-uri.here/ my-paula-dir --conll_rdf > output.ttl

Note that PAULA does not mark sentence boundaries (only indirectly, in annotations), so we do not generate `nif:Sentence`s.

We provide a *much* more advanced (but much slower) way to generate CoNLL-RDF as part of CoNLL conversion, see `Makefile`. This not only allows to infer sentence boundaries and detect different types of annotations (markables/spans, structs/trees, relations and token-level annotations), but also converts then to a CoNLL representation that can be easily processed. To call this functionality directly, use

    $> ./paula2conll.sh my-paula-dir > output.conll

or

    $> ./paula2conll.sh my-paula.zip > output.conll

To enable heuristic sentence splitting, use the `-split` flag:

    $> ./paula2conll.sh -split my-paula-dir > output.conll

Note that CoNLL generation is rather slow as it involves complex restructuring. This is used for debugging purposes mostly, for actual processing, we recommend working with POWLA directly.

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
- as our XPointer support is incomplete, it is possible that some generated URIs are invalid. Export to CoNLL and re-import to fix.

## Limitations

- we require that all PAULA files reside in the same directory. no subdirectories supported
- tested only for PCC2
- the CoNLL-RDF export generates only nif:Word and nif:nextWord, but no nif:Sentence, because these are not explicitly annotated in PAULA
- the usage of XPointer in PAULA-XML is not fully consistent, we tried to make our XPointer parser robust, but it does not cover the full language
  as an example, PCC2 contains *sequences of* xpointer fragments like

      (#xpointer(id('tok_30')/range-to(id('tok_33'))),#xpointer(id('tok_35')/range-to(id('tok_38'))),#xpointer(id('tok_40')/range-to(id('tok_48'))))
- as a result, some generated URIs will be non-valid

  PAULA issues:
  - there should not be a parenthesis around fragment identifiers
  - are comma-separated xpointers in line with the XPointer specification?
  - the comma-separated xpointers include the `#` prefix. this is incorrect, the `#` should go before all xpointers
  - we have workarounds for these issues, but it is neither clear whether we cover the full XPointer fragment of PAULA nor whether valid XPointer fragments will break because of these PAULA-specific workarounds
  - PAULA (ab)uses the file system to encode annotation features. PAULA "namespaces" are naming conventions for files (i.e., the first "."-separated substring of the base name), but their encoding in the data is indirect and optional (annoset file).
  - Within an annotation layer, PAULA can have multiple tiers (markable files). These are distinguished by their `markList/@type`. This information is propagated down to the elements. (An alternative encoding could use sub-layers, but this is not currently supported by POWLA.)

## POWLA to CoNLL

We provide a [Fintan](https://github.com/Pret-a-LLOD/Fintan)/[CoNLL-RDF](https://github.com/acoli-repo/conll-rdf) pipeline to convert POWLA to CoNLL. This is a generic converter (for any PAULA/POWLA data) primarily intended for validating conversion results. Because it is a generic converter, it is not optimized for specific types of annotations and relatively slow.

- we infer optional POWLA structures from recommended POWLA structures
- we infer TokenLayer, StructLayer and MarkLayer and encode them differently
  - TokenLayer: each annotation as string values in a separate column, using PAULA XML attribute names
  - MarkLayer: each annotation as IOBES-encoded string values in separate columns, using PAULA XML attribute names
  - StructLayer: each layer encoded as PTB-style tree in a single column, column name is derived from `rdfs:label` of StructLayer or from filename (as preserved in URI); seondary edges are processed like dependencies (RelLayer).
  - RelLayer: one or multiple pairs of `HEAD` and relation (as in CoNLL-U `DEPS`). Column name is derived from PAULA XML attribute name of annotations.
- treat every original PAULA annoset as a single sentence (no principled way to split it from PAULA input)
- `induce-nif-sentences.sparql` extrapolates sentence boundaries from annotations, this exploits the (lack of) overlap between span (mark) and struct annotations. It will insert a sentence break before every non-overlapping span/tree. Only use this if span annotation marks or is aligned with clause boundaries.

remarks:
- POWLA generation is fast, but CoNLL export can be *very* slow. Converting even the small PCC2 corpus can take 10 minutes or more. This is for three reasons:
  (a) The PAULA/POWLA data model is much more generic than CoNLL. Different types of annotations (trees, markables, relations) require complex transformations, often involving aggregates. These aggregates can be time-consuming.
  (b) PAULA generates an unstructured graph, so that the resulting POWLA graph has no consistent segmentation into smaller processing units (say, sentences) that can be processed individually. Instead, search is always performed over the complete graph (PAULA annoset). For production mode, it is helpful to implement an annotation-specific splitter functionality.
  (c) The converter is designed to process *any* PAULA data without manual interference. This involves a considerable number of inferences that cover exceptional cases and may be omitted for specific applications. In particular, it will remove and infer `powla:nextTerm` and `powla:next` properties. When working with consistent POWLA data, this is not necessary.
- when using POWLA to convert PAULA to CoNLL, you can massively speed up the process by feeding each layer (annoset) individually (plus text/token). The resulting CoNLL files can be merged, e.g., by [CoNLL-Merge](https://github.com/acoli-repo/conll-merge) and the result of the conversion can be further processed by CoNLL-RDF
- the initial conversion functionality was developed on individual annotation layers, without annoset file. If the annoset may group together heterogeneous information that cannot be reduced to a single layer type (e.g., multiple struct layers, a struct layer and a mark layer or a struct layer and a rel layer), these will be treated like struct layers. However, this can lead to unexpected results, e.g.,

  - the CoNLL property is named after the annoset element URI, e.g., `MERGED`
  - annotations are being conflated into a single priperty (i.e., the one derived from the layer ID = annoset element, e.g., `MERGED`)

  To suppress this behavior, remove `*.anno.xml` and `*.anno_feats.xml` before conversion.

- we produce CoNLL-RDF data structures, but note that we keep the original URI scheme not that we provide conventional sentence segmentation. We provide the generated CoNLL-RDF data, but for illustration purposes only. Further processing should start from the generated CoNLL data, instead.
- we generate PTB-style trees, but we do not validate whether powla:hasParent relations constitute a single tree. For discontinuous elements, these are silently expanded to the full extent.
- we provide annotations of POWLA nodes as `|`-separated set (unsorted) of attribute-value pairs. As we explicitly encode the attribute, this is more verbose than conventional PTB trees in CoNLL that only provide the value.
- PTB encoding in CoNLL cannot distinguish node and relation annotations. both as presented as node-level annotations, here.
- if multiple sets of structs overlap in their nodes (including tokens!), all annotations on these shared nodes will be provided in both trees, because the original annotation layer of individual `powla:hasAnnotation` subproperties is not tracked. To keep them separate, avoid node sharing and/or annotating tokens with struct-specific annotations.
- relations without annotations are not returned
- for RelLayers, we preserve the original directionality. For dependency annotations in PCC2, this means we point from head to dependent (CoNLL-U points the other way).
- as we cannot identify a primary type of dependency syntax, we use the `conll:HEAD` property only for marking the `nif:Sentence`
- except for labelled edges in trees, CoNLL does not support natively support relations between elements other than words. secondary edges and coreference annotations are are thus propagated to the individual words (i.e., duplicated). This encoding is lossy and non-reversible, but specific to the CoNLL format. (If used in combination with span annotation, it is reversible *by hand*, if we identify the corresponding spans. For secondary edges, it is not reversible as we cannot reliably refer to the attachment point in the PTB tree.)
- Note that this kind of encoding does also apply to SRL annotations, we do not generate the conventional CoNLL notation for SRLs that would permit for m:1 relations as we cannot easily reduce the original m:n encoding to a m:1 encoding (where is the head?) and we risk using the same  
- if the same property is used to serve multiple functions, the resulting CoNLL column will aggregate hetetogeneous information, e.g.,

      :tok_117 conll:TYPE "21:anaphor_antecedent|I-anaphoric|22:anaphor_antecedent". # (PCC2, 4282)

    In this, this is all coming from coreference (`mmax`) annotation, but the relation annotations (`21:...`, `22:...`) originate from the `type` feature of original relations, whereas the span annotations (`I-...`) originate from the `type` feature of original markables.
- if multiple overlapping markable spans are annotated, these are preserved in the conll export, however, if their annotations are distributed across multiple properties (e.g., two annotations per original markable), the synchronization between these properties is lost. This information is preserved in POWLA, but cannot be reliably encoded in CoNLL. This occurs with `mmax` (coref) annotations from PCC2, e.g.,

        10      die               B-defnp      B-sbj
        11      Dallgower         S-ne|I-defnp S-other|I-sbj
        12      Gemeindevertreter E-defnp      E-sbj

    As the example shows, interdependencies between annotations of different markables can be recovered from IOBES annotation, but only if preceding and following lines are taken into consideration.
- During conversion, we discovered some previously undetected data quality issues with the existing PAULA data: `mmax` markables systematically include the preceding punctuation symbol. In combination with an annotation that groups punctuation signs together with the preceding word, this will disable sentence splitting.
- CoNLL does not have a standard encoding for intersentential links (co-indexing has been used, but this requires globally unambiguous IDs -- URIs provide this, but are too verbose in practice). Here, we adopt a convention earlier used within the Copenhagen Dependency Treebank: if an intersentential relation is to be encoded, we add a sentence offset, separated by `:`, before the target ID, e.g.,

        1       .         _
        2       Nun       _
        3       sollen    _
        4       sie       -1:6:anaphor_antecedent|-1:2:anaphor_antecedent|-1:3:anaphor_antecedent|-1:4:anaphor_antecedent|-1:5:anaphor_antecedent       
        5       zeigen    _       
        6       ,         _
        7       wie       _
        8       sie       4:anaphor_antecedent
        9       die       _       
        10      Chance    _     
        11      verwerten _

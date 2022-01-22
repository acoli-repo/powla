# PAULA conversion

setup and demo:

    $> make

see samples/ for converted data

To convert a directory containing PAULA files, say, my-paula-dir/, run

    $> python3 paula2rdf.py http://put-your-base-uri.here/ my-paula-dir > output.ttl

If only one argument is provided, it must be directory. Then, it will also be used as base URI.

By default, we produce "pure" POWLA. Alternatively, you can create CoNLL-RDF:

    $> python3 paula2rdf.py http://put-your-base-uri.here/ my-paula-dir --conll_rdf > output.ttl

## Limitations

- we require that all PAULA files reside in the same directory. no subdirectories supported
- tested only for PCC2
- at the moment, we do not preserve PAULA namespaces (how are these declared?)
- the CoNLL-RDF export generates only nif:Word and nif:nextWord, but no nif:Sentence, because these are not explicitly annotated in PAULA
- the usage of XPointer in PAULA-XML is not fully consistent, we tried to make our XPointer parser robust, but it does not cover the full language
  as an example, PCC2 contains *sequences of* xpointer fragments like

      (#xpointer(id('tok_30')/range-to(id('tok_33'))),#xpointer(id('tok_35')/range-to(id('tok_38'))),#xpointer(id('tok_40')/range-to(id('tok_48'))))

  issues:
  - there should not be a parenthesis around fragment identifiers
  - are comma-separated xpointers in line with the XPointer specification?
  - the comma-separated xpointers include the `#` prefix. this is incorrect, the `#` should go before all xpointers

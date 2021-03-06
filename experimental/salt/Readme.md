# Salt wrapper

Salt is the Java implementation of the PAULA Object Model as used both in the
ANNIS corpus management system and in the Pepper converter suite (that is used
to populate ANNIS). Other Salt applications are not known, but its developers
worked in close cooperation with the developers of SynAF.

Salt is conceptually equivalent with PAULA-XML and the only way to create SALT
data that we are aware of is by means of Pepper. Instead of providing a native
SALT implementation, we thus use the PAULA export of Pepper.

In principle, this provides us with RDF converters for 27 formats, although not all importers have been tested and there may be gaps (both in the coverage in the original converters and in our coverage of PAULA XPointer constructors).

## Usage

synopsis:
    `bash -e ./toRDF.sh PepperImporter SOURCE[1..n] [-split] [-conll]`

with

  - `PepperImporter` one of the importers of [pepper-wrapper](https://github.com/acoli-repo/pepper-wrapper).
  - `SOURCE`*i* one or multiple source files/directories
  - `(no flag)` return plain POWLA-RDF in Turtle format
  - `-split` infer `nif:Sentence`s and `nif:nextSentence` (plus `nif:Word`, `nif:nextWord`)
  - `-conll` output CoNLL data for all columns *found* in the respective file

Convert various linguistic annotations to POWLA-RDF (default), CoNLL-RDF (`-split`) or CoNLL-TSV (`-conll`). Write to stdout.

`toRDF` will
- install its dependencies (if necessary: CoNLL-RDF, pepper-wrapper, `../paula`)
- convert the data to PAULA XML using Pepper
- convert the PAULA XML data to POWLA using `../paula/paula2rdf.py`
- optional, with `-split`: induce CoNLL-RDF core data structures (`nif:Word`, `nif:Sentence`) and insert sentence splits (before tree or span breaks)
- optional, with `-conll`: convert to CoNLL-TSV, using `../paula/powla2conll.sparql`, column order is `ID` `WORD`, then lexicographical. Note: Depending on the complexity of the annotation, this can be rather slow. If a single layer is to be converted, this should not be a problem, though.

notes:
- tested on Ubuntu 20.04L
- if called with `PaulaImporter`, it will not use Pepper, but process the data directly
- performance is best when run on a single POWLA annoset, not on the full corpus
- as we only call Pepper converters, we inherit their limitations. This includes constraints about the structure of paths (for PaulaImporter), idiosyncratic naming constraints (e.g., TextImporter will fail if the file ending is not `*.txt`), actual bugs (CoraXMLImporter fails because it expects the CoraXML DTD to be bundled with Pepper, which it isn't). These issues are normally not documented here.
- our PAULA converter was developed from scratch and bottom-up, using official sample data as a basis. We do not guarantee to cover the full extent of the PAULA XML language in its various versions. In particular, we do not support language features that occur in DTD or documentation but not in the data we looked into. Furthermore, there are bugs on our own. In particular, our handling of XPointers is not fully equivalent with that of Pepper.
- Our support of XPointer is incomplete. Also, we do not implement XPointer proper, but both the subset of XPointer constructions observed in PAULA and the PAULA-specific extensions and variations that are not part of the XPointer draft spec

## Docker version

Build with

    $> docker build -f Dockerfile -t acoli/salt2rdf .

or with

    $> make docker

Example call (make sure to call `make samples` before):

    $> docker run --mount type=bind,source=`realpath samples/`,target=/source acoli/salt2rdf toRDF ExmaraldaImporter source/swc.zip

This is equivalent to the conventional

    $> bash -e ./toRDF.sh ExmaraldaImporter samples/swc.zip

## Examples

**Preparations**: To retrieve the data samples cited below, run `make` in the local directory.

### Working

- PAULA directory to POWLA-RDF

	    $> ./toRDF.sh PaulaImporter ../paula/data/DDB_paula/ > ddb.powla.ttl

- PAULA directory to CoNLL-RDF with heuristically inferred sentence boundaries

	    $> ./toRDF.sh PaulaImporter ../paula/data/DDB_paula/ -split > ddb.conll.ttl

- PAULA directory to CoNLL-TSV with heuristically inferred sentence boundaries

	    $> ./toRDF.sh PaulaImporter ../paula/data/DDB_paula/ -split -conll > ddb.conll

- PAULA zip archive to POWLA-RDF

	    $> ./toRDF.sh PaulaImporter ../paula/data/pcc2_PAULA.zip > pcc2.powla.ttl

  > Note 1: CoNLL generation *for this particular dataset* can take >10 minutes. This is because its annotations are particularly complex. Ideally process one layer at a time.

  > Note 2: While we support processing ZIP archives of PAULA XML data, we do not support nested ZIP files (ZIP files that contain another ZIP file that then contains the actual PAULA XML). However, some portals, e.g., [Laudatio](https://www.laudatio-repository.org/published/24/1) disseminate PAULA data as nested ZIPs. These must be decompressed before being fed into this converter.

- Exmaralada archive to POWLA-RDF

	    $> ./toRDF.sh EXMARaLDAImporter samples/swc.zip > swc.powla.ttl

  > Note: check whether export is complete. *For this data*, there may be an issue with end of spans, but it is not clear so far whether this is an issue in Pepper, with PAULA processing or with CoNLL export..

- Exmaralada archive to CoNLL

	    $> ./toRDF.sh EXMARaLDAImporter samples/swc.zip -conll > swc.conll

- Excel archive to POWLA-RDF

	    $> ./toRDF.sh SpreadsheetImporter samples/tuniz.zip > tunic.powla.ttl

  > Note: Operational but Pepper conversion is relatively slow. In practice, consider exporting Excel to TSV and then using CoNLL-RDF, instead

- Exmaralda archive to POWLA-RDF, Docker version

      $> docker run --mount type=bind,source=`realpath samples/`,target=/source acoli/salt2rdf toRDF ExmaraldaImporter source/swc.zip > swc.powla.ttl

### To be debugged

- Gate 7z archive to POWLA-RDF

	    $> ./toRDF.sh GateImporter samples/germanc.7z > germanc.powla.ttl

  > Note: tokens and segmentations ok, markables failed

- Exmaralda archive to CoNLL, Docker version

      $> docker run --mount type=bind,source=`realpath samples/`,target=/source acoli/salt2rdf toRDF ExmaraldaImporter source/swc.zip -conll > swc.conll

  > Note: This works fine in the shell version, but not in the Docker version.

### Operational, but dispreferred

- TreeTagger to POWLA-RDF

	    $> ./toRDF.sh TreeTaggerImporter samples/maerchen.zip > treetagger.powla.ttl

  > Note: This is a test for the TreeTaggerImporter, and it works as expected. In practice, however, we recommend to **not** use Pepper for that, but CoNLL-RDF, as Pepper/PAULA will loose sentence boundaries that then need to be inferred. To perform this inference over the complete corpus (or even just transitive search using `powla:nextTerm+`) is impractical as the search space can be enormous. In CoNLL-RDF, this is broken up into one axis within each sentence (`nif:nextWord`) and one over sentences (`nif:nextSentence`), which is *way* more performant.

- Excel archive to CoNLL

	    $> ./toRDF.sh SpreadsheetImporter samples/tuniz.zip -conll > tunic.conll

  > Note: This works, but is strongly discouraged, as traversing the `powla:nextTerm+` axis can be unperformant. Instead, consider to export TSV directly from your Spreadsheet software.

- Gate 7z archive to CoNLL

	    $> ./toRDF.sh GateImporter samples/germanc.7z -conll > germanc.conll

  > Note: in principle, this works (modulo the issues mentioned under the POWLA-RDF conversion)
  > However, a major drawback is that transitive search is always performed over the full document.
  > Many files failed with StackOverflow errors

- TextImporter

    $> ./toRDF.sh TextImporter Readme.md

  > Note: This will not produce any "real" output, but this is *not a bug*, because the resulting file does not contain any annotation (such as, for example, tokenization). And without annotations, the resulting POWLA data is just an empty set of triples ... For processing plain text, it is strongly recommended to perform tokenization first. For whitespace tokenization, this can be achieved with `sed -e s/'$'/'\n\n'/g -e s/'\s\s*'/'\n'/g` Readme.md > Readme.tok`. The resulting file is a (very simplistic) CoNLL-TSV format and can be processed with CoNLL-RDF.

### Not operational because of Pepper issues

- CoraXML to POWLA-RDF

	    $> ./toRDF.sh CoraXMLImporter samples/rem-coralled*xml > cora.powla.ttl

  > Note: At the moment, this fails because of [a Pepper issue}(https://github.com/korpling/pepper/issues/148)

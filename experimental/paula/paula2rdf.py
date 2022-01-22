import re,sys,os,traceback,argparse
from lxml import etree
import urllib.parse

args=argparse.ArgumentParser(description="""
    We assume that all PAULA files are in the same directory, and that links are always local in that directory.
    If so, we convert files individually.

    TODO: add -conll flag to use nif:Word and nif:nextWord instead of powla:nextTerm (we still don't do sentences, because these receive no special handling in PAULA)
""")
args.add_argument("baseURI", type=str, help="base URI for the PAULA file, should end in /")
args.add_argument("dir", type=str, nargs="?", help="directory that contains the PAULA-XML files, note that we expect a flat directory structure with one annoset per directory, defaults to baseuri", default=None)
args=args.parse_args()

if args.dir==None:
    args.dir=args.baseURI

#
# aux
##############

def decode_xlink(xlink,basetree,baseelems):

            parse=[]   # local XLink/XPointer expressions
            targets=[] # for id lookup
            string=None # for string ranges

            # ad hoc fixes
            # these a not well-formed XPointers, but they occur in PCC2
            while xlink.startswith("(") and xlink[-1]==")":
                xlink=xlink[1:-1]
            if ",#" in xlink:
                xlink=",".join(xlink.split(",#"))

            if xlink.startswith("#xpointer("):
                xlink=xlink[len("#xpointer("):-1].strip()
                while(len(xlink)>0):
                    if xlink.startswith("id("):
                        id=xlink.split("(")[1].split(")")[0]
                        xlink=")".join(xlink.split(")")[1:]).strip()
                        if xlink.startswith("/"):
                            xlink=xlink[1:]
                        parse.append(id)
                    elif xlink.startswith("range-to("):
                        parse.append("range-to")
                        xlink=xlink[len("range-to("):-1].strip()
                    elif xlink.startswith("string-range("):
                        parse.append("string-range")
                        xlink=xlink[len("string-range("):-1].strip()
                        parse.append(xlink.split(")")[0])
                        xlink=")".join(xlink.split(")")[1:])
                    else:
                        raise Exception("unsupported XPointer fragment \""+xlink+"\"")

            elif xlink.startswith("#") and xlink[1:] in baseelems: # direct link
                parse=[xlink[1:]]
            elif xlink.startswith("#") and "," in xlink: # enumeration (not sure this is valid, but it occurs)
                parse=[ p for p in xlink[1:].split(",") if p in baseelems ]
            else:
                raise Exception("unresolvable xlink \""+xlink+"\"")

            parse=[ re.sub(r"[\"']","",exp) for exp in parse ]

            for n,p in enumerate(parse):
                    if p in baseelems:
                        if not p in targets:
                            targets.append(p)
                    elif p=="range-to":
                        while targets[-1]!=parse[n+1]:
                            targets.append(baseelems[baseelems.index(targets[-1])+1])
                    elif p=="string-range": # note that we ignore the second element (search term) and just return the span
                        p=parse[n+1].split(",")
                        for context in basetree.xpath(p[0]):
                            context=context.text[int(p[2])-1:int(p[2])-1+int(p[3])]
                            if not string:
                                string=context
                            else:
                                string+= " ... "+context
                        # print(parse[n+1])
                    elif n==0 or not parse[n-1] in "string-range":
                        raise Exception("unsupported XPointer expression \""+p+"\"")
                    # last expression was string-range: ok
            targets=[os.path.join(args.baseURI,base)+"#"+p for p in targets]

            return parse,targets,string

#
# reading data
################

file2data={}
type2files={}

for file in os.listdir(args.dir):
    if file.endswith("xml"):
        with open(os.path.join(args.dir,file),"rb") as input:
            type=None
            content=input.read()
            tmp=content.decode("utf-8")
            if 'paula_text.dtd">' in tmp:
                type="text"
            elif 'paula_feat.dtd">' in tmp:
                type="feat"
            elif 'paula_mark.dtd">' in tmp:
                type="mark"
            elif 'paula_rel.dtd">' in tmp:
                type="rel"
            elif 'paula_struct.dtd">' in tmp:
                type="struct"

            if type:
                file2data[file]=content
                if not type in type2files:
                    type2files[type]=[]
                type2files[type].append(file)
            else:
                sys.stderr.print("warning: did not detect schema in "+file+":\n"+content+"\n")

#
# conversion
#####################

# prolog: prefixes and definitions
print("""
PREFIX powla: <http://purl.org/powla/powla.owl#> .
PREFIX conll: <http://ufal.mff.cuni.cz/conll2009-st/task-description.html#> .
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
PREFIX nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
PREFIX paula: <https://github.com/korpling/paula-xml#> . # annotation properties

# note that we don't support PAULA namespaces, not fully clear how these are being declared
# TODO: check against DTD on whether there are properties we're missing

paula:type rdfs:subPropertyOf powla:hasAnnotation.

""")

# to facilitate dev: process different types differently
for file in type2files["mark"] + type2files["struct"] + type2files["rel"] + type2files["feat"]:
    print("#",file)
    content=file2data[file]
    base=None
    feats=[] # for feature types in feat files, to avoid double definition

    if "xml:base=" in content.decode("utf-8"):
        base=[ row for row in content.decode("utf-8").split() if "xml:base" in row ][0]
        base=re.sub('"'," ",base)
        base=re.sub("'"," ",base)
        base=base.split()[1]
        #print(base)
    # print(content.decode("utf-8")) # or debugging only

    tree=etree.XML(content,etree.XMLParser())
    basetree=None
    if base in file2data:
        basetree=etree.XML(file2data[base])
        baseelems=basetree.xpath("//*[@id!='']")
        baseelems=[ tgt.attrib["id"] for tgt in baseelems ]
    lastUri=None
    for mark in tree.xpath("//mark"): # incl. toks
        type=tree.xpath("//markList/@type")[0]
        # special treatment for type=tok
        # note that we presuppose sequential order

        uri=args.baseURI+file+"#"+mark.attrib["id"]
        if "{http://www.w3.org/1999/xlink}href" in mark.attrib:
            xlink=mark.attrib["{http://www.w3.org/1999/xlink}href"].strip()

            parse,targets,string=decode_xlink(xlink,basetree,baseelems)

            if type=="tok" and lastUri!=None:
                print(f"<{lastUri}> powla:nextTerm <{uri}>.")
            if len(targets)>0:
                print("#",parse)
                for p in targets:
                    print(f"<{p}> powla:hasParent <{uri}> .")
                print()
            if string:
                    print(f"<{uri}> powla:string \"\"\"{string}\"\"\" .")
            lastUri=uri

    for struct in tree.xpath("//struct"):
        uri=args.baseURI+file+"#"+struct.attrib["id"]
        id=tree.xpath("//header/@paula_id")[0]
        type=tree.xpath("//structList[1]/@type")[0]
        if type=="annoSet":
            print(f"<{args.baseURI+file}> a powla:Document.")
            print(f"<{uri}> a powla:Layer .")
            for rel in struct.xpath("./rel"):
                if "{http://www.w3.org/1999/xlink}href" in rel.attrib:
                    xlink=rel.attrib["{http://www.w3.org/1999/xlink}href"].strip()
                    if not os.path.exists(os.path.join(args.dir,xlink)):
                        # PCC2 data has a gap here: URML data missing
                        sys.stderr.write("warning: file "+os.path.join(args.dir,xlink)+" not found, skipping\n")
                        sys.stderr.flush()
                    else:
                        reltree=etree.parse(os.path.join(args.dir,xlink))
                        ids=reltree.xpath("//*/@id")
                        for id in ids:
                            print(f"<{os.path.join(args.baseURI,xlink)+'#'+id}> powla:hasLayer <{uri}>.")

        else: # elif type in ["struct","const"]: # RST, TIGER
            print(f"<{uri}> a powla:Node.")

            for rel in struct.xpath("./rel"):
                if "{http://www.w3.org/1999/xlink}href" in rel.attrib:
                    xlink=rel.attrib["{http://www.w3.org/1999/xlink}href"].strip()
                    targets=None
                    if basetree and xlink.startswith("#"): # untested
                        _,targets,_=decode_xlink(xlink,basetree,baseelems)
                    elif xlink.startswith("#"): # this is the local document in PCC2 -- but this is not actually XML valid, because the other paths require the directory!
                        targets=[os.path.join(args.baseURI,file)+xlink]
                    else:
                        targets=[os.path.join(args.baseURI,xlink)]

                    ruri="[]" # blank node
                    if "id" in rel.attrib:
                        ruri="<"+os.path.join(args.baseURI,file)+"#"+rel.attrib["id"]+">"

                    for t in targets:
                        print(f"<{t}> powla:hasParent <{uri}>. ")
                        print(f"{ruri} a powla:Relation; powla:hasSource <{t}>; powla:hasTarget <{uri}>",end="")
                        if "type" in rel.attrib:
                            print(f"; paula:type \""+rel.attrib["type"]+"\"", end="")
                            # this seems to be an add hoc extension for RST
                        print(".")

        # else:
        #     raise Exception("unsupported struct type \""+type+"\" in "+file)
        #     sys.exit()
        if "{http://www.w3.org/1999/xlink}href" in struct.attrib:
            xlink=struct.attrib["{http://www.w3.org/1999/xlink}href"].strip()
            # parse,targets,string=decode_xlink(xlink,basetree,baseelems)
            print(xlink)
            # print(parse,targets,strings)
            sys.exit()
    for rel in tree.xpath("//relList/rel"):
        uri=args.baseURI+file+"#"+rel.attrib["id"]
        id=tree.xpath("//header/@paula_id")[0]
        if "{http://www.w3.org/1999/xlink}href" in rel.attrib:
                    source=rel.attrib["{http://www.w3.org/1999/xlink}href"].strip()
                    xlink=rel.attrib["target"].strip()  # PCC encodes dependencies head to dependent, note that we keep that
                    targets=[]
                    sources=[]
                    if basetree!=None and xlink.startswith("#"): # untested
                        _,targets,_=decode_xlink(xlink,basetree,baseelems)
                    else: # assume that the document name is encoded
                        targets=[os.path.join(args.baseURI,xlink)]
                    if basetree!=None and source.startswith("#"): # untested
                        _,sources,_=decode_xlink(source,basetree,baseelems)
                    else: # assume that the document name is encoded
                        sources=[os.path.join(args.baseURI,source)]

                    for t in targets:
                        for s in sources: # both should be singleton sets
                            print(f"<{uri}> a powla:Relation; powla:hasSource <{s}>; powla:hasTarget <{t}>.")
    for feat in tree.xpath("//feat"):
        type=None # shouldn't be used
        try:
            type=tree.xpath("//featList/@type")[0].strip()
        except:
            pass

        if "{http://www.w3.org/1999/xlink}href" in feat.attrib:
            xlink=feat.attrib["{http://www.w3.org/1999/xlink}href"].strip()
            targets=None
            if basetree and xlink.startswith("#"): # untested
                        _,targets,_=decode_xlink(xlink,basetree,baseelems)
            elif xlink.startswith("#"): # this is the local document in PCC2 -- but this is not actually XML valid, because the other paths require the directory!
                        targets=[os.path.join(args.baseURI,file)+xlink]
            else:
                        targets=[os.path.join(args.baseURI,xlink)]
            for t in targets: # should be singleton
                if "value" in feat.attrib:
                    # from type, we derive the property name
                    if type==None:
                                type="powla:hasAnnotation" # should not be used !
                    else:
                                type="paula:"+urllib.parse.quote(type)
                                if not type in feats:
                                    print(f"{type} rdfs:subPropertyOf powla:hasAnnotation.")
                                    feats.append(type)

                    val=feat.attrib["value"]
                    print(f"<{t}> {type} \"{val}\".")
                if "target" in feat.attrib: # PAULA 1.0 (in PCC2), in PAULA 1.1, this should actually be a rel file
                    reltgt=feat.attrib["target"]
                    reltargets=None
                    if basetree and reltgt.startswith("#"): # untested
                                _,reltargets,_=decode_xlink(reltgt,basetree,baseelems)
                    elif reltgt.startswith("#"): # this is the local document in PCC2 -- but this is not actually XML valid, because the other paths require the directory!
                                reltargets=[os.path.join(args.baseURI,file)+reltgt]
                    else:
                                reltargets=[os.path.join(args.baseURI,reltgt)]
                    if reltargets!=None:
                        for r in reltargets:
                            print(f"[] a powla:Relation; powla:hasSource <{t}>; powla:hasTarget <{r}>; paula:type \"{type}\" .")

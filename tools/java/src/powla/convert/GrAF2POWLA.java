package powla.convert;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.query.ResultSet;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xces.graf.io.*; 
import org.apache.xerces.impl.xpath.XPath;
import org.semanticweb.owlapi.apibinding.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.*;
import java.net.*;
import java.util.*;
import java.io.*;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.xces.graf.api.*;
import org.xces.graf.io.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import com.sun.org.apache.xpath.internal.XPathFactory;

/** read GrAF, build RDF */
public class GrAF2POWLA {

	final String namespace;
	final String prefix;
	javax.xml.xpath.XPath xpath = javax.xml.xpath.XPathFactory.newInstance().newXPath();
    
	final Model model;
	final static String rdf    = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	final static String rdfs   = "http://www.w3.org/2000/01/rdf-schema#";				
	final static String owl	  = "http://www.w3.org/2002/07/owl#";						
	final static String powla  = "http://purl.org/powla/powla.owl#";	
	final static String dc     = "http://purl.org/dc/elements/1.1/";

	final Property rdf_type;
	final Property rdfs_subPropertyOf;
	final Property rdfs_subClassOf;
	final Property rdfs_label;
    final Resource owl_Restriction;
    final Resource owl_Class;
    final Property owl_onProperty;
    final Property owl_hasValue;
    final Property owl_ObjectProperty;
    final Property owl_DatatypeProperty;

    final Property powla_hasAnnotation;	
    final Resource powla_Nonterminal;
    final Property powla_hasChild;
	final Property powla_rootOfDocument;
	final Resource powla_Root;
	final Resource powla_Document;
	final Resource powla_Layer;
	final Resource powla_DocumentLayer;
	final Property powla_hasSubDocument;
	final Property powla_hasLayer;
	final Property powla_hasDocument;
	final Resource powla_Terminal;
	final Property powla_start;
	final Property powla_end;
	final Property powla_next;
	final Property powla_string;
	final Property powla_firstTerminal;
	final Property powla_lastTerminal;

	final Resource corpus;
	
    final GraphParser parser; 
	
	/** create model, declare vocabulary, declare ontology 
	 * @throws SAXException */
	public GrAF2POWLA(String prefix, String namespace) throws SAXException, MalformedURLException  {
		
		parser = new GraphParser();
		
		//System.err.println("GrAF2POWLA.<init>("+namespace+" "+namespace+")");
		this.prefix=prefix;
		if(namespace.matches(".*[#/]$")) { 
				this.namespace=namespace;
		} else this.namespace=namespace+"#";
		String uri = namespace.replaceFirst("[#/]$", "");
		
		model = ModelFactory.createDefaultModel();
		try {
			// if the Model already exists, add its content
			model.read(URI.create(uri).toURL().openStream(), null);
		} catch (IOException e) { }
		
		model.setNsPrefix("rdf", rdf);
        model.setNsPrefix("rdfs", rdfs);
        model.setNsPrefix("owl", owl);
        model.setNsPrefix("dc", dc);
        model.setNsPrefix("powla", powla);
        model.setNsPrefix(prefix,this.namespace);

        rdf_type = model.createProperty(rdf+"type");
        rdfs_subPropertyOf = model.createProperty(rdfs+"subPropertyOf");
        rdfs_subClassOf = model.createProperty(rdfs+"subClassOf");
        rdfs_label = model.createProperty(rdfs+"label");
        owl_Restriction = model.createResource(owl+"Restriction");
        owl_Class = model.createResource(owl+"Class");
        owl_onProperty = model.createProperty(owl+"onProperty");
        owl_hasValue = model.createProperty(owl+"hasValue");
        owl_ObjectProperty = model.createProperty(owl+"ObjectProperty");
        owl_DatatypeProperty = model.createProperty(owl+"DatatypeProperty");
    	Property owl_imports = model.createProperty(owl+"imports");
     
        powla_hasAnnotation = model.createProperty(powla+"hasAnnotation");
        powla_hasAnnotation.addProperty(rdf_type,owl_DatatypeProperty);
    	powla_Nonterminal = model.createResource(powla+"Nonterminal").addProperty(rdf_type, owl_Class);
    	powla_hasChild = model.createProperty(powla+"hasChild");
    	powla_rootOfDocument = model.createProperty(powla+"rootOfDocument");
    	powla_Root = model.createResource(powla+"Root").addProperty(rdf_type, owl_Class);
    	powla_Document = model.createResource(powla+"Document").addProperty(rdf_type,owl_Class);
    	powla_Layer = model.createResource(powla+"Layer").addProperty(rdf_type,owl_Class);
    	powla_DocumentLayer = model.createResource(powla+"DocumentLayer").addProperty(rdf_type,owl_Class);
    	powla_hasSubDocument = model.createProperty(powla+"hasSubDocument");
    	powla_hasSubDocument.addProperty(rdf_type,owl_ObjectProperty);
    	powla_hasLayer = model.createProperty(powla+"hasLayer");
    	powla_hasLayer.addProperty(rdf_type, owl_ObjectProperty);
    	powla_hasDocument = model.createProperty(powla+"hasDocument");
    	powla_hasDocument.addProperty(rdf_type, owl_ObjectProperty);
    	powla_Terminal = model.createResource(powla+"Terminal");
    	powla_start = model.createProperty(powla+"startPosition");
    	powla_end = model.createProperty(powla+"endPosition");
    	powla_next = model.createProperty(powla+"nextNode"); 
    	powla_string = model.createProperty(powla+"hasStringValue");
    	powla_firstTerminal = model.createProperty(powla+"firstTerminal");
    	powla_firstTerminal.addProperty(rdf_type, owl_ObjectProperty);
    	powla_lastTerminal = model.createProperty(powla+"lastTerminal");
    	powla_lastTerminal.addProperty(rdf_type, owl_ObjectProperty);


        // declare ontology
        model.createResource(uri)
        	.addProperty(rdf_type, model.createResource(owl+"Ontology")/*.addProperty(rdf_type,owl_Class)*/)
        	.addProperty(owl_imports, model.createResource(powla.replaceFirst("#$","")));
        
        // declare corpus
        corpus = 
        	model.createResource(this.namespace+prefix)
        		.addProperty(rdf_type, model.createResource(powla+"Corpus").addProperty(rdf_type,owl_Class))
        		.addProperty(model.createProperty(powla+"documentID"), prefix);
	}
	
	/** reads an *.anc file and all xml files referred to therein 
	 * @throws SAXException 
	 * @throws IOException 
	 * 	 *  */
	public void addDocument(String documentID, File ancFile) throws SAXException, IOException {
        System.err.print("initialize GrAF parsing .");
        GraphParser parser = new GraphParser(); 
        System.err.print(".");
    	File ancDir = ancFile.getParentFile();
    	// heuristic filters for ANC/GrAF naming conventions
        System.err.println(". ok");

        
        System.err.print("analyze XCES meta data ..");
        String textFile = null;
        try {
			textFile = xpath.evaluate("//@loc[1]", new InputSource(new FileReader(ancFile)));
		} catch (XPathExpressionException e) {}
        File oldDir = new File(System.getProperty("user.dir"));
        System.setProperty("user.dir", ancDir.toString());
        textFile = (new File(textFile)).getAbsolutePath();
        System.setProperty("user.dir", oldDir.toString());
        NodeList annoFiles = null;
        try {
			annoFiles=(NodeList)xpath.evaluate("//*[name()='annotations'][1]/*[name()='annotation']", new InputSource(new FileReader(ancFile)), XPathConstants.NODESET);
		} catch (XPathExpressionException e) {}
        System.err.println(". ok");
 
        System.err.print("checking dependencies .");
        // sort annoFiles such that annotation layer that has a dependency is presented *after* its dependencies        
        Hashtable<String,Vector<String>> dependsOn = new Hashtable<String,Vector<String>>();
        Hashtable<String,Integer> type2nr = new Hashtable<String,Integer>();
        
        for(int i = 0; i<annoFiles.getLength();i++) {
        	Element annoFile = (Element)annoFiles.item(i);
        	String file = annoFile.getAttribute("ann.loc");
        	String layerID = annoFile.getAttribute("type");
        	type2nr.put(layerID,i);
        	if(!file.equals(textFile)) {
                try {
	        		System.setProperty("user.dir", ancDir.toString());
		        	NodeList dependencies = (NodeList)xpath.evaluate("/*[name()='graph']/*[name()='header'][1]/*[name()='dependencies'][1]/*[name()='dependsOn']", 
		        			new InputSource(new FileReader((new File(file)).getAbsolutePath())), XPathConstants.NODESET);
		        	Vector<String> deps = new Vector<String>();
		        	for(int j=0; j<dependencies.getLength();j++) 
		        		deps.add(xpath.evaluate("@type", dependencies.item(j)));
		        	if(deps.size()>0) dependsOn.put(layerID,deps);
		        	System.setProperty("user.dir", oldDir.toString());
                } catch (Exception e) {
                	System.err.print("\nwhile reading "+(new File(file)).getAbsolutePath()+": "+e.getClass().getCanonicalName());
                }
        	}
        }
        System.err.print(".");
        
        /*for(String i : dependsOn.keySet()) {
        	System.err.print("\n"+i+":");
        	for(String s : dependsOn.get(i))
        		System.err.print(" "+s);

        }*/
	    
        Vector<Integer> processingOrder = new Vector<Integer>();
        HashSet<String> processed = new HashSet<String>();
        Vector<String> unprocessed = new Vector<String>(type2nr.keySet());
        Collections.sort(unprocessed); // String ordering, to make sure that order is reproducible regardless of the original order in the xces (*.anc) file)
        if(unprocessed.contains("seg")) { // base segmentation is to be processed before other layers of annotation that may introduce their own segmentation
        	unprocessed.remove("seg");
        	unprocessed.insertElementAt("seg", 0);
        }
        
        while(unprocessed.size()>0) {
        	boolean foundProcessable = false;
        	int position = 0;
        	while(!foundProcessable) {
        		String i = unprocessed.get(position);
        		boolean depsResolved = true;
	        	if(dependsOn.get(i)!=null) 
	        		for(int j = 0; depsResolved && j<dependsOn.get(i).size(); j++)
	        			depsResolved=processed.contains(dependsOn.get(i).get(j));
	        	if(depsResolved) {
	        		processingOrder.add(type2nr.get(i));
	        		processed.add(i);
	            	unprocessed.removeElementAt(position);
	            	position=0;
	            	foundProcessable=true;
	        	} else 
	        		position++;
        	}
        }
        System.err.println(". ok");

        System.err.print("create and annotate Document "+prefix+":"+documentID+" .");
		Resource document = 
			model.createResource(namespace+documentID)
				.addProperty(rdf_type,powla_Document);
		System.err.print(".");
		// metadata (at the moment a provisional direct RDF linearization of the XCES header)
		addMetadata(document,ancFile);
		System.err.println(". ok");
		
        		
        for(int i : processingOrder) {
        	Element annoFile = (Element)annoFiles.item(i);
        	String file = annoFile.getAttribute("ann.loc");
        		String layerID = annoFile.getAttribute("type");
	        	String name = annoFile.getTextContent(); // unused
	        	
	        	System.setProperty("user.dir", ancDir.toString());
	        	file = (new File(file)).getAbsolutePath();
	        	System.setProperty("user.dir", oldDir.toString());
	        
	        if(!file.equals(textFile)) {        
	        	System.err.print("parse "+file+" ..");
	        	IGraph graph = parser.parse(new File(file));        	
	        	System.err.println(". ok");

	        	System.err.print("integrate "+file+" ..");
	        	addDocumentLayer(layerID, document, graph, new File(textFile));
	        	System.err.println(". ok");
        	}
        }
        
        System.err.print("consolidate ..");
        consolidate(document);
        System.err.println(". ok");
	}
	
	/** recode entire XCES header from an *.anc file in RDF (preliminary representation formalism) 
	 * @throws FileNotFoundException 
	 * @throws XPathExpressionException */
	protected Resource addMetadata(Resource document, File xcesFile) throws FileNotFoundException {
		Element cesHeader = null;
		try {
			cesHeader = (Element)xpath.evaluate("/*[name()='cesHeader']",new InputSource(new FileReader(xcesFile)), XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		String namespace=cesHeader.getAttribute("xmlns");
		if(namespace==null || namespace.equals("")) namespace=xcesFile.toURI().toASCIIString();
		if(!namespace.matches(".*[/#]$")) namespace=namespace+"#";
		model.setNsPrefix("xces", namespace);
		return addMetadata(document, cesHeader, namespace);
	}
	
	/** recursive through the XML, everything is just copied */
	private Resource addMetadata(Resource resource, Element element, String namespace) {
		// copy all attributes from element to resource
		NamedNodeMap atts = element.getAttributes();
		for(int i = 0; i<atts.getLength();i++) {
			Attr att = (Attr)atts.item(i);
			String myNamespace = att.getNamespaceURI();
			if(myNamespace==null)
				myNamespace=namespace;
			if(!att.getLocalName().equals("xmlns")) {
				Property prop = model.createProperty(myNamespace+att.getLocalName());
				prop.addProperty(rdf_type, owl_DatatypeProperty);
				resource.addProperty(prop, att.getValue());
			}
		}

		// TEXT => rdfs:label 
		NodeList texts=null;
		try {
			texts = (NodeList) xpath.evaluate("./text()",element,XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		for(int i = 0; i<texts.getLength();i++)
			resource.addProperty(rdfs_label, texts.item(i).getTextContent().trim());		
		
		// create Resource nodes for child elements, property $namespace:metadata
		Property metadata = model.createProperty(namespace+"metadata");
		metadata.addProperty(rdf_type, owl_ObjectProperty);
		NodeList children = null;
		try {
			children = (NodeList) xpath.evaluate("./*",element,XPathConstants.NODESET);
		} catch (XPathExpressionException e) {}
		for(int i =0; i<children.getLength();i++) {
			Element child = (Element)children.item(i);
			String myNamespace = child.getNamespaceURI();
			if(myNamespace==null) myNamespace=namespace;
			Resource r = model.createResource(myNamespace+element.getLocalName());
			resource.addProperty(metadata, r);
			addMetadata(r,child,namespace);
		}
		return resource;
	}
	

	/** creates firstTerminal and lastTerminal for all Nodes */
	protected Resource consolidate(Resource document) {
		// find all roots, then create first and last terminal recursively

		ResultSet results = 
			query("SELECT ?root " +
				"WHERE { ?layer a <"+powla_DocumentLayer+">. " +
						"?layer <"+powla_hasDocument+"> <"+document+">. " +
						"?layer <"+powla_rootOfDocument+"> ?root }");	
		while (results.hasNext())
	    	consolidateNodeExtensions(results.nextSolution().getResource("root"));

		return document;
	}
	
	/** perform a SPARQL query on model */
	public ResultSet query(String query) {
		return QueryExecutionFactory.create(QueryFactory.create(query), model).execSelect();
	}
		
	/** set firstTerminal and lastTerminal for every Node recursively, starting from the Root */
	protected void consolidateNodeExtensions(Resource root) {
		StmtIterator hasChildren = root.listProperties(powla_hasChild); 
		if(root.hasProperty(rdf_type, powla_Nonterminal)) {
			Resource firstTerminal = null;
			long start = Long.MAX_VALUE;
			Resource lastTerminal = null;
			long end = Long.MIN_VALUE;
			while(hasChildren.hasNext()) {
				Resource child = hasChildren.next().getObject().asResource();
				try {
					Resource firstChildTerminal = null;
					Resource lastChildTerminal = null;
					if(child.hasProperty(rdf_type, powla_Terminal)) {
						firstChildTerminal=child;
						lastChildTerminal=child;
					} else if(child.hasProperty(rdf_type, powla_Nonterminal)) {
						consolidateNodeExtensions(child);
						firstChildTerminal=child.getPropertyResourceValue(powla_firstTerminal);
						lastChildTerminal=child.getPropertyResourceValue(powla_lastTerminal);
					}
					
					long pos = firstChildTerminal.getProperty(powla_start).getLong();
					if(pos<=start) {
						firstTerminal=firstChildTerminal;
						start=pos;
					}
					pos = lastChildTerminal.getProperty(powla_end).getLong();
					if(pos>=end) {
						lastTerminal=lastChildTerminal;
						end=pos;
					}
				} catch (NullPointerException e) {
					System.err.println("\nwhile consolidating "+root);
					e.printStackTrace(); // tocheck: why does that happen ?
				}
			}
			try {
				if(firstTerminal!=null) root.addProperty(powla_firstTerminal, firstTerminal);
				if(lastTerminal!=null) root.addProperty(powla_lastTerminal, lastTerminal);
			} catch (NullPointerException e) {
				System.err.println("\nwhile consolidating "+root);
				e.printStackTrace(); // shouldn't occur
			}
		}
	}	
	
	/** reads a GrAF file (*.xml) 
	 * @param textFile 
	 * @throws IOException */
	public void addDocumentLayer(String layerID, Resource document, IGraph graph, File textFile) throws IOException {

		// declare Document and Layer /* preliminary */
		corpus.addProperty(powla_hasSubDocument, document);
		
		Resource layer = 
			model.createResource(namespace+layerID)
				.addProperty(rdfs_subClassOf, powla_Layer)
				.addProperty(rdf_type,owl_Class);
		
		// all instances of layer have layerID (OWL/DL)
		layer.addProperty(rdfs_subClassOf, 
				model.createResource()
					.addProperty(rdf_type, owl_Restriction)
					.addProperty(owl_onProperty, model.createResource(powla+"layerID"))
					.addProperty(owl_hasValue, layerID));
		
		Resource documentLayer = 		
			model.createResource(namespace+document.getLocalName()+"_"+layerID)
				.addProperty(rdf_type, powla_DocumentLayer)
				.addProperty(powla_hasDocument,document)
				.addProperty(rdf_type,layer);
		document.addProperty(powla_hasLayer, documentLayer);
		
		// define Nonterminals (GrAF Nodes) and Links
		// identify Roots 
		for(INode node : graph.nodes()) {
			Resource nonterminal = 
				model.createResource(namespace+node.getId())
					.addProperty(rdf_type, powla_Nonterminal);
			addAnnotations(nonterminal, node.annotations());

			// check whether it is a Root node 
			// (A POWLA Root is not to be confused with a GrAF Root. 
			// A GrAF Root is a top-level node in the entire graph, a 
			// POWLA Root is a top-level node within in the layer)
			if(node.getInEdges().size()==0) { // TODO: make sure not to count pointing relations !
				nonterminal.addProperty(rdf_type, powla_Root);
				documentLayer.addProperty(powla_rootOfDocument, nonterminal);
			}

			// hasLayer
			if(node.getOutEdges().size()>0 || node.getLinks().size()>0) {
				if(nonterminal.getPropertyResourceValue(powla_hasLayer)==null) 
					nonterminal.addProperty(powla_hasLayer, documentLayer);
				// otherwise, the dependency analysis in addDocument() guarantees that the earlier declaration was the *original* layer
			}

			
			// hasChild // todo: support PointingRelations (apparently, GrAF Edges are mostly dominance relations, how would coreference in GrAF look like ?)
			for(IEdge out : node.getOutEdges())
				nonterminal.addProperty(powla_hasChild, model.createResource(namespace+out.getTo().getId()));
			
			// todo: check whether all regions are covered by children
			if(node.getOutEdges().size()==0) 
				for(ILink link : node.getLinks())
					for(IRegion region : link.regions()) 
						nonterminal.addProperty(powla_hasChild, model.createResource(namespace+region.getId()));
		}
		
		// define Relations (Edges)
		Resource powla_Relation = model.createResource(powla+"Relation");
		Property powla_isSourceOf = model.createProperty(powla+"isSourceOf");
		Property powla_hasTarget = model.createProperty(powla+"hasTarget");
		for(IEdge edge : graph.edges()) {
			Resource src = 
				model.createResource(namespace+edge.getFrom().getId());
			Resource tgt =
				model.createResource(namespace+edge.getTo().getId());
			Resource relation =
				model.createResource(namespace+edge.getId())
					.addProperty(rdf_type, powla_Relation)
					.addProperty(powla_isSourceOf,src)
					.addProperty(powla_hasTarget,tgt);
			addAnnotations(relation, edge.annotations());
		}
		
		// define Terminals (Regions)
		// note: in GrAF, Regions are never annotated directly, hence, all annotations are attached to Nonterminals
		Hashtable<Long,Resource> start2term = new Hashtable<Long,Resource>();
		for(IRegion region : graph.getRegions()) {
				Resource terminal =
					model.createResource(namespace+region.getId());
				long start = -1;
				long end = -1; 
				try {
					start = ((Number)region.getStart().getOffset()).longValue();
					end = ((Number)region.getEnd().getOffset()).longValue();
				} catch (GrafException e) {
					e.printStackTrace();
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
				
				// base segmentation: assume non-overlapping, totally ordered sequence of IRegions
				// todo: resolve conflicting segmentation (cf. Chiarcos et al. 2009, LAW, however, not clear how this would look like in the GrAF API)
				if(layerID.equals("seg")) {
					terminal.addProperty(rdf_type, powla_Terminal);
					terminal.addLiteral(powla_start, start).addLiteral(powla_end, end); // Fact++: unsupported datatype long
					terminal.addProperty(powla_string, getString(textFile, start, end));
					terminal.addProperty(rdf_type, powla_Root);
					terminal.addProperty(powla_hasLayer,documentLayer);
					documentLayer.addProperty(powla_rootOfDocument, terminal);
					start2term.put(start,terminal);
				} else {
					// retrieve terminals that match start and end tag
					try {
						Resource startTerm =
							this.query("SELECT ?term " +
									"WHERE { ?term <"+rdf_type+"> <"+powla_Terminal+"> ." +
										   " ?term <"+powla_start+"> "+model.createTypedLiteral(start)+" " +
										   "} " +
									"LIMIT 1" ).next().getResource("term");
						Resource endTerm = 
							this.query("SELECT ?term " +
									"WHERE { ?term <"+rdf_type+"> <"+powla_Terminal+"> ." +
										   " ?term <"+powla_end+"> "+model.createTypedLiteral(end)+" " +
										   "} " +
									"LIMIT 1" ).next().getResource("term");
						if(!(startTerm.equals(endTerm) && startTerm.getURI().equals(terminal.getURI()))) {
							// if the Region covers multiple Terminals or introduces a new id, then
							// create a Nonterminal node for this "Terminal", in PAULA terminology, this would be a "Markable"
							terminal.addProperty(rdf_type, powla_Nonterminal);
							
							// GrAF ids are not necessarily unique within an annotation project, but only within an XML file.
							// If another Nonterminal with the same id but different extension exists, give a warning and skip it
							// note that firstTerminal and lastTerminal are defined for GrAF Regions only, for GrAF Nodes, these are inferred during the consolidation
							if(model.contains(terminal,rdf_type,powla_Nonterminal)) {
								try {
									Resource earlierStartTerm =
												this.query("SELECT ?term " +
													"WHERE { <"+terminal+"> <"+powla_firstTerminal+"> ?term }" +
													"LIMIT 1").next().getResource("term");
									Resource earlierEndTerm =
										this.query("SELECT ?term " +
											"WHERE { <"+terminal+"> <"+powla_lastTerminal+"> ?term }" +
											"LIMIT 1").next().getResource("term");
									if(!earlierStartTerm.equals(startTerm) || !earlierEndTerm.equals(endTerm))
										System.err.println("range conflict: GrAF Region "+terminal+" with range "+earlierStartTerm+".."+earlierEndTerm+" " +
												"cannot be assigned range "+startTerm+".."+endTerm);
								} catch (NoSuchElementException e) {
									System.err.println("id conflict: GrAF Region "+terminal+" was previously encountered as GrAF Node, skipping");
									// otherwise, it would have firstTerminal or lastTerminal
								}
							} else if(model.contains(terminal,rdf_type,powla_Terminal)) { 
								if(!terminal.equals(startTerm) || !terminal.equals(endTerm))
									System.err.println("id conflict: GrAF Region "+terminal+" with range "+startTerm+".."+endTerm+" was previously" +
											" encountered as GrAF base segment, skipping");
							} else { // we can create a fresh Nonterminal
								terminal.addProperty(powla_firstTerminal, startTerm).addProperty(powla_lastTerminal, endTerm);
								for(Resource t = startTerm; !t.equals(endTerm); t=startTerm.getPropertyResourceValue(powla_next).asResource())
									terminal.addProperty(powla_hasChild,t);
								terminal.addProperty(powla_hasChild,endTerm);
								terminal.addProperty();
							}
						}
					} catch(NoSuchElementException e) {
						System.err.println("no Terminals found as delimiters of non-base segment "+terminal+" (start="+start+", end="+end+"): "+e.getMessage());
					}
				}
		}
		
		// nextNode (getRegions() seems to iterate over Regions in incorrect [reverse ?] order)
		Vector<Long> starts = new Vector<Long>(start2term.keySet());
		Collections.sort(starts);
		for(int i = 1; i<starts.size(); i++) 
			start2term.get(starts.get(i-1))
				.addProperty(powla_next, 
						start2term.get(starts.get(i)));		
	}
		
	protected String getString(File txtFile, long start, long end) throws IOException {
		int length = (int)(end-start);
		char[] cbuf = new char[length];
		FileReader in = new FileReader(txtFile);
		in.skip(start);
		in.read(cbuf, 0, length);
		in.close();
		return new String(cbuf);
	}

	protected Resource addAnnotations(Resource r, Iterable<IAnnotation> annotations) { 
		for(IAnnotation annotation : annotations) { // GrAF is redundant here, label+annotationSet is replicated in feature structure
			// labels
			String value = annotation.getLabel(); 
			String att = annotation.getAnnotationSet().getName(); // getType() ?
			if(att.contains(":")) {
				System.err.print("warning: non-supported attribute "+att+" at "+r);
			} else {
				Property powla_hasAnno = 
					model.createProperty(powla+"has_"+att.replaceAll("[#/:]","."));
				powla_hasAnno.addProperty(rdf_type, owl_DatatypeProperty)
							 .addProperty(rdfs_subPropertyOf, powla_hasAnnotation);
				r.addProperty(powla_hasAnno, value);
			}
			
			// feats
			for(IFeature feat : annotation.features()) {
				value = feat.getStringValue();
				// no support for IFeatureStructure, if necessary, these should be modeled with native RDF, 
				// because this is metadata (about annotation), not annotation
				att = feat.getName();
				if(att.contains(":")) {
					System.err.print("warning: non-supported attribute "+att+" at "+r);
					Property powla_hasAnno = 
						model.createProperty(powla+"has_"+att.replaceAll("[#/:]",".")+"_fs");
					powla_hasAnno.addProperty(rdf_type, owl_DatatypeProperty)
								 .addProperty(rdfs_subPropertyOf, powla_hasAnnotation);
					r.addProperty(powla_hasAnno, value);
				}
			}
		}
		return r;
	}

	
	/*
	 * @args[0] prefix and namespace, e.g., mycorpus=http://mycorpora.org/mycorpus.owl# (should end with # or /)
	 * @args[1] *.anc file
	 * @args[2] output ("-" for stdout or file)
	 */
    public static void main(String[] args) throws Exception {

        String prefix = args[0].replaceFirst("=.*","");
    	String namespace = args[0].replaceFirst(".*=", "");
    	String ancFile = args[1];
    	Writer out = null;
    	if(args.length<3 || args[2].equals("-")) {
    		out=new OutputStreamWriter(System.out);
    	} else {
    		out=new FileWriter(args[2]);
    	}
    	
        System.err.print("initialize RDF model ..");
        GrAF2POWLA g2p = new GrAF2POWLA(prefix,namespace);
        System.err.println(". ok");
    	
        String documentID = ancFile.replaceAll(".*[\\\\/]","").replaceFirst("[-\\.].*", "");        
        g2p.addDocument(documentID, new File(ancFile));
        
        System.err.print("dump to ");
        if(args.length>2) {
        	System.err.print(args[2]+" ..");
        } else System.err.print("stdout ..");
        g2p.model.write(out,"RDF/XML-ABBREV");
        System.err.println(". ok");    
    }
}

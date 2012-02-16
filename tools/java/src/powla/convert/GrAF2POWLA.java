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
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.vocabulary.*;
import com.sun.org.apache.xpath.internal.XPathFactory;

/** read GrAF, build RDF 
 * 
 * remarks:
 * - this converter assumes unique ids within an annotation project (*.anc), 
 *   multiply defined GrAF IDs are to be resolved externally, using DisambiguateGrAFIDs
 * 
 * known bugs: 
 * - no support for Terminals with empty strings (yet) in mpqa
 * - conflicting segmentations are heuristically resolved only
 * - last Region is not linked to its predecessor with a powla_next property
 * - only works if the model covers exactly one text (base segmentation)
 * */
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
    final Resource powla_Relation;
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
	final Property powla_layerID;
	final Property powla_hasSource; 
	final Property powla_hasTarget ; 
	
	final Resource corpus;
	
    final GraphParser parser; 
	
	/** create model, declare vocabulary, declare ontology 
	 * @throws SAXException */
	public GrAF2POWLA(String prefix, String namespace) throws SAXException, MalformedURLException  {

		PrintStream stdout = System.out;
		System.setOut(System.err); // redirect GrAF log messages (including asynchronous messages) to stderr

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
			model.read(URI.create(uri).toURL().openStream(), this.namespace);
		} catch (IOException e) { 
			System.err.println("creating new model for URI "+uri);
			model.removeAll();
		} catch (JenaException e) {
			System.err.println("warning: URI "+uri+" does not resolve to a valid RDF resource, ignoring its content:");
			System.err.println("(details: "+e.getMessage()+")");
			model.removeAll();
		}
		System.setOut(stdout);
		
		model.setNsPrefix("rdf", rdf);
        model.setNsPrefix("rdfs", rdfs);
        model.setNsPrefix("owl", owl);
        model.setNsPrefix("dc", dc);
        model.setNsPrefix("powla", powla);
        model.setNsPrefix(prefix,this.namespace);
        model.setNsPrefix("",this.namespace);

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
    	powla_hasChild.addProperty(rdf_type, owl_ObjectProperty);
    	powla_rootOfDocument = model.createProperty(powla+"rootOfDocument");
    	powla_rootOfDocument.addProperty(rdf_type, owl_ObjectProperty);
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
    	powla_Terminal.addProperty(rdf_type, owl_Class);
    	powla_start = model.createProperty(powla+"startPosition");
    	powla_start.addProperty(rdf_type, owl_DatatypeProperty);
    	powla_end = model.createProperty(powla+"endPosition");
    	powla_end.addProperty(rdf_type, owl_DatatypeProperty);
    	powla_next = model.createProperty(powla+"nextNode");
    	powla_next.addProperty(rdf_type, owl_ObjectProperty);
    	powla_string = model.createProperty(powla+"hasStringValue");
    	powla_firstTerminal = model.createProperty(powla+"firstTerminal");
    	powla_firstTerminal.addProperty(rdf_type, owl_ObjectProperty);
    	powla_lastTerminal = model.createProperty(powla+"lastTerminal");
    	powla_lastTerminal.addProperty(rdf_type, owl_ObjectProperty);
    	powla_layerID = model.createProperty(powla+"layerID");
    	powla_layerID.addProperty(rdf_type, owl_DatatypeProperty);
    	powla_Relation = model.createResource(powla+"Relation");
    	powla_Relation.addProperty(rdf_type, owl_Class);
    	powla_hasTarget = model.createProperty(powla+"hasTarget");
    	powla_hasTarget.addProperty(rdf_type, owl_ObjectProperty);
    	powla_hasSource= model.createProperty(powla+"hasSource");
    	powla_hasSource.addProperty(rdf_type, owl_ObjectProperty);
    	
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
    	if(ancDir==null) ancDir=new File(".");
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
		// addMetadata(document,ancFile);
		System.err.println(". ok");
		
        		
        for(int i : processingOrder) {
        	Element annoFile = (Element)annoFiles.item(i);
        	String file = annoFile.getAttribute("ann.loc");
        		String layerID = annoFile.getAttribute("type");
	        	String name = annoFile.getTextContent(); // unused
	        	
	        	System.setProperty("user.dir", ancDir.toString());
	        	file = (new File(file)).getAbsolutePath();
	        	System.setProperty("user.dir", oldDir.toString());
	        
	        try {
	        if(!file.equals(textFile)) {        
	        	System.err.print("parse "+file+" ..");
	        	IGraph graph = parser.parse(new File(file));
	        	System.err.println(". ok");

	        	System.err.print("integrate "+file+" ..");
	        	addDocumentLayer(layerID, document, graph, new File(textFile));
	        	System.err.println(". ok");
        	}
	        } catch(NullPointerException e) {
	        	e.printStackTrace(); // generated by GrAF
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
		try {
			return QueryExecutionFactory.create(QueryFactory.create(query), model).execSelect();
		} catch (Exception e) {
			System.err.println(query);
			return QueryExecutionFactory.create(QueryFactory.create(query), model).execSelect();
		}
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
					
					try { 
						// in GrAF, nodes without anchoring in the primary data are possible, e.g., empty arguments in FrameNet annotations, cf, MASC, HistoryGreek-fn.xmk#fn-n3950
						// in PAULA, this is currently not allowed, but for consistency with GrAF, POWLA should support that
						// print a warning
						long pos = firstChildTerminal.getProperty(powla_start).getLong(); // TODO: problems processing longs here, probably just escaping
						// long pos = Long.parseLong(firstChildTerminal.getProperty(powla_start).getString());
						if(pos<=start) {
							firstTerminal=firstChildTerminal;
							start=pos;
						}
						// pos = Long.parseLong(lastChildTerminal.getProperty(powla_start).getString());
						pos = lastChildTerminal.getProperty(powla_end).getLong();
						if(pos>=end) {
							lastTerminal=lastChildTerminal;
							end=pos;
						}
					} catch (NullPointerException e) {
						System.err.println("warning: node "+child+" is not anchored in the primary data");
					}
				} catch (NullPointerException e) {
					System.err.println("\nwhile consolidating "+root);
					e.printStackTrace(); // shouldn't occur
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
					.addProperty(owl_onProperty, powla_layerID)
					.addProperty(owl_hasValue, layerID));
		
		Resource documentLayer = 		
			model.createResource(namespace+document.getLocalName()+"_"+layerID)
				.addProperty(rdf_type, powla_DocumentLayer)
				.addProperty(powla_hasDocument,document)
				.addProperty(rdf_type,layer);
		document.addProperty(powla_hasLayer, documentLayer);

		
		// define Terminals (Regions)
		// note: in GrAF, Regions are never annotated directly, hence, all annotations are attached to Nonterminals
		Hashtable<Long,Resource> start2term = new Hashtable<Long,Resource>();
		for(IRegion region : graph.getRegions()) {
				Resource terminal =
					model.createResource(namespace+region.getId());
				terminal.addProperty(powla_hasLayer,documentLayer);
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
					terminal.addLiteral(powla_start, model.createTypedLiteral(start)).addLiteral(powla_end, model.createTypedLiteral(end)); // Fact++: unsupported datatype long
					terminal.addProperty(powla_string, getString(textFile, start, end));
					terminal.addProperty(rdf_type, powla_Root);
					documentLayer.addProperty(powla_rootOfDocument, terminal);
					start2term.put(start,terminal);
				} else {
					// retrieve terminals that match start and end tag
					try {
						Resource startTerm =
							this.query("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
									"SELECT ?term " +
									"WHERE { ?term <"+rdf_type+"> <"+powla_Terminal+"> ." +
									   " ?term <"+powla_start+"> \""+start+"\"^^xsd:long " +
										   "} " +
									"LIMIT 1" ).next().getResource("term");
						Resource endTerm = 
							this.query("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
									"SELECT ?term " +
									"WHERE { ?term <"+rdf_type+"> <"+powla_Terminal+"> ." +
									   " ?term <"+powla_end+"> \""+end+"\"^^xsd:long " +
										   "} " +
									"LIMIT 1" ).next().getResource("term");
						if(!(startTerm.equals(endTerm) && startTerm.getURI().equals(terminal.getURI()))) {							
							// more cases are to be supported:
							// 1. Region covers same data as startTerm, but with different ID
							// 2. Region covers a span of multiple original Regions 
							// 3. Region can be an empty sting between two original Regions
							// 4. Region can correspond to a substring of one original Region
							// 5. Region can overlap with one original Region in a substring
							// we only consider cases 1-2 here (3-5 do not occur with MASC):
							// - create a Nonterminal node for this "Terminal", in PAULA terminology, this would be a "Markable"
						
							// 0. id has been used before
							if(model.contains(terminal,rdf_type,powla_Terminal)) { 
								if(!terminal.equals(startTerm) || !terminal.equals(endTerm))
									System.err.println("id conflict: GrAF Region "+terminal+" with range "+startTerm+".."+endTerm+" was previously" +
											" encountered as GrAF base segment, skipping");
							} else if(model.contains(terminal,rdf_type,powla_Nonterminal)) { 
								if(!terminal.equals(startTerm) || !terminal.equals(endTerm))
									System.err.println("id conflict: GrAF Region "+terminal+" with range "+startTerm+".."+endTerm+" was previously" +
											" encountered as GrAF base segment, skipping");
							} else 
							
							// 1. Region covers same data as startTerm, but with different ID => define Region as a child node
							if(startTerm.equals(endTerm)) {
								terminal.addProperty(rdf_type, powla_Nonterminal);
								terminal.addProperty(powla_hasChild, startTerm);
							}
							// 2. Region covers a span of multiple original Regions => define covered Regions as child nodes
							//    TODO: if an empty element precedes the first or follow the last Region covered, it should be excluded
							else if(startTerm.getProperty(powla_end).getLong() <= endTerm.getProperty(powla_end).getLong()) {
								terminal.addProperty(rdf_type, powla_Nonterminal);
								while(startTerm.getProperty(powla_end).getLong() < endTerm.getProperty(powla_end).getLong()) {
									terminal.addProperty(powla_hasChild, startTerm);
									if(startTerm.getProperty(powla_next)==null) {
										System.err.println("error: "+powla_next+" for "+startTerm.getURI()+ " undefined");
										startTerm = endTerm;
									} else {
										Resource nextTerm=startTerm.getProperty(powla_next).getResource();
										if(nextTerm==null) {
											System.err.println("error: no Resource found for "+startTerm.getProperty(powla_next));
											startTerm=endTerm;
										} else
											startTerm=nextTerm;
									}
								}
								terminal.addProperty(powla_hasChild, endTerm);
							} 
							
							// 3. an empty Region that coincides with both the end of one segment and the beginning with another
							//    (e.g., MASC, 110CYL067-mpqa.xml, mpqa_r170)
							//    [=> endTerm precedes startTerm]
							//    => insert new Terminal
							//    [BTW: AFAIK, this should not occur, because startTerm and endTerm have been found and base segments should be non-overlapping]
							else if(end==start) {
								Resource predecessor = endTerm;
								Resource successor = startTerm;
								System.err.println("warning: create a new Terminal between "+predecessor+" and "+successor);
								terminal.addProperty(rdf_type, powla_Terminal);
								terminal.addLiteral(powla_start, model.createTypedLiteral(start)).addLiteral(powla_end, model.createTypedLiteral(end));
								terminal.addProperty(powla_string, getString(textFile, start, end));
								predecessor.removeAll(powla_next);
								predecessor.addProperty(powla_next,terminal);
								terminal.addProperty(powla_next, successor);								
							} else { //unhandled situation, should not occur
								System.err.println("error: check start and end attributes of Terminals " +
										startTerm.getURI()+" ["+startTerm.getProperty(powla_start).getLong()+".."+startTerm.getProperty(powla_end).getLong()+"] and "+
										endTerm.getURI()+" ["+endTerm.getProperty(powla_start).getLong()+".."+endTerm.getProperty(powla_end).getLong()+"]");
							}		
						}
					} catch(NoSuchElementException e) {
						System.err.println("warning: non-base segment "+terminal+" (start="+start+", end="+end+") conflicts with the base segmentation");
						// 3. Region is an empty sting, or a non-segmented (whitespace) string between two original Regions // TODO
						// 4. Region can correspond to a substring of one original Region // TODO
						// 5. Region can overlap with one original Region in a substring  // TODO

						// right now, all Terminals completely contained in the Region
						// if between two Terminals (i.e., no Terminal covered), then create a new Terminal
						// heuristic: take an arbitrary Terminal contained in the Region, from there crawl to left and to right until correct boundaries found

						// 1. assign all Terms as children that are completely covered
						ResultSet terms =
							this.query("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
									"SELECT ?term " +
									"WHERE { ?term <"+rdf_type+"> <"+powla_Terminal+"> ." +
										"?term <"+powla_start+"> ?start FILTER (?start >= \""+start+"\"^^xsd:long)."+
										"?term <"+powla_end+"> ?end FILTER (?end <= \""+end+"\"^^xsd:long)."+
										"}");
						if(terms.hasNext()) {
							System.err.println("warning: redefined "+terminal+" to cover all Regions it fully contains");
							terminal.addProperty(rdf_type, powla_Nonterminal);
							while(terms.hasNext())
								terminal.addProperty(powla_hasChild, terms.next().getResource("term"));
						} else {
							// 2. assign all Terms as children that overlap (if nothing is found that is fully covered)
							terms = 
								this.query("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
									"SELECT ?term " +
									"WHERE { ?term <"+rdf_type+"> <"+powla_Terminal+"> ." +
										"{ ?term <"+powla_start+"> ?start FILTER (?start <= \""+start+"\"^^xsd:long). "+
										"?term <"+powla_end+"> ?end FILTER (?end >= \""+start+"\"^^xsd:long) "+
										"} UNION {"+
										"?term <"+powla_start+"> ?start FILTER (?start < \""+end+"\"^^xsd:long). "+
										"?term <"+powla_end+"> ?end FILTER (?end >= \""+end+"\"^^xsd:long) }"+
										"}");
							if(start!=end && terms.hasNext()) { // if start==end, then this is an empty string => new Terminal
								System.err.println("warning: redefined "+terminal+" to cover all Regions it partially contains");
								terminal.addProperty(rdf_type, powla_Nonterminal);
								while(terms.hasNext())
									terminal.addProperty(powla_hasChild, terms.next().getResource("term"));
							} else {
								// 3. if between two Terminals (i.e., no Terminal covered), then create a new Terminal
								
								terms = 
									this.query("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
										"SELECT ?predecessor ?successor " +
										"WHERE { ?predecessor <"+rdf_type+"> <"+powla_Terminal+"> ." +
											"?predecessor <"+powla_end+"> ?end FILTER (?end <= \""+start+"\"^^xsd:long). "+
											"?predecessor <"+powla_next+"> ?successor ."+
											"?successor <"+powla_start+"> ?start FILTER (?start >= \""+end+"\"^^xsd:long)"+
											"} LIMIT 1");
								if(terms.hasNext()) {
									QuerySolution solution = terms.next();
									Resource predecessor = solution.getResource("predecessor");
									Resource successor = solution.getResource("successor");
									System.err.println("warning: create a new Terminal between "+predecessor+" and "+successor);
									terminal.addProperty(rdf_type, powla_Terminal);
									terminal.addLiteral(powla_start, model.createTypedLiteral(start)).addLiteral(powla_end, model.createTypedLiteral(end));
									terminal.addProperty(powla_string, getString(textFile, start, end));
									predecessor.removeAll(powla_next);
									predecessor.addProperty(powla_next,terminal);
									terminal.addProperty(powla_next, successor);								
								} else {
									
									// 3a: new segment following the current segmentation
									
									terms = 
										this.query("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
											"SELECT ?predecessor " +
											"WHERE { ?predecessor <"+rdf_type+"> <"+powla_Terminal+"> ." +
											"OPTIONAL { ?predecessor <"+powla_next+"> ?successor } "+
											"FILTER (!(bound(?successor))). "+
											"?predecessor <"+powla_end+"> ?end FILTER (?end <= \""+start+"\"^^xsd:long)" +
										"} LIMIT 1");

									// because of this query, the model must only contain one document
									
									if(terms.hasNext()) {
										QuerySolution solution = terms.next();
										Resource predecessor = solution.getResource("predecessor");
										System.err.println("warning: create a new Terminal after "+predecessor);
										terminal.addProperty(rdf_type, powla_Terminal);
										terminal.addLiteral(powla_start, model.createTypedLiteral(start)).addLiteral(powla_end, model.createTypedLiteral(end));
										terminal.addProperty(powla_string, getString(textFile, start, end));
										predecessor.addProperty(powla_next,terminal);
									} else {

										// 3b: new segment preceding the current segmentation
										
										terms = 
											this.query("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
												"SELECT ?successor " +
												"WHERE { ?successor <"+rdf_type+"> <"+powla_Terminal+"> ." +
												"OPTIONAL { ?predecessor <"+powla_next+"> ?successor } "+
												"FILTER (!(bound(?predecessor))). "+
												"?successor <"+powla_start+"> ?start FILTER (?start >= \""+end+"\"^^xsd:long)" +
											"} LIMIT 1");

										if(terms.hasNext()) {
											QuerySolution solution = terms.next();
											Resource successor = solution.getResource("successor");
											System.err.println("warning: create a new Terminal before "+successor);
											terminal.addProperty(rdf_type, powla_Terminal);
											terminal.addLiteral(powla_start, model.createTypedLiteral(start)).addLiteral(powla_end, model.createTypedLiteral(end));
											terminal.addProperty(powla_string, getString(textFile, start, end));
											terminal.addProperty(powla_next,successor);
										}
									}
								}
							}
						}
					}
				}				
			}

		// if we work with the base segmentation, then restore the original order of segments, based on their positions (distorted by graph.getRegions())
		if(start2term.size()>0) {
			Vector<Resource> sortedTerms = new Vector<Resource>();
			Vector<Long> sortedStarts = new Vector<Long>();
			for(Map.Entry<Long, Resource> e : start2term.entrySet()) {
				long myStart = e.getKey();
				boolean inserted=false;				
				for(int i = 0; !inserted && i<sortedTerms.size();i++) {
					long start = sortedStarts.get(i);
					if(myStart<start) {
						inserted=true;
						sortedTerms.insertElementAt(e.getValue(), i);
						sortedStarts.insertElementAt(e.getKey(), i);
					}
				}
				if(!inserted) {
					sortedTerms.add(e.getValue());
					sortedStarts.add(e.getKey());
				}
			}
			for(int i = 0; i<sortedTerms.size()-2; i++) {
				Resource term = sortedTerms.get(i);
				term.addProperty(powla_next, sortedTerms.get(i+1));
			}
		}
		
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
		for(IEdge edge : graph.edges()) {
			Resource src = 
				model.createResource(namespace+edge.getFrom().getId());
			Resource tgt =
				model.createResource(namespace+edge.getTo().getId());
			Resource relation =
				model.createResource(namespace+edge.getId())
					.addProperty(rdf_type, powla_Relation)
					.addProperty(powla_hasSource,src)
					.addProperty(powla_hasTarget,tgt);
			addAnnotations(relation, edge.annotations());
		}
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
		HashSet<String> knownAttributes = new HashSet<String>();
		for(IAnnotation annotation : annotations) { // GrAF is redundant here, label+annotationSet is replicated in feature structure
			// labels
			String value = annotation.getLabel(); 
			String att = annotation.getAnnotationSet().getName(); // getType() ?
			if(att.contains(":")) {
				if(!knownAttributes.contains(att)) {
					System.err.println("warning: non-supported attribute "+att+" at "+r);
					knownAttributes.add(att);
				}
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

	/** in a GrAF project (*.anc), IDs in different files are unambiguous only among groups of documents that depend one on another<br/>
	 *  independent annotation layers (xml files) may use the same id for different nodes
	 *  
	 * @param file *.anc file
	 * @return Hashtable: layer2grafID2modifiedgrafID (layers represented by layer id)
	 */
	protected Hashtable<String, Hashtable<String,String>> disambiguateGrAFIDs(File file) {
		Hashtable<String,Hashtable<String,String>> result = new Hashtable<String,Hashtable<String,String>>();
		Hashtable<String,String> layerDependsOn = new Hashtable<String,String>();
		
		Hashtable<String,String> idOnLayer = new Hashtable<String,String>();
		return result;
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
        g2p.model.write(out,"RDF/XML");
        System.err.println(". ok");    
    }

}

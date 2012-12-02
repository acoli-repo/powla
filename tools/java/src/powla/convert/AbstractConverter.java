package powla.convert;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;

import org.xml.sax.SAXException;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;

import powla.POWLAModel;

abstract class AbstractConverter extends POWLAModel {

	public static class Exception extends java.lang.Exception {
		public Exception(java.lang.Exception e) {
			super(e);
		}
		public Exception(String s) {
			super(s);
		}
		public Exception() {
			super();
		}
	}
	
	final protected String namespace;
	final protected String prefix;
    		
	/** create model, declare vocabulary, declare ontology 
	 * @throws SAXException */
	public AbstractConverter(String prefix, String namespace) throws AbstractConverter.Exception  {

		PrintStream stdout = System.out;
		
		this.prefix=prefix;
		if(namespace.matches(".*[#/]$")) { 
				this.namespace=namespace;
		} else this.namespace=namespace+"#";
		String uri = namespace.replaceFirst("[#/]$", "");
		
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
		
        model.setNsPrefix(prefix,this.namespace);
        model.setNsPrefix("",this.namespace);

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

	abstract void addDocument(String s, File f) throws Exception;
	
	/** perform a SPARQL query on model */
	public ResultSet query(String query) {
		try {
			return QueryExecutionFactory.create(QueryFactory.create(query), model).execSelect();
		} catch (java.lang.Exception e) {
			System.err.println(query);
			return QueryExecutionFactory.create(QueryFactory.create(query), model).execSelect();
		}
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
				
	/** optimization for queries: 
	 *  set firstTerminal and lastTerminal for every Node recursively, starting from the Root */
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
	

}

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

import powla.POWLAModel;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.vocabulary.*;
import com.sun.org.apache.xpath.internal.XPathFactory;

/** read wacky-style CoNLL data, build RDF 
 *  for the source format, see http://wacky.sslmit.unibo.it
 * 
 * */
public class CoNLL2POWLA extends AbstractConverter {

	/** this doesn't resolve, the reference corpora (pukwac and wackypedia) can be obtained from
	http://wacky.sslmit.unibo.it, but they do not use the CoNLL specs of MaltParser 1.4
	(http://www.maltparser.org/) which they used for their data */
	final protected static String conll  = "http://wacky.sslmit.unibo.it/conll#"; 

    final protected Property conll_func;
    final protected Property conll_pos;
    final protected Property conll_lemma;
	
	public CoNLL2POWLA(String prefix, String namespace) throws Exception {
		super(prefix, namespace);
        conll_func = model.createProperty(conll+"has_func");
        conll_func.addProperty(rdf_type,owl_DatatypeProperty)
        		  .addProperty(rdfs_subPropertyOf,powla_hasAnnotation);
        conll_pos = model.createProperty(conll+"has_pos");
        conll_pos.addProperty(rdf_type,owl_DatatypeProperty)
        		  .addProperty(rdfs_subPropertyOf,powla_hasAnnotation);
        conll_lemma = model.createProperty(conll+"has_lemma");
        conll_lemma.addProperty(rdf_type,owl_DatatypeProperty)
        		  .addProperty(rdfs_subPropertyOf,powla_hasAnnotation);
	}

	/** reads an CoNLL file, stores all annotations in the layer "conll"
	 * 	 *  */
	public void addDocument(String documentID, File conllFile) throws Exception {

        System.err.print("create and annotate Document "+prefix+":"+documentID+" .");
		Resource document = 
			model.createResource(namespace+documentID)
				.addProperty(rdf_type,powla_Document);
		System.err.print(".");
		// no metadata 
		System.err.print(".");
		
		// id is written to the 4th column
		// it is usually the running number, starting with 1 (0 = ROOT), but we do not rely on that
		int sentence = 1;
		int start=0;
		Vector<String> nr2uri = new Vector<String>();
		Vector<String> nr2string = new Vector<String>();
		Vector<String> nr2lemma = new Vector<String>();
		Vector<String> nr2pos = new Vector<String>();
		Vector<String> nr2id = new Vector<String>();
		Vector<String> nr2parent = new Vector<String>();
		Vector<String> nr2edge = new Vector<String>();
		Vector<Integer> nr2start = new Vector<Integer>();

		// TODO: create document layer
		String layerID="conll";
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
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(conllFile));
			for(String line = in.readLine(); line!=null; line=in.readLine())
				if(!line.trim().startsWith("#")) { // remove comment lines
					String[] fields = line.split("\t");
					if(fields.length==1 && fields[0].trim().equals("")) {
						if(nr2uri.size()>0) {
							addSentence(documentLayer, namespace+documentID+".conll"+sentence, nr2uri, nr2string, nr2lemma, nr2pos, nr2id, nr2parent, nr2edge, nr2start);
							nr2uri.clear();
							nr2string.clear();
							nr2lemma.clear();
							nr2pos.clear();
							nr2id.clear();
							nr2parent.clear();
							nr2edge.clear();
							nr2start.clear();
							sentence++;
						}
					} else if(fields.length>=6) {
						nr2uri.add(namespace+documentID+".conll"+sentence+"_"+nr2uri.size());
						nr2string.add(fields[0]);
						nr2lemma.add(fields[1]);
						nr2pos.add(fields[2]);
						nr2id.add(fields[3]);
						nr2parent.add(fields[4]);
						nr2edge.add(fields[5]);
						nr2start.add(start);
						start=start+fields[0].length()+1;
					} else System.err.println("warning: did not find 6 columns in \""+line+"\"");
				}
			if(nr2uri.size()>0)
				addSentence(documentLayer, namespace+documentID+".conll"+sentence, nr2uri, nr2string, nr2lemma, nr2pos, nr2id, nr2parent, nr2edge, nr2start);
		} catch (IOException e) {
			throw new Exception(e);
		}
		        
        System.err.print("consolidate ..");
        consolidate(document);
        System.err.println(". ok");
	}
	
	private void addSentence(Resource documentLayer, 
			String sentenceURI,
			Vector<String> nr2uri, Vector<String> nr2string,
			Vector<String> nr2lemma, Vector<String> nr2pos,
			Vector<String> nr2id, Vector<String> nr2parent,
			Vector<String> nr2edge, Vector<Integer> nr2start) {
		System.err.print("#");
		
		// create a root individual (should be a sentence, but can also be any other sequence of tokens)
		Resource sentence = model.createResource(sentenceURI);
		sentence.addProperty(powla_hasLayer,documentLayer);
		sentence.addProperty(rdf_type, powla_Root);
		sentence.addProperty(rdf_type, powla_Nonterminal);
		documentLayer.addProperty(powla_rootOfDocument, sentence);
		
		// create terminals
		Vector<Resource> nr2terminal = new Vector<Resource>();
		
		for(int i = 0; i<nr2uri.size(); i++) {
			System.err.print("+");
			Resource terminal = model.createResource(nr2uri.get(i));
			terminal.addProperty(powla_hasLayer,documentLayer)
					.addProperty(rdf_type, powla_Terminal)
					.addLiteral(powla_start, model.createTypedLiteral(nr2start.get(i)))
				    .addLiteral(powla_end, model.createTypedLiteral(nr2start.get(i)+nr2string.get(i).length())); 
					// Fact++: unsupported datatype long
			terminal.addProperty(powla_string, nr2string.get(i))
					.addProperty(conll_pos, nr2pos.get(i))
					.addProperty(conll_lemma, nr2lemma.get(i));
			sentence.addProperty(powla_hasChild, terminal);
			
			nr2terminal.add(terminal);
		}

		// create succession relations (within the sentence only)
		Resource terminal = null;
		for(Resource successor : nr2terminal) {
			if(terminal!=null) 
				terminal.addProperty(powla_next,successor);
			terminal=successor;
		}
		
		// create pointing relations (assumed here to be pointing "downwards")
		for(int i = 0; i<nr2uri.size(); i++) {
			Resource tgt = nr2terminal.get(i);
			String parent = nr2parent.get(i);
			int parentNr = nr2id.indexOf(parent);
			Resource src = sentence; // unless another parent is found
			if(parentNr>=0) src = nr2terminal.get(parentNr);
			Resource relation =
				model.createResource(nr2uri.get(i)+"_"+nr2edge.get(i))
					.addProperty(rdf_type, powla_Relation)
					.addProperty(powla_hasSource,src)
					.addProperty(powla_hasTarget,tgt)
					.addProperty(conll_func,nr2edge.get(i));
		}
	}
	
	/*
	 * @args[0] prefix and namespace, e.g., mycorpus=http://mycorpora.org/mycorpus.owl# (should end with # or /)
	 * after -o, a file name is expected to which the results are written (default: stdout)
	 * same for -e and stderr
	 * other @args are source files
	 */
    public static void main(String[] args) throws java.lang.Exception {

        System.err.print("initialize RDF model ..");
        String prefix = args[0].replaceFirst("=.*","");
    	String namespace = args[0].replaceFirst(".*=", "");
		Writer out=new OutputStreamWriter(System.out);

        CoNLL2POWLA c2p = new CoNLL2POWLA(prefix,namespace);
        System.err.println(". ok");
    	
		for(int i = 1; i<args.length; i++)
			if(args[i].equals("-e")) {
				System.err.println("redirect stderr to "+args[i+1]);
				System.setErr(new PrintStream(new FileOutputStream(args[++i])));
			} else if(args[i].equals("-o")) {
				System.err.println("redirect stdout to "+args[i+1]);
				out=new FileWriter(args[++i]); 
			} else {
	    		File src = new File(args[i]);
	    		String documentID=src.getName().replaceAll(".*[\\\\/]","").replaceFirst("[.][^.]*$", "");
	    		System.err.print("read "+args[i]+", using id \""+documentID+"\" ..");
	    		c2p.addDocument(documentID, src);
	    		System.err.println(". ok");
			}
		
		System.err.print("dump results ..");
        c2p.model.write(out,"RDF/XML");
        System.err.println(". ok");    
    }
}

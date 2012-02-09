package powla.query;

import virtuoso.sesame2.driver.*;
import org.openrdf.repository.*;
import org.openrdf.rio.*;
import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

import org.openrdf.query.*;
import org.openrdf.*;
import org.openrdf.rio.rdfxml.*;
import org.openrdf.query.resultio.sparqlxml.*;
import org.openrdf.model.*;

/** always close() connections, even in case of exceptions */
public class QueryPowla {
	
	Repository myRepository = new VirtuosoRepository("jdbc:virtuoso://localhost:1111","dba","dba");
	
	public QueryPowla() throws RepositoryException {
		myRepository.initialize();
	}
	
	public void addRDF(RepositoryConnection con, File f, String baseURI) throws RepositoryException,IOException,RDFParseException {
		// RepositoryConnection con = myRepository.getConnection();
		con.add(f,baseURI,RDFFormat.RDFXML);
		// con.commit();
		// con.close(); // no, commit() does nothing unless 5000 statements are assembled, see http://boards.openlinksw.com/phpBB3/viewtopic.php?f=12&t=847 
	}

	/** url kann durch new URL(String) erzeugt werden */
	public void addRDF(RepositoryConnection con, URL url, String baseURI) throws RepositoryException,IOException,RDFParseException {
		// RepositoryConnection con = myRepository.getConnection();
		con.add(url,baseURI,RDFFormat.RDFXML);
		// con.commit();
		// con.close();
	}
	
	/** run a CONSTRUCT query against the DB and write the results back immediately */
	public void precompile(RepositoryConnection con, String queryString, String baseURI) throws RepositoryException,IOException,MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, RDFParseException { 
		GraphQueryResult result = con.prepareGraphQuery(QueryLanguage.SPARQL, queryString).evaluate();
		while(result.hasNext())
			con.add(result.next());
	}
	/** man kann über das ergebnis iterieren
	while (result.hasNext()) {
   BindingSet bindingSet = result.next();
   Value valueOfX = bindingSet.getValue("x");
   Value valueOfY = bindingSet.getValue("y");
}
	*/
	public void evalSelectQuery(String queryString, OutputStream rawout) throws RepositoryException,IOException,MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException {
		RepositoryConnection con = myRepository.getConnection();
		SPARQLResultsXMLWriter out = new SPARQLResultsXMLWriter(rawout);
		con.prepareTupleQuery(QueryLanguage.SPARQL, queryString).evaluate(out);
		con.close();
		rawout.flush();
		}
	
	/** preprocessor for evalSelectQuery that supports AQL-like operators<br/>
	 *  NOTE: assumes that every AQL-statement (node instantiation or relation spec) is written in exactly one line, operators should be surrounded by whitespaces <br/>
	 *  support only one annotation per relation so far <br/>
	 *  dot after AQL statements is not preceded by whitespaces */
	public static String queryPreprocessor(String queryString) {
		StringBuffer result = new StringBuffer();
		// declare default namespaces
		result.append(
				"PREFIX powla: <file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#>\n"+
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n");
		BufferedReader in = new BufferedReader(new StringReader(queryString));
		try {
			for(String line=in.readLine(); line!=null; line=in.readLine()) {
				line=line.replaceAll("\\t"," ").replaceAll("  *"," ").trim();
				
				// layer ids for nodes, written layer:?a [at most once per line]
				if(line.indexOf(":?")!=-1) {
					String layerid = line.replaceAll(":\\?.*","").trim();
					String variable = line.replaceAll(".*:\\?","?").trim();
					String annotation = "";
					if(variable.indexOf(":[")!=-1) {
						annotation=variable.replaceAll(".*:\\[","\\[");
						variable=variable.replaceAll(":\\[.*", "");
					}
					variable=variable.replaceAll("\\.$","").trim();
					String layer = "?layer_"+new Integer((int)(Math.random()*10000)); // hacky !!!
					result.append(
							variable+" powla:hasLayer "+layer+".\n"+
							layer+" powla:layerID '"+layerid+"'.\n");
					line="";
					if(!annotation.equals("")) {
						line=variable+":"+annotation+".";
					}
				}
				
				if(line.indexOf(":[")!=-1) { // Node queries, e.g. ?a:[cat="NP"]
					String variable = line.replaceAll(":\\[.*", "").trim();
					result.append(variable+" a powla:Node.\n");
					String[] annotations = line.replaceAll(".*\\[", "").replaceAll("\\].*","").split("&");
					for(int i = 0; i<annotations.length; i++) {
						String attributeType = annotations[i].replaceAll("=.*","").trim();
						String attributeValue = annotations[i].replaceAll(".*=","").trim().replaceAll("\"","").replaceAll("/","").trim();
						// String property = "?prop_"+new Integer((int)(Math.random()*10000)); // hacky !!!
						//result.append(variable+" "+property+" '"+attributeValue+"'  FILTER (bound("+property+") && regex("+property+",'.*#has_"+attributeType+"')).\n");
						boolean isRegExp = annotations[i].indexOf("=/")!=-1;
						if(!isRegExp) {
							result.append(variable+" powla:has_"+attributeType+" '"+attributeValue+"'.\n"); 
						} else {
							String value = "?val_"+attributeType+"_"+new Integer((int)(Math.random()*10000)); // hacky !!!
							result.append(variable+" powla:has_"+attributeType+" "+value+" FILTER regex("+value+", '"+attributeValue+"').\n");
						}
					}		
				} else {
					// the following variables may not be well-defined if the current line is not an AQL statement
				String arg1 = line.replaceAll(" .*", "");
				String operator = line.replaceFirst("^[^ ]* ","").replaceFirst(" [^ ]*$", "");
				String arg2 = line.replaceAll(".* ","").replaceFirst("[ ]*\\.$", "");
				if(operator.indexOf("->")!=-1) { // pointing relation
					String relation = "?rel_"+new Integer((int)(Math.random()*10000)); // hacky !!!
					String property2 = "?prop_"+new Integer((int)(Math.random()*10000)); // hacky !!!
					result.append("{ SELECT "+arg1+" "+arg2+"\n"+
							"WHERE { \n"+
								arg1+" powla:isSourceOf "+relation+".\n"+
								relation+" powla:hasTarget "+arg2+".\n");
					String[] annotations = operator.replaceAll(".*\\[", "").replaceAll("\\].*","").split("&");
					for(int i = 0; i<annotations.length; i++) {
						String attributeType = annotations[i].replaceAll("=.*","").trim();
						String attributeValue = annotations[i].replaceAll(".*=","").trim().replaceAll("\"","").replaceAll("/","").trim();
						// String property = "?prop_"+new Integer((int)(Math.random()*10000)); // hacky !!!
						//result.append(relation+" "+property+" '"+attributeValue+"'  FILTER (bound("+property+") && regex("+property+",'.*#has_"+attributeType+"')).\n");
						boolean isRegExp = annotations[i].indexOf("=/")!=-1;
						if(!isRegExp) {
							result.append(relation+" powla:has_"+attributeType+" '"+attributeValue+"'.\n"); 
						} else {
							String value = "?val_"+attributeType+"_"+new Integer((int)(Math.random()*10000)); // hacky !!!
							result.append(relation+" powla:has_"+attributeType+" "+value+" FILTER regex("+value+", '"+attributeValue+"').\n");
						}
					}
					result.append(
								"OPTIONAL { "+arg1+" "+property2+" "+arg2+" } FILTER (!bound("+property2+") || "+property2+" != powla:hasChild)\n"+
								"}}");
					if(operator.indexOf("*")!=-1) 
						result.append(" OPTION(TRANSITIVE, t_in ("+arg1+"), t_out ("+arg2+"), t_min(1)).\n");
				} else if(operator.indexOf(">")!=-1) { // dominance relation
					String relation = "?rel_"+new Integer((int)(Math.random()*10000)); // hacky !!!
					String property2 = "?prop_"+new Integer((int)(Math.random()*10000)); // hacky !!!
					if(operator.indexOf("[")!=-1) 
					result.append("{ SELECT "+arg1+" "+arg2+"\n"+
							"WHERE { \n"+
								arg1+" powla:isSourceOf "+relation+".\n"+
								relation+" powla:hasTarget "+arg2+".\n");
					String[] annotations = operator.replaceAll(".*\\[", "").replaceAll("\\].*","").split("&");
					for(int i = 0; i<annotations.length; i++) {
						String attributeType = annotations[i].replaceAll("=.*","").trim();
						String attributeValue = annotations[i].replaceAll(".*=","").trim().replaceAll("\"","").replaceAll("/","").trim();
						// String property = "?prop_"+new Integer((int)(Math.random()*10000)); // hacky !!!
						// result.append(relation+" "+property+" '"+attributeValue+"'  FILTER (bound("+property+") && regex("+property+",'.*#has_"+attributeType+"')).\n");
						// result.append(relation+" powla:has_"+attributeType+" '"+attributeValue+"'.\n");
						boolean isRegExp = annotations[i].indexOf("=/")!=-1;
						if(!isRegExp) {
							result.append(relation+" powla:has_"+attributeType+" '"+attributeValue+"'.\n"); 
						} else {
							String value = "?val_"+attributeType+"_"+new Integer((int)(Math.random()*10000)); // hacky !!!
							result.append(relation+" powla:has_"+attributeType+" "+value+" FILTER regex("+value+", '"+attributeValue+"').\n");
						}

					}
					result.append(arg1+" powla:hasChild "+arg2);
					if(operator.indexOf("[")!=-1) 
							result.append(".\n}}");
					if(operator.indexOf("*")!=-1) 
						result.append(" OPTION(TRANSITIVE, t_in ("+arg1+"), t_out ("+arg2+"), t_min(1))");
					result.append(".\n");
				} else if(operator.indexOf(".")!=-1) { // precedence (only . and .* so far)
					String aChild = "?aChild_"+new Integer((int)(Math.random()*10000)); // hacky !!!
					String bChild = "?bChild_"+new Integer((int)(Math.random()*10000)); // hacky !!!
					result.append(
							arg1+" powla:lastTerminal "+aChild+" OPTION(TRANSITIVE, t_in ("+arg1+"), t_out ("+aChild+"), t_min (0), t_max (1)).\n"+
							arg2+" powla:firstTerminal "+bChild+" OPTION(TRANSITIVE, t_in ("+arg2+"), t_out ("+bChild+"), t_min (0), t_max (1)).\n"+
							aChild+" powla:nextNode "+bChild);
					if(operator.indexOf("*")!=-1) 
						result.append(" OPTION(TRANSITIVE, t_in ("+arg1+"), t_out ("+arg2+"), t_min(1), t_max(50))");		
					result.append(".\n");
				} else if(operator.indexOf("_i_")!=-1) { // inclusion
					String aStart ="?aStart_"+new Integer((int)(Math.random()*10000)); // hacky !!!
					String bStart ="?bStart_"+new Integer((int)(Math.random()*10000)); // hacky !!!
					String aEnd ="?aEnd_"+new Integer((int)(Math.random()*10000)); // hacky !!!
					String bEnd ="?bEnd_"+new Integer((int)(Math.random()*10000)); // hacky !!!
					result.append(
							arg1+" powla:firstTerminal "+aStart+" OPTION(TRANSITIVE, t_in ("+arg1+"), t_out ("+aStart+"), t_min (0), t_max (1)).\n"+
							arg2+" powla:firstTerminal "+bStart+" OPTION(TRANSITIVE, t_in ("+arg2+"), t_out ("+bStart+"), t_min (0), t_max (1)).\n"+
							aStart+" powla:nextNode "+bStart+" OPTION(TRANSITIVE, t_in("+aStart+"), t_out("+bStart+"), t_min(0), t_max (50)).\n"+
							arg1+" powla:lastTerminal " +aEnd+  " OPTION(TRANSITIVE, t_in ("+arg1+"), t_out ("+aEnd+  "), t_min (0), t_max (1)).\n"+
							arg2+" powla:lastTerminal " +bEnd+  " OPTION(TRANSITIVE, t_in ("+arg2+"), t_out ("+bEnd+  "), t_min (0), t_max (1)).\n"+
							bEnd+" powla:nextNode "+aEnd+" OPTION(TRANSITIVE, t_in("+aEnd+"), t_out("+bEnd+"), t_min(0), t_max (50)).\n");
				} else if(operator.indexOf("_=_")!=-1) { // same extension
					String aStart ="?aStart_"+new Integer((int)(Math.random()*10000)); // hacky !!!
					String aEnd ="?aEnd_"+new Integer((int)(Math.random()*10000)); // hacky !!!
					result.append(
							arg1+" powla:firstTerminal "+aStart+" OPTION(TRANSITIVE, t_in ("+arg1+"), t_out ("+aStart+"), t_min (0), t_max (1)).\n"+
							arg2+" powla:firstTerminal "+aStart+" OPTION(TRANSITIVE, t_in ("+arg2+"), t_out ("+aStart+"), t_min (0), t_max (1)).\n"+
							arg1+" powla:lastTerminal " +aEnd+  " OPTION(TRANSITIVE, t_in ("+arg1+"), t_out ("+aEnd+  "), t_min (0), t_max (1)).\n"+
							arg2+" powla:lastTerminal " +aEnd+  " OPTION(TRANSITIVE, t_in ("+arg2+"), t_out ("+aEnd+  "), t_min (0), t_max (1)).\n");
				} else if(operator.indexOf("_l_")!=-1) { // sharing left border 
					String aStart ="?aStart_"+new Integer((int)(Math.random()*10000)); // hacky !!!
					result.append(
							arg1+" powla:firstTerminal "+aStart+" OPTION(TRANSITIVE, t_in ("+arg1+"), t_out ("+aStart+"), t_min (0), t_max (1)).\n"+
							arg2+" powla:firstTerminal "+aStart+" OPTION(TRANSITIVE, t_in ("+arg2+"), t_out ("+aStart+"), t_min (0), t_max (1)).\n");
				} else if(operator.indexOf("_r_")!=-1) { // sharing right border
					String aEnd ="?aEnd_"+new Integer((int)(Math.random()*10000)); // hacky !!!
					result.append(
							arg1+" powla:lastTerminal " +aEnd+  " OPTION(TRANSITIVE, t_in ("+arg1+"), t_out ("+aEnd+  "), t_min (0), t_max (1)).\n"+
							arg2+" powla:lastTerminal " +aEnd+  " OPTION(TRANSITIVE, t_in ("+arg2+"), t_out ("+aEnd+  "), t_min (0), t_max (1)).\n");
				} else result.append(line+"\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace(); // shouldn't happen
		}

		return result.toString();
	}
	
	public void evalConstructQuery(String queryString, OutputStream rawout) throws RepositoryException,IOException,MalformedQueryException, QueryEvaluationException, RDFHandlerException {
		RepositoryConnection con = myRepository.getConnection();
		RDFXMLWriter out = new RDFXMLWriter(rawout);
		con.prepareGraphQuery(QueryLanguage.SPARQL, queryString).evaluate(out);
		con.close();
	}

	/** to be called before any queries are run against the data base.
	 *  <ul>
	 *  <li>builds hasChildTrans axis</li>
	 *  <li>propagates startPosition and endPosition to all parents</li>
	 *  <li>assigns the DocumentLayer the property "precompiled done"
	 *  </ul> 
	 *  is applied to all DocumentLayers that do not have the "precompiled done" property
	 *  NOTE: necessary only for transitivity precompilation, with virtuoso not necessary
	 * @throws RepositoryException 
	 * @throws MalformedQueryException 
	 * @throws QueryEvaluationException 
	 */
/*	public void queryPrecompilation() throws RepositoryException, QueryEvaluationException, MalformedQueryException {
		RepositoryConnection con = myRepository.getConnection();
		// retrieve DocumentLayers without precompiled done
		String query = 
		"PREFIX powla: <file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#>\n"+
		"PREFIX pq: <file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla-query-precompilation.owl#>\n"+
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
		"CONSTRUCT { ?d rdf:type powla:DocumentLayer }\n"+
		"WHERE {\n"+
		"?d rdf:type powla:DocumentLayer.\n"+
		"OPTIONAL { ?d pq:precompiled ?x}.\n"+
		"FILTER (!(bound(?x)))\n"+
		"}";
		GraphQueryResult documentLayers = con.prepareGraphQuery(QueryLanguage.SPARQL, query).evaluate();
		
		con.close();
	}*/
	
	public static void main(String argv[]) throws Exception {
		boolean help = false;
		QueryPowla test = new QueryPowla();
		boolean select = false;
		boolean construct = false;
		boolean update = false;
		String baseURI ="";
		if(argv.length>0) {
			try {
				if(argv[0].equals("-h") || argv[0].equals("-?")) {
					help=true;
				} else if(argv[0].equals("-add")){ // add files
					System.err.print("add files with baseURI "+argv[argv.length-1]+" .");
					RepositoryConnection con = test.myRepository.getConnection(); System.err.print(".");
					for(int i = 1; i<argv.length-1;i++) {
						System.err.print(". \n"+argv[i]+" ..");
						test.addRDF(con,new File(argv[i]),argv[argv.length-1]);
					}
					con.commit();	System.err.print(".");
					con.close();	System.err.println(". ok");
				} else if(argv[0].equals("-url")){ // add URLs
					System.err.print("add web resources with baseURI "+argv[argv.length-1]+" .");
					RepositoryConnection con = test.myRepository.getConnection(); System.err.print(".");
					for(int i = 1; i<argv.length-1;i++) {
						System.err.print(". \n"+argv[i]+" ..");
						test.addRDF(con,new URL(argv[i]),argv[argv.length-1]);
					}
					con.commit(); 	System.err.print(".");
					con.close();	System.err.println(". ok");
				} else if(argv[0].equals("-select")) { // select queries
					select=true;
				} else if(argv[0].equals("-construct")) { // construct queries
					construct=true;
				} else if(argv[0].equals("-update")) { // update 
					update=true;
					baseURI=argv[1];
				} else {
					System.err.print("incorrect argument sequence: ");
					for(int i = 0; i<argv.length; i++)
						System.err.print(argv[i]+" ");
					System.err.print("\n");
					help=true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				help=true;
			}
		}

		if(help) {
			System.err.println(
					"Synopsis: QueryPowla [-add|-url source [source2 ...] baseURI]\n"+
					"          QueryPowla [-select|-update|-h|-?]\n"+
					"          QueryPowla [-update baseURI]\n"+
					"\t-add\tadd the rdf file in source to the repository\n"+
					"\t-url\tadd the rdf file specified by the url in source to the repository\n"+
					"\t-select\tread SELECT queries from stdin and run them against the repository\n"+
					"\t-construct\tread CONSTRUCT queries from stdin and run them against the repository\n"+
					"\t-update\tlike -construct, but write the results back to the repository\n"+
					"\t-h|-?\thelp (this text)\n"+
					"\tsource\tRDF XML file or its URL\n"+
					"\tbaseURI\tbase uri for the statements from source in the repository\n");
		} else {
		System.out.print("your query (or !q to quit, no <CTRL>+c, please):\n[finish with empty line]\r");
		String query="";
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		RepositoryConnection con = null;
		if(update) con= test.myRepository.getConnection();
		for(String line = in.readLine(); line!=null && !line.trim().equalsIgnoreCase("!q"); line=in.readLine()) {
			if(!line.trim().equals("")) {
					query=line;
					for(line = in.readLine(); line !=null && !line.trim().equals(""); line=in.readLine()) {
						query=query+"\n"+line;
					}
					System.err.println("preprocessing query "+query);
					query=queryPreprocessor(query);
					System.err.println("processing query "+query);
					if(select||(!construct&&!update&&query.trim().contains("SELECT"))) {
							test.evalSelectQuery(query, System.out);
						} else if(construct||(!select&&!update&&(query.contains("CONSTRUCT") || query.contains("DESCRIBE")))) {
							test.evalConstructQuery(query, System.out);
						} else if(update) {
							test.precompile(con,query, baseURI);
						} else 
							System.err.println("error: unknown keyword "+query.trim().replaceAll("[ \t\n].*", "")+"\n"+
							"use SELECT, DESCRIBE or CONSTRUCT");
			}
			System.out.print("your query (or !q to quit, no <CTRL>+c, please):\n[finish with empty line]\r");
		}
		if(update) { con.commit(); con.close(); }
	}
	}
}

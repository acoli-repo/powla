package powla.convert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xces.graf.api.IGraph;
import org.xces.graf.io.GraphParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.Resource;
import com.ibm.icu.util.StringTokenizer;

/** in MASC v.1.0.3, GrAF IDs are not unique within an annotation project, i.e., different annotation layers (*.xml) 
 *  within an annotation project (*.anc) may introduce nodes with the same id <br/>
 *  after calling DisambiguateGrAFIDs.main(File ancFile), unambiguous IDs are introduced and the original files are replaced
 *  a backup file with the original IDs is preserved, log is written to stderr
 * 
 * @author Christian Chiarcos
 *
 */
public class DisambiguateGrAFIDs {

	protected static javax.xml.xpath.XPath xpath = javax.xml.xpath.XPathFactory.newInstance().newXPath();
	
	/** reads an *.anc file and all xml files referred to therein 
	 * @throws SAXException 
	 * @throws IOException 
	 * 	 *  */
	public static void processANCfile(File ancFile) throws SAXException, IOException {
        System.err.print("read "+ancFile.toString()+" ..");
		File ancDir = ancFile.getParentFile().getAbsoluteFile().getCanonicalFile();
    	if(ancDir==null) ancDir=new File(".");
    	File oldDir = new File(System.getProperty("user.dir")).getAbsoluteFile().getCanonicalFile();
    	System.err.println(". ok");
    	
    	NodeList annoFiles = null;
        try {
			annoFiles=(NodeList)xpath.evaluate("//*[name()='annotations'][1]/*[name()='annotation']", new InputSource(new FileReader(ancFile)), XPathConstants.NODESET);
		} catch (XPathExpressionException e) {}
 
        System.err.print("checking dependencies ..");
        Hashtable<String,Vector<String>> dependsOn = new Hashtable<String,Vector<String>>();

    	Vector<String> files = new Vector<String>();
    	Hashtable<String,String> file2layer = new Hashtable<String,String>(); // we presuppose that these are unique
    	Hashtable<String,String> layer2file = new Hashtable<String,String>(); // we presuppose that these are unique
        
        for(int i = 0; i<annoFiles.getLength();i++) {
        	Element annoFile = (Element)annoFiles.item(i);
        	String file = annoFile.getAttribute("ann.loc");
        	files.add(file);
        	String layerID = annoFile.getAttribute("type");
        	file2layer.put(file,layerID);
        	layer2file.put(layerID,file);
        	
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
        System.err.println(". ok");
                
        for(String i : dependsOn.keySet()) {
        	System.err.print(i+":");
        	for(String s : dependsOn.get(i))
        		System.err.print(" "+s);
        	System.err.println();
        }

        // process every xml file: list all IDs and IDREFs
        System.err.print("retrieve @xml:id, @targets, @ref, @to, @from attributes and root elements ..");
        Hashtable<String,HashSet<String>> file2ids = new Hashtable<String,HashSet<String>>();

        for(String file : files) {        	
        	    try {
	        		System.setProperty("user.dir", ancDir.toString());
		        	NodeList idAttribs = (NodeList)xpath.evaluate("//*/@xml:id",
		        			new InputSource(new FileReader((new File(file)).getAbsolutePath())), XPathConstants.NODESET);
		        	HashSet<String> ids = new HashSet<String>();
		        	for(int i = 0; i<idAttribs.getLength();i++)
		        		ids.add(idAttribs.item(i).getNodeValue());
		        	NodeList idRefs = (NodeList)xpath.evaluate("//*/@ref",
		        			new InputSource(new FileReader((new File(file)).getAbsolutePath())), XPathConstants.NODESET);
		        	for(int i = 0; i<idRefs.getLength();i++)
		        		ids.add(idRefs.item(i).getNodeValue());
		        	idRefs = (NodeList)xpath.evaluate("//link/@targets",
		        			new InputSource(new FileReader((new File(file)).getAbsolutePath())), XPathConstants.NODESET);
		        	for(int i = 0; i<idRefs.getLength();i++) {
		        		StringTokenizer targets = new StringTokenizer(idRefs.item(i).getNodeValue()," ");
		        		while(targets.hasMoreTokens()) 
		        			ids.add(targets.nextToken());
		        	}
		        	idRefs = (NodeList)xpath.evaluate("//root/text()",
		        			new InputSource(new FileReader((new File(file)).getAbsolutePath())), XPathConstants.NODESET);
		        	for(int i = 0; i<idRefs.getLength();i++)
		        		ids.add(idRefs.item(i).getNodeValue().trim());

		        	file2ids.put(file,ids);
		        	System.err.print(".");
		        	
                } catch (Exception e) {
                	System.err.print("\nwhile reading "+(new File(file)).getAbsolutePath()+": "+e.getClass().getCanonicalName());
                }
	        	System.setProperty("user.dir", oldDir.toString());
        }

        System.err.println(". ok");

        boolean ambiguityFound = false;
        System.err.print("check for ambiguous ids .");
        Hashtable<String,HashSet<String>> id2files = new Hashtable<String,HashSet<String>>();
        for(String file : files)
        	if(file2ids.get(file)!=null)
        		for(String id : file2ids.get(file)) {
        			if(id2files.get(id)==null) id2files.put(id,new HashSet<String>());
        			id2files.get(id).add(file);
        		}
        System.err.print(".");
        for(Map.Entry<String,HashSet<String>> e : id2files.entrySet()) {
        	String id = e.getKey();
        	HashSet<String> independentFiles = new HashSet<String>(e.getValue());
        	if(e.getValue().size()>1) {
        		// duplicate ids are unambiguous only if one file is dependent on the other or there is another file 
        		// => construct independentFiles, a set of files that cannot have inherited the file id
        		for(String file : e.getValue())
	        		if(file2layer.get(file)!=null && 
	        		   dependsOn.get(file2layer.get(file))!=null)
	        				for(String sublayer : dependsOn.get(file2layer.get(file)))
	        					if(e.getValue().contains(layer2file.get(sublayer)))
	        						independentFiles.remove(file);
	        	if(independentFiles.size()>1) {
	        		ambiguityFound=true;
	        		System.err.print("ambiguous id "+id+" (in");
	        		for(String file : independentFiles) 
	        			System.err.print(" "+file2layer.get(file));
	        		System.err.println(")");
	        	}
        	}
        }
        
        if(!ambiguityFound) {
        		System.err.println(". no ambiguities");
        } else 
        	for(String file : files) {        	
	        	System.err.print("update "+file+" ..");
	    	    try {
	    	    	boolean modified=false;
	    	    	System.setProperty("user.dir", ancDir.toString());
	    	    	System.err.println("ancDir: "+ancDir.toString()+" ("+ancDir.getCanonicalPath()+", "+
	    	    			ancDir.getAbsolutePath()+")");
	        		File srcFile = new File(file); //.replaceAll("\\\\",File.separator).replaceAll("/", File.separator));
	        		// System.err.println(srcFile);
	        		
	        		File backupFile = new File(srcFile.toString()+".bak");
	        		int backups = 1;
	        		while(backupFile.exists()) {
	        			backupFile = new File(srcFile.toString()+".bak"+backups);
	        			backups++;
	        		}
	        		
	        		if(!srcFile.exists()) System.err.println("did not find "+srcFile.getCanonicalPath());
	        		if(!srcFile.canRead()) System.err.println("cannot read "+srcFile.getCanonicalPath());
	        		if(!srcFile.renameTo(backupFile)) {
	        			BufferedReader in = new BufferedReader(new FileReader(srcFile.getCanonicalPath()));
	        			FileWriter out = new FileWriter(backupFile);
	        			for(String line = in.readLine(); line!=null; line=in.readLine())
	        				out.write(line+"\n");
	        			out.flush();
	        			out.close();
	        			in.close();
	        		}        			

	        		File tgtFile = new File(file);
	        		System.err.println("tgtFile: "+tgtFile.toString()+" ("+tgtFile.getCanonicalPath()+", "+
	    	    			tgtFile.getAbsolutePath()+" <= "+file+" in "+new File(".").getAbsolutePath()+")");
	        		
	        		tgtFile.createNewFile();
	        		BufferedReader in = new BufferedReader(new FileReader(backupFile));
	        		FileWriter out = new FileWriter(tgtFile);
	        		for(String line = in.readLine(); line!=null; line=in.readLine()) {
	        			
	        			// find local @xml:id, @ref, @from and @to
	        			StringTokenizer st = new StringTokenizer(line," "); 
	        			HashSet<String> localIDs = new HashSet<String>();
	        			while(st.hasMoreTokens()) {
	        				String s= st.nextToken();
	        				if(s.startsWith("xml:id=")||s.startsWith("ref=")||s.startsWith("from=")||s.startsWith("to="))
	        					localIDs.add(s.replaceFirst(".*=", "").replaceFirst("['\"]","").replaceFirst("[\"'].*",""));
	        			}
	        			// find local @targets (NOTE: assume there is only one @targets per line)
	        			if(line.contains("targets=")) {
	        				String[] targets = line.replaceAll("'","\"").replaceFirst(".*targets=\"","").replaceFirst("\".*","").split("\\s");
	        				for(int j = 0; j<targets.length; j++) 
	        					localIDs.add(targets[j]);
	        			}	        			
	        			// find local @root elements (NOTE: assume there is only one root per line, no linebreaks)
	        			if(line.contains("<root>")) {
	        				String root = line.replaceFirst(".*<root>","").replaceFirst("</root>.*", "").trim();
	        				localIDs.add(root);
	        			}

	        			// System.err.println(localIDs+"\t"+line);
	        			
	        			// check where these were declared (either in the imports or locally) => layerID prefix
	        	        for(String id : localIDs) {
	        	        	String sourceLayer = file2layer.get(file);
	        	        	HashSet<String> potentialSourceLayers = new HashSet<String>();
	        	        	potentialSourceLayers.add(file2layer.get(file));
	        	        	if(dependsOn.get(file2layer.get(file))!=null)
	        	        			potentialSourceLayers.addAll(dependsOn.get(file2layer.get(file)));
	        	        	for(String layer : new HashSet<String>(potentialSourceLayers))
	        	        		if(id2files.get(id)!=null && 
	        	        				layer2file.get(layer)!=null && 
	        	        				!id2files.get(id).contains(layer2file.get(layer)))
	        	        			potentialSourceLayers.remove(layer);
	        	        	for(String layer : new HashSet<String>(potentialSourceLayers))
	        	        		if(dependsOn.get(layer)!=null)
	        	        			for(String subLayer : dependsOn.get(layer)) 
	        	        				if(potentialSourceLayers.contains(subLayer))
	        	        					potentialSourceLayers.remove(layer);
	        	        	sourceLayer = potentialSourceLayers.iterator().next();
	        	        	if(potentialSourceLayers.size()>1) {
	        		        	System.err.println("error: ambiguous id "+id+" defined in layers "+potentialSourceLayers+", "+file2layer.get(file)+" depends on both");
	        		        } /*else 
	        		        	System.err.println("found "+id+" in "+sourceLayer);*/
	        	        	
	            			// rename every $id to $layerID_$id
	        				line=line.replaceAll(" xml:id=[\"']"+id+"[\"']"," xml:id=\""+sourceLayer+"_"+id+"\"");
	        				line=line.replaceAll(" ref=[\"']"+id+"[\"']"," ref=\""+sourceLayer+"_"+id+"\"");
	        				line=line.replaceAll(" from=[\"']"+id+"[\"']"," from=\""+sourceLayer+"_"+id+"\"");
	        				line=line.replaceAll(" to=[\"']"+id+"[\"']"," to=\""+sourceLayer+"_"+id+"\"");
	        				line=line.replaceAll("(targets=[\"'])"+id+"([\"'\\s])", "$1"+sourceLayer+"_"+id+"$2");
	        				line=line.replaceAll("(targets=[\"'][^\"']*\\s)"+id+"([\"'\\s])", "$1"+sourceLayer+"_"+id+"$2");
	        				line=line.replaceAll("<root>"+id+"</root>","<root>"+sourceLayer+"_"+id+"</root>");
	        				System.err.println("\t["+id+"=>"+sourceLayer+"_"+id+"]");
	        			}
	        			
	        			out.write(line+"\n");
	        			out.flush();
	        		}
	        		in.close();
	        		out.close();
	        		
	        		if(!modified)
	        			backupFile.delete();
	        		System.err.println(". ok");
	        			        	
	            } catch (Exception ex) {
	            	System.err.println("while reading "+(new File(file)).getAbsolutePath()+": "+ex.getClass().getCanonicalName());
	            	ex.printStackTrace();
	            }
	        	System.setProperty("user.dir", oldDir.toString());
	        }
        
	    
	}
	

	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.err.println("DisambiguateGrAFIDs: make sure that all files referenced in an *.anc file use unambiguous node and edge IDs\n"+
				"synopsis: DisambiguateGrAFIDs [FILE1 .. FILEn]\n"+
				"\tFILE1 .. FILEn GrAF project (*.anc) files\n" +
				"\tif ambiguities are found, every XML file referenced in the project files will be\n" +
				"\tmodified in-place, with a *.bak file created");
		for(int i = 0; i<args.length; i++) {
			File file = new File(args[i]);
			if(file.exists() && file.canRead()) {
				processANCfile(file);
			} else {
				System.err.println("error: could not access file "+file);
			}
		}
		System.err.println("done");
	}

}

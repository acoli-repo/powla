package powla.convert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
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
 * 
 * @author christian
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
		File ancDir = ancFile.getParentFile();
    	if(ancDir==null) ancDir=new File(".");
    	File oldDir = new File(System.getProperty("user.dir"));
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
        System.err.print("retrieve xml:id and ref attributes ..");
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
		        	file2ids.put(file,ids);
		        	System.err.print(".");
		        	
		        	System.setProperty("user.dir", oldDir.toString());
                } catch (Exception e) {
                	System.err.print("\nwhile reading "+(new File(file)).getAbsolutePath()+": "+e.getClass().getCanonicalName());
                }
        }
        System.err.println(". ok");
        
        for(String file : files) {        	
        	System.err.print("update "+file+" ..");
    	    try {
    	    	boolean modified=false;
        		File srcFile = new File(file.replaceAll("\\\\","/"));
        		System.setProperty("user.dir", srcFile.getAbsolutePath()); // ancDir.toString());
        		File backupFile = new File(srcFile.toString()+".bak");
        		int backups = 1;
        		while(backupFile.exists()) {
        			backupFile = new File(srcFile.toString()+".bak"+backups);
        			backups++;
        		}
        		
        		if(!srcFile.exists()) System.err.println("did not find "+srcFile.getCanonicalPath()+" in dir "+srcFile.getParent());
        		if(!srcFile.canRead()) System.err.println("cannot read "+srcFile.getCanonicalPath());
        		if(!srcFile.renameTo(backupFile)) {
        			BufferedReader in = new BufferedReader(new FileReader(srcFile));
        			FileWriter out = new FileWriter(backupFile);
        			for(String line = in.readLine(); line!=null; line=in.readLine())
        				out.write(line+"\n");
        			out.flush();
        			out.close();
        			in.close();
        		}        			
        		File tgtFile = new File(file);
        		tgtFile.createNewFile();
        		BufferedReader in = new BufferedReader(new FileReader(backupFile));
        		FileWriter out = new FileWriter(tgtFile);
        		for(String line = in.readLine(); line!=null; line=in.readLine()) {
        			
        			// find local @xml:id and @ref
        			StringTokenizer st = new StringTokenizer(line," "); 
        			HashSet<String> localIDs = new HashSet<String>();
        			while(st.hasMoreTokens()) {
        				String s= st.nextToken();
        				if(s.startsWith("xml:id=")||s.startsWith("ref="))
        					localIDs.add(s.replaceFirst(".*=", "").replaceFirst("['\"]","").replaceFirst("[\"'].*",""));
        			}
        			// System.err.println(localIDs+"\t"+line);
        			
        			// check where these were declared (either in the imports or locally) => layerID prefix
        			for(String id : localIDs) {
        				modified=true;
        				String layer=file2layer.get(file);
        				String sourceLayer = layer;
        				if(dependsOn.get(layer)!=null)
	        				for(String subLayer : dependsOn.get(layer)) {
	        					// System.err.println("check "+subLayer);
	        					if(file2ids.get(layer2file.get(subLayer)).contains(id)) {
	        						// System.err.println("found "+id+" in "+subLayer);
	        						sourceLayer = subLayer;
	                				// we don't check for unambiguous source definitions here
	        					}
	        				}
        				
            			// rename every $id to $layerID_$id
        				line=line.replaceAll(" xml:id=[\"']"+id+"[\"']"," xml:id=\""+sourceLayer+"_"+id+"\"");
        				line=line.replaceAll(" ref=[\"']"+id+"[\"']"," ref=\""+sourceLayer+"_"+id+"\"");
        				//System.err.println(id+" => "+sourceLayer+"_"+id);
        				System.err.print(".");
        			}
        			
        			out.write(line+"\n");
        			out.flush();
        		}
        		in.close();
        		out.close();
        		
        		if(!modified)
        			backupFile.delete();
        		System.err.println(". ok");
        			        	
	        	System.setProperty("user.dir", oldDir.toString());
            } catch (Exception e) {
            	System.err.print("\nwhile reading "+(new File(file)).getAbsolutePath()+": "+e.getClass().getCanonicalName());
            	e.printStackTrace();
            }
    }
    System.err.println(". ok");
        
	    
	}
	

	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		for(int i = 0; i<args.length; i++) 
			processANCfile(new File(args[i]));
		System.err.println("done");
	}

}

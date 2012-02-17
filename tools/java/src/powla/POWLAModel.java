package powla;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * This class can be used to initialize a Jena Model and to provide the proper core terminology 
 * 
 * TODO: validation method
 * 
 * @author christian
 *
 */
public abstract class POWLAModel {

	final protected Model model;
	final protected static String rdf    = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	final protected static String rdfs   = "http://www.w3.org/2000/01/rdf-schema#";				
	final protected static String owl	  = "http://www.w3.org/2002/07/owl#";						
	final protected static String powla  = "http://purl.org/powla/powla.owl#";	
	final protected static String dc     = "http://purl.org/dc/elements/1.1/";

	final protected Property rdf_type;
	final protected Property rdfs_subPropertyOf;
	final protected Property rdfs_subClassOf;
	final protected Property rdfs_label;
    final protected Resource owl_Restriction;
    final protected Resource owl_Class;
    final protected Property owl_onProperty;
    final protected Property owl_hasValue;
    final protected Property owl_ObjectProperty;
    final protected Property owl_DatatypeProperty;
	final protected Property owl_imports;
    
    final protected Property powla_hasAnnotation;	
    final protected Resource powla_Nonterminal;
    final protected Resource powla_Relation;
    final protected Property powla_hasChild;
	final protected Property powla_rootOfDocument;
	final protected Resource powla_Root;
	final protected Resource powla_Document;
	final protected Resource powla_Layer;
	final protected Resource powla_DocumentLayer;
	final protected Property powla_hasSubDocument;
	final protected Property powla_hasLayer;
	final protected Property powla_hasDocument;
	final protected Resource powla_Terminal;
	final protected Property powla_start;
	final protected Property powla_end;
	final protected Property powla_next;
	final protected Property powla_string;
	final protected Property powla_firstTerminal;
	final protected Property powla_lastTerminal;
	final protected Property powla_layerID;
	final protected Property powla_hasSource; 
	final protected Property powla_hasTarget ; 
	
	protected Resource corpus;

	/** todo: load POWLA ontology */
	public POWLAModel() {
		model = ModelFactory.createDefaultModel();
		model.setNsPrefix("rdf", rdf);
        model.setNsPrefix("rdfs", rdfs);
        model.setNsPrefix("owl", owl);
        model.setNsPrefix("dc", dc);
        model.setNsPrefix("powla", powla);

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
        owl_imports = model.createProperty(owl+"imports");

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
	}
}

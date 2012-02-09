<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
      xmlns:xs="http://www.w3.org/2001/XMLSchema"
      exclude-result-prefixes="xs"
      version="2.0"
      xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
      xmlns:owl="http://www.w3.org/2002/07/owl#"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
      xmlns:olia="http://nachhalt.sfb632.uni-potsdam.de/owl/olia.owl#"
      xmlns:stts="http://nachhalt.sfb632.uni-potsdam.de/owl/stts.owl#"
      xmlns:tiger="http://nachhalt.sfb632.uni-potsdam.de/owl/tiger.owl#"
      xmlns:tiger-syntax="http://nachhalt.sfb632.uni-potsdam.de/owl/tiger-syntax.owl#"
      xmlns:powla="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#"
      xmlns:olia_system="http://nachhalt.sfb632.uni-potsdam.de/owl/system.owl#"
      xmlns:corpus="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/corpus.owl#"
      xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">

<xsl:output encoding="UTF-8" indent="yes" method="xml"/>

<!-- tiger-specific routines , don't use this for large corpora, position calculation is a rather hard task -->
<xsl:template match="/">

    <rdf:RDF
        xmlns:corpus="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/corpus.owl#"
        xmlns:base="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#"
        xmlns="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
        xmlns:owl="http://www.w3.org/2002/07/owl#"
        xmlns:powla="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#"
        xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
        xmlns:olia="http://nachhalt.sfb632.uni-potsdam.de/owl/olia.owl#"
        xmlns:stts="http://nachhalt.sfb632.uni-potsdam.de/owl/stts.owl#"
        xmlns:tiger="http://nachhalt.sfb632.uni-potsdam.de/owl/tiger.owl#"
        xmlns:tiger-syntax="http://nachhalt.sfb632.uni-potsdam.de/owl/tiger-syntax.owl#"
        xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
        
        <owl:Ontology rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/corpus.owl">
            <owl:imports rdf:resource="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl"/>
            <owl:imports rdf:resource="http://nachhalt.sfb632.uni-potsdam.de/owl/stts.owl"/>
            <owl:imports rdf:resource="http://nachhalt.sfb632.uni-potsdam.de/owl/stts-link.rdf"/>
            <owl:imports rdf:resource="http://nachhalt.sfb632.uni-potsdam.de/owl/olia.owl"/>
            <owl:imports rdf:resource="http://nachhalt.sfb632.uni-potsdam.de/owl/tiger-link.rdf"/>
            <owl:imports rdf:resource="http://nachhalt.sfb632.uni-potsdam.de/owl/tiger.owl"/>
            <owl:imports rdf:resource="http://nachhalt.sfb632.uni-potsdam.de/owl/tiger-syntax.owl"/>
        </owl:Ontology>
        
        <xsl:call-template name="initialize-powla"/>
        
        <xsl:call-template name="write-corpus">
            <xsl:with-param name="corpusid" select="/corpus/@id"/>
        </xsl:call-template>
        
        <xsl:call-template name="write-layer">
            <xsl:with-param name="id">syntax</xsl:with-param>
            <xsl:with-param name="type">StructLayer</xsl:with-param>
            <xsl:with-param name="documentid" select="/corpus/@id"/>
        </xsl:call-template>

        <xsl:for-each select="/corpus/body">
            <xsl:apply-templates/>
        </xsl:for-each>
    </rdf:RDF>
</xsl:template>

    <xsl:template match="s">
        <xsl:for-each select=".//t">
            <xsl:call-template name="write-terminal">
                <xsl:with-param name="id" select="@id"/>
                <xsl:with-param name="layerid">syntax</xsl:with-param>
                <xsl:with-param name="rootid" select="./ancestor::s[1]/graph[1]/@root"/>
                <xsl:with-param name="prec-terminal" select="./preceding::t[1]/@id"/>
                <xsl:with-param name="next-terminal" select="./following::t[1]/@id"/>
                <xsl:with-param name="start-position">
                    <xsl:variable name="tmp">
                        <xsl:for-each select="./preceding::t">
                            <xsl:value-of select="concat(@word,' ')"/>
                        </xsl:for-each>
                    </xsl:variable>
                    <xsl:value-of select="string-length($tmp)"/>
                </xsl:with-param>
                <xsl:with-param name="string">
                    <xsl:value-of select="@word"/>
                </xsl:with-param>
            </xsl:call-template>
            <xsl:for-each select="@*">
                <xsl:if test="name()!='id' and name()!='word' and string-length(.)&gt;0">
                    <xsl:call-template name="assign-annotation">
                        <xsl:with-param name="name" select="name()"/>
                        <xsl:with-param name="value" select="."/>
                        <xsl:with-param name="attachTo" select="../@id"/>
                    </xsl:call-template>
                </xsl:if>
            </xsl:for-each>
            <!--xsl:call-template name="link-with-ontology">
                <xsl:with-param name="id" select="@id"/>
                <xsl:with-param name="annotation" select="@pos"/>
                <xsl:with-param name="annomodel">http://nachhalt.sfb632.uni-potsdam.de/owl/stts.owl</xsl:with-param>
            </xsl:call-template-->
            <!--xsl:call-template name="link-with-ontology">
                <xsl:with-param name="id" select="@id"/>
                <xsl:with-param name="annotation" select="@morph"/>
                <xsl:with-param name="annomodel">http://nachhalt.sfb632.uni-potsdam.de/owl/tiger.owl</xsl:with-param>
            </xsl:call-template-->
        </xsl:for-each>
        <xsl:for-each select=".//nt">
            <xsl:call-template name="write-nonterminal">
                <xsl:with-param name="id" select="@id"/>
                <xsl:with-param name="layerid">syntax</xsl:with-param>
                <xsl:with-param name="rootid" select="./ancestor::s[1]/graph[1]/@root"/>
                <xsl:with-param name="first-terminal">
                    <xsl:call-template name="get-first-terminal"/>
                </xsl:with-param>
                <xsl:with-param name="last-terminal">
                    <xsl:call-template name="get-last-terminal"/>
                </xsl:with-param>
            </xsl:call-template>
            <xsl:for-each select="@*">
                <xsl:if test="name()!='id' and string-length(.)&gt;0">
                    <xsl:call-template name="assign-annotation">
                        <xsl:with-param name="name" select="name()"/>
                        <xsl:with-param name="value" select="."/>
                        <xsl:with-param name="attachTo" select="../@id"/>
                    </xsl:call-template>
                </xsl:if>
            </xsl:for-each>
            <!--xsl:call-template name="link-with-ontology">
                <xsl:with-param name="id" select="@id"/>
                <xsl:with-param name="annotation" select="@cat"/>
                <xsl:with-param name="annomodel">http://nachhalt.sfb632.uni-potsdam.de/owl/tiger-syntax.owl</xsl:with-param>
            </xsl:call-template-->
            <xsl:for-each select="edge">
                <xsl:variable name="id" select="concat(../@id,'_to_',@idref)"/>
                <xsl:call-template name="write-dom-rel">
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="src" select="../@id"/>
                    <xsl:with-param name="tgt" select="@idref"/>
                </xsl:call-template>
                <xsl:for-each select="@*">
                    <xsl:if test="name()!='idref' and string-length(.)&gt;0">
                        <xsl:call-template name="assign-annotation">
                            <xsl:with-param name="name" select="name()"/>
                            <xsl:with-param name="value" select="."/>
                            <xsl:with-param name="attachTo" select="$id"/>
                        </xsl:call-template>
                    </xsl:if>
                </xsl:for-each>
                <xsl:call-template name="assign-annotation">
                    <xsl:with-param name="name">type</xsl:with-param>
                    <xsl:with-param name="value">edge</xsl:with-param>
                    <xsl:with-param name="attachTo" select="$id"/>
                </xsl:call-template>
                <!--xsl:call-template name="link-with-ontology">
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="annotation" select="@label"/>
                    <xsl:with-param name="annomodel">http://nachhalt.sfb632.uni-potsdam.de/owl/tiger-syntax.owl</xsl:with-param>
                </xsl:call-template-->
            </xsl:for-each>
            <xsl:for-each select="secedge">
                <xsl:variable name="id" select="concat(../@id,'_to_',@idref)"/>
                <xsl:call-template name="write-point-rel">
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="src" select="../@id"/>
                    <xsl:with-param name="tgt" select="@idref"/>
                </xsl:call-template>
                <xsl:for-each select="@*">
                    <xsl:if test="name()!='idref' and string-length(.)&gt;0">
                        <xsl:call-template name="assign-annotation">
                            <xsl:with-param name="name" select="name()"/>
                            <xsl:with-param name="value" select="."/>
                            <xsl:with-param name="attachTo" select="$id"/>
                        </xsl:call-template>
                    </xsl:if>
                </xsl:for-each>
                <xsl:call-template name="assign-annotation">
                    <xsl:with-param name="name">type</xsl:with-param>
                    <xsl:with-param name="value">secedge</xsl:with-param>
                    <xsl:with-param name="attachTo" select="$id"/>
                </xsl:call-template>
                <!--xsl:call-template name="link-with-ontology">
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="annotation" select="@label"/>
                    <xsl:with-param name="annomodel">http://nachhalt.sfb632.uni-potsdam.de/owl/tiger-syntax.owl</xsl:with-param>
                </xsl:call-template-->
            </xsl:for-each>
        </xsl:for-each>
        <xsl:call-template name="write-root">
            <xsl:with-param name="id" select="graph/@root"/>
            <xsl:with-param name="first-terminal" select=".//t[1]/@id"/>
            <xsl:with-param name="last-terminal" select=".//t[last()]/@id"/>
            <xsl:with-param name="layer-id">syntax</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!-- called on an nt node, returns first terminal -->
    <xsl:template name="get-first-terminal">
        <xsl:param name="nt_id" select="@id"/>
        <xsl:param name="t_id" select="./ancestor-or-self::graph//t[1]/@id"/>
        <xsl:variable name="covered">
            <xsl:call-template name="is-covered">
                <xsl:with-param name="nt_id" select="$nt_id"/>
                <xsl:with-param name="t_id" select="$t_id"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$covered='true'">
                <xsl:value-of select="$t_id"/>
            </xsl:when>
            <xsl:when test="count(./ancestor-or-self::graph//t[@id=$t_id]/following-sibling::t[1])=0">
                <xsl:message>warning: nonterminal <xsl:value-of select="$nt_id"/> does not cover any terminal node</xsl:message>
            </xsl:when> 
            <xsl:otherwise>
                <xsl:call-template name="get-first-terminal">
                    <xsl:with-param name="nt_id" select="$nt_id"/>
                    <xsl:with-param name="t_id" select="./ancestor-or-self::graph//t[@id=$t_id]/following-sibling::t[1]/@id"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- called on an nt node, returns last terminal -->
    <xsl:template name="get-last-terminal">
        <xsl:param name="nt_id" select="@id"/>
        <xsl:param name="t_id" select="./ancestor-or-self::graph//t[last()]/@id"/>
        <xsl:variable name="covered">
            <xsl:call-template name="is-covered">
                <xsl:with-param name="nt_id" select="$nt_id"/>
                <xsl:with-param name="t_id" select="$t_id"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$covered='true'">
                <xsl:value-of select="$t_id"/>
            </xsl:when>
            <xsl:when test="count(./ancestor-or-self::graph//t[@id=$t_id]/preceding-sibling::t[1])=0">
                <xsl:message>warning: nonterminal <xsl:value-of select="$nt_id"/> does not cover any terminal node</xsl:message>
            </xsl:when> 
            <xsl:otherwise>
                <xsl:call-template name="get-last-terminal">
                    <xsl:with-param name="nt_id" select="$nt_id"/>
                    <xsl:with-param name="t_id" select="./ancestor-or-self::graph//t[@id=$t_id]/preceding-sibling::t[1]/@id"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- note: exploits the fact that every node has at most one parent linked with an edge -->   
    <xsl:template name="is-covered">
        <xsl:param name="t_id"/>
        <xsl:param name="nt_id"/>
        <!--xsl:message>is-covered(<xsl:value-of select="$t_id"/>, <xsl:value-of select="$nt_id"/>)</xsl:message-->
        <xsl:choose>
            <xsl:when test="$nt_id=$t_id">true</xsl:when>
            <xsl:when test="count(./ancestor-or-self::graph[1]//nt[@id=$nt_id][1])=1 and count(./ancestor-or-self::graph[1]//*[name()='t' or name()='nt'][@id=$t_id][1])=1 and count(./ancestor-or-self::graph[1]//nt[edge/@idref=$t_id][1])=1">
                <xsl:call-template name="is-covered">
                    <xsl:with-param name="nt_id" select="$nt_id"/>
                    <xsl:with-param name="t_id" select="./ancestor-or-self::graph//nt[edge/@idref=$t_id][1]/@id"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>false</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- generic POWLA conversion routines --> 
        <!-- no metadata yet -->

    <!-- some static declarations in order to guarantee OWL/DL: reasoners can be quite picky wrt imports --> 
    <xsl:template name="initialize-powla">
        <owl:ObjectProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#firstTerminal"/>
        <owl:ObjectProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#hasChild"/>
        <owl:ObjectProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#hasDocument"/>
        <owl:ObjectProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#hasLayer"/>
        <owl:ObjectProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#hasNonterminal"/>
        <owl:ObjectProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#hasParent"/>
        <owl:ObjectProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#hasRoot"/>
        <owl:ObjectProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#hasSource"/>
        <owl:ObjectProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#hasSubDocument"/>
        <owl:ObjectProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#hasSuperDocument"/>
        <owl:ObjectProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#hasTarget"/>
        <owl:ObjectProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#hasTerminal"/>
        <owl:ObjectProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#isSourceOf"/>
        <owl:ObjectProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#isTargetOf"/>
        <owl:ObjectProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#lastTerminal"/>
        <owl:ObjectProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#nextNode"/>
        <owl:ObjectProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#previousNode"/>
        <owl:ObjectProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#rootOfDocument"/>
        <owl:DatatypeProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#documentID"/>
        <owl:DatatypeProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#endPosition"/>
        <owl:DatatypeProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#endPosition"/>
        <owl:DatatypeProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#hasAnnotation"/>
        <owl:DatatypeProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#hasMetadata"/>
        <owl:DatatypeProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#layerID"/>
        <owl:DatatypeProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#startPosition"/>
        <owl:DatatypeProperty rdf:about="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#hasStringValue"/>
    </xsl:template>
    
    
    <xsl:template name="write-corpus">
        <xsl:param name="corpusid"/>
        <powla:Corpus rdf:about="{concat('',$corpusid)}">
            <powla:documentID>
                <xsl:value-of select="$corpusid"/>
            </powla:documentID>
        </powla:Corpus>
    </xsl:template>
    
    <xsl:template name="write-layer">
        <xsl:param name="id"/>
        <xsl:param name="type">DocumentLayer</xsl:param>
        <xsl:param name="documentid"/>
        <powla:DocumentLayer rdf:about="{$id}">
            <powla:hasDocument rdf:resource="{$documentid}"/>
            <powla:layerID>
               <xsl:value-of select="$id"/> 
            </powla:layerID>
        </powla:DocumentLayer>
        <xsl:element name="{concat('powla:',$type)}">
            <xsl:attribute name="rdf:about">
                <xsl:value-of select="concat('',$id)"/>
            </xsl:attribute>
        </xsl:element>
        <powla:Document rdf:about="{concat('',$documentid)}">
            <powla:hasLayer rdf:resource="{$id}"/>
        </powla:Document>
    </xsl:template>

    <xsl:template name="write-terminal">
        <xsl:param name="id"/>
        <xsl:param name="layerid"/>
        <xsl:param name="next-terminal"/>
        <xsl:param name="prec-terminal"/>
        <xsl:param name="rootid"/>
        <xsl:param name="start-position"/>
        <xsl:param name="string"/>
        <powla:Terminal rdf:about="{concat('',$id)}">
            <powla:hasLayer rdf:resource="{$layerid}"/>
            <powla:hasRoot rdf:resource="{$rootid}"/>
            <xsl:if test="string-length($next-terminal)&gt;0">
                <powla:nextNode rdf:resource="{$next-terminal}"/>
            </xsl:if>
            <xsl:if test="string-length($prec-terminal)&gt;0">
                <powla:previousNode rdf:resource="{$prec-terminal}"/>
            </xsl:if>
            <powla:hasStringValue>
                <xsl:value-of select="$string"/>
            </powla:hasStringValue>
            <powla:startPosition>
                <xsl:value-of select="$start-position"/>
            </powla:startPosition>
            <powla:endPosition>
                <xsl:value-of select="string-length($string)+number($start-position)"/>
            </powla:endPosition>
        </powla:Terminal>
        <powla:Root rdf:about="{concat('',$rootid)}">
            <powla:hasTerminal rdf:resource="{$id}"/>
        </powla:Root>
    </xsl:template>
    
    <xsl:template name="assign-annotation">
        <xsl:param name="name"/>
        <xsl:param name="attachTo"/>
        <xsl:param name="value"/>
        <rdf:Description  rdf:about="{concat('#has_',$name)}">
            <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#DatatypeProperty"/>
            <rdfs:subPropertyOf rdf:resource="file:/C:/Dokumente%20und%20Einstellungen/Christian/Desktop/powla/powla.owl#hasAnnotation"/>
        </rdf:Description>
        <rdf:Description rdf:about="{concat('',$attachTo)}">
            <xsl:element name="{concat('powla:has_',$name)}">
                <xsl:value-of select="$value"/>
            </xsl:element>
        </rdf:Description>
    </xsl:template>

    <xsl:template name="write-nonterminal">
        <xsl:param name="id"/>
        <xsl:param name="rootid"/>
        <xsl:param name="layerid"/>
        <xsl:param name="first-terminal"/>
        <xsl:param name="last-terminal"/>
        <powla:Nonterminal rdf:about="{concat('',$id)}">
            <powla:hasLayer rdf:resource="{$layerid}"/>
            <powla:hasRoot rdf:resource="{$rootid}"/>
            <powla:firstTerminal rdf:resource="{$first-terminal}"/>
            <powla:lastTerminal rdf:resource="{$last-terminal}"/>
        </powla:Nonterminal>
    </xsl:template>

    <!-- dom rel are Relations accompanied by a hasChild (i.e., coverage) relationship, not explicitly marked -->
    <xsl:template name="write-dom-rel">
        <xsl:param name="id"/>
        <xsl:param name="src"/>
        <xsl:param name="tgt"/>
        <powla:Nonterminal rdf:about="{concat('',$src)}">
            <powla:hasChild rdf:resource="{$tgt}"/>
        </powla:Nonterminal>
        <powla:Node rdf:about="{concat('',$tgt)}">
            <powla:hasParent rdf:resource="{$src}"/>
        </powla:Node>
        <xsl:call-template name="write-point-rel">
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="src" select="$src"/>
            <xsl:with-param name="tgt" select="$tgt"/>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template name="write-point-rel">
        <xsl:param name="src"/>
        <xsl:param name="tgt"/>
        <xsl:param name="id"/>
        <powla:Relation rdf:about="{concat('',$id)}">
            <powla:hasSource rdf:resource="{$src}"/>
            <powla:hasTarget rdf:resource="{$tgt}"/>
        </powla:Relation>
        <powla:Node rdf:about="{concat('',$src)}">
            <powla:isSourceOf rdf:resource="{$id}"/>
        </powla:Node>
        <powla:Node rdf:about="{concat('',$tgt)}">
            <powla:isTargetOf rdf:resource="{$id}"/>
        </powla:Node>
    </xsl:template>
    
    <xsl:template name="write-root">
        <xsl:param name="id"/>
        <xsl:param name="first-terminal"/>
        <xsl:param name="last-terminal"/>
        <xsl:param name="layer-id"/>
        <powla:Root rdf:about="{concat('',$id)}">
            <powla:firstTerminal rdf:resource="{$first-terminal}"/>
            <powla:lastTerminal rdf:resource="{$last-terminal}"/>
        </powla:Root>
        <powla:Layer rdf:about="{concat('',$layer-id)}">
            <powla:rootOfDocument rdf:resource="{$id}"/>
        </powla:Layer>
    </xsl:template>
    
    <!-- olia routines (take care, heavy workload) -->
    <xsl:template name="link-with-ontology">
        <xsl:param name="annomodel"/>
        <xsl:param name="annotation"/>
        <xsl:param name="id"/>
        <xsl:if test="string-length($annotation)&gt;0">
            <xsl:message>
                <xsl:value-of select="concat('link-with-ontology(',$annomodel,',',$annotation,',',$id,')')"/>
            </xsl:message>
            <xsl:for-each select="document($annomodel)/rdf:RDF/*[$annotation = olia_system:hasTag/text()]"> <!-- or 
                starts-with($annotation,olia_system:hasTagStartingWith/text()) or 
                ends-with($annotation,olia_system:hasTagEndingWith/text()) or 
                contains($annotation,olia_system:hasTagContaining/text())]"-->
                    <!-- the xsl2 functions DO work, but too slow for any reasonable application ... but these things shouldn't be done in XSL/T anyway, here just as a proof of concept -->
                <rdf:Description rdf:about="{$id}">
                    <rdf:type rdf:resource="{concat($annomodel,'#',name())}"/>
                    <xsl:for-each select=".//*">
                        <xsl:copy>
                        <xsl:for-each select="@*">
                            <xsl:copy/>
                        </xsl:for-each>
                        <xsl:for-each select="text()">
                            <xsl:copy/>
                        </xsl:for-each>
                        </xsl:copy>
                    </xsl:for-each>
                </rdf:Description>
                <xsl:message>did it</xsl:message>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>

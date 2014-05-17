<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
      xmlns:xs="http://www.w3.org/2001/XMLSchema"
      exclude-result-prefixes="xs"
      version="2.0">

<xsl:output method="xml" indent="yes"/>
    
    <xsl:param name="id" select="base-uri()"/>
    
    <!-- convert a file with labeled tree nodes to tiger xml:
         (1) /*/* => sentence
         (2) ignore all element names
         (3) keep attributes as node features, except for edge_XYZ, these will become edge features (without edge_ prefix) 
    -->
    <xsl:template match="/">
        <corpus xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:noNamespaceSchemaLocation="http://www.ims.uni-stuttgart.de/projekte/TIGER/TIGERSearch/public/TigerXML.xsd"
            id="{$id}">
            
            <body id="{replace(replace($id,'.*[\\/]',''),'\.[^\.]*$','')}">
                <xsl:for-each select="/*/*[(not(exists(@cat)) or @cat!='CODE') and (not(exists(@pos)) or @pos!='CODE')]">
                    <xsl:variable name="sid" select="count(./preceding-sibling::*)+1"/>
                    <xsl:variable name="sprec" select="count(./preceding::*)+count(./ancestor::*)"/>
                    <s id="s{$sid}">
                        <graph root="s{$sid}_1">
                           <terminals>
                               <xsl:for-each select="./descendant-or-self::*[not(exists(*))][not(exists/@pos) or @pos!='CODE']">
                                   <xsl:variable name="nid" select="count(./preceding::*)+count(./ancestor::*)-$sprec+1"/>
                                   <t id="s{$sid}_{$nid}">
                                       <xsl:for-each select="@*">
                                           <xsl:copy-of select="."/>
                                       </xsl:for-each>
                                   </t>
                               </xsl:for-each>
                           </terminals>
                            <nonterminals>
                                <xsl:for-each select="./descendant-or-self::*[exists(*)][not(exists(@cat)) or @cat!='CODE']">
                                    <xsl:variable name="nid" select="count(./preceding::*)+count(./ancestor::*)-$sprec+1"/>
                                    <nt id="s{$sid}_{$nid}">
                                        <xsl:for-each select="@*">
                                            <xsl:if test="name()!='edge_label'">
                                                <xsl:copy-of select="."/>
                                            </xsl:if>
                                        </xsl:for-each>
                                        <xsl:for-each select="*">
                                            <edge idref="s{$sid}_{count(./preceding::*)+count(./ancestor::*)-$sprec+1}">
                                                <xsl:attribute name="label">
                                                    <xsl:choose>
                                                        <xsl:when test="normalize-space(@edge_label)=''">--</xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:value-of select="@edge_label"/>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </xsl:attribute>
                                            </edge>
                                        </xsl:for-each>
                                    </nt>
                                </xsl:for-each>
                            </nonterminals>
                        </graph>
                    </s>
                </xsl:for-each>
            </body>
        </corpus>
    </xsl:template>
</xsl:stylesheet>
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <xsl:include href="../two/2nd.xsl" />
    
    <xsl:template match="/input" >
        <output>
            <xsl:apply-templates />
        </output>    
    </xsl:template>
    
    <xsl:template match="*">
        <unmatched-element localname="{local-name(.)}">
            <xsl:apply-templates />
        </unmatched-element>
    </xsl:template>
    
    <xsl:template match="one">
        <xsl:variable name="external" select="document('1st.xml')" />
        <xsl:copy-of select="$external/data1" />
    </xsl:template>

</xsl:stylesheet>

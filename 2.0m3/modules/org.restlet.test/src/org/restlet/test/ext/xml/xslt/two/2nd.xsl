<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:include href="riap://component/three/3rd.xsl" />

    <xsl:template match="*[@attTwo='2']">      
        <xsl:variable name="external" select="document('2nd.xml')" />
        <xsl:copy-of select="$external/data2" />
    </xsl:template>    
    
</xsl:stylesheet>

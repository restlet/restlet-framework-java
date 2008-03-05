<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
 <html><head></head><body>
 <p align="center" style="font-family:Tahoma; font-size:64px; color:red">
  <xsl:value-of select="." />
 </p>
 </body></html>
</xsl:template>

</xsl:stylesheet>
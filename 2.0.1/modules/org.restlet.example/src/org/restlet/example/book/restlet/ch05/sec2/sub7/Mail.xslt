<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<email>
			<head>
				<subject>
					<xsl:value-of select="/mail/subject" />
				</subject>
				<from>chunkylover53@rmep.org</from>
				<to>test@domain.com</to>
			</head>
			<body>
				<xsl:value-of select="/mail/content" />
			</body>
		</email>
	</xsl:template>
</xsl:stylesheet>
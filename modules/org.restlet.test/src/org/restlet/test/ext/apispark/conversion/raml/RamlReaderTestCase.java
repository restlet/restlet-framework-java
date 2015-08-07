package org.restlet.test.ext.apispark.conversion.raml;

import java.io.InputStream;

import org.junit.Test;
import org.raml.model.Raml;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.restlet.ext.apispark.internal.conversion.raml.RamlReader;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.test.RestletTestCase;
import org.restlet.test.ext.apispark.conversion.DefinitionComparator;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RamlReaderTestCase extends RestletTestCase {
    
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testRamlImport() throws Exception {
        InputStream savedDefinitionStream = getClass().getResourceAsStream("api.rwadef");
        Definition savedDefinition = mapper.readValue(savedDefinitionStream, Definition.class);
        
        String savedRamlLocation = getClass().getResource("api.raml").toString();
        Raml savedRaml = new RamlDocumentBuilder().build(savedRamlLocation);

        Definition translatedDefinition = RamlReader.translate(savedRaml);

        DefinitionComparator.compareDefinitions(savedDefinition, translatedDefinition);
    }
}

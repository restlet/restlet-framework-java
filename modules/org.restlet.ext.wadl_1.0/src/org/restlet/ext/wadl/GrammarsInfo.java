/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.wadl;

import java.util.List;

/**
 * Describes the grammars used by representation descriptions. This is
 * especially useful to formally describe XML representations using XML Schema
 * or Relax NG standards.
 * 
 * @author Jerome Louvel
 */
public class GrammarsInfo {

    private List<DocumentationInfo> documentations;

    private List<IncludeInfo> includes;

    public List<DocumentationInfo> getDocumentations() {
        return documentations;
    }

    public List<IncludeInfo> getIncludes() {
        return includes;
    }

    public void setDocumentations(List<DocumentationInfo> doc) {
        this.documentations = doc;
    }

    public void setIncludes(List<IncludeInfo> includes) {
        this.includes = includes;
    }
}

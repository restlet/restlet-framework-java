/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.ext.odata.deepexpand.model;

import java.util.List;

import org.restlet.test.ext.odata.deepexpand.model.Multilingual;
import org.restlet.test.ext.odata.deepexpand.model.Student;

/**
 * Generated by the generator tool for the OData extension for the Restlet
 * framework.<br>
 * 
 * @see <a
 *      href="http://praktiki.metal.ntua.gr/CoopOData/CoopOData.svc/$metadata">Metadata
 *      of the target OData service</a>
 * 
 */
public class Nationality {

    private String code;

    private int id;

    private Multilingual name;

    private List<Student> students;

    /**
     * Constructor without parameter.
     * 
     */
    public Nationality() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param id
     *            The identifiant value of the entity.
     */
    public Nationality(int id) {
        this();
        this.id = id;
    }

    /**
     * Returns the value of the "code" attribute.
     * 
     * @return The value of the "code" attribute.
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the value of the "id" attribute.
     * 
     * @return The value of the "id" attribute.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the value of the "name" attribute.
     * 
     * @return The value of the "name" attribute.
     */
    public Multilingual getName() {
        return name;
    }

    /**
     * Returns the value of the "students" attribute.
     * 
     * @return The value of the "students" attribute.
     */
    public List<Student> getStudents() {
        return students;
    }

    /**
     * Sets the value of the "code" attribute.
     * 
     * @param code
     *            The value of the "code" attribute.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Sets the value of the "id" attribute.
     * 
     * @param id
     *            The value of the "id" attribute.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the value of the "name" attribute.
     * 
     * @param name
     *            " The value of the "name" attribute.
     */
    public void setName(Multilingual name) {
        this.name = name;
    }

    /**
     * Sets the value of the "students" attribute.
     * 
     * @param students
     *            " The value of the "students" attribute.
     */
    public void setStudents(List<Student> students) {
        this.students = students;
    }

}
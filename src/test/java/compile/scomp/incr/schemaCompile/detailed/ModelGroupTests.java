/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package compile.scomp.incr.schemaCompile.detailed;

import org.apache.xmlbeans.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static compile.scomp.common.CompileTestBase.*;
import static compile.scomp.incr.schemaCompile.detailed.IncrCompilationTests.getBaseSchema;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Disabled("Currently all tests receive a duplicate schema entry exception")
public class ModelGroupTests {

    private static final File outincr = xbeanOutput(INCR_PATH);
    private static final File out = xbeanOutput(OUT_PATH);

    private final List<XmlError> errors = new ArrayList<>();
    private final XmlOptions xm = new XmlOptions();

    private File obj1File, obj2File;

    public ModelGroupTests() {
        xm.setErrorListener(errors);
        xm.setSavePrettyPrint();
    }

    @BeforeEach
    public void setUp() throws IOException {
        clearOutputDirs();
        errors.clear();

        obj1File = File.createTempFile("obj1_", ".xsd");
        obj2File = File.createTempFile("obj2_", ".xsd");
    }

    @AfterEach
    public void tearDown() throws Exception {
        obj1File.delete();
        obj2File.delete();
    }

    private XmlObject[] getSchema(File objFile, String schemaString) throws IOException, XmlException {
        OutputStream fos = Files.newOutputStream(objFile.toPath());
        OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
        if (!schemaString.startsWith("<?xml")) {
            osw.write(getSchemaTop("baz"));
        }
        osw.write(schemaString);
        if (!schemaString.startsWith("<?xml")) {
            osw.write(getSchemaBottom());
        }
        osw.close();

        XmlObject obj = XmlObject.Factory.parse(objFile);
        obj.documentProperties().setSourceName(objFile.toURI().toASCIIString());
        return new XmlObject[]{obj};
    }

    @Test
    void test_model_diffns_choice2seqchange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject[] schemas = getSchema(obj1File,
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:choice>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:choice>" +
                "</xs:complexType>");
        XmlObject[] schemas2 = getSchema(obj2File,
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:sequence>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:sequence>" +
                "</xs:complexType>");

        SchemaTypeSystem base = compileSchemas(schemas, out, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, outincr, xm);

        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        //        if (incr.findElement(incrTypes[0]).getType().g
        assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    void test_model_seq2choicechange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject[] schemas = getSchema(obj1File,
            "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
            "<xs:complexType name=\"aType\">" +
            "<xs:sequence>" +
            "<xs:element name=\"a\" type=\"xs:string\" />" +
            "<xs:element name=\"b\" type=\"xs:string\" />" +
            "</xs:sequence>" +
            "</xs:complexType>");
        XmlObject[] schemas2 = getSchema(obj2File,
            "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
            "<xs:complexType name=\"aType\">" +
            "<xs:choice>" +
            "<xs:element name=\"a\" type=\"xs:string\" />" +
            "<xs:element name=\"b\" type=\"xs:string\" />" +
            "</xs:choice>" +
            "</xs:complexType>");

        SchemaTypeSystem base = compileSchemas(schemas, out, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, outincr, xm);

        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    void test_model_seq2choicechange_diffns() throws Exception {
        XmlObject[] schemas = {XmlObject.Factory.parse(getBaseSchema("bar", "elName", "attrName", "string"))};

        XmlObject[] schemas2 = getSchema(obj2File,
            "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
            "<xs:complexType name=\"aType\">" +
            "<xs:sequence>" +
            "<xs:element name=\"a\" type=\"xs:string\" />" +
            "<xs:element name=\"b\" type=\"xs:string\" />" +
            "</xs:sequence>" +
            "</xs:complexType>");

        SchemaTypeSystem base = compileSchemas(schemas, out, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, outincr, xm);

        QName[] baseTypes = new QName[]{new QName("http://bar", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        //        if (incr.findElement(incrTypes[0]).getType().g
        assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    void test_model_seq2allchange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject[] schemas = getSchema(obj1File,
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:sequence>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:sequence>" +
                "</xs:complexType>");
        XmlObject[] schemas2 = getSchema(obj2File,
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:all>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:all>" +
                "</xs:complexType>");

        SchemaTypeSystem base = compileSchemas(schemas, out, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, outincr, xm);

        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        //        if (incr.findElement(incrTypes[0]).getType().g
        assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    void test_model_all2seqchange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject[] schemas = getSchema(obj1File,
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:all>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:all>" +
                "</xs:complexType>");
        XmlObject[] schemas2 = getSchema(obj2File,
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:sequence>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:sequence>" +
                "</xs:complexType>");

        SchemaTypeSystem base = compileSchemas(schemas, out, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, outincr, xm);

        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        //        if (incr.findElement(incrTypes[0]).getType().g
        assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    void test_model_all2choicechange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject[] schemas = getSchema(obj1File,
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:all>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:all>" +
                "</xs:complexType>");
        XmlObject[] schemas2 = getSchema(obj2File,
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:choice>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:choice>" +
                "</xs:complexType>");

        SchemaTypeSystem base = compileSchemas(schemas, out, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, outincr, xm);

        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        //        if (incr.findElement(incrTypes[0]).getType().g
        assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    void test_model_choice2choicechange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject[] schemas = getSchema(obj1File,
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:choice>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:choice>" +
                "</xs:complexType>");
        XmlObject[] schemas2 = getSchema(obj2File,
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:all>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:all>" +
                "</xs:complexType>");

        SchemaTypeSystem base = compileSchemas(schemas, out, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, outincr, xm);

        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        //        if (incr.findElement(incrTypes[0]).getType().g
        assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        assertTrue(errors.isEmpty());
    }
}

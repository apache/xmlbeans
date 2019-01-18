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

import compile.scomp.common.CompileCommon;
import compile.scomp.common.CompileTestBase;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static org.junit.Assert.assertNotSame;


@Ignore("Currently all tests receive a duplicate schema entry exception")
public class ModelGroupTests extends CompileTestBase {

    File obj1File, obj2File;

    @Before
    public void setUp() throws IOException {
        CompileCommon.deltree(CompileCommon.xbeanOutput(outputDir));
        out = CompileCommon.xbeanOutput(outPath);
        sanity = CompileCommon.xbeanOutput(sanityPath);
        outincr = CompileCommon.xbeanOutput(incrPath);

        errors = new ArrayList();
        xm = new XmlOptions();
        xm.setErrorListener(errors);
        xm.setSavePrettyPrint();

        obj1File = File.createTempFile("obj1_", ".xsd");
        obj2File = File.createTempFile("obj2_", ".xsd");
    }

    @After
    public void tearDown() throws Exception {
        if (errors.size() > 0) {
            errors.clear();
        }
        obj1File.delete();
        obj2File.delete();
    }

    private XmlObject[] getSchema(File objFile, String schemaString) throws IOException, XmlException {
        FileOutputStream fos = new FileOutputStream(objFile);
        OutputStreamWriter osw = new OutputStreamWriter(fos, Charset.forName("UTF-8"));
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
    public void test_model_diffns_choice2seqchange() throws Exception {
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

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        //        if (incr.findElement(incrTypes[0]).getType().g
        assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    @Test
    public void test_model_seq2choicechange() throws Exception {
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

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    @Test
    public void test_model_seq2choicechange_diffns() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject[] schemas = getSchema(obj1File,
            getBaseSchema("bar", "elName", "string", "attrName", "string"));
        XmlObject[] schemas2 = getSchema(obj2File,
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:sequence>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:sequence>" +
                "</xs:complexType>");

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://bar", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        //        if (incr.findElement(incrTypes[0]).getType().g
        assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    @Test
    public void test_model_seq2allchange() throws Exception {
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

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        //        if (incr.findElement(incrTypes[0]).getType().g
        assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    @Test
    public void test_model_all2seqchange() throws Exception {
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

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        //        if (incr.findElement(incrTypes[0]).getType().g
        assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    @Test
    public void test_model_all2choicechange() throws Exception {
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

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        //        if (incr.findElement(incrTypes[0]).getType().g
        assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    @Test
    public void test_model_choice2choicechange() throws Exception {
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

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        //        if (incr.findElement(incrTypes[0]).getType().g
        assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }
}

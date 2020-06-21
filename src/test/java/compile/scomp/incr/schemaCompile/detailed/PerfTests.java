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
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertNotSame;


public class PerfTests extends CompileTestBase {
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
    }

    @After
    public void tearDown() throws Exception {
        if (errors.size() > 0)
            errors.clear();
    }

    @Ignore("throws duplicate global type")
    @Test
    public void test_perf_choice2seqchange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject obj1 = XmlObject.Factory.parse(getSchemaTop("baz") +
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:choice>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:choice>" +
                "</xs:complexType>" + getSchemaBottom());
        XmlObject obj2 = XmlObject.Factory.parse(getSchemaTop("baz") +
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:sequence>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:sequence>" +
                "</xs:complexType>" + getSchemaBottom());
        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj2");

        long initBase = System.currentTimeMillis();
        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        long endBase = System.currentTimeMillis();

        long initIncr = System.currentTimeMillis();
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);
        long endIncr = System.currentTimeMillis();

        checkPerf(initBase, endBase, initIncr, endIncr);
        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }



}

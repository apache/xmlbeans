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

import junit.framework.TestCase;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import org.apache.xmlbeans.impl.tool.CodeGenUtil;
import org.apache.xmlbeans.impl.tool.SchemaCodeGenerator;
import org.apache.xmlbeans.impl.tool.Diff;
import org.w3.x2001.xmlSchema.SchemaDocument;
import org.w3.x2001.xmlSchema.TopLevelComplexType;
import org.apache.xmlbeans.*;

import java.io.File;
import java.io.StringWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import tools.util.TestRunUtil;
import compile.scomp.common.CompileCommon;
import compile.scomp.common.CompileTestBase;

import javax.xml.namespace.QName;


/**
 * @author jacobd
 * Date: Aug 2, 2004
 */
public class IncrCompilationTests extends CompileTestBase {


    public IncrCompilationTests(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(IncrCompilationTests.class);
    }

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

    public void tearDown() throws IOException {
        if (errors.size() > 0)
            errors.clear();
    }


    public void test_dupetype_diffns() throws Exception {
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(getBaseSchema("bar", "elName", "string", "attrName", "string"));
        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj2");

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr;
        try {
            incr = incrCompileXsd(base, schemas2, builtin, xm);
        } catch (XmlException xmlEx) {
            throw new Exception(xmlEx.getError().toString());
        }
        if (base.findElement(new QName("http://baz", "elName")) == null)
            throw new Exception("BASE: Baz elName was not found");

        if (incr.findElement(new QName("http://baz", "elName")) == null)
            throw new Exception("INCR: Baz elName was not found");

        for (int i = 0; i < incr.globalElements().length; i++) {
            System.out.println("[" + i + "]-" + incr.globalElements()[i].getName());
        }

        for (int i = 0; i < base.globalElements().length; i++) {
            System.out.println("[" + i + "]-" + base.globalElements()[i].getName());
        }

        if (incr.findElement(new QName("http://bar", "elName")) == null)
            throw new Exception("INCR: Bar elName was not found");

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }


    public void test_dupens_difftypename() throws Exception {
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(getBaseSchema("baz", "elName2", "string", "attrName2", "string"));
        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj2");

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName"),
                                        new QName("http://baz", "elName2")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }


    /**
     * This test should not change sts since xmlobject is same
     * @throws Exception
     */
    public void test_dupens_dupetypename() throws Exception {
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName", "string"));
        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj2");

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        Assert.assertEquals(base, incr);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    public void test_dupens_attrnamechange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName2", "string"));
        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj2");

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        Assert.assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    public void test_dupens_attrtypechange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName", "int"));
        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj2");

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        Assert.assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }


    public void test_dupens_eltypechange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "int", "attrName", "string"));
        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj2");

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        //        if (incr.findElement(incrTypes[0]).getType().g
        Assert.assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    public void test_typechange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "int", "attrName2", "string"));
        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj2");

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        System.out.println("Type: " + incr.findElement(incrTypes[0]).getType());
        System.out.println("Name: " + incr.findElement(incrTypes[0]).getType().getName());
        System.out.println("Name: " + incr.findElement(incrTypes[0]).getType().getElementProperties()[0].getType());

        Assert.assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }


}

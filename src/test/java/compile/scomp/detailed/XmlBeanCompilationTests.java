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

package compile.scomp.detailed;

import compile.scomp.common.CompileTestBase;
import compile.scomp.common.mockobj.TestBindingConfig;
import compile.scomp.common.mockobj.TestFiler;
import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.xb.xmlconfig.ConfigDocument;
import org.junit.Test;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static common.Common.*;
import static compile.scomp.common.CompileTestBase.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Ensure that several compilation mechanisms all generate
 * the same schematypesystem
 */
public class XmlBeanCompilationTests {

    /**
     * Filer != null for BindingConfig to be used
     */
    @Test
    public void test_bindingconfig_extension_compilation() throws Exception {
        TestFiler f = new TestFiler();
        //initialize all of the values
        String extCaseDir = XBEAN_CASE_ROOT + P + "extensions" + P;
        String extSrcDir = getRootFile() + P + "src" + P + "test" + P + "java" + P + "xmlobject" + P + "extensions" + P;
        File[] cPath = CompileTestBase.getClassPath();
        String dir = extCaseDir + P + "interfaceFeature" + P + "averageCase";
        String dir2 = extCaseDir + P + "prePostFeature" + P + "ValueRestriction";

        ConfigDocument.Config bConf = ConfigDocument.Factory.parse(new File(dir + P + "po.xsdconfig")).getConfig();
        ConfigDocument.Config cConf = ConfigDocument.Factory.parse(new File(dir2 + P + "company.xsdconfig")).getConfig();

        String simpleConfig = "<xb:config " +
                              "xmlns:xb=\"http://xml.apache.org/xmlbeans/2004/02/xbean/config\"\n" +
                              " xmlns:ep=\"http://xbean.interface_feature/averageCase/PurchaseOrder\">\n" +
                              "<xb:namespace uri=\"http://xbean.interface_feature/averageCase/PurchaseOrder\">\n" +
                              "<xb:package>com.easypo</xb:package>\n" +
                              "</xb:namespace></xb:config>";
        ConfigDocument.Config confDoc = ConfigDocument.Factory.parse(simpleConfig).getConfig();
        ConfigDocument.Config[] confs = new ConfigDocument.Config[]{bConf, confDoc, cConf};

        File[] fList = Stream.of(
            "averageCase" + P + "existing" + P + "FooHandler.java",
            "averageCase" + P + "existing" + P + "IFoo.java",
            "ValueRestriction" + P + "existing" + P + "ISetter.java",
            "ValueRestriction" + P + "existing" + P + "SetterHandler.java"
        ).map(path -> new File(extSrcDir + P + "interfaceFeature" + P + path)).toArray(File[]::new);

        //use created BindingConfig
        TestBindingConfig bind = new TestBindingConfig(confs, fList, cPath);

        //set XSDs
        XmlObject obj1 = XmlObject.Factory.parse(new File(dir + P + "po.xsd"));
        XmlObject obj2 = XmlObject.Factory.parse(new File(dir2 + P + "company.xsd"));
        XmlObject[] schemas = {obj1, obj2};

        //filer must be present on this method
        List<XmlError> xm_errors = new ArrayList<>();
        XmlOptions xm_opts = new XmlOptions();
        xm_opts.setErrorListener(xm_errors);
        xm_opts.setSavePrettyPrint();

        SchemaTypeSystem apiSts = XmlBeans.compileXmlBeans("apiCompile", null,
            schemas, bind, XmlBeans.getBuiltinTypeSystem(), f, xm_opts);

        assertTrue("isIslookupPrefixForNamespace not invoked", bind.isIslookupPrefixForNamespace());
        assertTrue("isIslookupPackageForNamespace not invoked", bind.isIslookupPackageForNamespace());
        assertTrue("isIslookupSuffixForNamespace not invoked", bind.isIslookupSuffixForNamespace());
        assertTrue("isIslookupJavanameForQName not invoked", bind.isIslookupJavanameForQName());
        assertTrue("isIsgetInterfaceExtensionsString not invoked", bind.isIsgetInterfaceExtensionsString());
        assertTrue("isIsgetInterfaceExtensions not invoked", bind.isIsgetInterfaceExtensions());
        assertTrue("isIsgetPrePostExtensions not invoked", bind.isIsgetPrePostExtensions());
        assertTrue("isIsgetPrePostExtensionsString not invoked", bind.isIsgetPrePostExtensionsString());
    }

    /**
     * Verify basic incremental compilation
     * and compilation with partial SOM usages
     */
    @Test
    public void test_incrCompile() throws Exception {
        XmlObject obj1 = XmlObject.Factory.parse(FOR_XSD);
        obj1.documentProperties().setSourceName("OBJ1");
        XmlObject[] schemas = {obj1};
        QName sts1 = new QName("http://baz", "elName");

        XmlObject obj2 = XmlObject.Factory.parse(INCR_XSD);
        obj2.documentProperties().setSourceName("OBJ2");
        XmlObject[] schemas2 = {obj2};
        QName sts2 = new QName("http://bar", "elName");

        XmlObject obj3 = XmlObject.Factory.parse(ERR_XSD);
        obj3.documentProperties().setSourceName("OBJ3");
        XmlObject[] schemas3 = {obj3};
        QName sts3 = new QName("http://bar", "elErrName");

        SchemaTypeSystem sts;
        List<XmlError> err = new ArrayList<>();
        XmlOptions opt = new XmlOptions().setErrorListener(err);
        opt.setCompilePartialTypesystem();

        //BASIC COMPILATION
        sts = XmlBeans.compileXmlBeans(null, null, schemas, null, XmlBeans.getBuiltinTypeSystem(), null, opt);

        assertTrue("Errors should have been empty", err.isEmpty());
        // find element in the type System
        assertTrue("Could Not find Type from first Type System: " + sts1, findGlobalElement(sts.globalElements(), sts1));

        //SIMPLE INCR COMPILATION
        sts = XmlBeans.compileXmlBeans(null, sts, schemas2, null, XmlBeans.getBuiltinTypeSystem(), null, opt);
        assertTrue("Errors should have been empty", err.isEmpty());

        // find element in the type System
        assertTrue("Could Not find Type from first Type System: " + sts1, findGlobalElement(sts.globalElements(), sts1));
        assertTrue("Could Not find Type from 2nd Type System: " + sts2, findGlobalElement(sts.globalElements(), sts2));

        //BUILDING OFF BASE SIMPLE INCR COMPILATION
        sts = XmlBeans.compileXmlBeans(null, sts, schemas2, null, sts, null, opt);
        assertTrue("Errors should have been empty", err.isEmpty());

        // find element in the type System
        assertTrue("Could Not find Type from first Type System: " + sts1, findGlobalElement(sts.globalElements(), sts1));
        assertTrue("Could Not find Type from 2nd Type System: " + sts2, findGlobalElement(sts.globalElements(), sts2));

        //INCR COMPILATION WITH RECOVERABLE ERROR
        err.clear();
        SchemaTypeSystem b = XmlBeans.compileXmlBeans(null, sts, schemas3, null, XmlBeans.getBuiltinTypeSystem(), null, opt);
        // find element in the type System
        assertTrue("Could Not find Type from first Type System: " + sts1, findGlobalElement(b.globalElements(), sts1));

        assertTrue("Could Not find Type from 2nd Type System: " + sts2, findGlobalElement(b.globalElements(), sts2));

        assertTrue("Could Not find Type from 3rd Type System: " + sts3, findGlobalElement(b.globalElements(), sts3));

        //compare to the expected xm_errors
        assertEquals(1, err.size());
        XmlError xErr = err.get(0);
        assertEquals("src-resolve", xErr.getErrorCode());
        assertEquals("type 'bType@http://baz' not found.", xErr.getMessage());
    }
}

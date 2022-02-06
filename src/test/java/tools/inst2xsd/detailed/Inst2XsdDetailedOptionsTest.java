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
package tools.inst2xsd.detailed;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.inst2xsd.Inst2Xsd;
import org.apache.xmlbeans.impl.inst2xsd.Inst2XsdOptions;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import tools.inst2xsd.common.Inst2XsdCommon;

import java.io.File;

import static tools.inst2xsd.common.Inst2XsdCommon.*;
import static tools.inst2xsd.common.Inst2XsdTestBase.*;

public class Inst2XsdDetailedOptionsTest {

    @Test
    void test_simpleContentString_Russian() throws Exception {
        Inst2XsdOptions opt = getRussianOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
        XmlObject[] api = runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(Inst2XsdCommon.base)}, opt);

        checkLength(api, 1);

        Inst2Xsd.main(new String[]{"-validate", "-verbose",
                                   "-design", "rd",
                                   "-simple-content-types", "string",
                                   "-outDir", OPTION_CASES_DIR,
                                   "-outPrefix", "base",
                                   BASEXML});
        XmlObject cmdLine = XmlObject.Factory.parse(new File(EXPBASEXML));
        compare(api[0], cmdLine);
        XmlObject exp = XmlObject.Factory.parse(new File(OPTION_CASES_DIR + "base_rd_scs0.xsd"));
        compare(api[0], exp);

    }

    @Test
    @Disabled
    public void test_simpleContentString_Salami() throws Exception {
        Inst2XsdOptions opt = getSalamiOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
        XmlObject[] api = runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(Inst2XsdCommon.base)}, opt);
        checkLength(api, 1);

        Inst2Xsd.main(new String[]{"-validate", "-verbose",
                                   "-design", "ss",
                                   "-simple-content-types", "string",
                                   "-outDir", OPTION_CASES_DIR,
                                   "-outPrefix", "base",
                                   BASEXML});
        XmlObject cmdLine = XmlObject.Factory.parse(new File(EXPBASEXML));
        compare(api[0], cmdLine);
        XmlObject exp = XmlObject.Factory.parse(new File(OPTION_CASES_DIR + "base_ss_scs0.xsd"));
        compare(api[0], exp);
    }

    @Test
    void test_simpleContentString_Venetian() throws Exception {
        Inst2XsdOptions opt = getVenetianOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
        XmlObject[] api = runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(Inst2XsdCommon.base)}, opt);
        checkLength(api, 1);

        Inst2Xsd.main(new String[]{"-validate", "-verbose",
                                   "-design", "vb",
                                   "-simple-content-types", "string",
                                   "-outDir", OPTION_CASES_DIR,
                                   "-outPrefix", "base",
                                   BASEXML});

        XmlObject cmdLine = XmlObject.Factory.parse(new File(EXPBASEXML));
        compare(api[0], cmdLine);
        XmlObject exp = XmlObject.Factory.parse(new File(OPTION_CASES_DIR + "base_vb_scs0.xsd"));
        compare(api[0], exp);
    }

    @Test
    void test_simpleContentSmart_Russian() throws Exception {
        Inst2XsdOptions opt = getRussianOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_SMART);
        XmlObject[] api = runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(Inst2XsdCommon.base)}, opt);
        checkLength(api, 1);

        Inst2Xsd.main(new String[]{"-validate", "-verbose",
                                   "-design", "rd",
                                   "-simple-content-types", "smart",
                                   "-outDir", OPTION_CASES_DIR,
                                   "-outPrefix", "base",
                                   BASEXML});
        XmlObject cmdLine = XmlObject.Factory.parse(new File(EXPBASEXML));
        compare(api[0], cmdLine);
        XmlObject exp = XmlObject.Factory.parse(new File(OPTION_CASES_DIR + "base_rd0.xsd"));
        compare(api[0], exp);
    }

    @Test
    @Disabled
    public void test_simpleContentSmart_Salami() throws Exception {
        Inst2XsdOptions opt = getSalamiOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_SMART);
        XmlObject[] api = runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(Inst2XsdCommon.base)}, opt);
        checkLength(api, 1);

        Inst2Xsd.main(new String[]{"-validate", "-verbose",
                                   "-design", "ss",
                                   "-simple-content-types", "smart",
                                   "-outDir", OPTION_CASES_DIR,
                                   "-outPrefix", "base",
                                   BASEXML});
        XmlObject cmdLine = XmlObject.Factory.parse(new File(EXPBASEXML));
        compare(api[0], cmdLine);
        XmlObject exp = XmlObject.Factory.parse(new File(OPTION_CASES_DIR + "base_ss0.xsd"));
        compare(api[0], exp);
    }

    @Test
    void test_simpleContentSmart_Venetian() throws Exception {
        Inst2XsdOptions opt = getVenetianOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_SMART);
        XmlObject[] api = runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(Inst2XsdCommon.base)}, opt);
        checkLength(api, 1);

        Inst2Xsd.main(new String[]{"-validate", "-verbose",
                                   "-design", "vb",
                                   "-simple-content-types", "smart",
                                   "-outDir", OPTION_CASES_DIR,
                                   "-outPrefix", "base",
                                   BASEXML});
        XmlObject cmdLine = XmlObject.Factory.parse(new File(EXPBASEXML));
        compare(api[0], cmdLine);
        XmlObject exp = XmlObject.Factory.parse(new File(OPTION_CASES_DIR + "base_vb0.xsd"));
        compare(api[0], exp);
    }

    @Test
    void test_simpleContentSmart_NeverEnum_Russian() throws Exception {
        Inst2XsdOptions opt = getRussianOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_SMART);
        opt.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);
        XmlObject[] api = runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(Inst2XsdCommon.base)}, opt);
        checkLength(api, 1);

        Inst2Xsd.main(new String[]{"-validate", "-verbose",
                                   "-design", "rd",
                                   "-enumerations", "never",
                                   "-simple-content-types", "smart",
                                   "-outDir", OPTION_CASES_DIR,
                                   "-outPrefix", "base",
                                   BASEXML});
        XmlObject cmdLine = XmlObject.Factory.parse(new File(EXPBASEXML));
        compare(api[0], cmdLine);
        XmlObject exp = XmlObject.Factory.parse(new File(OPTION_CASES_DIR + "base_rd_eN0.xsd"));
        compare(api[0], exp);
    }

    @Test
    @Disabled
    public void test_simpleContentSmart_NeverEnum_Salami() throws Exception {
        Inst2XsdOptions opt = getSalamiOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_SMART);
        opt.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);
        XmlObject[] api = runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(Inst2XsdCommon.base)}, opt);
        checkLength(api, 1);

        Inst2Xsd.main(new String[]{"-validate", "-verbose",
                                   "-design", "ss",
                                   "-enumerations", "never",
                                   "-simple-content-types", "smart",
                                   "-outDir", OPTION_CASES_DIR,
                                   "-outPrefix", "base",
                                   BASEXML});
        XmlObject cmdLine = XmlObject.Factory.parse(new File(EXPBASEXML));
        compare(api[0], cmdLine);
        XmlObject exp = XmlObject.Factory.parse(new File(OPTION_CASES_DIR + "base_ss_eN0.xsd"));
        compare(api[0], exp);
    }

    @Test
    void test_simpleContentSmart_NeverEnum_Venetian() throws Exception {
        Inst2XsdOptions opt = getVenetianOptions();
        opt.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_SMART);
        XmlObject[] api = runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(Inst2XsdCommon.base)}, opt);
        checkLength(api, 1);

        Inst2Xsd.main(new String[]{"-validate", "-verbose",
                                   "-design", "vb",
                                   "-enumerations", "never",
                                   "-simple-content-types", "smart",
                                   "-outDir", OPTION_CASES_DIR,
                                   "-outPrefix", "base",
                                   BASEXML});
        XmlObject cmdLine = XmlObject.Factory.parse(new File(EXPBASEXML));
        compare(api[0], cmdLine);
        XmlObject exp = XmlObject.Factory.parse(new File(OPTION_CASES_DIR + "base_vb_eN0.xsd"));
        compare(api[0], exp);
    }

    @Test
    void test_simpleContentString_NeverEnum_Russian() throws Exception {
        Inst2XsdOptions opt = getRussianOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
        opt.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);
        XmlObject[] api = runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(Inst2XsdCommon.base)}, opt);
        checkLength(api, 1);

        Inst2Xsd.main(new String[]{"-validate", "-verbose",
                                   "-design", "rd",
                                   "-enumerations", "never",
                                   "-simple-content-types", "string",
                                   "-outDir", OPTION_CASES_DIR,
                                   "-outPrefix", "base",
                                   BASEXML});
        XmlObject cmdLine = XmlObject.Factory.parse(new File(EXPBASEXML));
        compare(api[0], cmdLine);
        XmlObject exp = XmlObject.Factory.parse(new File(OPTION_CASES_DIR + "base_rd_scs_eN0.xsd"));
        compare(api[0], exp);
    }

    @Test
    @Disabled
    public void test_simpleContentString_NeverEnum_Salami() throws Exception {
        Inst2XsdOptions opt = getSalamiOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
        opt.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);
        XmlObject[] api = runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(Inst2XsdCommon.base)}, opt);
        checkLength(api, 1);

        Inst2Xsd.main(new String[]{"-validate", "-verbose",
                                   "-design", "ss",
                                   "-enumerations", "never",
                                   "-simple-content-types", "string",
                                   "-outDir", OPTION_CASES_DIR,
                                   "-outPrefix", "base",
                                   BASEXML});
        XmlObject cmdLine = XmlObject.Factory.parse(new File(EXPBASEXML));
        compare(api[0], cmdLine);
        XmlObject exp = XmlObject.Factory.parse(new File(OPTION_CASES_DIR + "base_ss_scs_eN0.xsd"));
        compare(api[0], exp);
    }

    @Test
    void test_simpleContentString_NeverEnum_Venetian() throws Exception {
        Inst2XsdOptions opt = getVenetianOptions();
        opt.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
        XmlObject[] api = runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(Inst2XsdCommon.base)}, opt);
        checkLength(api, 1);

        Inst2Xsd.main(new String[]{"-validate", "-verbose",
                                   "-design", "vb",
                                   "-enumerations", "never",
                                   "-simple-content-types", "string",
                                   "-outDir", OPTION_CASES_DIR,
                                   "-outPrefix", "base",
                                   BASEXML});
        XmlObject cmdLine = XmlObject.Factory.parse(new File(EXPBASEXML));
        compare(api[0], cmdLine);
        XmlObject exp = XmlObject.Factory.parse(new File(OPTION_CASES_DIR + "base_vb_scs_eN0.xsd"));
        compare(api[0], exp);
    }


    //TODO: move to checkin - cursor issue
    @Test
    void test_simpleContentSmart() throws Exception {
        Inst2XsdOptions opt = getDefaultInstOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_SMART);
        SchemaDocument[] sDoc = getSchemaDoc(runInst2Xsd(XmlObject.Factory.parse(Inst2XsdCommon.base), opt));

        checkLength(sDoc, 1);
        XmlObject exp = XmlObject.Factory.parse(Inst2XsdCommon.base_expected_venetian, getXmlOptions());
        Inst2Xsd.main(new String[]{"-validate", "-verbose",
                                   "-simple-content-types", "smart",
                                   "-outDir", OPTION_CASES_DIR,
                                   "-outPrefix", "base",
                                   BASEXML});
        XmlObject cmdLine = XmlObject.Factory.parse(new File(EXPBASEXML));
        compare(sDoc[0], exp);
        compare(cmdLine, exp);


    }

    //TODO: move to checkin - cursor issue
    @Test
    void test_neverEnum() throws Exception {
        Inst2XsdOptions opt = getVenetianOptions();
        opt.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);
        SchemaDocument[] sDoc = getSchemaDoc(runInst2Xsd(XmlObject.Factory.parse(Inst2XsdCommon.base), opt));
        checkLength(sDoc, 1);
        XmlObject exp = XmlObject.Factory.parse(Inst2XsdCommon.base_expected_venetian, getXmlOptions());
        Inst2Xsd.main(new String[]{"-validate", "-verbose",
                                   "-enumerations", "never",
                                   "-outDir", OPTION_CASES_DIR,
                                   "-outPrefix", "base",
                                   BASEXML});
        XmlObject cmdLine = XmlObject.Factory.parse(new File(EXPBASEXML));
        compare(sDoc[0], exp);
        compare(cmdLine, exp);

    }

    //TODO: move to checkin - cursor issue
    @Test
    void test_simpleContentString() throws Exception {
        Inst2XsdOptions opt = getDefaultInstOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
        SchemaDocument[] sDoc = getSchemaDoc(runInst2Xsd(XmlObject.Factory.parse(Inst2XsdCommon.base), opt));

        checkLength(sDoc, 1);
        String stringContent = "<xs:schema attributeFormDefault =\"unqualified\" elementFormDefault=\"qualified\" " +
                "targetNamespace=\"baseNamespace\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
                "<xs:element name=\"a\" type=\"bas:aType\" xmlns:bas=\"baseNamespace\">" +
                "    <xs:annotation>\n" +
                "      <xs:documentation> Copyright 2004 The Apache Software Foundation\n" +
                "\n" +
                "     Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                "     you may not use this file except in compliance with the License.\n" +
                "     You may obtain a copy of the License at\n" +
                "\n" +
                "         http://www.apache.org/licenses/LICENSE-2.0\n" +
                "\n" +
                "     Unless required by applicable law or agreed to in writing, software\n" +
                "     distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                "     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                "     See the License for the specific language governing permissions and\n" +
                "     limitations under the License. </xs:documentation>\n" +
                "    </xs:annotation>\n" +
                "</xs:element>"+
                "<xs:complexType name=\"aType\">" +
                "<xs:sequence>" +
                "<xs:element type=\"xs:string\" name=\"b\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>" +
                "<xs:element type=\"xs:string\" name=\"c\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>" +
                "<xs:element type=\"xs:string\" name=\"d\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>" +
                "</xs:sequence>" +
                "</xs:complexType>" +
                "</xs:schema>";
        XmlObject exp = XmlObject.Factory.parse(stringContent, getXmlOptions());
        compare(sDoc[0], exp);
        Inst2Xsd.main(new String[]{"-validate", "-verbose",
                                   "-simple-content-types", "string",
                                   "-outDir", OPTION_CASES_DIR,
                                   "-outPrefix", "base",
                                   BASEXML});
        XmlObject cmdLine = XmlObject.Factory.parse(new File(EXPBASEXML));
        compare(sDoc[0], exp);
        compare(cmdLine, exp);

    }

    //TODO: move to checkin - cursor issue
    @Test
    void test_RussianDesign() throws Exception {
        SchemaDocument[] sDoc = getSchemaDoc(runInst2Xsd(XmlObject.Factory.parse(Inst2XsdCommon.base),
                getRussianOptions()));
        checkLength(sDoc, 1);

        XmlObject exp = XmlObject.Factory.parse(Inst2XsdCommon.base_expected_russian, getXmlOptions());
        Inst2Xsd.main(new String[]{"-validate", "-verbose",
                                   "-design", "rd",
                                   "-outDir", OPTION_CASES_DIR,
                                   "-outPrefix", "base",
                                   BASEXML});
        XmlObject cmdLine = XmlObject.Factory.parse(new File(EXPBASEXML));
        compare(sDoc[0], exp);
        compare(cmdLine, exp);

    }

    //TODO: move to checkin - cursor issue
    @Test
    @Disabled
    public void test_SalamiDesign() throws Exception {
        SchemaDocument[] sDoc = getSchemaDoc(runInst2Xsd(XmlObject.Factory.parse(Inst2XsdCommon.base),
                getSalamiOptions()));

        checkLength(sDoc, 1);

        XmlObject exp = XmlObject.Factory.parse(Inst2XsdCommon.base_expected_salami, getXmlOptions());
        Inst2Xsd.main(new String[]{"-validate", "-verbose",
                                   "-design", "ss",
                                   "-outDir", OPTION_CASES_DIR,
                                   "-outPrefix", "base",
                                   BASEXML});
        XmlObject cmdLine = XmlObject.Factory.parse(new File(EXPBASEXML));
        compare(sDoc[0], exp);
        compare(cmdLine, exp);
    }

    //TODO: move to checkin - cursor issue
    @Test
    void test_VenetianDesign() throws Exception {
        SchemaDocument[] sDoc = getSchemaDoc(runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(Inst2XsdCommon.base)},
                getVenetianOptions()));
        checkLength(sDoc, 1);
        XmlObject exp = XmlObject.Factory.parse(Inst2XsdCommon.base_expected_venetian, getXmlOptions());
        Inst2Xsd.main(new String[]{"-validate", "-verbose",
                                   "-design", "vb",
                                   "-outDir", OPTION_CASES_DIR,
                                   "-outPrefix", "base",
                                   BASEXML});
        XmlObject cmdLine = XmlObject.Factory.parse(new File(EXPBASEXML));
        compare(sDoc[0], exp);
        compare(cmdLine, exp);
    }

}

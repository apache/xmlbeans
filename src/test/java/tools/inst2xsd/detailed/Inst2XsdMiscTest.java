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
import org.junit.jupiter.api.Test;

import java.io.FileReader;

import static tools.inst2xsd.common.Inst2XsdCommon.getDefaultInstOptions;
import static tools.inst2xsd.common.Inst2XsdTestBase.*;

public class Inst2XsdMiscTest {

    public static final String EXPBASEXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" targetNamespace=\"baseNamespace\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "  <xs:element name=\"a\" type=\"bas:aType\" xmlns:bas=\"baseNamespace\">\n" +
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
            "  </xs:element>\n" +
            "  <xs:complexType name=\"aType\">\n" +
            "    <xs:sequence>\n" +
            "      <xs:element type=\"xs:string\" name=\"b\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>\n" +
            "      <xs:element type=\"xs:byte\" name=\"c\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>\n" +
            "      <xs:element type=\"xs:string\" name=\"d\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>\n" +
            "    </xs:sequence>\n" +
            "  </xs:complexType>\n" +
            "</xs:schema>";

    @Test
    void test_usingReader() throws Exception {
        FileReader[] fReader = new FileReader[]{new FileReader(BASEXML)};
        SchemaDocument[] doc = Inst2Xsd.inst2xsd( fReader, getDefaultInstOptions());
        checkLength(doc, 1);
        compare(doc[0],XmlObject.Factory.parse(EXPBASEXML) );
    }

    @Test
    void test_nulloptions() throws Exception {
        FileReader[] fReader = new FileReader[]{new FileReader(BASEXML)};
        SchemaDocument[] doc = Inst2Xsd.inst2xsd(fReader, null);
        checkLength(doc, 1);
        compare(doc[0], XmlObject.Factory.parse(EXPBASEXML));
    }

    @Test
    void test_notverboseoptions() throws Exception {
        FileReader[] fReader = new FileReader[]{new FileReader(BASEXML)};
        Inst2XsdOptions opts = getDefaultInstOptions();
        opts.setVerbose(false);
        SchemaDocument[] doc = Inst2Xsd.inst2xsd(fReader, opts);
        checkLength(doc, 1);
        compare(doc[0], XmlObject.Factory.parse(EXPBASEXML));
    }

    /*
    TODO: Redirect output these tests cause problems
    with the xml log
    */
    /*public void test_main_license() throws Exception {
        Inst2Xsd.main(new String[]{"-license"});
    }
    public void test_main_noarg_enum() throws Exception {
        Inst2Xsd.main(new String[]{"-enumerations"});
    }
    public void test_main_noarg_scs() throws Exception {
        Inst2Xsd.main(new String[]{"-simple-content-types"});
    }
    public void test_main_noarg_design() throws Exception {
        Inst2Xsd.main(new String[]{"-design"});
    }
    public void test_main_noarg_outDir() throws Exception {
        Inst2Xsd.main(new String[]{"-outDir"});
    }
    public void test_main_noarg_outPrefix() throws Exception {
        Inst2Xsd.main(new String[]{"-outPrefix"});
    }
    public void test_main_noarg_validate() throws Exception {
        Inst2Xsd.main(new String[]{"-validate"});
    }
    public void test_main_noarg_verbose() throws Exception {
        Inst2Xsd.main(new String[]{"-verbose"});
    }
    public void test_main_noarg_help() throws Exception {
        Inst2Xsd.main(new String[]{"-help"});
    }
    public void test_no_param() throws Exception {
        Inst2Xsd.main(null);
    }*/






    /*
    public void test_simpleContentString_Salami() throws Exception {
        Inst2XsdOptions opt = getSalamiOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
        XmlObject[] api = runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(common.base)}, opt);
        checkLength(api, 1);
        log(api);

        Inst2Xsd.main(new String[]{"-validate", "-verbose",
                                   "-design", "ss",
                                   "-simple-content-types", "string",
                                   "-outDir", OPTION_CASES_DIR,
                                   "-outPrefix", "base",
                                   BASEXML});
        XmlObject cmdLine = XmlObject.Factory.parse(new File(EXPBASEXML));
        log("Compare: Command Line to API");
        compare(api[0], cmdLine);
        XmlObject exp = XmlObject.Factory.parse(new File(OPTION_CASES_DIR + "base_ss_scs0.xsd"));
        log("Compare: Expected to API");
        compare(api[0], exp);
    }*/

}

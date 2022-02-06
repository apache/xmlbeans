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
package tools.inst2xsd.common;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.inst2xsd.Inst2Xsd;
import org.apache.xmlbeans.impl.inst2xsd.Inst2XsdOptions;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import tools.xml.XmlComparator;

import java.util.ArrayList;
import java.util.Collection;

import static common.Common.P;
import static common.Common.XBEAN_CASE_ROOT;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public final class Inst2XsdTestBase {
    private static final String caseroot = XBEAN_CASE_ROOT;
    //location of files under "cases folder"
    private static final String miscDir = caseroot + P + "tools";
    private static final String inst2xsdDir = miscDir + P + "inst2xsd" + P;
    public static final String OPTION_CASES_DIR = inst2xsdDir + P + "options" + P;
    public static String SCHEMA_CASES_DIR = inst2xsdDir + P + "schema" + P;
    public static final String BASEXML = OPTION_CASES_DIR + "base.xml";
    public static final String EXPBASEXML = OPTION_CASES_DIR + "base0.xsd";


    public static XmlObject getTypeXml(String val) throws Exception {
        return XmlObject.Factory.parse(setTypeVal(val));
    }

    private static String setTypeVal(String val) {
        String base_start = "<a xmlns=\"typeTests\">";
        String base_end = "</a>";
        return base_start + val + base_end;
    }

    public static XmlObject getAttrTypeXml(String val) throws Exception {
        return XmlObject.Factory.parse(setAttrVal(val));
    }

    private static String setAttrVal(String val) {
        String attr_base_end = "\" />";
        String attr_base_start = "<a xmlns=\"attrTests\" a=\"";
        return attr_base_start + val + attr_base_end;
    }

    //attribute testing methods

    private static String getAttrTypeXmlVenetian(String type) {
        return "<schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
               "targetNamespace=\"attrTests\" xmlns=\"http://www.w3.org/2001/XMLSchema\">" +
               "<element name=\"a\" type=\"att:aType\" xmlns:att=\"attrTests\"/>" +
               "<complexType name=\"aType\">" +
               "<simpleContent>" +
               "<extension base=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
               "<attribute type=\"xs:" + type + "\" name=\"a\"/>" +
               "</extension>" +
               "</simpleContent></complexType></schema>";
    }

    private static String getAttrTypeXmlRDandSS(String type) {
        return "<schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
               "targetNamespace=\"attrTests\" xmlns=\"http://www.w3.org/2001/XMLSchema\">" +
               "<element name=\"a\">" +
               "<complexType>" +
               "<simpleContent>" +
               "<extension base=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
               "<attribute type=\"xs:" + type + "\" name=\"a\"/>" +
               "</extension>" +
               "</simpleContent>" +
               "</complexType></element></schema>";
    }

    public static void runAttrTypeChecking(XmlObject act, String expType) throws Exception {
        runAttrTypeChecking(act, expType, Inst2XsdCommon.getVenetianOptions());
        runAttrTypeChecking(act, expType, Inst2XsdCommon.getRussianOptions());
        runAttrTypeChecking(act, expType, Inst2XsdCommon.getSalamiOptions());
        runAttrTypeChecking(act, expType, Inst2XsdCommon.getDefaultInstOptions());
    }

    private static void runAttrTypeChecking(XmlObject act, String expType, Inst2XsdOptions opt) throws Exception {
        SchemaDocument[] venetian = (SchemaDocument[]) runInst2Xsd(act, opt);
        checkLength(venetian, 1);

        if (opt.getDesign() == Inst2XsdOptions.DESIGN_RUSSIAN_DOLL ||
            opt.getDesign() == Inst2XsdOptions.DESIGN_SALAMI_SLICE) {
            compare(venetian[0], XmlObject.Factory.parse(getAttrTypeXmlRDandSS(expType)));
        } else if (opt.getDesign() == Inst2XsdOptions.DESIGN_VENETIAN_BLIND) {
            compare(venetian[0], XmlObject.Factory.parse(getAttrTypeXmlVenetian(expType)));
        } else {
            throw new Exception("Design style was not found");
        }

        checkInstance(venetian, new XmlObject[]{act});

    }

    //element value test methods
    public static void runTypeChecking(XmlObject act, String expType) throws Exception {
        Inst2XsdOptions[] opts = {
            Inst2XsdCommon.getVenetianOptions(),
            Inst2XsdCommon.getRussianOptions(),
            Inst2XsdCommon.getSalamiOptions(),
            Inst2XsdCommon.getDefaultInstOptions()
        };

        for (Inst2XsdOptions opt : opts) {
            SchemaDocument[] doc = (SchemaDocument[]) runInst2Xsd(act, opt);
            checkLength(doc, 1);
            checkInstance(doc, new XmlObject[]{act});
            compare(doc[0], XmlObject.Factory.parse(getExpTypeXml(expType)));
        }
    }

    private static String getExpTypeXml(String type) {
        return "<xs:schema attributeFormDefault=\"unqualified\" " +
               "elementFormDefault=\"qualified\" targetNamespace=\"typeTests\"" +
               " xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >" +
               "<xs:element name=\"a\" type=\"xs:" + type + "\"" +
               " xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" />" +
               "</xs:schema>";
    }

    //type coercion/LCD test methods
    public static void runLCDTypeCheckTest(String val1, String val2, String expType) throws Exception {
        runLCDTypeChecking(val1, val2, expType, Inst2XsdCommon.getVenetianOptions());
        runLCDTypeChecking(val1, val2, expType, Inst2XsdCommon.getRussianOptions());
        runLCDTypeChecking(val1, val2, expType, Inst2XsdCommon.getSalamiOptions());
        runLCDTypeChecking(val1, val2, expType, Inst2XsdCommon.getDefaultInstOptions());
    }

    private static void runLCDTypeChecking(String val1, String val2, String expType, Inst2XsdOptions opt) throws Exception {
        XmlObject act = getTypeCoerceXml(val1, val2);
        SchemaDocument[] venetian = (SchemaDocument[]) runInst2Xsd(act, opt);
        checkLength(venetian, 1);
        checkInstance(venetian, new XmlObject[]{act});

        if (opt.getDesign() == Inst2XsdOptions.DESIGN_VENETIAN_BLIND) {
            compare(venetian[0], getExpLCDXml_vb(expType));
        } else if (opt.getDesign() == Inst2XsdOptions.DESIGN_SALAMI_SLICE) {
            compare(venetian[0], getExpLCDXml_ss(expType));
        } else if (opt.getDesign() == Inst2XsdOptions.DESIGN_RUSSIAN_DOLL) {
            compare(venetian[0], getExpLCDXml_rd(expType));
        } else {
            compare(venetian[0], getExpLCDXml_vb(expType));
        }
    }

    private static String getTypeCoerceXmlString(String val1, String val2) {
        return "<a xmlns=\"typeCoercion\">" +
               "    <b c=\"" + val1 + "\">" + val1 + "</b>" +
               "    <b c=\"" + val2 + "\">" + val2 + "</b>" +
               "</a>";
    }

    private static XmlObject getTypeCoerceXml(String val1, String val2) throws XmlException {
        return XmlObject.Factory.parse(getTypeCoerceXmlString(val1, val2));
    }


    private static XmlObject getExpLCDXml_vb(String type) throws XmlException {
        return XmlObject.Factory.parse(
            "<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" targetNamespace=\"typeCoercion\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "  <xs:element name=\"a\" type=\"typ:aType\" xmlns:typ=\"typeCoercion\"/>\n" +
            "  <xs:complexType name=\"aType\">\n" +
            "    <xs:sequence>\n" +
            "      <xs:element type=\"typ:bType\" name=\"b\" maxOccurs=\"unbounded\" minOccurs=\"0\" xmlns:typ=\"typeCoercion\"/>\n" +
            "    </xs:sequence>\n" +
            "  </xs:complexType>\n" +
            "  <xs:complexType name=\"bType\">\n" +
            "    <xs:simpleContent>\n" +
            "      <xs:extension base=\"xs:" + type + "\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "        <xs:attribute type=\"xs:" + type + "\" name=\"c\" use=\"optional\"/>\n" +
            "      </xs:extension>\n" +
            "    </xs:simpleContent>\n" +
            "  </xs:complexType>\n" +
            "</xs:schema>");
    }

    private static XmlObject getExpLCDXml_ss(String type) throws XmlException {
        return XmlObject.Factory.parse(
            "<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" targetNamespace=\"typeCoercion\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "  <xs:element name=\"b\">\n" +
            "    <xs:complexType>\n" +
            "      <xs:simpleContent>\n" +
            "        <xs:extension base=\"xs:" + type + "\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "          <xs:attribute type=\"xs:" + type + "\" name=\"c\" use=\"optional\"/>\n" +
            "        </xs:extension>\n" +
            "      </xs:simpleContent>\n" +
            "    </xs:complexType>\n" +
            "  </xs:element>\n" +
            "  <xs:element name=\"a\">\n" +
            "    <xs:complexType>\n" +
            "      <xs:sequence>\n" +
            "        <xs:element ref=\"typ:b\" maxOccurs=\"unbounded\" minOccurs=\"0\" xmlns:typ=\"typeCoercion\"/>\n" +
            "      </xs:sequence>\n" +
            "    </xs:complexType>\n" +
            "  </xs:element>\n" +
            "</xs:schema>");
    }

    private static XmlObject getExpLCDXml_rd(String type) throws XmlException {
        return XmlObject.Factory.parse(
            "<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" targetNamespace=\"typeCoercion\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "  <xs:element name=\"a\">\n" +
            "    <xs:complexType>\n" +
            "      <xs:sequence>\n" +
            "        <xs:element name=\"b\" maxOccurs=\"unbounded\" minOccurs=\"0\">\n" +
            "          <xs:complexType>\n" +
            "            <xs:simpleContent>\n" +
            "              <xs:extension base=\"xs:" + type + "\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "                <xs:attribute type=\"xs:" + type + "\" name=\"c\" use=\"optional\"/>\n" +
            "              </xs:extension>\n" +
            "            </xs:simpleContent>\n" +
            "          </xs:complexType>\n" +
            "        </xs:element>\n" +
            "      </xs:sequence>\n" +
            "    </xs:complexType>\n" +
            "  </xs:element>\n" +
            "</xs:schema>");
    }


    public static XmlObject[] runInst2Xsd(XmlObject inst, Inst2XsdOptions options) {
        return Inst2Xsd.inst2xsd(new XmlObject[]{inst}, options);
    }

    public static XmlObject[] runInst2Xsd(XmlObject[] inst, Inst2XsdOptions options) {
        return Inst2Xsd.inst2xsd(inst, options);
    }


    public static SchemaDocument[] getSchemaDoc(XmlObject[] inst) throws XmlException {
        SchemaDocument[] docs = new SchemaDocument[inst.length];
        for (int i = 0; i < docs.length; i++) {
            docs[i] = SchemaDocument.Factory.parse(inst[i].xmlText());
        }
        return docs;

    }

    public static void runSchemaBuild(XmlObject inst, Inst2XsdOptions opts, XmlObject exp) throws Exception {
        XmlObject[] genSchema = runInst2Xsd(inst, opts);
        checkInstanceToAll(genSchema, inst, exp);
        checkLength(genSchema, 1);
        compare(genSchema[0], exp);
    }

    public static void runSchemaBuild(XmlObject inst, Inst2XsdOptions opts, XmlObject[] exp) throws XmlException {
        XmlObject[] genSchema = runInst2Xsd(inst, opts);
        checkInstanceToAll(genSchema, new XmlObject[]{inst}, exp);
        checkLength(genSchema, exp.length);
        compare(genSchema, exp);
    }

    //TODO: Make this error narrowed
    public static void checkLength(Object[] obj, int val) throws XmlException {
        if (obj.length < val) {
            throw new XmlException("Actual was smaller than expected");
        } else if (obj.length > val) {
            throw new XmlException("Actual was larger than expected");
        }
    }


    public static void compare(XmlObject[] act, XmlObject[] exp) throws XmlException {
        checkLength(act, exp.length);
        //Arrays.sort(act);
        //Arrays.sort(exp);
        //if (Arrays.equals(act, exp)){
        //    return;
        //}else{
        for (int i = 0; i < act.length; i++) {
            compare(act[i], exp[i]);
        }

    }

    public static void compare(XmlObject act, XmlObject exp) throws XmlException {
        XmlComparator.Diagnostic diag = XmlComparator.lenientlyCompareTwoXmlStrings(
            act.xmlText(Inst2XsdCommon.getXmlOptions()),
            exp.xmlText(Inst2XsdCommon.getXmlOptions()));
        if (diag.hasMessage()) {
            throw new XmlException("Xml Comparison Failed:\n" + diag.toString());
        }
    }

    private static void checkInstanceToAll(XmlObject[] actSchemaDoc, XmlObject inst,
                                           XmlObject expSchemas) throws Exception {
        checkInstanceToAll(getSchemaDoc(actSchemaDoc), new XmlObject[]{inst}, getSchemaDoc(new XmlObject[]{expSchemas}));
    }

    private static void checkInstanceToAll(XmlObject[] actSchemaDoc, XmlObject[] inst,
                                           XmlObject[] expSchemas) throws XmlException {
        checkInstanceToAll(getSchemaDoc(actSchemaDoc), inst, getSchemaDoc(expSchemas));
    }

    private static void checkInstanceToAll(SchemaDocument[] actSchemaDoc, XmlObject[] inst,
                                           SchemaDocument[] expSchemas) throws XmlException {
        checkInstance(actSchemaDoc, inst);
        checkInstance(expSchemas, inst);
    }

    private static boolean checkInstance(SchemaDocument[] sDocs, XmlObject[] inst) throws XmlException {
        if (validateInstances(sDocs, inst)) {
            return true;
        } else {
            throw new XmlException("Instance Failed to validate");
        }
    }

    /**
     * Copied from inst2Xsd as option may be removed
     */
    private static boolean validateInstances(SchemaDocument[] sDocs, XmlObject[] instances) {

        SchemaTypeLoader sLoader;
        Collection<XmlError> compErrors = new ArrayList<>();
        XmlOptions schemaOptions = new XmlOptions();
        schemaOptions.setErrorListener(compErrors);
        try {
            sLoader = XmlBeans.loadXsd(sDocs, schemaOptions);
        } catch (Exception e) {
            if (compErrors.isEmpty() || !(e instanceof XmlException)) {
                e.printStackTrace(System.out);
            }
            System.out.println("Schema invalid");
            for (Object compError : compErrors) {
                System.out.println(compError);
            }
            return false;
        }

        boolean result = true;

        for (int i = 0; i < instances.length; i++) {
            String instance = instances[i].toString();

            XmlObject xobj;

            try {
                assertNotNull(sLoader);
                xobj = sLoader.parse(instance, null, new XmlOptions().setLoadLineNumbers());
            } catch (XmlException e) {
                System.out.println("Error:\n" + instance + " not loadable: " + e);
                e.printStackTrace(System.out);
                result = false;
                continue;
            }

            Collection<XmlError> errors = new ArrayList<>();

            if (xobj.schemaType() == XmlObject.type) {
                System.out.println(instance + " NOT valid.  ");
                System.out.println("  Document type not found.");
                result = false;
            } else if (xobj.validate(new XmlOptions().setErrorListener(errors))) {
                System.out.println("Instance[" + i + "] valid.");
            } else {
                System.out.println("Instance[" + i + "] NOT valid.");
                for (Object error : errors) {
                    System.out.println("    " + error);
                }
                result = false;
            }
        }

        return result;
    }

}

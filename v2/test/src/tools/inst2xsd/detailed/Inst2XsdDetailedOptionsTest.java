package tools.inst2xsd.detailed;

import tools.inst2xsd.common.Inst2XsdTestBase;
import org.apache.xmlbeans.impl.inst2xsd.Inst2XsdOptions;
import org.apache.xmlbeans.impl.inst2xsd.Inst2Xsd;


import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlObject;
import org.w3.x2001.xmlSchema.SchemaDocument;

/**
 * @author jacobd
 *         Date: Aug 9, 2004
 */
public class Inst2XsdDetailedOptionsTest extends Inst2XsdTestBase {

    public Inst2XsdDetailedOptionsTest(String name) {
        super(name);
    }

    public void test_simpleContentString_Russian() throws Exception {
        Inst2XsdOptions opt = common.getRussianOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
        runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(common.base)}, opt);
    }

    public void test_simpleContentString_Salami() throws Exception {
        Inst2XsdOptions opt = common.getSalamiOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
        runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(common.base)}, opt);
    }

    public void test_simpleContentString_Venetian() throws Exception {
        Inst2XsdOptions opt = common.getVenetianOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
        runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(common.base)}, opt);
    }


    public void test_simpleContentSmart_Russian() throws Exception {
        Inst2XsdOptions opt = common.getRussianOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_SMART);
        this.runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(common.base)}, opt);
    }

    public void test_simpleContentSmart_Salami() throws Exception {
        Inst2XsdOptions opt = common.getSalamiOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_SMART);
        runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(common.base)}, opt);
    }

    public void test_simpleContentSmart_Venetian() throws Exception {
        Inst2XsdOptions opt = common.getVenetianOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_SMART);
        runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(common.base)}, opt);
    }


    public void test_simpleContentSmart_NeverEnum_Russian() throws Exception {
        Inst2XsdOptions opt = common.getRussianOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_SMART);
        opt.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);
        runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(common.base)}, opt);
    }

    public void test_simpleContentSmart_NeverEnum_Salami() throws Exception {
        Inst2XsdOptions opt = common.getSalamiOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_SMART);
        opt.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);
        runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(common.base)}, opt);
    }

    public void test_simpleContentSmart_NeverEnum_Venetian() throws Exception {
        Inst2XsdOptions opt = common.getVenetianOptions();
        opt.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_SMART);
        runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(common.base)}, opt);
    }

    public void test_simpleContentString_NeverEnum_Russian() throws Exception {
        Inst2XsdOptions opt = common.getRussianOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
        opt.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);
        runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(common.base)}, opt);
    }

    public void test_simpleContentString_NeverEnum_Salami() throws Exception {
        Inst2XsdOptions opt = common.getSalamiOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
        opt.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);
        runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(common.base)}, opt);
    }

    public void test_simpleContentString_NeverEnum_Venetian() throws Exception {
        Inst2XsdOptions opt = common.getVenetianOptions();
        opt.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
        runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(common.base)}, opt);
    }


    //TODO: move to checkin - cursor issue
    public void test_simpleContentSmart() throws Exception {
        Inst2XsdOptions opt = common.getDefaultInstOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_SMART);
        SchemaDocument[] sDoc = getSchemaDoc(runInst2Xsd(XmlObject.Factory.parse(common.base), opt));

        checkLength(sDoc, 1);
        XmlObject exp = XmlObject.Factory.parse(common.base_expected_venetian, common.getXmlOptions());
        compare(sDoc[0], exp);
    }

    //TODO: move to checkin - cursor issue
    public void test_neverEnum() throws Exception {
        Inst2XsdOptions opt = common.getVenetianOptions();
        opt.setUseEnumerations(Inst2XsdOptions.ENUMERATION_NEVER);
        SchemaDocument[] sDoc = getSchemaDoc(runInst2Xsd(XmlObject.Factory.parse(common.base), opt));
        checkLength(sDoc, 1);
        XmlObject exp = XmlObject.Factory.parse(common.base_expected_venetian, common.getXmlOptions());
        compare(sDoc[0], exp);
    }

    //TODO: move to checkin - cursor issue
    public void test_simpleContentString() throws Exception {
        Inst2XsdOptions opt = common.getDefaultInstOptions();
        opt.setSimpleContentTypes(Inst2XsdOptions.SIMPLE_CONTENT_TYPES_STRING);
        SchemaDocument[] sDoc = getSchemaDoc(runInst2Xsd(XmlObject.Factory.parse(common.base), opt));

        checkLength(sDoc, 1);
        String stringContent = "<xs:schema attributeFormDefault =\"unqualified\" elementFormDefault=\"qualified\" " +
                "targetNamespace=\"baseNamespace\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
                "<xs:element name=\"a\" type=\"bas:aType\" xmlns:bas=\"baseNamespace\"/>" +
                "<xs:complexType name=\"aType\" mixed=\"true\">" +
                "<xs:sequence>" +
                "<xs:element type=\"xs:string\" name=\"b\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>" +
                "<xs:element type=\"xs:string\" name=\"c\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>" +
                "<xs:element type=\"xs:string\" name=\"d\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>" +
                "</xs:sequence>" +
                "</xs:complexType>" +
                "</xs:schema>";
        XmlObject exp = XmlObject.Factory.parse(stringContent, common.getXmlOptions());
        compare(sDoc[0], exp);
    }

    //TODO: move to checkin - cursor issue
    public void test_RussianDesign() throws Exception {
        SchemaDocument[] sDoc = getSchemaDoc(runInst2Xsd(XmlObject.Factory.parse(common.base),
                common.getRussianOptions()));
        checkLength(sDoc, 1);

        XmlObject exp = XmlObject.Factory.parse(common.base_expected_russian, common.getXmlOptions());
        compare(sDoc[0], exp);
    }

    //TODO: move to checkin - cursor issue
    public void test_SalamiDesign() throws Exception {
        SchemaDocument[] sDoc = getSchemaDoc(runInst2Xsd(XmlObject.Factory.parse(common.base),
                common.getSalamiOptions()));

        checkLength(sDoc, 1);

        XmlObject exp = XmlObject.Factory.parse(common.base_expected_salami, common.getXmlOptions());

        compare(sDoc[0], exp);
    }

    //TODO: move to checkin - cursor issue
    public void test_VenetianDesign() throws Exception {
        SchemaDocument[] sDoc = getSchemaDoc(runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(common.base)},
                common.getVenetianOptions()));
        checkLength(sDoc, 1);
        XmlObject exp = XmlObject.Factory.parse(common.base_expected_venetian, common.getXmlOptions());
        compare(sDoc[0], exp);
    }
}

package tools.inst2xsd.common;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.inst2xsd.Inst2Xsd;
import org.apache.xmlbeans.impl.inst2xsd.Inst2XsdOptions;
import org.w3.x2001.xmlSchema.SchemaDocument;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;
import tools.xml.XmlComparator;

/**
 * @author jacobd
 *         Date: Aug 9, 2004
 */
public class Inst2XsdTestBase extends TestCase {

    public static final String P = File.separator;
    public static tools.inst2xsd.common.Inst2XsdCommon common;
    public static boolean _verbose = true;

    public static final String fwroot = System.getProperty("xbean.rootdir");
    public static String caseroot = fwroot + P + "test" + P + "cases" + P + "xbean";
    //location of files under "cases folder"
    public static String miscDir = caseroot + P + "tools";
    public static String inst2xsdDir = miscDir + P + "inst2xsd" + P;
    public static String OPTION_CASES_DIR = inst2xsdDir + P + "options" + P;
    public static String SCHEMA_CASES_DIR = inst2xsdDir + P + "schema" + P;
    public static String VALIDATION_CASES_DIR = inst2xsdDir + P + "validation" + P;

    private static String base_start = "<a xmlns=\"typeTests\">";
    private static String base_end = "</a>";

    private static String attr_base_start = "<a xmlns=\"attrTests\" a=\"";
    private static String attr_base_end = "\" />";


    public Inst2XsdTestBase(String name) {
        super(name);
    }

    public static final String test_getRootFilePath() throws IllegalStateException {
        String root = System.getProperty("xbean.rootdir");
        log("xbean.rootdir: "+root);
        if (root == null)
            throw new IllegalStateException("xbean.rootdir system property not found");

        return root;
    }


    public XmlObject getTypeXml(String val) throws Exception {
        return XmlObject.Factory.parse(setTypeVal(val));
    }

    public String setTypeVal(String val) {
        return base_start + val + base_end;
    }

    public XmlObject getAttrTypeXml(String val) throws Exception {
        return XmlObject.Factory.parse(setAttrVal(val));
    }

    public String setAttrVal(String val) {
        return attr_base_start + val + attr_base_end;
    }

    public String getExpTypeXml(String type) {
        return "<xs:schema attributeFormDefault=\"unqualified\" " +
                "elementFormDefault=\"qualified\" targetNamespace=\"typeTests\"" +
                " xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >" +
                "<xs:element name=\"a\" type=\"xs:" + type + "\"" +
                " xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" />" +
                "</xs:schema>";
    }



    public String getAttrTypeXmlVenetian(String primType, String derType) {
        return "<schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
                "targetNamespace=\"attrTests\" xmlns=\"http://www.w3.org/2001/XMLSchema\">" +
                "<element name=\"a\" type=\"att:aType\" xmlns:att=\"attrTests\"/>" +
                "<complexType name=\"aType\">" +
                "<simpleContent>" +
                "<extension base=\"xs:" + primType + "\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
                "<attribute type=\"xs:" + derType + "\" name=\"a\"/>" +
                "</extension>" +
                "</simpleContent></complexType></schema>";
    }

    public String getAttrTypeXmlVenetian(String type) {
        return "<schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
                "targetNamespace=\"attrTests\" xmlns=\"http://www.w3.org/2001/XMLSchema\">" +
                "<element name=\"a\" type=\"att:aType\" xmlns:att=\"attrTests\"/>" +
                "<complexType name=\"aType\">" +
                "<simpleContent>" +
                "<extension base=\"xs:" + type + "\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
                "<attribute type=\"xs:" + type + "\" name=\"a\"/>" +
                "</extension>" +
                "</simpleContent></complexType></schema>";
    }

    public String getAttrTypeXmlRDandSS(String primType, String derType ) {
        return "<schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
                "targetNamespace=\"attrTests\" xmlns=\"http://www.w3.org/2001/XMLSchema\">" +
                "<element name=\"a\">" +
                "<complexType>" +
                "<simpleContent>" +
                "<extension base=\"xs:" + primType + "\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
                "<attribute type=\"xs:" + derType + "\" name=\"a\"/>" +
                "</extension>" +
                "</simpleContent>" +
                "</complexType></element></schema>";
    }
    public String getAttrTypeXmlRDandSS(String type) {
        return "<schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
                "targetNamespace=\"attrTests\" xmlns=\"http://www.w3.org/2001/XMLSchema\">" +
                "<element name=\"a\">" +
                "<complexType>" +
                "<simpleContent>" +
                "<extension base=\"xs:" + type + "\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
                "<attribute type=\"xs:" + type + "\" name=\"a\"/>" +
                "</extension>" +
                "</simpleContent>" +
                "</complexType></element></schema>";
    }

    public void runAttrTypeChecking(XmlObject act, String expType) throws Exception {

        log("=== Venetian options ===");
        runAttrTypeChecking(act, expType, common.getVenetianOptions());
        log("=== Russian options ===");
        runAttrTypeChecking(act, expType, common.getRussianOptions());
        log("=== Salami options ===");
        runAttrTypeChecking(act, expType, common.getSalamiOptions());
        log("=== Default options ===");
        runAttrTypeChecking(act, expType, common.getDefaultInstOptions());
    }

    public void runAttrTypeChecking(XmlObject act, String primType, String derType) throws Exception {

        log("=== Venetian options ===");
        runAttrTypeChecking(act, primType, derType, common.getVenetianOptions());
        log("=== Russian options ===");
        runAttrTypeChecking(act, primType, derType, common.getRussianOptions());
        log("=== Salami options ===");
        runAttrTypeChecking(act, primType, derType, common.getSalamiOptions());
        log("=== Default options ===");
        runAttrTypeChecking(act, primType, derType, common.getDefaultInstOptions());
    }

    private void runAttrTypeChecking(XmlObject act, String primType, String derType, Inst2XsdOptions opt) throws Exception {
        SchemaDocument[] venetian = (SchemaDocument[]) runInst2Xsd(act, opt);
        checkLength(venetian, 1);

        if (opt.getDesign() == Inst2XsdOptions.DESIGN_RUSSIAN_DOLL ||
                opt.getDesign() == Inst2XsdOptions.DESIGN_SALAMI_SLICE)
            compare(venetian[0], XmlObject.Factory.parse(getAttrTypeXmlRDandSS(primType,derType)));
        else if (opt.getDesign() == Inst2XsdOptions.DESIGN_VENETIAN_BLIND)
            compare(venetian[0], XmlObject.Factory.parse(getAttrTypeXmlVenetian(primType, derType)));
        else
            throw new Exception("Design style was not found");

        checkInstance(venetian, new XmlObject[]{act});

    }

    private void runAttrTypeChecking(XmlObject act, String expType, Inst2XsdOptions opt) throws Exception {
        SchemaDocument[] venetian = (SchemaDocument[]) runInst2Xsd(act, opt);
        checkLength(venetian, 1);

        if (opt.getDesign() == Inst2XsdOptions.DESIGN_RUSSIAN_DOLL ||
                opt.getDesign() == Inst2XsdOptions.DESIGN_SALAMI_SLICE)
            compare(venetian[0], XmlObject.Factory.parse(getAttrTypeXmlRDandSS(expType)));
        else if (opt.getDesign() == Inst2XsdOptions.DESIGN_VENETIAN_BLIND)
            compare(venetian[0], XmlObject.Factory.parse(getAttrTypeXmlVenetian(expType)));
        else
            throw new Exception("Design style was not found");

        checkInstance(venetian, new XmlObject[]{act});

    }

    public void runTypeChecking(XmlObject act, String expType) throws Exception {
        log("=== Venetian options ===");
        runTypeChecking(act, expType, common.getVenetianOptions());
        log("=== Russian options ===");
        runTypeChecking(act, expType, common.getRussianOptions());
        log("=== Salami options ===");
        runTypeChecking(act, expType, common.getSalamiOptions());
        log("=== Default options ===");
        runTypeChecking(act, expType, common.getDefaultInstOptions());
    }


    private void runTypeChecking(XmlObject act, String expType, Inst2XsdOptions opt) throws Exception {
        SchemaDocument[] venetian = (SchemaDocument[]) runInst2Xsd(act, opt);
        checkLength(venetian, 1);
        log("actual: " + act);
        log("expType: " + expType);
        checkInstance(venetian, new XmlObject[]{act});
        compare(venetian[0], XmlObject.Factory.parse(getExpTypeXml(expType)));
    }

    public static XmlObject[] runInst2Xsd(String inst) throws XmlException {
        return runInst2Xsd(new XmlObject[]{XmlObject.Factory.parse(inst, common.getXmlOptions())},
                common.getDefaultInstOptions());

    }

    public static XmlObject[] runInst2Xsd(XmlObject inst) {
        return Inst2Xsd.inst2xsd(new XmlObject[]{inst}, common.getDefaultInstOptions());
    }

    public static XmlObject[] runInst2Xsd(XmlObject[] inst) {
        return Inst2Xsd.inst2xsd(inst, common.getDefaultInstOptions());
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
        log(genSchema);
        checkInstanceToAll(genSchema, inst, exp);
        checkLength(genSchema, 1);
        compare(genSchema[0], exp);

    }

    public static void runSchemaBuild(XmlObject inst, Inst2XsdOptions opts, XmlObject[] exp) throws Exception {
        XmlObject[] genSchema = runInst2Xsd(inst, opts);
        log(genSchema);
        checkInstanceToAll(genSchema, new XmlObject[]{inst}, exp);
        checkLength(genSchema, exp.length);
        compare(genSchema, exp);

    }

    public static void runSchemaBuild(XmlObject[] inst, Inst2XsdOptions opts, XmlObject[] exp) throws Exception {
        XmlObject[] genSchema = runInst2Xsd(inst, opts);
        checkInstanceToAll(genSchema, inst, exp);
        log(genSchema);
        compare(genSchema, exp);

    }

    //TODO: Make this error narrowed
    public static void checkLength(Object[] obj, int val) throws Exception {
        log("Length = " + obj.length + " exp: " + val);

        if (obj.length == val) {
            return;
        } else if (obj.length < val) {
            throw new Exception("Actual was smaller than expected");
        } else if (obj.length > val) {
            throw new Exception("Actual was larger than expected");
        } else {
            throw new Exception("Array Indexes did not compare correctly");
        }


    }


    public static void compare(XmlObject[] act, XmlObject[] exp) throws XmlException, Exception {
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

    public static void compare(XmlObject act, XmlObject exp)
            throws XmlException, Exception {
        XmlComparator.Diagnostic diag = XmlComparator.lenientlyCompareTwoXmlStrings(act.xmlText(common.getXmlOptions()),
                exp.xmlText(common.getXmlOptions()));
        if (diag.hasMessage()) {
            log("Expected: \n" + exp.xmlText(common.getXmlOptions()));
            log("Actual: \n" + act.xmlText(common.getXmlOptions()));
            throw new Exception("Xml Comparison Failed:\n" + diag.toString());
        }
    }

    public static void log(XmlObject[] doc) {
        if (_verbose) {
            for (int i = 0; i < doc.length; i++) {
                log("Schema[" + i + "] - " + doc[i].xmlText(common.getXmlOptions()));
            }
        }
    }

    public static void log(String msg) {
        if (_verbose)
            System.out.println(msg);
    }

    public static void log(XmlObject obj) {
        if (_verbose)
            System.out.println(obj.xmlText(common.getXmlOptions()));
    }


    public static boolean checkInstanceToAll(XmlObject[] actSchemaDoc, XmlObject inst,
                                             XmlObject expSchemas) throws Exception {
        return checkInstanceToAll(getSchemaDoc(actSchemaDoc), new XmlObject[]{inst}, getSchemaDoc(new XmlObject[]{expSchemas}));
    }

    public static boolean checkInstanceToAll(XmlObject[] actSchemaDoc, XmlObject[] inst,
                                             XmlObject[] expSchemas) throws Exception {
        return checkInstanceToAll(getSchemaDoc(actSchemaDoc), inst, getSchemaDoc(expSchemas));
    }

    public static boolean checkInstanceToAll(XmlObject[] actSchemaDoc, XmlObject[] inst,
                                             XmlObject expSchemas) throws Exception {
        return checkInstanceToAll(getSchemaDoc(actSchemaDoc), inst, getSchemaDoc(new XmlObject[]{expSchemas}));
    }

    public static boolean checkInstanceToAll(SchemaDocument[] actSchemaDoc, XmlObject inst,
                                             SchemaDocument expSchemas) throws Exception {
        return checkInstanceToAll(actSchemaDoc, new XmlObject[]{inst}, getSchemaDoc(new XmlObject[]{expSchemas}));
    }

    public static boolean checkInstanceToAll(SchemaDocument[] actSchemaDoc, XmlObject inst,
                                             SchemaDocument[] expSchemas) throws Exception {
        return checkInstanceToAll(actSchemaDoc, new XmlObject[]{inst}, expSchemas);
    }

    public static boolean checkInstanceToAll(SchemaDocument[] actSchemaDoc, XmlObject[] inst,
                                             SchemaDocument[] expSchemas) throws Exception {
        log("-= Comparing Actual to instance=-");
        if (checkInstance(actSchemaDoc, inst))
            log("-= Instance validated actual =-");

        log("-= Comparing Expected to instance=-");
        if (checkInstance(expSchemas, inst))
            log("-= Instance validated Expected =-");

        return true;
    }

    public static boolean checkInstance(SchemaDocument[] sDocs, XmlObject[] inst) throws Exception {
        if (validateInstances(sDocs, inst)) {
            return true;
        } else {
            //log("-= SCHEMAS =-");
            //log(sDocs);
            //log("-= INSTANCES =-");
            //log(inst);
            throw new Exception("Instance Failed to validate");
        }
    }

    /**
     * Copied from inst2Xsd as option may be removed
     *
     * @param sDocs
     * @param instances
     * @return
     */
    public static boolean validateInstances(SchemaDocument[] sDocs, XmlObject[] instances) {
        SchemaTypeLoader sLoader;
        Collection compErrors = new ArrayList();
        XmlOptions schemaOptions = new XmlOptions();
        schemaOptions.setErrorListener(compErrors);
        try {
            sLoader = XmlBeans.loadXsd(sDocs, schemaOptions);
        } catch (Exception e) {
            if (compErrors.isEmpty() || !(e instanceof XmlException)) {
                e.printStackTrace(System.out);
            }
            System.out.println("Schema invalid");
            for (Iterator errors = compErrors.iterator(); errors.hasNext();)
                System.out.println(errors.next());
            return false;
        }

        boolean result = true;

        for (int i = 0; i < instances.length; i++) {
            String instance = instances[i].toString();

            XmlObject xobj;

            try {
                xobj = sLoader.parse(instance, null, new XmlOptions().setLoadLineNumbers());
            } catch (XmlException e) {
                System.out.println("Error:\n" + instance + " not loadable: " + e);
                e.printStackTrace(System.out);
                result = false;
                continue;
            }

            Collection errors = new ArrayList();

            if (xobj.schemaType() == XmlObject.type) {
                System.out.println(instance + " NOT valid.  ");
                System.out.println("  Document type not found.");
                result = false;
            } else if (xobj.validate(new XmlOptions().setErrorListener(errors)))
                System.out.println("Instance[" + i + "] valid.");
            else {
                System.out.println("Instance[" + i + "] NOT valid.");
                for (Iterator it = errors.iterator(); it.hasNext();) {
                    System.out.println("    " + it.next());
                }
                result = false;
            }
        }

        return result;
    }

}

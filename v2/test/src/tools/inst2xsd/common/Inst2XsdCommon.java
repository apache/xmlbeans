package tools.inst2xsd.common;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.inst2xsd.Inst2XsdOptions;
import org.w3.x2001.xmlSchema.SchemaDocument;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author jacobd
 * Date: Aug 9, 2004
 */
public class Inst2XsdCommon {

    public static XmlOptions getXmlOptions(){
        XmlOptions xm = new XmlOptions();
        xm.setCompileDownloadUrls();
        xm.setSavePrettyPrintIndent(4);
        xm.setSavePrettyPrintOffset(4);
        xm.setSavePrettyPrint();
        return xm;
    }

    public static Inst2XsdOptions getDefaultInstOptions(){
        Inst2XsdOptions initOpt = new Inst2XsdOptions();
        initOpt.setVerbose(true);
        return initOpt;
    }

    public static Inst2XsdOptions getRussianOptions() {
        Inst2XsdOptions opt = getDefaultInstOptions();
        opt.setDesign(Inst2XsdOptions.DESIGN_RUSSIAN_DOLL);
        return opt;
    }

    public static Inst2XsdOptions getSalamiOptions() {
        Inst2XsdOptions opt = getDefaultInstOptions();
        opt.setDesign(Inst2XsdOptions.DESIGN_SALAMI_SLICE);
        return opt;
    }

    public static Inst2XsdOptions getVenetianOptions() {
        Inst2XsdOptions opt = getDefaultInstOptions();
        opt.setDesign(Inst2XsdOptions.DESIGN_VENETIAN_BLIND);
        return opt;
    }


    public static String base = "<a xmlns=\"baseNamespace\">" +
            "<b>abc</b>" +
            "<c>123</c>" +
            "<d />" +
            "</a>";
    public static String base_expected_russian = "<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
            "targetNamespace=\"baseNamespace\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
            "<xs:element name=\"a\" type=\"bas:aType\" xmlns:bas=\"baseNamespace\"/>" +
            "<xs:complexType name=\"aType\" mixed=\"true\">" +
            "<xs:sequence>" +
            "<xs:element type=\"xs:string\" name=\"b\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>" +
            "<xs:element type=\"xs:byte\" name=\"c\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>" +
            "<xs:element type=\"xs:string\" name=\"d\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>" +
            "</xs:sequence>" +
            "</xs:complexType>" +
            "</xs:schema>";

    public static String base_expected_salami = "<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
            "targetNamespace=\"baseNamespace\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
            "<xs:element name=\"a\">" +
            "<xs:complexType mixed=\"true\">" +
            "<xs:sequence>" +
            "<xs:element ref=\"bas:b\" xmlns:bas=\"baseNamespace\"/>" +
            "<xs:element ref=\"bas:c\" xmlns:bas=\"baseNamespace\"/>" +
            "<xs:element ref=\"bas:d\" xmlns:bas=\"baseNamespace\"/>" +
            "</xs:sequence>" +
            "</xs:complexType>" +
            "</xs:element>" +
            "<xs:element name=\"b\" type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>" +
            "<xs:element name=\"c\" type=\"xs:byte\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>"+
            "<xs:element name=\"d\" type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>" +
            "</xs:schema>";

    public static String base_expected_venetian = "<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
            "targetNamespace=\"baseNamespace\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
            "<xs:element name=\"a\" type=\"bas:aType\" xmlns:bas=\"baseNamespace\"/>" +
            "<xs:complexType name=\"aType\" mixed=\"true\">" +
            "<xs:sequence>" +
            "<xs:element type=\"xs:string\" name=\"b\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>" +
            "<xs:element type=\"xs:byte\" name=\"c\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>" +
            "<xs:element type=\"xs:string\" name=\"d\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>" +
            "</xs:sequence>" +
            "</xs:complexType>" +
            "</xs:schema>";

}

package tools.inst2xsd.checkin;

import tools.inst2xsd.common.Inst2XsdTestBase;

/**
 * @author jacobd
 * Date: Aug 9, 2004
 */
public class Inst2XsdAttrTest extends Inst2XsdTestBase {

    public Inst2XsdAttrTest(String name) {
        super(name);
    }

    // List of precedence for smart simple primitive type determination
    // byte, short, int, long, integer, float, double, decimal,
    // boolean
    // date, dateTime, time, gDuration,
    // QName ?,
    // anyUri ? - triggered only for http:// or www. constructs,
    // list types ?
    // string
    public void test_attrstring() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("a"), "string");
        runAttrTypeChecking(getAttrTypeXml("a2a"), "string");
        runAttrTypeChecking(getAttrTypeXml("a b c\n hello\t from\n\txmlbeans"), "string");
    }


    //public void test_attr() throws Exception {
    //    runAttrTypeChecking(getAttrTypeXml(""), "");
    //}

}

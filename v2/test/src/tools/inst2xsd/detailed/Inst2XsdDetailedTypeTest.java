package tools.inst2xsd.detailed;

import tools.inst2xsd.common.Inst2XsdTestBase;

/**
 * @author jacobd
 * Date: Aug 9, 2004
 */
public class Inst2XsdDetailedTypeTest extends Inst2XsdTestBase {

    public Inst2XsdDetailedTypeTest(String name) {
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
    //TODO: Value will become number
    // public void test_gYear() throws Exception {
    //runTypeChecking(getTypeXml("1999"), "gYear");
    //}

    public void test_gYearMonth() throws Exception {
        runTypeChecking(getTypeXml("1999-05"), "gYearMonth");
    }

    public void test_gMonthDay() throws Exception {
        runTypeChecking(getTypeXml("--02-15"), "gMonthDay");
    }

    public void test_gDay() throws Exception {
        runTypeChecking(getTypeXml("---15"), "gDay");
    }

    public void test_gMonth() throws Exception {
        runTypeChecking(getTypeXml("--02--"), "gMonth");
    }

    public void test_duration() throws Exception {
        runTypeChecking(getTypeXml("P1347Y"), "duration");
        runTypeChecking(getTypeXml("P1347M"), "duration");
        runTypeChecking(getTypeXml("P1Y2MT2H"), "duration");
        runTypeChecking(getTypeXml("P0Y1347M"), "duration");
        runTypeChecking(getTypeXml("P0Y1347M0D"), "duration");
        runTypeChecking(getTypeXml("-P1347M"), "duration");
    }

    //THIS becomes string as expected
    //public void test_hexBinary() throws Exception {
    //    runTypeChecking(getTypeXml("0FB7"), "hexBinary");
    //}

    public void test_anyuri() throws Exception {
        runTypeChecking(getTypeXml("gopher://spinaltap.micro.umn.edu/00/Weather/California/Los%20Angeles"), "anyURI");
        runTypeChecking(getTypeXml("ftp://ftp.is.co.za/rfc/rfc1808.txt"), "anyURI");
        runTypeChecking(getTypeXml("http://www.math.uio.no/faq/compression-faq/part1.html"), "anyURI");
        runTypeChecking(getTypeXml("www.math.uio.no/faq/compression-faq/part1.html"), "anyURI");
        runTypeChecking(getTypeXml("mailto:mduerst@ifi.unizh.ch"), "string");
        runTypeChecking(getTypeXml("news:comp.infosystems.www.servers.unix"), "anyURI");
        runTypeChecking(getTypeXml("telnet://melvyl.ucop.edu/"), "anyURI");
        runTypeChecking(getTypeXml("./this:that"), "anyURI");
    }

    /** 0, and 1 get picked up by byte */
    public void test_boolean() throws Exception {
        runTypeChecking(getTypeXml("true"), "boolean");
        runTypeChecking(getTypeXml("false"), "boolean");
    }

    public void test_QName() throws Exception {
        runTypeChecking(getTypeXml("xsd:string"), "QName");
        runTypeChecking(getTypeXml("xsi:int"), "QName");
        runTypeChecking(getTypeXml("foo:baz"), "QName");
    }

}


import tools.inst2xsd.common.Inst2XsdTestBase;

/**
 * @author jacobd
 * Date: Aug 9, 2004
 */
public class Inst2XsdDetailedAttrTest extends Inst2XsdTestBase {

    public Inst2XsdDetailedAttrTest(String name) {
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
    //runTypeChecking(getAttrTypeXml("1999"), "gYear");
    //}

    public void test_attrgYearMonth() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("1999-05"), "gYearMonth");
    }

    public void test_attrgMonthDay() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("--02-15"), "gMonthDay");
    }

    public void test_attrgDay() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("---15"), "gDay");
    }

    public void test_attrgMonth() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("--02--"), "gMonth");
    }

    public void test_attrduration() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("P1347Y"), "duration");
        runAttrTypeChecking(getAttrTypeXml("P1347M"), "duration");
        runAttrTypeChecking(getAttrTypeXml("P1Y2MT2H"), "duration");
        runAttrTypeChecking(getAttrTypeXml("P0Y1347M"), "duration");
        runAttrTypeChecking(getAttrTypeXml("P0Y1347M0D"), "duration");
        runAttrTypeChecking(getAttrTypeXml("-P1347M"), "duration");
    }

    //THIS becomes string as expected
    //public void test_attrhexBinary() throws Exception {
    //    runAttrTypeChecking(getAttrTypeXml("0FB7"), "hexBinary");
    //}

    public void test_attranyuri() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("gopher://spinaltap.micro.umn.edu/00/Weather/California/Los%20Angeles"), "anyURI");
        runAttrTypeChecking(getAttrTypeXml("ftp://ftp.is.co.za/rfc/rfc1808.txt"), "anyURI");
        runAttrTypeChecking(getAttrTypeXml("http://www.math.uio.no/faq/compression-faq/part1.html"), "anyURI");
        runAttrTypeChecking(getAttrTypeXml("www.math.uio.no/faq/compression-faq/part1.html"), "anyURI");
        runAttrTypeChecking(getAttrTypeXml("mailto:mduerst@ifi.unizh.ch"), "string");
        runAttrTypeChecking(getAttrTypeXml("news:comp.infosystems.www.servers.unix"), "anyURI");
        runAttrTypeChecking(getAttrTypeXml("telnet://melvyl.ucop.edu/"), "anyURI");
        runAttrTypeChecking(getAttrTypeXml("./this:that"), "anyURI");
    }

    /** 0, and 1 get picked up by byte */
    public void test_attrboolean() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("true"), "boolean");
        runAttrTypeChecking(getAttrTypeXml("false"), "boolean");
    }

}

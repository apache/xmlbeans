package tools.inst2xsd.detailed;

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

    //todo: move to checkin once restriction issue gets fixed
    public void test_attrbyte() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("123"), "byte");
        runAttrTypeChecking(getAttrTypeXml("+100"), "byte");
        runAttrTypeChecking(getAttrTypeXml("-1"), "byte");
        runAttrTypeChecking(getAttrTypeXml("0"), "byte");
        runAttrTypeChecking(getAttrTypeXml("127"), "byte");
        runAttrTypeChecking(getAttrTypeXml("-128"), "byte");
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

    public void test_attrshort() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("-129"), "short");
        runAttrTypeChecking(getAttrTypeXml("128"), "short");
        runAttrTypeChecking(getAttrTypeXml("3000"), "short");
        runAttrTypeChecking(getAttrTypeXml("-3000"), "short");
        runAttrTypeChecking(getAttrTypeXml("-32768"), "short");
        runAttrTypeChecking(getAttrTypeXml("32767"), "short");
    }

    public void test_attrint() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("39000"), "int");
        runAttrTypeChecking(getAttrTypeXml("32768"), "int");
        runAttrTypeChecking(getAttrTypeXml("-32769"), "int");
        runAttrTypeChecking(getAttrTypeXml("-39000"), "int");
        runAttrTypeChecking(getAttrTypeXml("126789675"), "int");
        runAttrTypeChecking(getAttrTypeXml("2147483647"), "int");
        runAttrTypeChecking(getAttrTypeXml("-2147483648"), "int");
    }

    public void test_attrlong() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("2147483648"), "long");
        runAttrTypeChecking(getAttrTypeXml("-2147483649"), "long");
        runAttrTypeChecking(getAttrTypeXml("-2150000000"), "long");
        runAttrTypeChecking(getAttrTypeXml("2150000000"), "long");
        runAttrTypeChecking(getAttrTypeXml("-9223372036854775808"), "long");
        runAttrTypeChecking(getAttrTypeXml("9223372036854775807"), "long");
    }

    public void test_attrinteger() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("9300000000000000000"), "integer");
        runAttrTypeChecking(getAttrTypeXml("-9300000000000000000"), "integer");
        runAttrTypeChecking(getAttrTypeXml("-9223372036854775809"), "integer");
        runAttrTypeChecking(getAttrTypeXml("9223372036854775808"), "integer");
    }

    public void test_attrfloat() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("12.78e-2"), "float");
        runAttrTypeChecking(getAttrTypeXml("1267.43233E12"), "float");
        runAttrTypeChecking(getAttrTypeXml("-1E4"), "float");
        runAttrTypeChecking(getAttrTypeXml("INF"), "float");
        runAttrTypeChecking(getAttrTypeXml("-INF"), "float");
        runAttrTypeChecking(getAttrTypeXml("NaN"), "float");
        runAttrTypeChecking(getAttrTypeXml("-1.23"), "float");
        runAttrTypeChecking(getAttrTypeXml("12678967.543233"), "float");
        runAttrTypeChecking(getAttrTypeXml("+100000.00"), "float");
    }

    //TODO: NOT COMPLETELY SURE HOW TO GET THESE WITHOUT
    //CAUSING AN Number EXCEPTION
    //public void test_attrdouble() throws Exception {
    //    runAttrTypeChecking(getAttrTypeXml(""), "double");
    //}

    //public void test_attrdecimal() throws Exception {
    //    runAttrTypeChecking(getAttrTypeXml(""), "decimal");
    //}
    //Value will become number
    // public void test_attrgYear() throws Exception {
    //runAttrTypeChecking(getAttrTypeXml("1999"), "gYear");
    //}


    public void test_attrdate() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("1999-05-31"), "date");
    }

    public void test_attrdateTime() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("1999-05-31T13:20:00-05:00"), "dateTime");
        runAttrTypeChecking(getAttrTypeXml("2000-03-04T20:00:00Z"), "dateTime");
        runAttrTypeChecking(getAttrTypeXml("2000-03-04T23:00:00+03:00"), "dateTime");
    }

    public void test_attrtime() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("13:20:00-05:00"), "time");
        runAttrTypeChecking(getAttrTypeXml("00:00:00"), "time");
        runAttrTypeChecking(getAttrTypeXml("13:20:00Z"), "time");
    }

    public void test_attrQName() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("xsd:string"), "QName");
        runAttrTypeChecking(getAttrTypeXml("xsi:int"), "QName");
        runAttrTypeChecking(getAttrTypeXml("foo:baz"), "QName");
    }

    public void test_attrCDATA() throws Exception {
        runAttrTypeChecking(getAttrTypeXml("<![CDATA[ " +
                "function matchwo(a, b) {" +
                "if (a < b && a < 0) " +
                "    then { " +
                "        return 1 " +
                "    } else { " +
                "        return 0 " +
                "    } " +
                "} ]]>"), "string");
    }

}

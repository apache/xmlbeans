package xmlcursor.xpath;


import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Test;

import java.util.HashMap;


public class CustomerTest {

    private static void test_xpath(int id, String xml, String xpath) throws XmlException {
        XmlObject xmlObj = XmlObject.Factory.parse(xml);
        XmlObject[] results = xmlObj.selectPath(xpath);
    }

    private static void test_xquery(int id, String xquery) throws XmlException {
        String sXml =
            "<?xml version=\"1.0\"?>\n" +
            "<!DOCTYPE book SYSTEM \"book.dtd\">\n" +
            "\n" +
            "<book>\n" +
            "  <title>Data on the Web</title>\n" +
            "  <author>Serge Abiteboul</author>\n" +
            "  <author>Peter Buneman</author>\n" +
            "  <author>Dan Suciu</author>\n" +
            "  <section id=\"intro\" difficulty=\"easy\" >\n" +
            "    <title>Introduction</title>\n" +
            "    <p>Text ... </p>\n" +
            "    <section>\n" +
            "      <title>Audience</title>\n" +
            "      <p>Text ... </p>\n" +
            "    </section>\n" +
            "    <section>\n" +
            "      <title>Web Data and the Two Cultures</title>\n" +
            "      <p>Text ... </p>\n" +
            "      <figure height=\"400\" width=\"400\">\n" +
            "        <title>Traditional client/server architecture</title>\n" +
            "        <image source=\"csarch.gif\"/>\n" +
            "      </figure>\n" +
            "      <p>Text ... </p>\n" +
            "    </section>\n" +
            "  </section>\n" +
            "  <section id=\"syntax\" difficulty=\"medium\" >\n" +
            "    <title>A Syntax For Data</title>\n" +
            "    <p>Text ... </p>\n" +
            "    <figure height=\"200\" width=\"500\">\n" +
            "      <title>Graph representations of structures</title>\n" +
            "      <image source=\"graphs.gif\"/>\n" +
            "    </figure>\n" +
            "    <p>Text ... </p>\n" +
            "    <section>\n" +
            "      <title>Base Types</title>\n" +
            "      <p>Text ... </p>\n" +
            "    </section>\n" +
            "    <section>\n" +
            "      <title>Representing Relational Databases</title>\n" +
            "      <p>Text ... </p>\n" +
            "      <figure height=\"250\" width=\"400\">\n" +
            "        <title>Examples of Relations</title>\n" +
            "        <image source=\"relations.gif\"/>\n" +
            "      </figure>\n" +
            "    </section>\n" +
            "    <section>\n" +
            "      <title>Representing Object Databases</title>\n" +
            "      <p>Text ... </p>\n" +
            "    </section>\n" +
            "  </section>\n" +
            "</book>";
        XmlObject xmlObj = XmlObject.Factory.parse(sXml);
        XmlObject[] results = xmlObj.execQuery(xquery);
    }

    @Test
    public void test_xpath() throws XmlException {
        String sXml1 =
            "<report>\n" +
            "  <section>\n" +
            "    <section.title>Procedure</section.title>\n" +
            "     <section.content>\n" +
            "      The patient was taken to the operating room where she was placed\n" +
            "      in supine position and\n" +
            "      </section.content> </section></report>";
        test_xpath(2, sXml1,
            "./report/section/section.title[text() = \"Procedure\"]");
    }

    @Test
    public void test_xquery() throws XmlException {
        final String xquery1 =
            "for $b in $this/bib/book "
            +
            "  where $b/publisher[text() = \"Addison-Wesley\"] and $b[@year > 1992] "
            + "return "
            + "    <book year=\"{ $b/@year }\"> "
            + "{ $b/title }"
            + "</book>";

        test_xquery(1, xquery1);

        final String xquery2 =
            "for $b in $this/bib/book "
            + "  where $b/publisher = \"Addison-Wesley\" and $b/@year > 1992 "
            + "return "
            + "    <book year=\"{ $b/@year }\"> "
            + "{ $b/title }"
            + "</book>";

        test_xquery(2, xquery2);
    }

    @Test
    public void testXMLBeans() throws XmlException {
        XmlObject doc = XmlObject.Factory.parse(" <contact xmlns=\"http://dearjohn/address-book\"/>");
        HashMap<String, String> nsMap = new HashMap<String, String>();
        nsMap.put("ns", "http://dearjohn/address-book");
        doc.execQuery("/ns:contact", new
            XmlOptions().setLoadAdditionalNamespaces(nsMap));
    }
}

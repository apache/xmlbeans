package scomp.xmlbeans583.detailed;

import org.junit.Test;
import scomp.common.BaseCase;
import xsd.xmlToStringTest1Xsd.Ns1T1;
import xsd.xmlToStringTest2Xsd.Ns2E1Document;
import xsd.xmlToStringTest3Xsd.Ns3E1Document;

import static org.junit.Assert.assertNotNull;

public class Xmlbeans583Test extends BaseCase {
    @Test
    public void testXmlToString() throws Throwable {
        final Ns2E1Document ns2e1Doc = Ns2E1Document.Factory.newInstance();
        ns2e1Doc.addNewNs2E1().addNewNs2T1E1().setNs1T1E1("Ns1T1E1");

        final Ns3E1Document ns3e1Doc = Ns3E1Document.Factory.newInstance();
        ns3e1Doc.addNewNs3E1().setNs3T1E1(Ns1T1.Factory.parse(ns2e1Doc.getNs2E1().getNs2T1E1().toString()));

        final Ns3E1Document pNs3e1Doc = Ns3E1Document.Factory.parse(ns3e1Doc.toString());
        final Ns2E1Document pNs2e1Doc = Ns2E1Document.Factory.newInstance();
        pNs2e1Doc.addNewNs2E1().setNs2T1E1(pNs3e1Doc.getNs3E1().getNs3T1E1());

        assertNotNull(Ns2E1Document.Factory.parse(pNs2e1Doc.toString()));
    }
}

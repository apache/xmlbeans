package drtcases.prePostFeature.ValueRestriction;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import prePostFeature.xbean.valueRestriction.company.*;
import org.apache.xmlbeans.XmlString;

/**
 * Created by IntelliJ IDEA.
 * User: ykadiysk
 * Date: May 5, 2004
 * Time: 11:23:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class ValueRestrictionTest extends TestCase
{
    public ValueRestrictionTest(String s)
    {
        super(s);
    }

    public static Test suite()
    {
        return new TestSuite(ValueRestrictionTest.class);
    }

    public void test() throws Exception
    {
        CompanyDocument poDoc;

        poDoc = CompanyDocument.Factory.newInstance();
        CompanyType po = poDoc.addNewCompany();

        int LEN = 20;

        StringBuffer sExpected = new StringBuffer();
        sExpected.append("<com:company xmlns:com=\"prePostFeature/xbean/ValueRestriction/company\"><departments>");
        DepartmentType dept = po.addNewDepartments();
        ConsultantType[] it = new ConsultantType[LEN];
        for (int i = 0; i < LEN; i++)
        {
            it[i] = dept.addNewConsultant();
            XmlString s = XmlString.Factory.newInstance();
            it[i].setAge(50);
            sExpected.append("<consultant age=\"50\"/>");
        }

        sExpected.append("</departments></com:company>");

        assertEquals(sExpected.toString(), poDoc.xmlText());
        assertTrue(poDoc.validate());


        prePostFeature.ValueRestriction.existing.SetterHandler.bReady = true;


        for (int i = 0; i < LEN; i++)
        {
            it[i].setAge(150);
        }

        assertEquals(sExpected.toString(), poDoc.xmlText());
        assertTrue(poDoc.validate());

        for (int i = 0; i < LEN; i++)
        {
            it[i].setEmployeeAge(150);
        }
        assertTrue(sExpected.toString().equals(poDoc.xmlText()));
        assertTrue(poDoc.validate());
    }
}


/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2000-2003 The Apache Software Foundation.  All rights 
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer. 
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:  
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must 
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written 
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache 
*    XMLBeans", nor may "Apache" appear in their name, without prior 
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems 
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package drtcases;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.openuri.lut.ListsDocument;
import org.openuri.lut.UnionsDocument;
import org.openuri.lut.IncidentReportsDocument;
import org.openuri.lut.DateOrDateTime;

import java.util.List;
import java.util.Arrays;
import java.util.Calendar;
import java.math.BigInteger;

import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlDate;
import org.apache.xmlbeans.XmlDateTime;
import org.apache.xmlbeans.XmlCalendar;

public class ListAndUnionTests extends TestCase
{
    public ListAndUnionTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(ListAndUnionTests.class); }

    public void testListGetters() throws Exception
    {
        ListsDocument lists = ListsDocument.Factory.parse(
                "<lut:lists xmlns:lut='http://openuri.org/lut'><lut:int-list>2 4 8 16 32</lut:int-list><lut:nni-list>unbounded 3 unbounded 6</lut:nni-list></lut:lists>");
        List intList = lists.getLists().getIntList();
        Assert.assertEquals(new Integer(2), intList.get(0));
        Assert.assertEquals(new Integer(4), intList.get(1));
        Assert.assertEquals(new Integer(8), intList.get(2));
        Assert.assertEquals(new Integer(16), intList.get(3));
        Assert.assertEquals(new Integer(32), intList.get(4));
        Assert.assertEquals(5, intList.size());

        List nniList = lists.getLists().getNniList();
        Assert.assertEquals("unbounded", nniList.get(0));
        Assert.assertEquals(BigInteger.valueOf(3), nniList.get(1));
        Assert.assertEquals("unbounded", nniList.get(2));
        Assert.assertEquals(BigInteger.valueOf(6), nniList.get(3));
        Assert.assertEquals(4, nniList.size());
    }

    public void testListSetters() throws Exception
    {
        ListsDocument doc = ListsDocument.Factory.newInstance();
        ListsDocument.Lists lists = doc.addNewLists();
        lists.setIntList(Arrays.asList(new Object[] { new Integer(4), new Integer(18) }));
        lists.setNniList(Arrays.asList(new Object[] { BigInteger.valueOf(1), BigInteger.valueOf(2), "unbounded" }));
        String xtext = doc.xmlText();

        ListsDocument docrt = ListsDocument.Factory.parse(xtext);
        List intList = docrt.getLists().getIntList();
        Assert.assertEquals(new Integer(4), intList.get(0));
        Assert.assertEquals(new Integer(18), intList.get(1));
        Assert.assertEquals(2, intList.size());

        List nniList = docrt.getLists().getNniList();
        Assert.assertEquals(BigInteger.valueOf(1), nniList.get(0));
        Assert.assertEquals(BigInteger.valueOf(2), nniList.get(1));
        Assert.assertEquals("unbounded", nniList.get(2));
        Assert.assertEquals(3, nniList.size());
    }

    public void testUnionGetters() throws Exception
    {
        UnionsDocument unions = UnionsDocument.Factory.parse(
                "<lut:unions xmlns:lut='http://openuri.org/lut'><lut:nni>unbounded</lut:nni><lut:sizes>2 3 5 7 11</lut:sizes></lut:unions>");

        Assert.assertEquals("unbounded", unions.getUnions().getNni());

        Assert.assertTrue(unions.getUnions().getSizes() instanceof List);
        List sizes = (List)unions.getUnions().getSizes();
        Assert.assertEquals(new Integer(2), sizes.get(0));
        Assert.assertEquals(new Integer(3), sizes.get(1));
        Assert.assertEquals(new Integer(5), sizes.get(2));
        Assert.assertEquals(new Integer(7), sizes.get(3));
        Assert.assertEquals(new Integer(11), sizes.get(4));
        Assert.assertEquals(5, sizes.size());

        UnionsDocument unions2 = UnionsDocument.Factory.parse(
                "<lut:unions xmlns:lut='http://openuri.org/lut'><lut:nni>37</lut:nni><lut:sizes>all</lut:sizes></lut:unions>");

        Assert.assertEquals(BigInteger.valueOf(37), unions2.getUnions().getNni());
        Assert.assertEquals("all", unions2.getUnions().getSizes());
    }

    public void testUnionSetters() throws Exception
    {

        // create a document
        UnionsDocument doc = UnionsDocument.Factory.newInstance();
        UnionsDocument.Unions unions = doc.addNewUnions();
        unions.setNni("unbounded");
        unions.setSizes(Arrays.asList(new Object[] { new Integer(5), new Integer(22) }));

        // round trip to s text
        String xtext = doc.xmlText();
        UnionsDocument docrt = UnionsDocument.Factory.parse(xtext);

        // verify contents
        Assert.assertEquals("unbounded", docrt.getUnions().getNni());
        List sizes = (List)docrt.getUnions().getSizes();
        Assert.assertEquals(new Integer(5), sizes.get(0));
        Assert.assertEquals(new Integer(22), sizes.get(1));
        Assert.assertEquals(2, sizes.size());

        // change the original document
        unions.setNni(new Integer(11));
        unions.setSizes("unknown");

        // round trip it again
        xtext = doc.xmlText();
        docrt = UnionsDocument.Factory.parse(xtext);

        // verify contents again
        Assert.assertEquals(BigInteger.valueOf(11), docrt.getUnions().getNni());
        Assert.assertEquals("unknown", docrt.getUnions().getSizes());
    }

    public void testUnionArray() throws Exception
    {
        IncidentReportsDocument doc = IncidentReportsDocument.Factory.parse(
                 "<lut:incident-reports xmlns:lut='http://openuri.org/lut'><lut:when>2001-08-06T03:34:00</lut:when><lut:when>2002-01-04</lut:when><lut:when>2002-08-26T23:10:00</lut:when></lut:incident-reports>");
        IncidentReportsDocument.IncidentReports reports = doc.getIncidentReports();
        DateOrDateTime[] dt = reports.xgetWhenArray();
        Calendar[] gd = reports.getWhenArray();
        Assert.assertEquals(3, dt.length);
        Assert.assertEquals(3, gd.length);
        for (int i = 0; i < 3; i++)
        {
            Assert.assertEquals(((SimpleValue)dt[i]).gDateValue(), new GDate(gd[i]));
            Assert.assertEquals(gd[i], dt[i].objectValue());
        }

        Assert.assertEquals(new XmlCalendar("2001-08-06T03:34:00"), gd[0]);
        Assert.assertEquals(new XmlCalendar("2002-01-04"), gd[1]);
        Assert.assertEquals(new XmlCalendar("2002-08-26T23:10:00"), gd[2]);

        Assert.assertEquals(XmlDateTime.type, dt[0].instanceType());
        Assert.assertEquals(XmlDate.type, dt[1].instanceType());
        Assert.assertEquals(XmlDateTime.type, dt[2].instanceType());

        reports.setWhenArray(0, new XmlCalendar("1980-04-18"));
        reports.setWhenArray(1, new XmlCalendar("1970-12-20T04:33:00"));

        dt = reports.xgetWhenArray();
        gd = reports.getWhenArray();

        Assert.assertEquals(new XmlCalendar("1980-04-18"), gd[0]);
        Assert.assertEquals(new XmlCalendar("1970-12-20T04:33:00"), gd[1]);
        Assert.assertEquals(new XmlCalendar("2002-08-26T23:10:00"), gd[2]);

        Assert.assertEquals(XmlDate.type, dt[0].instanceType());
        Assert.assertEquals(XmlDateTime.type, dt[1].instanceType());
        Assert.assertEquals(XmlDateTime.type, dt[2].instanceType());

    }

}

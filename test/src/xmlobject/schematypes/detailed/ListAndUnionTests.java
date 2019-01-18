/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package xmlobject.schematypes.detailed;

import org.junit.Assert;
import org.apache.xmlbeans.*;
import org.junit.Test;
import org.openuri.lut.DateOrDateTime;
import org.openuri.lut.IncidentReportsDocument;
import org.openuri.lut.ListsDocument;
import org.openuri.lut.UnionsDocument;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ListAndUnionTests {
    @Test
    public void testListGetters() throws Exception {
        ListsDocument lists = ListsDocument.Factory.parse(
            "<lut:lists xmlns:lut='http://openuri.org/lut'><lut:int-list>2 4 8 16 32</lut:int-list><lut:nni-list>unbounded 3 unbounded 6</lut:nni-list></lut:lists>");
        List intList = lists.getLists().getIntList();
        assertEquals(2, intList.get(0));
        assertEquals(4, intList.get(1));
        assertEquals(8, intList.get(2));
        assertEquals(16, intList.get(3));
        assertEquals(32, intList.get(4));
        assertEquals(5, intList.size());

        List nniList = lists.getLists().getNniList();
        assertEquals("unbounded", nniList.get(0));
        assertEquals(BigInteger.valueOf(3), nniList.get(1));
        assertEquals("unbounded", nniList.get(2));
        assertEquals(BigInteger.valueOf(6), nniList.get(3));
        assertEquals(4, nniList.size());
    }

    @Test
    public void testListSetters() throws Exception {
        ListsDocument doc = ListsDocument.Factory.newInstance();
        ListsDocument.Lists lists = doc.addNewLists();
        lists.setIntList(Arrays.asList(4, 18));
        lists.setNniList(Arrays.asList(BigInteger.valueOf(1), BigInteger.valueOf(2), "unbounded"));
        String xtext = doc.xmlText();

        ListsDocument docrt = ListsDocument.Factory.parse(xtext);
        List intList = docrt.getLists().getIntList();
        assertEquals(4, intList.get(0));
        assertEquals(18, intList.get(1));
        assertEquals(2, intList.size());

        List nniList = docrt.getLists().getNniList();
        assertEquals(BigInteger.valueOf(1), nniList.get(0));
        assertEquals(BigInteger.valueOf(2), nniList.get(1));
        assertEquals("unbounded", nniList.get(2));
        assertEquals(3, nniList.size());
    }

    @Test
    public void testUnionGetters() throws Exception {
        UnionsDocument unions = UnionsDocument.Factory.parse(
            "<lut:unions xmlns:lut='http://openuri.org/lut'><lut:nni>unbounded</lut:nni><lut:sizes>2 3 5 7 11</lut:sizes></lut:unions>");

        assertEquals("unbounded", unions.getUnions().getNni());

        Assert.assertTrue(unions.getUnions().getSizes() instanceof List);
        List sizes = (List) unions.getUnions().getSizes();
        assertEquals(2, sizes.get(0));
        assertEquals(3, sizes.get(1));
        assertEquals(5, sizes.get(2));
        assertEquals(7, sizes.get(3));
        assertEquals(11, sizes.get(4));
        assertEquals(5, sizes.size());

        UnionsDocument unions2 = UnionsDocument.Factory.parse(
            "<lut:unions xmlns:lut='http://openuri.org/lut'><lut:nni>37</lut:nni><lut:sizes>all</lut:sizes></lut:unions>");

        assertEquals(BigInteger.valueOf(37), unions2.getUnions().getNni());
        assertEquals("all", unions2.getUnions().getSizes());
    }

    @Test
    public void testUnionSetters() throws Exception {

        // create a document
        UnionsDocument doc = UnionsDocument.Factory.newInstance();
        UnionsDocument.Unions unions = doc.addNewUnions();
        unions.setNni("unbounded");
        unions.setSizes(Arrays.asList(5, 22));

        // round trip to s text
        String xtext = doc.xmlText();
        UnionsDocument docrt = UnionsDocument.Factory.parse(xtext);

        // verify contents
        assertEquals("unbounded", docrt.getUnions().getNni());
        List sizes = (List) docrt.getUnions().getSizes();
        assertEquals(5, sizes.get(0));
        assertEquals(22, sizes.get(1));
        assertEquals(2, sizes.size());

        // change the original document
        unions.setNni(11);
        unions.setSizes("unknown");

        // round trip it again
        xtext = doc.xmlText();
        docrt = UnionsDocument.Factory.parse(xtext);

        // verify contents again
        assertEquals(BigInteger.valueOf(11), docrt.getUnions().getNni());
        assertEquals("unknown", docrt.getUnions().getSizes());
    }

    @Test
    public void testUnionArray() throws Exception {
        IncidentReportsDocument doc = IncidentReportsDocument.Factory.parse(
            "<lut:incident-reports xmlns:lut='http://openuri.org/lut'>" +
                "<lut:when>2001-08-06T03:34:00</lut:when>" +
                "<lut:when>2002-01-04</lut:when>" +
                "<lut:when>2002-08-26T23:10:00</lut:when>" +
                "</lut:incident-reports>");
        IncidentReportsDocument.IncidentReports reports = doc.getIncidentReports();
        DateOrDateTime[] dt = reports.xgetWhenArray();
        Calendar[] gd = reports.getWhenArray();
        assertEquals(3, dt.length);
        assertEquals(3, gd.length);
        for (int i = 0; i < 3; i++) {
            assertEquals(((SimpleValue) dt[i]).getGDateValue(), new GDate(gd[i]));
            assertEquals(gd[i], dt[i].getObjectValue());
        }

        assertEquals(new XmlCalendar("2001-08-06T03:34:00"), gd[0]);
        assertEquals(new XmlCalendar("2002-01-04"), gd[1]);
        assertEquals(new XmlCalendar("2002-08-26T23:10:00"), gd[2]);

        assertEquals(XmlDateTime.type, dt[0].instanceType());
        assertEquals(XmlDate.type, dt[1].instanceType());
        assertEquals(XmlDateTime.type, dt[2].instanceType());

        reports.setWhenArray(0, new XmlCalendar("1980-04-18"));
        reports.setWhenArray(1, new XmlCalendar("1970-12-20T04:33:00"));

        dt = reports.xgetWhenArray();
        gd = reports.getWhenArray();

        assertEquals(new XmlCalendar("1980-04-18"), gd[0]);
        assertEquals(new XmlCalendar("1970-12-20T04:33:00"), gd[1]);
        assertEquals(new XmlCalendar("2002-08-26T23:10:00"), gd[2]);

        assertEquals(XmlDate.type, dt[0].instanceType());
        assertEquals(XmlDateTime.type, dt[1].instanceType());
        assertEquals(XmlDateTime.type, dt[2].instanceType());

    }

}

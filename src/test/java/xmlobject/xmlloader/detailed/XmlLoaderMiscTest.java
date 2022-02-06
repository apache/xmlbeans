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


package xmlobject.xmlloader.detailed;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tranxml.tranXML.version40.CarLocationMessageDocument;
import org.tranxml.tranXML.version40.GeographicLocationDocument.GeographicLocation;
import tools.util.JarUtil;
import xmlcursor.common.Common;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static xmlcursor.common.BasicCursorTestCase.jcur;


public class XmlLoaderMiscTest {
    @Test
    void testNewInstance() throws Exception {
        try (XmlCursor m_xc = XmlObject.Factory.newInstance().newCursor()) {
            assertEquals(TokenType.STARTDOC, m_xc.currentTokenType());
            m_xc.toNextToken();
            assertEquals(TokenType.ENDDOC, m_xc.currentTokenType());
        }
    }

    @Test
    void testTypeForClass() throws Exception {
        try (XmlCursor m_xc = jcur(Common.TRANXML_FILE_CLM)) {
            m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//GeographicLocation");
            m_xc.toNextSelection();
            GeographicLocation gl = (GeographicLocation) m_xc.getObject();
            assertNotNull(gl.schemaType());
            assertEquals(gl.schemaType(), XmlBeans.typeForClass(GeographicLocation.class));
        }
    }

    @Test
    void testGetBuiltInTypeSystem() {
        SchemaTypeSystem sts = XmlBeans.getBuiltinTypeSystem();
        assertNotNull(sts, "XmlBeans.getBuiltinTypeSystem() returned null");
        String sSchemaURI = "http://www.w3.org/2001/XMLSchema";
        QName name = new QName(sSchemaURI, "dateTime");
        SchemaType stDateTime = sts.findType(name);
        assertEquals(14, stDateTime.getBuiltinTypeCode());
    }

    @Disabled
    public void testTypeLoaderUnion() {
        // TODO
    }

    @Test
    void testTypeLoaderForClassLoader() throws Exception {
        SchemaTypeLoader stl = XmlBeans.typeLoaderForClassLoader(CarLocationMessageDocument.class.getClassLoader());
        assertNotNull(stl, "typeLoaderForClassLoader failed with CarLocationMessageDocument.class");
        try (XmlCursor m_xc = stl.parse(JarUtil.getResourceFromJar(Common.TRANXML_FILE_CLM), null, null).newCursor()){
            m_xc.selectPath(Common.CLM_NS_XQUERY_DEFAULT + "$this//FleetID");
            m_xc.toNextSelection();
            assertEquals("FLEETNAME", m_xc.getTextValue());
        }
    }

    @Test
    void testGetContextTypeLoader() throws Exception {
        SchemaTypeLoader stl = XmlBeans.getContextTypeLoader();
        assertNotNull(stl, "getContextTypeLoader failed");

        Vector<Thread> vThreads = new Vector<>();
        Set<SchemaTypeLoader> STLset = Collections.synchronizedSet(new HashSet<>());
        for (int i = 0; i < 10000; i++) {
            Thread t = new BogusThread(STLset);
            vThreads.add(t);
            t.start();
        }
        for (int i = 0; i < 10000; i++) {
            ((BogusThread) vThreads.elementAt(i)).join();
        }
        // each thread should create a unique type loader.
        // so count of objects in set should be 10000
        assertEquals(10000, STLset.size());
    }


    private static class BogusThread extends Thread {
        private final Set<SchemaTypeLoader> set;

        public BogusThread(Set<SchemaTypeLoader> set) {
            this.set = set;
        }

        public void run() {
            SchemaTypeLoader s = XmlBeans.getContextTypeLoader();
            set.add(s);
        }
    }
}


/*
 * Copyright (c) 2001-2003 World Wide Web Consortium, (Massachusetts Institute
 * of Technology, Institut National de Recherche en Informatique et en
 * Automatique, Keio University). All Rights Reserved. This program is
 * distributed under the W3C's Software Intellectual Property License. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See W3C License
 * http://www.w3.org/Consortium/Legal/ for more details.
 */

/*
 * $Log: DOMTest.java,v $
 * Revision 1.12  2004/01/05 08:27:14  dom-ts-4
 * XHTML compatible L3 Core tests  (bug 455)
 *
 * Revision 1.11  2003/12/30 06:17:08  dom-ts-4
 * Miscellaneous L&S changes based on implementor feedback (bug 447)
 *
 * Revision 1.10  2003/12/19 22:21:04  dom-ts-4
 * willBeModified violation detection support (bug 412)
 * Revision 1.9 2003/12/09 08:22:27 dom-ts-4 Additional
 * L&S tests, mostly configuration (Bug 401)
 *
 * Revision 1.8 2003/12/02 03:49:29 dom-ts-4 Load/save fixup (bug 396)
 *
 * Revision 1.7 2003/06/27 05:36:05 dom-ts-4 contentType condition fixes:
 * http://www.w3.org/Bugs/Public/show_bug.cgi?id=241
 *
 * Revision 1.6 2003/04/24 05:02:05 dom-ts-4 Xalan-J support for L3 XPath
 * http://www.w3.org/Bugs/Public/show_bug.cgi?id=191
 *
 * Revision 1.5 2003/04/23 05:48:24 dom-ts-4 DOMTSML and framework support for
 * createXPathEvaluator http://www.w3.org/Bugs/Public/show_bug.cgi?id=190
 *
 * Revision 1.4 2003/04/03 07:18:23 dom-ts-4 Added openStream method
 *
 * Revision 1.3 2002/08/13 04:44:46 dom-ts-4 Added getImplementation()
 *
 * Revision 1.2 2002/02/03 04:22:35 dom-ts-4 DOM4J and Batik support added.
 * Rework of parser settings
 *
 * Revision 1.1 2001/07/23 04:52:20 dom-ts-4 Initial test running using JUnit.
 *
 */

package org.w3c.domts;

import dom.common.Loader;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import tools.util.JarUtil;

/**
 * This is an abstract base class for generated DOM tests
 */

public abstract class DOMTest {
    private Document _XBeanDoc;

    public DOMImplementation getImplementation() {
        return _XBeanDoc.getImplementation();
    }

    public Document load(String docURI, boolean willBeModified)
        throws DOMTestLoadException {


        try {
            String sXml = JarUtil.getResourceFromJar(
                "xbean/dom/W3C/level2/core/files/" + docURI + ".xml");
            Loader _loader = Loader.getLoader();
            _XBeanDoc = _loader.load(sXml);
        } catch (Exception e) {
            throw new DOMTestLoadException(e);
        }
        return _XBeanDoc;
    }

    abstract public String getTargetURI();

    public final boolean isExpandEntityReferences() {
        return false;
    }
}

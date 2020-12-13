/*   Copyright 2017, 2018 The Apache Software Foundation
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

package org.apache.xmlbeans.impl.common;

import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlOptionsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Provides handy methods for working with SAX parsers and readers
 */
public final class SAXHelper {
    private static final Logger logger = LoggerFactory.getLogger(SAXHelper.class);
    private static long lastLog;

    private SAXHelper() {
    }

    /**
     * Creates a new SAX XMLReader, with sensible defaults
     */
    public static XMLReader newXMLReader(XmlOptions options) throws SAXException, ParserConfigurationException {
        XMLReader xmlReader = saxFactory(options).newSAXParser().getXMLReader();
        xmlReader.setEntityResolver(IGNORING_ENTITY_RESOLVER);
        trySetSAXFeature(xmlReader, XMLConstants.FEATURE_SECURE_PROCESSING);
        trySetXercesSecurityManager(xmlReader, options);
        return xmlReader;
    }

    public static final EntityResolver IGNORING_ENTITY_RESOLVER = new EntityResolver() {
        @Override
        public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
            return new InputSource(new StringReader(""));
        }
    };

    static SAXParserFactory saxFactory() {
        return saxFactory(new XmlOptionsBean());
    }

    static SAXParserFactory saxFactory(XmlOptions options) {
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        saxFactory.setValidating(false);
        saxFactory.setNamespaceAware(true);
        trySetSAXFeature(saxFactory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        trySetSAXFeature(saxFactory, XMLBeansConstants.FEATURE_LOAD_DTD_GRAMMAR, options.isLoadDTDGrammar());
        trySetSAXFeature(saxFactory, XMLBeansConstants.FEATURE_LOAD_EXTERNAL_DTD, options.isLoadExternalDTD());
        return saxFactory;
    }

    private static void trySetSAXFeature(SAXParserFactory spf, String feature, boolean flag) {
        try {
            spf.setFeature(feature, flag);
        } catch (Exception e) {
            logger.warn("SAX Feature unsupported: {}", feature, e);
        } catch (AbstractMethodError ame) {
            logger.warn("Cannot set SAX feature {} because outdated XML parser in classpath", feature, ame);
        }
    }

    private static void trySetSAXFeature(XMLReader xmlReader, String feature) {
        try {
            xmlReader.setFeature(feature, true);
        } catch (Exception e) {
            logger.warn("SAX Feature unsupported: {}", feature, e);
        } catch (AbstractMethodError ame) {
            logger.warn("Cannot set SAX feature {} because outdated XML parser in classpath", feature, ame);
        }
    }

    private static void trySetXercesSecurityManager(XMLReader xmlReader, XmlOptions options) {
        // Try built-in JVM one first, standalone if not
        for (String securityManagerClassName : new String[]{
            //"com.sun.org.apache.xerces.internal.util.SecurityManager",
            "org.apache.xerces.util.SecurityManager"
        }) {
            try {
                Object mgr = Class.forName(securityManagerClassName).getDeclaredConstructor().newInstance();
                Method setLimit = mgr.getClass().getMethod("setEntityExpansionLimit", Integer.TYPE);
                setLimit.invoke(mgr, options.getEntityExpansionLimit());
                xmlReader.setProperty(XMLBeansConstants.SECURITY_MANAGER, mgr);
                // Stop once one can be setup without error
                return;
            } catch (Throwable e) {     // NOSONAR - also catch things like NoClassDefError here
                // throttle the log somewhat as it can spam the log otherwise
                if (System.currentTimeMillis() > lastLog + TimeUnit.MINUTES.toMillis(5)) {
                    logger.warn("SAX Security Manager could not be setup [log suppressed for 5 minutes]", e);
                    lastLog = System.currentTimeMillis();
                }
            }
        }

        // separate old version of Xerces not found => use the builtin way of setting the property
        try {
            xmlReader.setProperty(XMLBeansConstants.ENTITY_EXPANSION_LIMIT, options.getEntityExpansionLimit());
        } catch (SAXException e) {     // NOSONAR - also catch things like NoClassDefError here
            // throttle the log somewhat as it can spam the log otherwise
            if (System.currentTimeMillis() > lastLog + TimeUnit.MINUTES.toMillis(5)) {
                logger.warn("SAX Security Manager could not be setup [log suppressed for 5 minutes]", e);
                lastLog = System.currentTimeMillis();
            }
        }
    }
}

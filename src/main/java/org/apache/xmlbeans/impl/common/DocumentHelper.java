/*   Copyright 2004-2018 The Apache Software Foundation
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public final class DocumentHelper {
    private static final Logger LOG = LogManager.getLogger(DocumentHelper.class);
    private static long lastLog;

    private DocumentHelper() {}

    private static class DocHelperErrorHandler implements ErrorHandler {

        public void warning(SAXParseException exception) throws SAXException {
            LOG.atWarn().withThrowable(exception).log(asString(exception));
        }

        public void error(SAXParseException exception) throws SAXException {
            LOG.atError().withThrowable(exception).log(asString(exception));
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            LOG.atFatal().withThrowable(exception).log(asString(exception));
            throw exception;
        }

        private String asString(SAXParseException ex) {
            StringBuilder sb = new StringBuilder();

            String systemId = ex.getSystemId();
            if (systemId != null) {
                int index = systemId.lastIndexOf('/');
                if (index != -1)
                    systemId = systemId.substring(index + 1);
                sb.append(systemId);
            }
            sb.append(':');
            sb.append(ex.getLineNumber());
            sb.append(':');
            sb.append(ex.getColumnNumber());
            sb.append(": ");
            sb.append(ex.getMessage());

            return sb.toString();
        }
    }

    /**
     * Creates a new document builder, with sensible defaults
     *
     * @param xmlOptions the factory option
     * @throws IllegalStateException If creating the DocumentBuilder fails, e.g.
     *  due to {@link ParserConfigurationException}.
     */
    public static DocumentBuilder newDocumentBuilder(XmlOptions xmlOptions) {
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory(xmlOptions).newDocumentBuilder();
            documentBuilder.setEntityResolver(SAXHelper.IGNORING_ENTITY_RESOLVER);
            documentBuilder.setErrorHandler(new DocHelperErrorHandler());
            return documentBuilder;
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("cannot create a DocumentBuilder", e);
        }
    }

    private static DocumentBuilderFactory documentBuilderFactory(XmlOptions options) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setValidating(false);
        trySetFeature(documentBuilderFactory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        trySetFeature(documentBuilderFactory, XMLBeansConstants.FEATURE_LOAD_DTD_GRAMMAR, options.isLoadDTDGrammar());
        trySetFeature(documentBuilderFactory, XMLBeansConstants.FEATURE_LOAD_EXTERNAL_DTD, options.isLoadExternalDTD());
        trySetFeature(documentBuilderFactory, XMLBeansConstants.FEATURE_DISALLOW_DOCTYPE_DECL, options.disallowDocTypeDeclaration());
        trySetXercesSecurityManager(documentBuilderFactory, options);
        return documentBuilderFactory;
    }

    private static void trySetFeature(DocumentBuilderFactory dbf, String feature, boolean enabled) {
        try {
            dbf.setFeature(feature, enabled);
        } catch (Exception e) {
            LOG.atWarn().withThrowable(e).log("SAX Feature unsupported: {}", feature);
        } catch (AbstractMethodError ame) {
            LOG.atWarn().withThrowable(ame).log("Cannot set SAX feature {} because of outdated XML parser in classpath", feature);
        }
    }

    private static void trySetXercesSecurityManager(DocumentBuilderFactory dbf, XmlOptions options) {
        // Try built-in JVM one first, standalone if not
        for (String securityManagerClassName : new String[]{
                //"com.sun.org.apache.xerces.internal.util.SecurityManager",
                "org.apache.xerces.util.SecurityManager"
        }) {
            try {
                Object mgr = Class.forName(securityManagerClassName).getDeclaredConstructor().newInstance();
                Method setLimit = mgr.getClass().getMethod("setEntityExpansionLimit", Integer.TYPE);
                setLimit.invoke(mgr, options.getEntityExpansionLimit());
                dbf.setAttribute(XMLBeansConstants.SECURITY_MANAGER, mgr);
                // Stop once one can be setup without error
                return;
            } catch (ClassNotFoundException e) {
                // continue without log, this is expected in some setups
            } catch (Throwable e) {     // NOSONAR - also catch things like NoClassDefError here
                if(System.currentTimeMillis() > lastLog + TimeUnit.MINUTES.toMillis(5)) {
                    LOG.atWarn().withThrowable(e).log("DocumentBuilderFactory Security Manager could not be setup [log suppressed for 5 minutes]");
                    lastLog = System.currentTimeMillis();
                }
            }
        }

        // separate old version of Xerces not found => use the builtin way of setting the property
        try {
            dbf.setAttribute(XMLBeansConstants.ENTITY_EXPANSION_LIMIT, options.getEntityExpansionLimit());
        } catch (Throwable e) {
            if(System.currentTimeMillis() > lastLog + TimeUnit.MINUTES.toMillis(5)) {
                LOG.atWarn().withThrowable(e).log("DocumentBuilderFactory Entity Expansion Limit could not be setup [log suppressed for 5 minutes]");
                lastLog = System.currentTimeMillis();
            }
        }
    }

    /**
     * Parses the given stream via the default (sensible)
     * DocumentBuilder
     * @param inp Stream to read the XML data from
     * @return the parsed Document
     */
    public static Document readDocument(XmlOptions xmlOptions, InputStream inp) throws IOException, SAXException {
        return newDocumentBuilder(xmlOptions).parse(inp);
    }

    /**
     * Parses the given stream via the default (sensible)
     * DocumentBuilder
     * @param inp sax source to read the XML data from
     * @return the parsed Document
     */
    public static Document readDocument(XmlOptions xmlOptions, InputSource inp) throws IOException, SAXException {
        return newDocumentBuilder(xmlOptions).parse(inp);
    }

    // must only be used to create empty documents, do not use it for parsing!
    private static final DocumentBuilder documentBuilderSingleton = newDocumentBuilder(new XmlOptions());

    /**
     * Creates a new DOM Document
     */
    public static Document createDocument() {
        return documentBuilderSingleton.newDocument();
    }
}

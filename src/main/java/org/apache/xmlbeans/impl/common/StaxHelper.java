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

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.apache.xmlbeans.XmlOptionsBean;

/**
 * Provides handy methods for working with StAX parsers and readers
 */
public final class StaxHelper {
    private static final XBLogger logger = XBLogFactory.getLogger(StaxHelper.class);

    private StaxHelper() {}

    /**
     * Creates a new StAX XMLInputFactory, with sensible defaults
     */
    public static XMLInputFactory newXMLInputFactory(XmlOptionsBean options) {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        trySetProperty(factory, XMLInputFactory.IS_NAMESPACE_AWARE, true);
        trySetProperty(factory, XMLInputFactory.IS_VALIDATING, false);
        trySetProperty(factory, XMLInputFactory.SUPPORT_DTD, options.isLoadDTDGrammar());
        trySetProperty(factory, XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, options.isLoadExternalDTD());
        return factory;
    }

    /**
     * Creates a new StAX XMLOutputFactory, with sensible defaults
     */
    public static XMLOutputFactory newXMLOutputFactory(XmlOptionsBean options) {
        XMLOutputFactory factory = XMLOutputFactory.newFactory();
        trySetProperty(factory, XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
        return factory;
    }

    /**
     * Creates a new StAX XMLEventFactory, with sensible defaults
     */
    public static XMLEventFactory newXMLEventFactory(XmlOptionsBean options) {
        return XMLEventFactory.newFactory();
    }
            
    private static void trySetProperty(XMLInputFactory factory, String feature, boolean flag) {
        try {
            factory.setProperty(feature, flag);
        } catch (Exception e) {
            logger.log(XBLogger.WARN, "StAX Property unsupported", feature, e);
        } catch (AbstractMethodError ame) {
            logger.log(XBLogger.WARN, "Cannot set StAX property because outdated StAX parser in classpath", feature, ame);
        }
    }

    private static void trySetProperty(XMLOutputFactory factory, String feature, boolean flag) {
        try {
            factory.setProperty(feature, flag);
        } catch (Exception e) {
            logger.log(XBLogger.WARN, "StAX Property unsupported", feature, e);
        } catch (AbstractMethodError ame) {
            logger.log(XBLogger.WARN, "Cannot set StAX property because outdated StAX parser in classpath", feature, ame);
        }
    }
}

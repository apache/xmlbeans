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

package org.apache.xmlbeans.impl.schema;

import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Node;

import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

@SuppressWarnings("unchecked")
public class AbstractDocumentFactory<T> extends ElementFactory<T> {
    public AbstractDocumentFactory(SchemaTypeSystem typeSystem, String typeHandle) {
        super(typeSystem, typeHandle);
    }

    /**
     * @param xmlAsString the string value to parse
     */
    public T parse(String xmlAsString) throws XmlException {
        return (T) getTypeLoader().parse(xmlAsString, getType(), null);
    }

    public T parse(String xmlAsString, XmlOptions options) throws XmlException {
        return (T) getTypeLoader().parse(xmlAsString, getType(), options);
    }

    /**
     * @param file the file from which to load an xml document
     */
    public T parse(File file) throws XmlException, IOException {
        return (T) getTypeLoader().parse(file, getType(), null);
    }

    public T parse(File file, XmlOptions options) throws XmlException, IOException {
        return (T) getTypeLoader().parse(file, getType(), options);
    }

    public T parse(URL u) throws XmlException, IOException {
        return (T) getTypeLoader().parse(u, getType(), null);
    }

    public T parse(URL u, XmlOptions options) throws XmlException, IOException {
        return (T) getTypeLoader().parse(u, getType(), options);
    }

    public T parse(InputStream is) throws XmlException, IOException {
        return (T) getTypeLoader().parse(is, getType(), null);
    }

    public T parse(InputStream is, XmlOptions options) throws XmlException, IOException {
        return (T) getTypeLoader().parse(is, getType(), options);
    }

    public T parse(Reader r) throws XmlException, IOException {
        return (T) getTypeLoader().parse(r, getType(), null);
    }

    public T parse(Reader r, XmlOptions options) throws XmlException, IOException {
        return (T) getTypeLoader().parse(r, getType(), options);
    }

    public T parse(XMLStreamReader sr) throws XmlException {
        return (T) getTypeLoader().parse(sr, getType(), null);
    }

    public T parse(XMLStreamReader sr, XmlOptions options) throws XmlException {
        return (T) getTypeLoader().parse(sr, getType(), options);
    }

    public T parse(Node node) throws XmlException {
        return (T) getTypeLoader().parse(node, getType(), null);
    }

    public T parse(Node node, XmlOptions options) throws XmlException {
        return (T) getTypeLoader().parse(node, getType(), options);
    }
}

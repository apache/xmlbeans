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

package org.apache.xmlbeans.impl.jaxb.runtime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.io.Reader;
import java.net.URL;

import javax.xml.transform.Source;

import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.impl.values.TypeStoreFactory;

public class UnmarshallerImpl implements Unmarshaller
{
    private final SchemaTypeLoader _stl;

    boolean _validating = false;
    ValidationEventHandler _handler = new DefaultValidationEventHandler();
    private XmlOptions _options;

    UnmarshallerImpl(SchemaTypeLoader stl, ContextImpl context)
    {
        assert stl != null;
        _stl = stl;

        _options = new XmlOptions();
        _options.put(TypeStoreFactory.KEY,  context);
    }

    public void setValidating(boolean validating)
    {
        _validating = validating;
    }

    public boolean isValidating()
    {
        return _validating;
    }

    public void setEventHandler(ValidationEventHandler handler) 
    {
        _handler = handler;
    }

    public ValidationEventHandler getEventHandler() 
    {
        return _handler;
    }

    public void setProperty(String prop, Object value) throws PropertyException
    {
        throw new PropertyException(prop,  value);
    }

    public Object getProperty(String prop) throws PropertyException
    {
        throw new PropertyException(prop);
    }

    public Object unmarshal(File f)
        throws JAXBException
    {
        XmlObject o;
        try {
            o = _stl.parse(f,  null,  _options);
        }
        catch (IOException e) {
            throw new JAXBException(e);
        }
        catch (XmlException e) {
            throw new JAXBException(e);
        }

        o = postProcess(o);
        return o;
    }

    public Object unmarshal(URL url)
        throws JAXBException
    {
        throw new UnsupportedOperationException("NYI");
    }

    public Object unmarshal(InputStream is)
        throws JAXBException
    {
        XmlObject o;
        try {
            o = _stl.parse(is,  null,  _options);
        }
        catch (IOException e) {
            throw new JAXBException(e);
        }
        catch (XmlException e) {
            throw new JAXBException(e);
        }

        o = postProcess(o);
        return o;
    }

    public Object unmarshal(Reader r)
        throws JAXBException
    {
        XmlObject o;
        try {
            o = _stl.parse(r,  null,  _options);
        }
        catch (IOException e) {
            throw new JAXBException(e);
        }
        catch (XmlException e) {
            throw new JAXBException(e);
        }

        o = postProcess(o);
        return o;
    }

    public Object unmarshal(InputSource s)
        throws JAXBException
    {
        if (s.getByteStream() != null)
            return unmarshal(s.getByteStream());
        else if (s.getCharacterStream() != null)
            return unmarshal(s.getCharacterStream());
        else
            throw new JAXBException("Invalid input source.");
    }

    public Object unmarshal(Node node)
        throws JAXBException
    {
        XmlObject o;
        try {
            o = _stl.parse(node,  null,  _options);
        }
        catch (XmlException e) {
            throw new JAXBException(e);
        }

        o = postProcess(o);
        return o;

    }

    public Object unmarshal(Source source)
        throws JAXBException
    {
        throw new UnsupportedOperationException("NYI");
    }

    XmlObject postProcess(XmlObject o) throws UnmarshalException
    {
        if (_validating)
        {
            try {
                ValidatorImpl.validateImpl(o, _handler);
            }
            catch (ValidationException e) {
                throw new UnmarshalException(e);
            }

        }

        XmlCursor c = o.newCursor();
        if (c.toFirstChild())
            return c.getObject();
        else
            return null;
    }

    // similar to org.apache.xmlbeans.XmlSaxHandler
    public UnmarshallerHandler getUnmarshallerHandler()
    {
        throw new UnsupportedOperationException("NYI");
    }

}

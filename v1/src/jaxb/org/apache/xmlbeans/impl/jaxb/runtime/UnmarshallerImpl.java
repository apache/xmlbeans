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

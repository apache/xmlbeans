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

import java.util.Map;
import java.util.HashMap;
import java.io.OutputStream;
import java.io.Writer;
import java.io.IOException;

import javax.xml.bind.Marshaller;
import javax.xml.bind.MarshalException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.PropertyException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.helpers.AbstractMarshallerImpl;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

public class MarshallerImpl implements Marshaller
{
    private ValidationEventHandler _handler = new DefaultValidationEventHandler();
    private XmlOptions _options = new XmlOptions().setSaveOuter();


    static Map JAXB_TO_XBEAN_PROPERTY_MAP;
    static {
        JAXB_TO_XBEAN_PROPERTY_MAP = new HashMap();
        JAXB_TO_XBEAN_PROPERTY_MAP.put(JAXB_ENCODING, XmlOptions.CHARACTER_ENCODING);
        JAXB_TO_XBEAN_PROPERTY_MAP.put(JAXB_FORMATTED_OUTPUT, XmlOptions.SAVE_PRETTY_PRINT);
    }

    static String getXbeanPropForJaxbProp(String jaxbName) {
        return (String)JAXB_TO_XBEAN_PROPERTY_MAP.get(jaxbName);
    }

    public void setProperty(String prop, Object value)
        throws PropertyException
    {
        String xbeanProp = getXbeanPropForJaxbProp(prop);
        if (xbeanProp == null)
            throw new PropertyException(prop + " is unsupported.");

        _options.put(xbeanProp, value);

    }
    public Object getProperty(String prop)
        throws PropertyException
    {
        String xbeanProp = getXbeanPropForJaxbProp(prop);
        if (xbeanProp == null)
            throw new PropertyException(prop + " is unsupported.");

        return _options.get(xbeanProp);
    }

    public javax.xml.bind.ValidationEventHandler getEventHandler()
    {
        return _handler;
    }

    public void setEventHandler(ValidationEventHandler handler)
    {
        _handler = handler;
    }

    void marshal(Object obj, ContentHandler ch, LexicalHandler lh)
        throws MarshalException, JAXBException
    {
        XmlObject xmlobj = getXmlObject(obj);

        try {
            xmlobj.save(ch,  lh, _options);
        }
        catch (SAXException e) {
            throw new JAXBException(e);
        }
    }

    public void marshal(Object obj, ContentHandler ch)
        throws MarshalException, JAXBException
    {
        marshal(obj, ch, null);
    }

    public void marshal(Object obj, Node node)
        throws MarshalException, JAXBException
    {
        throw new UnsupportedOperationException();
    }

    public void marshal(Object obj, OutputStream out)
        throws MarshalException, JAXBException
    {
        XmlObject xmlobj = getXmlObject(obj);

        try {
            xmlobj.save(out, _options);
        }
        catch (IOException e) {
            throw new JAXBException(e);
        }
    }

    public void marshal(Object obj,  Writer out)
        throws MarshalException, JAXBException
    {
        XmlObject xmlobj = getXmlObject(obj);

        try {
            xmlobj.save(out,  _options);
        }
        catch (IOException e) {
            throw new JAXBException(e);
        }
    }

    public void marshal(Object obj, javax.xml.transform.Result result)
        throws MarshalException, JAXBException
    {
        if (result instanceof SAXResult)
        {
            SAXResult saxresult = (SAXResult)result;
            marshal(obj, saxresult.getHandler(), saxresult.getLexicalHandler());
        }
        else if (result instanceof DOMResult)
        {
            DOMResult domresult = (DOMResult)result;
            marshal(obj, domresult.getNode());
        }
        else if (result instanceof StreamResult)
        {
            StreamResult sr = (StreamResult)result;
            OutputStream os = sr.getOutputStream();
            Writer w = sr.getWriter();

            if (os != null)
                marshal(obj, os);
            else if (w != null)
                marshal(obj, w);
            else 
                throw new MarshalException("invalid Result parameter");
        }
        else
            throw new MarshalException("invalid Result parameter");
    }

    public org.w3c.dom.Node getNode(Object o)
    {
        throw new UnsupportedOperationException();
    }

    static XmlObject getXmlObject(Object obj) throws MarshalException {
        if (! ( obj instanceof XmlObject ) )
            throw new MarshalException("Cannot marshal object. It was not created by this JAXB provider.");

        return (XmlObject)obj;
    }
}

/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights 
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

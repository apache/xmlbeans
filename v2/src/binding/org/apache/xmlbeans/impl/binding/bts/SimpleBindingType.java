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
* originally based on software copyright (c) 2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/
package org.apache.xmlbeans.impl.binding.bts;

import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.common.XmlWhitespace;

/**
 * A binding of a simple user-defined type that operates by
 * delegating to another well-known (e.g., builtin) binding.
 */ 
public class SimpleBindingType extends BindingType
{
    private XmlTypeName asIfXmlType;
    private int whitespace = XmlWhitespace.WS_UNSPECIFIED;


    public SimpleBindingType(BindingTypeName btName)
    {
        super(btName);
    }

    public SimpleBindingType(org.apache.xml.xmlbeans.bindingConfig.BindingType node)
    {
        super(node);
        org.apache.xml.xmlbeans.bindingConfig.SimpleType stNode = (org.apache.xml.xmlbeans.bindingConfig.SimpleType)node;
        org.apache.xml.xmlbeans.bindingConfig.AsXmlType as_xml = stNode.getAsXml();
        asIfXmlType = XmlTypeName.forString(as_xml.getStringValue());

        if (as_xml.isSetWhitespace()) {
            org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.Enum ws =
                as_xml.getWhitespace();
            if (ws.equals(org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.PRESERVE)) {
                whitespace = XmlWhitespace.WS_PRESERVE;
            } else if (ws.equals(org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.REPLACE)) {
                whitespace = XmlWhitespace.WS_REPLACE;
            } else if (ws.equals(org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.COLLAPSE)) {
                whitespace = XmlWhitespace.WS_COLLAPSE;
            } else {
                throw new AssertionError("invalid whitespace: " + ws);
            }

        }
    }

    protected org.apache.xml.xmlbeans.bindingConfig.BindingType write(org.apache.xml.xmlbeans.bindingConfig.BindingType node)
    {
        org.apache.xml.xmlbeans.bindingConfig.SimpleType stNode = (org.apache.xml.xmlbeans.bindingConfig.SimpleType)super.write(node);

        org.apache.xml.xmlbeans.bindingConfig.AsXmlType as_if = stNode.addNewAsXml();
        as_if.setStringValue(asIfXmlType.toString());

        switch (whitespace) {
            case XmlWhitespace.WS_UNSPECIFIED:
                break;
            case XmlWhitespace.WS_PRESERVE:
                as_if.setWhitespace(org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.PRESERVE);
                break;
            case XmlWhitespace.WS_REPLACE:
                as_if.setWhitespace(org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.REPLACE);
                break;
            case XmlWhitespace.WS_COLLAPSE:
                as_if.setWhitespace(org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.COLLAPSE);
                break;
            default:
                throw new AssertionError("invalid whitespace: " + whitespace);
        }


        stNode.setAsXml(as_if);
        return stNode;
    }


    // typically the "as if" type is the closest base builtin type.
    public XmlTypeName getAsIfXmlType()
    {
        return asIfXmlType;
    }

    public void setAsIfXmlType(XmlTypeName asIfXmlType)
    {
        this.asIfXmlType = asIfXmlType;
    }
    
    // question: do we want an "as if Java type" as well?
    
    public BindingTypeName getAsIfBindingTypeName()
    {
        return BindingTypeName.forPair(getName().getJavaName(), asIfXmlType);
    }


    /**
     * Gets whitespace facet -- use the constants from
     * org.apache.xmlbeans.impl.common.XmlWhitespace
     *
     * @return whitespace constant from XmlWhitespace
     */
    public int getWhitespace()
    {
        return whitespace;
    }

    /**
     * Sets whitespace facet -- use the constants from
     * org.apache.xmlbeans.impl.common.XmlWhitespace
     *
     * @param ws  whitespace constant from XmlWhitespace
     */
    public void setWhitespace(int ws)
    {
        switch (ws) {
            case XmlWhitespace.WS_UNSPECIFIED:
            case XmlWhitespace.WS_PRESERVE:
            case XmlWhitespace.WS_REPLACE:
            case XmlWhitespace.WS_COLLAPSE:
                whitespace = ws;
                break;
            default:
                throw new IllegalArgumentException("invalid whitespace: " + ws);
        }
    }
}

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

import org.apache.xmlbeans.XmlOptions;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.io.IOException;


/**
 * Represents a BindingLoader whose contents are loaded from a
 * single binding-config file. (See binding-config.xsd) 
 */
public class BindingFile extends BaseBindingLoader
{

    /**
     * This constructor is used when making a new one out of the blue.
     */ 
    public BindingFile()
    {
        // nothing to do - all maps are empty
    }
    
    /**
     * Loader
     */ 
    public static BindingFile forDoc(org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument doc)
    {
        return new BindingFile(doc);
    }
    
    /**
     * This constructor loads an instance from an XML file
     */ 
    protected BindingFile(org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument doc)
    {
        List errors = new ArrayList();
        if (!doc.validate(new XmlOptions().setErrorListener(errors)))
            throw new IllegalArgumentException(errors.size() > 0 ? errors.get(0).toString() : "Invalid binding-config document");
        
        // todo: in the loops below, validate that entries are unique, or modify schema to do so.
        
        org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType[] btNodes =
                doc.getBindingConfig().getBindings().getBindingTypeArray();
        for (int i = 0; i < btNodes.length; i++)
        {
            BindingType next = BindingType.loadFromBindingTypeNode(this, btNodes[i]);
            addBindingType(next, false, false);
        }
        org.apache.xmlbeans.x2003.x09.bindingConfig.Mapping[] mNodes =
                doc.getBindingConfig().getJavaToXml().getMappingArray();
        for (int i = 0; i < mNodes.length; i++)
        {
            JavaName jName = JavaName.forString(mNodes[i].getJavatype());
            XmlName xName = XmlName.forString(mNodes[i].getXmlcomponent());
            xmlFromJava.put(jName, pair(jName, xName));
        }
        
        mNodes = doc.getBindingConfig().getXmlToPojo().getMappingArray();
        for (int i = 0; i < mNodes.length; i++)
        {
            JavaName jName = JavaName.forString(mNodes[i].getJavatype());
            XmlName xName = XmlName.forString(mNodes[i].getXmlcomponent());
            javaFromXmlPojo.put(xName, pair(jName, xName));
        }

        mNodes = doc.getBindingConfig().getXmlToXmlobj().getMappingArray();
        for (int i = 0; i < mNodes.length; i++)
        {
            JavaName jName = JavaName.forString(mNodes[i].getJavatype());
            XmlName xName = XmlName.forString(mNodes[i].getXmlcomponent());
            javaFromXmlObj.put(xName, pair(jName, xName));
        }
    }
    
    /**
     * Writes out to XML
     */ 
    public org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument write() throws IOException
    {
        org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument doc =
                org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument.Factory.newInstance();
        write(doc);
        return doc;
    }
    
    /**
     * This function copies an instance into an empty doc.
     */ 
    private void write(org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument doc)
    {
        if (doc.getBindingConfig() != null)
            throw new IllegalArgumentException("Can only write into empty doc");
        org.apache.xmlbeans.x2003.x09.bindingConfig.BindingConfigDocument.BindingConfig bcNode = doc.addNewBindingConfig();
        
        // make tables
        org.apache.xmlbeans.x2003.x09.bindingConfig.BindingTable btabNode = bcNode.addNewBindings();
        org.apache.xmlbeans.x2003.x09.bindingConfig.MappingTable jtabNode = bcNode.addNewJavaToXml();
        org.apache.xmlbeans.x2003.x09.bindingConfig.MappingTable pojotabNode = bcNode.addNewXmlToPojo();
        org.apache.xmlbeans.x2003.x09.bindingConfig.MappingTable xotabNode = bcNode.addNewXmlToXmlobj();
        
        // fill em in: binding types (delegate to BindingType.write)
        for (Iterator i = bindingTypes.values().iterator(); i.hasNext(); )
        {
            BindingType bType = (BindingType)i.next();
            org.apache.xmlbeans.x2003.x09.bindingConfig.BindingType btNode = btabNode.addNewBindingType();
            bType.write(btNode);
        }

        // from-java mappings
        for (Iterator i = xmlFromJava.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry entry = (Map.Entry)i.next();
            JavaName jName = (JavaName)entry.getKey();
            XmlName xName = (XmlName)entry.getValue();
            org.apache.xmlbeans.x2003.x09.bindingConfig.Mapping mNode = jtabNode.addNewMapping();
            mNode.setJavatype(jName.toString());
            mNode.setXmlcomponent(xName.toString());
        }
            
        // to-pojo
        for (Iterator i = javaFromXmlPojo.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry entry = (Map.Entry)i.next();
            JavaName jName = (JavaName)entry.getValue();
            XmlName xName = (XmlName)entry.getKey();
            org.apache.xmlbeans.x2003.x09.bindingConfig.Mapping mNode = pojotabNode.addNewMapping();
            mNode.setJavatype(jName.toString());
            mNode.setXmlcomponent(xName.toString());
        }

        // to-xmlobj
        for (Iterator i = javaFromXmlObj.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry entry = (Map.Entry)i.next();
            JavaName jName = (JavaName)entry.getValue();
            XmlName xName = (XmlName)entry.getKey();
            org.apache.xmlbeans.x2003.x09.bindingConfig.Mapping mNode = xotabNode.addNewMapping();
            mNode.setJavatype(jName.toString());
            mNode.setXmlcomponent(xName.toString());
        }
    }

    public void addBindingType(BindingType bType, boolean fromJavaDefault, boolean fromXmlDefault)
    {
        bindingTypes.put(BaseBindingLoader.pair(bType.getJavaName(), bType.getXmlName()), bType);
        if (fromJavaDefault)
        {
            if (bType.isXmlObject())
                javaFromXmlObj.put(bType.getXmlName(), bType.getJavaName());
            else
                javaFromXmlPojo.put(bType.getXmlName(), bType.getJavaName());
        }
        if (fromXmlDefault)
        {
            xmlFromJava.put(bType.getJavaName(), bType.getXmlName());
        }
    }

  public Collection getBindingTypes()
  {
    return Collections.unmodifiableCollection(bindingTypes.values());
  }


}

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

package org.apache.xmlbeans.impl.binding.bts;

import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.io.IOException;


/**
 * Represents a BindingLoader whose contents are loaded from a
 * single binding-config file. (See binding-config.xsd)
 */
public class BindingFile extends BaseBindingLoader
{

    // ========================================================================
    // Factory

    /**
     * Loader
     */
    public static BindingFile forDoc(org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument doc)
    {
        return new BindingFile(doc);
    }

    // ========================================================================
    // Constructors

    /**
     * This constructor is used when making a new one out of the blue.
     */
    public BindingFile()
    {
        // nothing to do - all maps are empty
    }

    /**
     * This constructor loads an instance from an XML file
     */
    protected BindingFile(org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument doc)
    {
        validateDoc(doc);

        // todo: in the loops below, validate that entries are unique, or modify schema to do so.

        org.apache.xml.xmlbeans.bindingConfig.BindingType[] btNodes =
            doc.getBindingConfig().getBindings().getBindingTypeArray();
        for (int i = 0; i < btNodes.length; i++) {
            BindingType next = BindingType.loadFromBindingTypeNode(btNodes[i]);
            addBindingType(next, false, false);
        }
        org.apache.xml.xmlbeans.bindingConfig.Mapping[] mNodes =
            doc.getBindingConfig().getJavaToXml().getMappingArray();
        for (int i = 0; i < mNodes.length; i++) {
            JavaTypeName jName = JavaTypeName.forString(mNodes[i].getJavatype());
            XmlTypeName xName = XmlTypeName.forString(mNodes[i].getXmlcomponent());
            addTypeFor(jName, BindingTypeName.forPair(jName, xName));
        }

        mNodes = doc.getBindingConfig().getJavaToElement().getMappingArray();
        for (int i = 0; i < mNodes.length; i++) {
            JavaTypeName jName = JavaTypeName.forString(mNodes[i].getJavatype());
            XmlTypeName xName = XmlTypeName.forString(mNodes[i].getXmlcomponent());
            addElementFor(jName, BindingTypeName.forPair(jName, xName));
        }

        mNodes = doc.getBindingConfig().getXmlToPojo().getMappingArray();
        for (int i = 0; i < mNodes.length; i++) {
            JavaTypeName jName = JavaTypeName.forString(mNodes[i].getJavatype());
            XmlTypeName xName = XmlTypeName.forString(mNodes[i].getXmlcomponent());
            addPojoFor(xName, BindingTypeName.forPair(jName, xName));
        }

        mNodes = doc.getBindingConfig().getXmlToXmlobj().getMappingArray();
        for (int i = 0; i < mNodes.length; i++) {
            JavaTypeName jName = JavaTypeName.forString(mNodes[i].getJavatype());
            XmlTypeName xName = XmlTypeName.forString(mNodes[i].getXmlcomponent());
            addXmlObjectFor(xName, BindingTypeName.forPair(jName, xName));
        }
    }

    // ========================================================================
    // Public methods

    /**
     * Writes out to XML
     */
    public org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument write()
        throws IOException
    {
        // Here we should use the BindingConfigDocument classloader
        // rather than the thread context classloader.  This is
        // because in some situations (such as when being invoked by ant)
        // the context classloader is potentially weird (because
        // of the design of ant).

        SchemaTypeLoader loader = XmlBeans.typeLoaderForClassLoader(
            org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument.class.getClassLoader());

        org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument doc =
            (org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument)
            loader.newInstance(org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument.type, null);

        write(doc);
        //validateDoc(doc);
        return doc;
    }

    private void validateDoc(org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument doc)
    {
        List errors = new ArrayList();
        if (!doc.validate(new XmlOptions().setErrorListener(errors)))
            throw new IllegalStateException(errors.size() > 0 ? errors.get(0).toString() : "Invalid binding-config document");
    }


    public void addBindingType(BindingType bType,
                               boolean fromJavaDefault,
                               boolean fromXmlDefault)
    {
        addBindingType(bType);
        if (fromXmlDefault) {
            if (bType.getName().getJavaName().isXmlObject())
                addXmlObjectFor(bType.getName().getXmlName(), bType.getName());
            else
                addPojoFor(bType.getName().getXmlName(), bType.getName());
        }
        if (fromJavaDefault) {
            if (bType.getName().getXmlName().getComponentType() == XmlTypeName.ELEMENT)
                addElementFor(bType.getName().getJavaName(), bType.getName());
            else
                addTypeFor(bType.getName().getJavaName(), bType.getName());
        }
    }

    // ========================================================================
    // Private methods

    /**
     * This function copies an instance into an empty doc.
     */
    private void write(org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument doc)
    {
        if (doc.getBindingConfig() != null)
            throw new IllegalArgumentException("Can only write into empty doc");
        org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument.BindingConfig
            bcNode = doc.addNewBindingConfig();

        // make tables
        org.apache.xml.xmlbeans.bindingConfig.BindingTable btabNode = bcNode.addNewBindings();
        org.apache.xml.xmlbeans.bindingConfig.MappingTable typetabNode = bcNode.addNewJavaToXml();
        org.apache.xml.xmlbeans.bindingConfig.MappingTable elementtabNode = bcNode.addNewJavaToElement();
        org.apache.xml.xmlbeans.bindingConfig.MappingTable pojotabNode = bcNode.addNewXmlToPojo();
        org.apache.xml.xmlbeans.bindingConfig.MappingTable xotabNode = bcNode.addNewXmlToXmlobj();

        // fill em in: binding types (delegate to BindingType.write)
        for (Iterator i = bindingTypes().iterator(); i.hasNext();) {
            BindingType bType = (BindingType)i.next();
            org.apache.xml.xmlbeans.bindingConfig.BindingType btNode = btabNode.addNewBindingType();
            bType.write(btNode);
        }
        // from-java mappings
        for (Iterator i = typeMappedJavaTypes().iterator(); i.hasNext();) {
            JavaTypeName jName = (JavaTypeName)i.next();
            BindingTypeName pair = lookupTypeFor(jName);
            org.apache.xml.xmlbeans.bindingConfig.Mapping mNode = typetabNode.addNewMapping();
            mNode.setJavatype(jName.toString());
            mNode.setXmlcomponent(pair.getXmlName().toString());
        }
        // from-java mappings
        for (Iterator i = elementMappedJavaTypes().iterator(); i.hasNext();) {
            JavaTypeName jName = (JavaTypeName)i.next();
            BindingTypeName pair = lookupElementFor(jName);
            org.apache.xml.xmlbeans.bindingConfig.Mapping mNode = elementtabNode.addNewMapping();
            mNode.setJavatype(jName.toString());
            mNode.setXmlcomponent(pair.getXmlName().toString());
        }
        // to-pojo
        for (Iterator i = pojoMappedXmlTypes().iterator(); i.hasNext();) {
            XmlTypeName xName = (XmlTypeName)i.next();
            BindingTypeName pair = lookupPojoFor(xName);
            org.apache.xml.xmlbeans.bindingConfig.Mapping mNode = pojotabNode.addNewMapping();
            mNode.setJavatype(pair.getJavaName().toString());
            mNode.setXmlcomponent(xName.toString());
        }
        // to-xmlobj
        for (Iterator i = xmlObjectMappedXmlTypes().iterator(); i.hasNext();) {
            XmlTypeName xName = (XmlTypeName)i.next();
            BindingTypeName pair = lookupXmlObjectFor(xName);
            org.apache.xml.xmlbeans.bindingConfig.Mapping mNode = xotabNode.addNewMapping();
            mNode.setJavatype(pair.getJavaName().toString());
            mNode.setXmlcomponent(xName.toString());
        }
    }
}

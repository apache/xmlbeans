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
package org.apache.xmlbeans.impl.inst2xsd.util;

import org.w3.x2001.xmlSchema.SchemaDocument;
import org.apache.xmlbeans.XmlString;

import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com) Date: Jul 16, 2004
 */
public class TypeSystemHolder
{
    Map _globalElements;   // QName -> Element
    Map _globalAttributes; // QName -> Attribute
    Map _globalTypes;      // QName -> Type

    public TypeSystemHolder()
    {
        _globalElements = new HashMap();
        _globalAttributes = new HashMap();
        _globalTypes = new HashMap();
    }

    public void addGlobalElement(Element element)
    {
        assert element.isGlobal() && !element.isRef();
        _globalElements.put(element.getName(), element);
    }

    public Element getGlobalElement(QName name)
    {
        return (Element)_globalElements.get(name);
    }

    public Element[] getGlobalElements()
    {
        Collection col = _globalElements.values();
        return (Element[])col.toArray(new Element[col.size()]);
    }

    public void addGlobalAttribute(Attribute attribute)
    {
        assert attribute.isGlobal() && !attribute.isRef();
        _globalAttributes.put(attribute.getName(), attribute);
    }

    public Attribute getGlobalAttribute(QName name)
    {
        return (Attribute)_globalAttributes.get(name);
    }

    public Attribute[] getGlobalAttributes()
    {
        Collection col = _globalAttributes.values();
        return (Attribute[])col.toArray(new Attribute[col.size()]);
    }

    public void addGlobalType(Type type)
    {
        assert type.isGlobal() && type.getName()!=null : "type must be a global type before being added.";
        _globalTypes.put(type.getName(), type);
    }

    public Type getGlobalType(QName name)
    {
        return (Type)_globalTypes.get(name);
    }

    public Type[] getGlobalTypes()
    {
        Collection col = _globalTypes.values();
        return (Type[])col.toArray(new Type[col.size()]);
    }

    public SchemaDocument[] getSchemaDocuments()
    {
        // recompute everything, should cache it and track changes
        Map nsToSchemaDocs = new HashMap();

        for (Iterator iterator = _globalElements.keySet().iterator(); iterator.hasNext();)
        {
            QName globalElemName = (QName) iterator.next();
            String tns = globalElemName.getNamespaceURI();
            SchemaDocument schDoc = getSchemaDocumentForTNS(nsToSchemaDocs, tns);

            fillUpGlobalElement((Element)_globalElements.get(globalElemName), schDoc, tns);
        }

        for (Iterator iterator = _globalAttributes.keySet().iterator(); iterator.hasNext();)
        {
            QName globalAttName = (QName) iterator.next();
            String tns = globalAttName.getNamespaceURI();
            SchemaDocument schDoc = getSchemaDocumentForTNS(nsToSchemaDocs, tns);

            fillUpGlobalAttribute((Attribute)_globalAttributes.get(globalAttName), schDoc, tns);
        }

        for (Iterator iterator = _globalTypes.keySet().iterator(); iterator.hasNext();)
        {
            QName globalTypeName = (QName) iterator.next();
            String tns = globalTypeName.getNamespaceURI();
            SchemaDocument schDoc = getSchemaDocumentForTNS(nsToSchemaDocs, tns);

            fillUpGlobalType((Type)_globalTypes.get(globalTypeName), schDoc, tns);
        }

        Collection schDocColl = nsToSchemaDocs.values();
        return (SchemaDocument[])schDocColl.toArray(new SchemaDocument[schDocColl.size()]);
    }

    private static SchemaDocument getSchemaDocumentForTNS(Map nsToSchemaDocs, String tns)
    {
        SchemaDocument schDoc = (SchemaDocument)nsToSchemaDocs.get(tns);
        if (schDoc==null)
        {
            schDoc = SchemaDocument.Factory.newInstance();
            nsToSchemaDocs.put(tns, schDoc);
        }
        return schDoc;
    }

    private static org.w3.x2001.xmlSchema.SchemaDocument.Schema getTopLevelSchemaElement(SchemaDocument schDoc, 
        String tns)
    {
        org.w3.x2001.xmlSchema.SchemaDocument.Schema sch = schDoc.getSchema();
        if (sch==null)
        {
            sch = schDoc.addNewSchema();
            sch.setAttributeFormDefault(org.w3.x2001.xmlSchema.FormChoice.Enum.forString("unqualified"));
            sch.setElementFormDefault(org.w3.x2001.xmlSchema.FormChoice.Enum.forString("qualified"));
            if (!tns.equals(""))
                sch.setTargetNamespace(tns);
        }
        return sch;
    }

    // Global Elements
    private void fillUpGlobalElement(Element globalElement, SchemaDocument schDoc, String tns)
    {
        assert tns.equals(globalElement.getName().getNamespaceURI());

        org.w3.x2001.xmlSchema.SchemaDocument.Schema sch = getTopLevelSchemaElement(schDoc, tns);

        org.w3.x2001.xmlSchema.TopLevelElement topLevelElem = sch.addNewElement();
        topLevelElem.setName(globalElement.getName().getLocalPart());

        fillUpElementDocumentation(topLevelElem, globalElement.getComment());

        Type elemType = globalElement.getType();
        fillUpTypeOnElement(elemType, topLevelElem, tns);
    }

    private void fillUpLocalElement(Element element, org.w3.x2001.xmlSchema.LocalElement localSElement, String tns)
    {
        fillUpElementDocumentation(localSElement, element.getComment());
        if (!element.isRef())
        {
            assert element.getName().getNamespaceURI().equals(tns);
            fillUpTypeOnElement(element.getType(), localSElement, tns);
            localSElement.setName(element.getName().getLocalPart());
        }
        else
        {
            localSElement.setRef(element.getName());
        }

        if (element.getMaxOccurs()==Element.UNBOUNDED)
        {
            localSElement.setMaxOccurs("unbounded");
        }
        if (element.getMinOccurs()!=1)
        {
            localSElement.setMinOccurs(new BigInteger("" + element.getMinOccurs()));
        }
    }

    private void fillUpTypeOnElement(Type elemType, org.w3.x2001.xmlSchema.Element parentSElement, String tns)
    {
        if (elemType.isGlobal())
        {
            assert elemType.getName()!=null : "Global type must have a name.";
            parentSElement.setType(elemType.getName());
        }
        else if (elemType.getContentType()==Type.SIMPLE_TYPE_SIMPLE_CONTENT)
        {
            if (elemType.isEnumeration())
                fillUpEnumeration(elemType, parentSElement);
            else
                parentSElement.setType(elemType.getName());
        }
        else
        {
            org.w3.x2001.xmlSchema.LocalComplexType localComplexType = parentSElement.addNewComplexType();
            fillUpContentForComplexType(elemType, localComplexType, tns);
        }
    }

    private void fillUpEnumeration(Type type, org.w3.x2001.xmlSchema.Element parentSElement)
    {
        assert type.isEnumeration() && !type.isComplexType() : "Enumerations must be on simple types only.";
        org.w3.x2001.xmlSchema.RestrictionDocument.Restriction restriction = parentSElement.addNewSimpleType().addNewRestriction();
        restriction.setBase(type.getName());
        for (int i = 0; i < type.getEnumerationValues().size(); i++)
        {
            String value = (String) type.getEnumerationValues().get(i);
            restriction.addNewEnumeration().setValue(XmlString.Factory.newValue(value));
        }
    }

    private void fillUpAttributesInComplexTypesSimpleContent(Type elemType,
        org.w3.x2001.xmlSchema.SimpleExtensionType sExtension, String tns)
    {
        for (int i = 0; i < elemType.getAttributes().size(); i++)
        {
            Attribute att = (Attribute) elemType.getAttributes().get(i);
            org.w3.x2001.xmlSchema.Attribute sAttribute = sExtension.addNewAttribute();
            fillUpLocalAttribute(att, sAttribute, tns);
        }
    }

    private void fillUpAttributesInComplexTypesComplexContent(Type elemType,
        org.w3.x2001.xmlSchema.ComplexType localSComplexType, String tns)
    {
        for (int i = 0; i < elemType.getAttributes().size(); i++)
        {
            Attribute att = (Attribute) elemType.getAttributes().get(i);
            org.w3.x2001.xmlSchema.Attribute sAttribute = localSComplexType.addNewAttribute();
            fillUpLocalAttribute(att, sAttribute, tns);
        }
    }

    private void fillUpLocalAttribute(Attribute att, org.w3.x2001.xmlSchema.Attribute sAttribute, String tns)
    {
        if (att.isRef())
        {
            sAttribute.setRef(att.getName());
        }
        else
        {
            assert att.getName().getNamespaceURI()==tns || att.getName().getNamespaceURI().equals("");
            sAttribute.setType(att.getType().getName());
            sAttribute.setName(att.getName().getLocalPart());
            if (att.isOptional())
                sAttribute.setUse(org.w3.x2001.xmlSchema.Attribute.Use.OPTIONAL);
        }
    }

    private void fillUpContentForComplexType(Type type, org.w3.x2001.xmlSchema.ComplexType sComplexType, String tns)
    {
        if (type.getContentType()==Type.COMPLEX_TYPE_SIMPLE_CONTENT)
        {
            org.w3.x2001.xmlSchema.SimpleContentDocument.SimpleContent simpleContent = sComplexType.addNewSimpleContent();

            assert type.getExtensionType()!=null && type.getExtensionType().getName()!=null : "Extension type must exist and be named for a COMPLEX_TYPE_SIMPLE_CONTENT";

            org.w3.x2001.xmlSchema.SimpleExtensionType ext = simpleContent.addNewExtension();
            ext.setBase(type.getExtensionType().getName());

            fillUpAttributesInComplexTypesSimpleContent(type, ext, tns);
        }
        else
        {
            if (type.getContentType()==Type.COMPLEX_TYPE_MIXED_CONTENT)
            {
                sComplexType.setMixed(true);
            }

            org.w3.x2001.xmlSchema.ExplicitGroup explicitGroup;
            if (type.getTopParticleForComplexOrMixedContent()==Type.PARTICLE_SEQUENCE)
            {
                explicitGroup = sComplexType.addNewSequence();
            }
            else if (type.getTopParticleForComplexOrMixedContent()==Type.PARTICLE_CHOICE_UNBOUNDED)
            {
                explicitGroup = sComplexType.addNewChoice();
                explicitGroup.setMaxOccurs("unbounded");
                explicitGroup.setMinOccurs(new BigInteger("0"));
            }
            else { throw new IllegalStateException("Unknown particle type in complex and mixed content"); }

            for (int i = 0; i < type.getElements().size(); i++)
            {
                Element child = (Element) type.getElements().get(i);
                org.w3.x2001.xmlSchema.LocalElement childLocalElement = explicitGroup.addNewElement();
                fillUpLocalElement(child, childLocalElement, tns);
            }

            fillUpAttributesInComplexTypesComplexContent(type, sComplexType, tns);
        }
    }

    // Global Attributes
    private void fillUpGlobalAttribute(Attribute globalAttribute, SchemaDocument schDoc, String tns)
    {
        assert tns.equals(globalAttribute.getName().getNamespaceURI());
        org.w3.x2001.xmlSchema.SchemaDocument.Schema sch = getTopLevelSchemaElement(schDoc, tns);

        org.w3.x2001.xmlSchema.TopLevelAttribute topLevelAtt = sch.addNewAttribute();
        topLevelAtt.setName(globalAttribute.getName().getLocalPart());

        Type elemType = globalAttribute.getType();

        if (elemType.getContentType()==Type.SIMPLE_TYPE_SIMPLE_CONTENT)
        {
            topLevelAtt.setType(elemType.getName());
        }
        else
        {
            //org.w3.x2001.xmlSchema.LocalSimpleType localSimpleType = topLevelAtt.addNewSimpleType();
            throw new IllegalStateException();
        }
    }

    private static void fillUpElementDocumentation(org.w3.x2001.xmlSchema.Element element, String comment)
    {
        if (comment!=null && comment.length()>0)
        {
            org.w3.x2001.xmlSchema.DocumentationDocument.Documentation documentation = element.addNewAnnotation().addNewDocumentation();
            documentation.set(org.apache.xmlbeans.XmlString.Factory.newValue(comment));
        }
    }

    // Global Types
    private void fillUpGlobalType(Type globalType, SchemaDocument schDoc, String tns)
    {
        assert tns.equals(globalType.getName().getNamespaceURI());
        org.w3.x2001.xmlSchema.SchemaDocument.Schema sch = getTopLevelSchemaElement(schDoc, tns);

        org.w3.x2001.xmlSchema.TopLevelComplexType topLevelComplexType = sch.addNewComplexType();
        topLevelComplexType.setName(globalType.getName().getLocalPart());

        fillUpContentForComplexType(globalType, topLevelComplexType, tns);
    }

    public String toString()
    {
        return "TypeSystemHolder{" +
            "_globalElements=" + _globalElements +
            "}";
    }
}

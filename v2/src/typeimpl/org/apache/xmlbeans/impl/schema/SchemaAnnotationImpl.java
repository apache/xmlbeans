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

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaAnnotation;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.w3.x2001.xmlSchema.AppinfoDocument;
import org.w3.x2001.xmlSchema.Annotated;
import org.w3.x2001.xmlSchema.AnnotationDocument;
import org.w3.x2001.xmlSchema.DocumentationDocument;
import org.apache.xmlbeans.SchemaComponent;

public class SchemaAnnotationImpl implements SchemaAnnotation
{
    private SchemaContainer _container;
    private AppinfoDocument.Appinfo[] _appInfo;
    private DocumentationDocument.Documentation[] _documentation;
    private Attribute[] _attributes;

    public XmlObject[] getApplicationInformation()
    {   return _appInfo; }

    public XmlObject[] getUserInformation()
    {   return _documentation; }

    public Attribute[] getAttributes()
    {   return _attributes; }

    public int getComponentType()
    {   return ANNOTATION; }

    public SchemaTypeSystem getTypeSystem()
    {   return _container != null ? _container.getTypeSystem() : null; }

    SchemaContainer getContainer()
    {   return _container; }

    public QName getName()
    {   return null; }

    public SchemaComponent.Ref getComponentRef()
    {   return null; }

    public static SchemaAnnotationImpl getAnnotation(SchemaContainer c,
        Annotated elem)
    {
        AnnotationDocument.Annotation ann = elem.getAnnotation();

        return getAnnotation(c, elem, ann);
    }

    public static SchemaAnnotationImpl getAnnotation(SchemaContainer c,
        XmlObject elem, AnnotationDocument.Annotation ann)
    {
        // Check option
        if (StscState.get().noAnn())
            return null;

        SchemaAnnotationImpl result = new SchemaAnnotationImpl(c);
        // Retrieving attributes, first attributes on the enclosing element
        ArrayList attrArray = new ArrayList(2);
        addNoSchemaAttributes(elem, attrArray);
        if (ann == null)
        {
            if (attrArray.size() == 0)
                return null; // no annotation present
            // no annotation element present, but attributes on the enclosing
            // element present, so we have an annotation component
            result._appInfo = new AppinfoDocument.Appinfo[0];
            result._documentation = new DocumentationDocument.Documentation[0];
        }
        else
        {
            result._appInfo = ann.getAppinfoArray();
            result._documentation = ann.getDocumentationArray();
            // Now the attributes on the annotation element
            addNoSchemaAttributes(ann, attrArray);
        }
        
        result._attributes =
            (AttributeImpl[]) attrArray.toArray(new AttributeImpl[attrArray.size()]);
        return result;
    }

    private static void addNoSchemaAttributes(XmlObject elem, List attrList)
    {
        XmlCursor cursor = elem.newCursor();
        boolean hasAttributes = cursor.toFirstAttribute();
        while (hasAttributes)
        {
            QName name = cursor.getName();
            String namespaceURI = name.getNamespaceURI();
            if ("".equals(namespaceURI) ||
                "http://www.w3.org/2001/XMLSchema".equals(namespaceURI))
                ; // no nothing
            else
                attrList.add(new AttributeImpl(name, cursor.getTextValue())); //add the attribute
            hasAttributes = cursor.toNextAttribute();
        }
        cursor.dispose();
    }

    private SchemaAnnotationImpl(SchemaContainer c)
    {
        _container = c;
    }

    /*package*/ SchemaAnnotationImpl(SchemaContainer c,
        AppinfoDocument.Appinfo[] aap,
        DocumentationDocument.Documentation[] adoc,
        Attribute[] aat)
    {
        _container = c;
        _appInfo = aap;
        _documentation = adoc;
        _attributes = aat;
    }

    /*package*/ static class AttributeImpl implements Attribute
    {
        private QName _name;
        private String _value;

        /*package*/ AttributeImpl(QName name, String value)
        {
            _name = name;
            _value = value;
        }

        public QName getName()
        {   return _name; }

        public String getValue()
        {   return _value; }
    }
}

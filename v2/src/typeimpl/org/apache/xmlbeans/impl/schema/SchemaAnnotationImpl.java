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
    private SchemaTypeSystem _typeSystem;
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
    {   return _typeSystem; }

    public QName getName()
    {   return null; }

    public SchemaComponent.Ref getComponentRef()
    {   return null; }

    public static SchemaAnnotationImpl getAnnotation(SchemaTypeSystem ts,
        Annotated elem)
    {
        AnnotationDocument.Annotation ann = elem.getAnnotation();

        return getAnnotation(ts, elem, ann);
    }

    public static SchemaAnnotationImpl getAnnotation(SchemaTypeSystem ts,
        XmlObject elem, AnnotationDocument.Annotation ann)
    {
        // Check option
        if (StscState.get().noAnn())
            return null;

        SchemaAnnotationImpl result = new SchemaAnnotationImpl(ts);
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

    private SchemaAnnotationImpl(SchemaTypeSystem ts)
    {
        _typeSystem = ts;
    }

    /*package*/ SchemaAnnotationImpl(SchemaTypeSystem ts,
        AppinfoDocument.Appinfo[] aap,
        DocumentationDocument.Documentation[] adoc,
        Attribute[] aat)
    {
        _typeSystem = ts;
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

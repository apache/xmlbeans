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

package org.apache.xmlbeans.impl.binding.compile;

import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JProperty;
import org.apache.xmlbeans.impl.binding.bts.*;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlObject;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.math.BigInteger;

public class Both2Bind implements BindingFileResult
{
    public static String TYPE_MATCHER = "TYPE_MATCHER";
    
    private TypeMatcher matcher;
    private BothSourceSet bss;
    private Map scratchFromXmlName = new LinkedHashMap();
    private Map scratchFromJavaName = new LinkedHashMap();
    private Map scratchFromBindingName = new LinkedHashMap();
    private BindingFile bindingFile = new BindingFile();
    private LinkedList resolveQueue = new LinkedList();
    private Collection errors = new ArrayList(); // just strings for now
    private static final XmlOptions empty_opts = new XmlOptions();

    public static BindingFileResult bind(BothSourceSet bss, XmlOptions opts)
    {
        if (opts == null)
            opts = empty_opts;
        Both2Bind binder = new Both2Bind(bss, opts);
        binder.bind();
        return binder;
    }

    private Both2Bind(BothSourceSet bss, XmlOptions opts)
    {
        matcher = buildTypeMatcher((String)opts.get(TYPE_MATCHER));
        this.bss = bss;
    }
    
    private void bind()
    {
        // Let the passed matcher propose any matches it wishes to
        
        resolveInitiallyMatchedTypes();
        
        // consider: when to generate warnings for missing matches?
        
        // Now we recurse through data structures and match up properties,
        // also adding new types to match based on position in props.
        while (moreToResolve()) {
            Scratch scratch = dequeueToResolve();
            resolveBinding(scratch);
        }
        
    }


    private static TypeMatcher buildTypeMatcher(String className)
    {
        if (className == null) {
            return new SimpleTypeMatcher();
        }
        else {
            try {
                return (TypeMatcher)Both2Bind.class.getClassLoader().loadClass(className).newInstance();
            }
            catch (ClassNotFoundException cnfe) {
                throw new AssertionError(cnfe);
            }
            catch (InstantiationException ie) {
                throw new AssertionError(ie);
            }
            catch (IllegalAccessException iae) {
                throw new AssertionError(iae);
            }
        }
    }
    
    /**
     * Scratch area corresponding to a schema type, used for the binding
     * computation.
     */ 
    private static class Scratch
    {
        Scratch(JClass jClass, JavaTypeName javaName, SchemaType schemaType, XmlTypeName xmlName, int category)
        {
            this.jClass = jClass;
            this.javaName = javaName;
            this.schemaType = schemaType;
            this.xmlName = xmlName;
            this.category = category;
            this.bindingTypeName = BindingTypeName.forPair(javaName, xmlName);
        }
        
        private BindingType bindingType;
        private SchemaType schemaType; // may be null
        private JavaTypeName javaName;
        private XmlTypeName xmlName;
        private JClass jClass;
        private BindingTypeName bindingTypeName;
        private TypeMatcher.MatchedProperties onBehalfOf;

        private int category;

        // atomic types get a treatAs
        private XmlTypeName asIf;
        private boolean isStructureResolved;

        // categories of Scratch, established at ctor time
        public static final int ATOMIC_TYPE = 1;
        public static final int STRUCT_TYPE = 2;
        public static final int LITERALARRAY_TYPE = 3;
        public static final int SOAPARRAY_REF = 4;
        public static final int SOAPARRAY = 5;
        public static final int ELEMENT = 6;
        public static final int ATTRIBUTE = 7;

        public int getCategory()
        {
            return category;
        }

        public JClass getJClass()
        {
            return jClass;
        }

        public JavaTypeName getJavaName()
        {
            return javaName;
        }

        public BindingTypeName getBindingTypeName()
        {
            return bindingTypeName;
        }

        public BindingType getBindingType()
        {
            return bindingType;
        }

        public void setBindingType(BindingType bindingType)
        {
            this.bindingType = bindingType;
        }

        public SchemaType getSchemaType()
        {
            return schemaType;
        }

        public XmlTypeName getXmlName()
        {
            return xmlName;
        }

        public XmlTypeName getAsIf()
        {
            return asIf;
        }

        public void setAsIf(XmlTypeName xmlName)
        {
            this.asIf = xmlName;
        }
        
        public void addQNameProperty(QNameProperty prop)
        {
            if (!(bindingType instanceof ByNameBean))
                throw new IllegalStateException();
            ((ByNameBean)bindingType).addProperty(prop);
        }
        
        public Collection getQNameProperties()
        {
            if (!(bindingType instanceof ByNameBean))
                throw new IllegalStateException();
            return ((ByNameBean)bindingType).getProperties();
        }

        public boolean isStructureResolved()
        {
            return this.isStructureResolved;
        }
        
        public void setStructureResolved(boolean isStructureResolved)
        {
            this.isStructureResolved = isStructureResolved;
        }

        public void setOnBehalfOf(TypeMatcher.MatchedProperties onBehalfOf)
        {
            this.onBehalfOf = onBehalfOf;
        }

        public TypeMatcher.MatchedProperties getOnBehalfOf()
        {
            return onBehalfOf;
        }
    }
    
    private static XmlTypeName normalizedXmlTypeName(SchemaType sType)
    {
        if (sType.isDocumentType())
            return XmlTypeName.forGlobalName(XmlTypeName.ELEMENT, sType.getDocumentElementName());
        if (sType.isAttributeType())
            return XmlTypeName.forGlobalName(XmlTypeName.ATTRIBUTE, sType.getDocumentElementName());
        return XmlTypeName.forSchemaType(sType);
    }
    
    /**
     * Returns a schema type which is the closest base type for the given schema
     * type which is builtin and compatible with the given Java class.
     * 
     * Or returns null if no builtin base class is known to be compatible.
     */ 
    private static SchemaType computeCompatibleBuiltin(JavaTypeName javaName, SchemaType sType)
    {
        // only interesting builtins are simple
        if (!sType.isSimpleType() && sType.getContentType() != SchemaType.SIMPLE_CONTENT)
            return null;
        
        // See if the java class is actually a compatible primitive
        BindingLoader builtins = BuiltinBindingLoader.getInstance();
        
        // find the closest simple base type
        while (!sType.isSimpleType())
            sType = sType.getBaseType();
        
        // look for a base type compatible with the given primitive
        while (sType != null)
        {
            if (null != builtins.getBindingType(BindingTypeName.forPair(javaName, XmlTypeName.forSchemaType(sType))))
                return sType;
            sType = sType.getBaseType();
        }
        
        return null;
    }
    
    /**
     * Arrays currently not automatically handled.
     */ 
    private static boolean isCompatibleArray(JClass jClass, SchemaType sType)
    {
        return false;
    }
    
    /**
     * This function goes through all relevant schema types, plus soap
     * array types, and creates a scratch area for each.  Each
     * scratch area is also marked at this time with an XmlTypeName,
     * a schema type, and a category.
     */ 
    private void resolveInitiallyMatchedTypes()
    {
        TypeMatcher.MatchedType[] matchedTypes = matcher.matchTypes(bss);
        
        for (int i = 0; i < matchedTypes.length; i++)
        {
            Scratch scratch = createScratch(matchedTypes[i].getJClass(), matchedTypes[i].getSType());
            scratchFromBindingName.put(scratch.getBindingTypeName(), scratch);
        }
        
        // Now run through and make sure we're unique in both S+J
        // and add the matches to the "unique" tables.
        for (Iterator i = scratchIterator(); i.hasNext(); )
        {
            Scratch scratch = (Scratch)i.next();
            boolean skip = false;
            
            createBindingType(scratch, true);
            
            if (!scratchFromXmlName.containsKey(scratch.getXmlName()))
                scratchFromXmlName.put(scratch.getXmlName(), scratch);
            else
            {
                skip = true;
                addError(new Object[] { scratch.getJClass(),
                                        ((Scratch)scratchFromXmlName.get(scratch.getXmlName())).getJClass(),
                                        scratch.getSchemaType() },
                                        "Both " + scratch.getJavaName() + " and " + ((Scratch)scratchFromXmlName.get(scratch.getXmlName())).getJavaName() + " match Schema " + scratch.getXmlName());
            }
            
            if (!scratchFromJavaName.containsKey(scratch.getJavaName()))
                scratchFromJavaName.put(scratch.getJavaName(), scratch);
            else
            {
                skip = true;
                addError(new Object[] { scratch.getSchemaType(),
                                        ((Scratch)scratchFromJavaName.get(scratch.getJavaName())).getSchemaType(),
                                        scratch.getJClass() },
                                        "Both " + scratch.getXmlName() + " and " + ((Scratch)scratchFromJavaName.get(scratch.getJavaName())).getXmlName() + " match Java " + scratch.getJavaName());
            }
            
            if (!skip)
                queueToResolve(scratch);
        }
    }
    
    private static Scratch createScratch(JClass jClass, SchemaType sType)
    {
        XmlTypeName xmlName = normalizedXmlTypeName(sType);
        JavaTypeName javaName = JavaTypeName.forJClass(jClass);
        Scratch scratch;
            
        SchemaType simpleBuiltin = computeCompatibleBuiltin(javaName, sType);
            
        if (simpleBuiltin != null)
        {
            // simple types are atomic
            // todo: what about simple content, custom codecs, etc?
            scratch = new Scratch(jClass, javaName, sType, xmlName, Scratch.ATOMIC_TYPE);
            scratch.setAsIf(XmlTypeName.forSchemaType(simpleBuiltin));
        }
        else if (sType.isDocumentType())
        {
            scratch = new Scratch(jClass, javaName, sType, xmlName, Scratch.ELEMENT);
            scratch.setAsIf(XmlTypeName.forSchemaType(sType.getProperties()[0].getType()));
        }
        else if (sType.isAttributeType())
        {
            scratch = new Scratch(jClass, javaName, sType, xmlName, Scratch.ATTRIBUTE);
            scratch.setAsIf(XmlTypeName.forSchemaType(sType.getProperties()[0].getType()));
        }
        else if (isCompatibleArray(jClass, sType))
        {
            scratch = new Scratch(jClass, javaName, sType, xmlName, Scratch.LITERALARRAY_TYPE);
        }
        else
        {
            scratch = new Scratch(jClass, javaName, sType, xmlName, Scratch.STRUCT_TYPE);
        }
        
        return scratch;
    }
    
    
    /**
     * Computes a BindingType for a scratch.
     */ 
    private void createBindingType(Scratch scratch, boolean shouldDefault)
    {
        assert(scratch.getBindingType() == null);
        
        BindingTypeName btName = BindingTypeName.forPair(scratch.getJavaName(), scratch.getXmlName());
        
        switch (scratch.getCategory())
        {
            case Scratch.ATOMIC_TYPE:
            case Scratch.SOAPARRAY_REF:
            case Scratch.ATTRIBUTE:
                SimpleBindingType simpleResult = new SimpleBindingType(btName);
                simpleResult.setAsIfXmlType(scratch.getAsIf());
                scratch.setBindingType(simpleResult);
                bindingFile.addBindingType(simpleResult, shouldDefault, shouldDefault);
                break;
                
            case Scratch.ELEMENT:
                SimpleDocumentBinding docResult = new SimpleDocumentBinding(btName);
                docResult.setTypeOfElement(scratch.getAsIf());
                scratch.setBindingType(docResult);
                bindingFile.addBindingType(docResult, shouldDefault, shouldDefault);
                break;
                
            case Scratch.STRUCT_TYPE:
                ByNameBean byNameResult = new ByNameBean(btName);
                scratch.setBindingType(byNameResult);
                bindingFile.addBindingType(byNameResult, shouldDefault, shouldDefault);
                break;
                
            case Scratch.LITERALARRAY_TYPE:
                throw new UnsupportedOperationException();
                
            case Scratch.SOAPARRAY:
                throw new UnsupportedOperationException();
                
            default:
                throw new IllegalStateException("Unrecognized category");
        }
    }
    
    /**
     * Looks on both the path and in the current scratch area for
     * the binding type corresponding to the given pair.  Must
     * be called after all the binding types have been created.
     */ 
    private BindingType bindingTypeForMatchedTypes(JClass jClass, SchemaType sType, TypeMatcher.MatchedProperties onBehalfOf)
    {
        // note that jClass may differ from property type because of arrays
        BindingTypeName btName = BindingTypeName.forTypes(jClass, sType); 
        
        // First look in locally compiled bindings
        Scratch scratch = (Scratch)scratchFromBindingName.get(btName);
        if (scratch != null)
            return scratch.getBindingType();
        
        // Then look on path
        BindingType result = bss.getTylarLoader().getBindingLoader().getBindingType(btName);
        if (result != null)
            return result;
        
        // Not found?  Then allocate and queue for processing
        scratch = createScratch(jClass, sType);
        scratch.setOnBehalfOf(onBehalfOf);
        createBindingType(scratch, false);
        queueToResolve(scratch);
        
        return scratch.getBindingType();
    }
    
    private void queueToResolve(Scratch scratch)
    {
        resolveQueue.add(scratch);
    }
    
    private boolean moreToResolve()
    {
        return !resolveQueue.isEmpty();
    }
    
    private Scratch dequeueToResolve()
    {
        return (Scratch)resolveQueue.removeFirst();
    }


    /**
     * Returns an iterator for all the Scratch's
     */ 
    private Iterator scratchIterator()
    {
        return scratchFromBindingName.values().iterator();
    }
    
    
    private void resolveBinding(Scratch scratch)
    {
        switch (scratch.getCategory())
        {
            case Scratch.ATOMIC_TYPE:
                return; // nothing to do that's not already done
                
            case Scratch.ELEMENT:
                // must ensure that the element's type is bound to the underlying JClass
                bindingTypeForMatchedTypes(scratch.getJClass(), scratch.getSchemaType().getProperties()[0].getType(), null);
                return;
                
            case Scratch.STRUCT_TYPE:
                resolveStructure(scratch);
                return;
                
            case Scratch.LITERALARRAY_TYPE:
            default:
                return;
        }                    
    }
    
    private static class SchemaPropertyName
    {
        QName qName;
        boolean isAttribute;
        
        public static SchemaPropertyName forProperty(SchemaProperty sProp)
        {
            return new SchemaPropertyName(sProp.getName(), sProp.isAttribute());
        }

        private SchemaPropertyName(QName qName, boolean attribute)
        {
            this.qName = qName;
            isAttribute = attribute;
        }

        public QName getQName()
        {
            return qName;
        }

        public boolean isAttribute()
        {
            return isAttribute;
        }

        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (!(o instanceof SchemaPropertyName)) return false;

            final SchemaPropertyName schemaPropertyName = (SchemaPropertyName) o;

            if (isAttribute != schemaPropertyName.isAttribute) return false;
            if (!qName.equals(schemaPropertyName.qName)) return false;

            return true;
        }

        public int hashCode()
        {
            int result;
            result = qName.hashCode();
            result = 29 * result + (isAttribute ? 1 : 0);
            return result;
        }
    }
    
    /**
     * Returns the set of elements and attributes which have a
     * minimum occurance greater than zero.  (In other words,
     * binding to them is not optional if you ever want to
     * serialize your XML out.)  The result Set contains
     * SchemaPropertyNames.
     */ 
    private static Set computeRequiredProperties(SchemaType sType)
    {
        Set result = new HashSet();
        
        SchemaProperty[] sProps = sType.getProperties();
        for (int i = 0; i < sProps.length; i++)
        {
            if (sProps[i].getMinOccurs().signum() > 0)
                result.add(SchemaPropertyName.forProperty(sProps[i]));
        }
        
        return result;
    }
    
    private void resolveStructure(Scratch scratch)
    {
        Object[] context;
        if (scratch.getOnBehalfOf() == null)
            context = new Object[] { scratch.getJClass(), scratch.getSchemaType() };
        else
            context = new Object[] { scratch.getOnBehalfOf().getJProperty(), scratch.getOnBehalfOf().getSProperty() };
        
        if (scratch.getSchemaType().isSimpleType() || scratch.getSchemaType() == XmlObject.type)
        {
            addError(context, "Java class " + scratch.getJavaName() + " does not match Schema type " + scratch.getXmlName());
            return;
        }
        
        // todo: check inheritance validity (inheritance in java + xml should match up)

        // todo: when looking at java + schema properties, be aware of inheritance issues

        // now, match up the names
        TypeMatcher.MatchedProperties[] matchedProperties =
                matcher.matchProperties(scratch.getJClass(), scratch.getSchemaType());
        
        // The only requirements:
        // (1) every required schema attribute or element must be accounted for
        // (2) cardinality must match
        
        Set requiredProperties = computeRequiredProperties(scratch.getSchemaType());
        
        for (int i = 0; i < matchedProperties.length; i++)
        {
            SchemaProperty sProp = matchedProperties[i].getSProperty();
            JProperty jProp = matchedProperties[i].getJProperty();
            
            // first, remove a matched schema property name when seen
            requiredProperties.remove(SchemaPropertyName.forProperty(sProp));
            
            // Extract property types to recurse on
            JClass jPropType = jProp.getType();
            SchemaType sPropType = sProp.getType();
            
            // Check cardinality, skip into type
            boolean multiple = isMultiple(sProp);
            JavaTypeName collection = null;
            if (multiple)
            {
                if (!jPropType.isArray())
                    addError(new Object[] { jProp, sProp }, "Property " + jProp + " in " + scratch.getJClass() + " is an array, but " + sProp.getName() + " in " + scratch.getSchemaType() + " is a singleton.");
                else
                {
                    collection = JavaTypeName.forJClass(jPropType);
                    jPropType = jPropType.getArrayComponentType();
                }
            }
            
            // A matcher can say that a declared type is "really" another type.
            // The normal matcher just returns the same thing back.
            jPropType = matcher.substituteClass(jPropType);
            
            // Queues the binding type for this property for processing if needed
            BindingType bType = bindingTypeForMatchedTypes(jPropType, sPropType, matchedProperties[i]);
                
            QNameProperty prop = new QNameProperty();
            prop.setQName(sProp.getName());
            prop.setAttribute(sProp.isAttribute());
            prop.setSetterName(MethodName.create(jProp.getSetter()));
            prop.setGetterName(MethodName.create(jProp.getGetter()));
            prop.setCollectionClass(collection);
            prop.setBindingType(bType);
            prop.setNillable(sProp.hasNillable() != SchemaProperty.NEVER);
            prop.setOptional(isOptional(sProp));
            prop.setMultiple(multiple);
            
            scratch.addQNameProperty(prop);
        }
        
        if (!requiredProperties.isEmpty())
        {
            int missing = requiredProperties.size();
            String reason;
            if (missing > 1)
                reason = "No match for " + missing + " schema element or attribute names.";
            else
            {
                SchemaPropertyName spName = (SchemaPropertyName)requiredProperties.iterator().next();
                if (spName.isAttribute())
                    reason = "No match for required attribute " + spName.getQName().getLocalPart();
                else
                    reason = "No match for required element " + spName.getQName().getLocalPart();
            }
            
            addError(context, "Java class " + scratch.getJavaName() + " does not match schema type " + scratch.getXmlName() + " (" + reason + ")");
        }
    }

    private static boolean isMultiple(SchemaProperty sProp)
    {
        BigInteger max = sProp.getMaxOccurs();
        if (max == null) return true;
        return (max.compareTo(BigInteger.ONE) > 0);
    }

    private static boolean isOptional(SchemaProperty sProp)
    {
        BigInteger min = sProp.getMinOccurs();
        return (min.signum() == 0);
    }

    
    private void addError(Object[] context, String message)
    {
        for (int i = 0; i < context.length; i++)
        {
            Object obj = context[i];
            assert(obj instanceof JClass || obj instanceof SchemaType || obj instanceof SchemaProperty || obj instanceof JProperty);
        }
        
        System.out.println(message);
        errors.add(message);
    }
    
    /**
     * Prints the binding file generated by this binding.
     */ 
    public BindingFile getBindingFile()
    {
        return bindingFile;
    }

    
}


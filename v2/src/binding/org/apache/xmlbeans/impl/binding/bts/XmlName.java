/**
 * XBeans implementation.
 * Author: David Bau
 * Date: Oct 1, 2003
 */
package org.apache.xmlbeans.impl.binding.bts;

import org.apache.xmlbeans.impl.common.XMLChar;

import javax.xml.namespace.QName;

/**
 * An XmlName is a way of uniquely identifying any
 * logical component in a schema.
 * 
 * For a diagram of the kinds of components that can be referenced,
 * see http://www.w3.org/TR/xmlschema-1/components.gif
 * 
 * A signature looks like this:
 * 
 * a-name1|t|e=name2|p.3|s|p.5|c|p.0|t|e=name3@my-namespace
 * 
 * This reads as:
 * The attribute declaration called "name1" (unqualified) inside
 * the anonymous type of
 * the element declaration called "name2" (qualified) which is
 * the third particle of
 * the sequence which is
 * the fifth particle of
 * the choice which is
 * the zeroth particle of
 * the anonymous type of
 * the element named name3 (qualified) in
 * the namespace my-namespace.
 * 
 * Hyphens (-) introduce unqualified names
 * Equals (=) introduce qualified names
 * Dot (.) introduce numbers
 * Anonymous components are just single-letters.
 *
 * There are these types of components
 *
 * n = Notations (named, global only)
 * e = Element decls (named, global or inside t or p)
 * Identity constraint defs
 *   k = key/unique/keyref (named, in elts only)
 * g = Model group defs (named, global only)
 * Model group compositors:
 *   l = All compositors (anon, inside p or g)
 *   s = Sequence compositors (anon, inside p or g)
 *   c = Choice compositors (anon, inside p or g)
 * p = Particles (numbered, inside t or l or s or c)
 * w = Wildcards (anon, inside p only)
 * v = Attribute Uses (named, in type only, inside t)
 * a = Attribute Declarations (named, global or inside t or v)
 * r = Attribute group definitions (named, global only)
 * t = Type definitions (name when global, or anon inside i, m, e, a, t)
 * 
 * Not part of the spec, but we treat as components:
 * d = document type definition (named, global only)
 * b = attribute-container type definition (named, global only)
 * m = union member definition (numbered, inside t only)
 * y = soap array of dimension n (numbered, referencing global t or another y only)
 *
 * A canonical signature will shortcut the following:
 * 
 * - global elements do not need to be explicitly nested inside their
 *   containing document types (e=name|d=name@namespace -> e=name@namespace)
 * - global attributes do not need to be explicitly nested inside their
 *   containing attribute types (e=name|d=name@namespace -> e=name@namespace)
 * - an element of a type, if its name is unique, may be referenced without the
 *   intervening particle path.  (e.g., e=name2|p.3|s|p.5|c|p.0|t can be
 *   reduced to e=name2|t)
 * - an attribute of a type may be referenced without explicitly putting it
 *   inside an attribute use (e.g., a-name1|v-name1|t -> a-name1|t)
 * 
 * Notice SOAP arrays are included in the naming schema, as follows:
 * 
 * A soap array type written like this:
 *     x:drg[,,,][,][,,,]
 * 
 * Has the following signature:
 *     y.3|y.2|y.4|t=drg@foobar
 */
public class XmlName
{
    private String namespace;
    private String path;
    
    public static final int NOTATION = 'n';
    public static final int ELEMENT = 'e';
    public static final int ID_CONSTRAINT = 'k';
    public static final int MODEL_GROUP = 'g';
    public static final int ALL = 'l';
    public static final int SEQUENCE = 's';
    public static final int CHOICE = 'c';
    public static final int PARTICLE = 'p';
    public static final int WILDCARD = 'w';
    public static final int ATTRIBUTE_USE = 'v';
    public static final int ATTRIBUTE = 'a';
    public static final int ATTRIBUTE_GROUP = 'r';
    public static final int TYPE = 't';
    public static final int DOCUMENT_TYPE = 'd';
    public static final int ATTRIBUTE_TYPE = 'b';
    public static final int MEMBER = 'm';
    public static final int SOAP_ARRAY = 'y';
    
    /**
     * This function is used to see if a path is valid or not.
     */ 
    public boolean valid()
    {
        XmlName outerComponent = null;
        int outerType = 0;
        String localName = internalGetStringName();
        
        boolean hasNumber = internalGetNumber() >= 0;
        boolean hasName = (localName != null);
        boolean isAnonymous = internalIsAnonymous();
        boolean isQualified = internalIsQualified();
        boolean isGlobal = !isNestedComponent();
    
        if (!isGlobal)
        {
            outerComponent = getOuterComponent();
            outerType = outerComponent.getComponentType();
        }
        
        boolean result;
        
        if (localName != null && !XMLChar.isValidNCName(localName))
            return false;
        
        switch (getComponentType())
        {
            case NOTATION:
                result = (isGlobal && hasName && isQualified);
                
            case ELEMENT:
                result = (hasName && (isGlobal && isQualified || outerType == TYPE || outerType == PARTICLE));
                
            case ID_CONSTRAINT:
                result = (hasName && outerType == ELEMENT);
                
            case MODEL_GROUP:
                result = (hasName && isGlobal);
                
            case ALL:
            case SEQUENCE:
            case CHOICE:
                result = (isAnonymous && (outerType == PARTICLE || outerType == MODEL_GROUP));
                
            case PARTICLE:
                result = (hasNumber && (outerType == SEQUENCE || outerType == CHOICE || outerType == ALL || outerType == TYPE));
                
            case WILDCARD:
                result = (isAnonymous && (outerType == PARTICLE || outerType == TYPE || outerType == ATTRIBUTE_GROUP));
                
            case ATTRIBUTE_USE:
                result = (hasName && (outerType == TYPE || outerType == ATTRIBUTE_GROUP));
                
            case ATTRIBUTE:
                result = (hasName && (isGlobal && isQualified || outerType == TYPE || outerType == ATTRIBUTE_USE));
                
            case ATTRIBUTE_GROUP:
                result = (hasName && isQualified && isGlobal);
                
            case TYPE:
                result = ((hasName && isQualified && isGlobal) || (isAnonymous && outerType == TYPE || outerType == ELEMENT || outerType == ATTRIBUTE || outerType == MEMBER));
                
            case DOCUMENT_TYPE:
                result = (hasName && isQualified && isGlobal);
                
            case ATTRIBUTE_TYPE:
                result = (hasName && isQualified && isGlobal);
                
            case MEMBER:
                result = (isAnonymous && outerType == TYPE);
                
            case SOAP_ARRAY:
                result = (hasNumber && (outerType == SOAP_ARRAY || outerType == TYPE && !outerComponent.isNestedComponent()));
                
            default:
                result = false;
        }
        if (!result)
            return false;
        
        if (isGlobal)
            return true;
        
        return outerComponent.valid();
    }
    
    /**
     * Creates an XMLName based on the given String signature.
     * 
     * This signature is described in the javadoc for this class.
     */ 
    public static XmlName forString(String signature)
    {
        String path;
        String namespace;
        int atSign = signature.indexOf('@');
        if (atSign < 0)
        {
            namespace = "";
            path = signature;
        }
        else
        {
            namespace = signature.substring(atSign + 1);
            path = signature.substring(0, atSign);
        }
        return forPathAndNamespace(path, namespace);
    }
    
    /**
     * Creates an XMLName for a schema type with the given fully-qualified QName.
     */ 
    public static XmlName forTypeNamed(QName name)
    {
        return forPathAndNamespace("t=" + name.getLocalPart(), name.getNamespaceURI());
    }
    
    /**
     * Creates an XMLName for a global schema element with the given fully-qualified QName.
     */ 
    public static XmlName forElementNamed(QName name)
    {
        return forPathAndNamespace("e=" + name.getLocalPart(), name.getNamespaceURI());
    }
    
    /**
     * Creates an XMLName for a global schema attribute with the given fully-qualified QName.
     */ 
    public static XmlName forAttributeNamed(QName name)
    {
        return forPathAndNamespace("a=" + name.getLocalPart(), name.getNamespaceURI());
    }

    private static XmlName forPathAndNamespace(String path, String namespace)
    {
        return new XmlName(path, namespace);
    }
    
    private XmlName(String path, String namespace)
    {
        if (path == null || namespace == null)
            throw new IllegalArgumentException();
        
        this.path = path;
        this.namespace = namespace;
    }
    
    boolean isNestedComponent()
    {
        int index = path.indexOf('|');
        return index >= 0;
    }
    
    XmlName getOuterComponent()
    {
        int index = path.indexOf('|');
        if (index < 0)
            return null;
        return forPathAndNamespace(path.substring(index + 1), namespace);
    }
    
    public int getComponentType()
    {
        if (path.length() > 0)
            return path.charAt(0);
        return 0; // unknown type
    }
    
    /**
     * Returns negative if there is no number
     */ 
    private int internalGetNumber()
    {
        if (path.length() <= 1 || path.charAt(1) != '.')
            return -1;
        
        int index = path.indexOf('|');
        if (index < 0)
            index = path.length();
        
        try
        {
            return Integer.parseInt(path.substring(2, index));
        }
        catch (Exception e)
        {
            return -1;
        }
    }
    
    /**
     * Returns the number locating this component within its parent,
     * or throws an exception if there is none.  Only union members (m)
     * and particles (p) have numbers.
     */ 
    public int getNumber()
    {
        int result = internalGetNumber();
        if (result < 0)
            throw new IllegalStateException("Path has no number");
        return result;
    }
    
    private String internalGetStringName()
    {
        if (path.length() <= 1 || path.charAt(1) != '=' && path.charAt(1) != '-')
            return null;
        
        int index = path.indexOf('|');
        if (index < 0)
            index = path.length();
        
        return path.substring(2, index);
    }
    
    private boolean internalIsQualified()
    {
        return (path.length() > 1 && path.charAt(1) == '=');
    }
    
    private boolean internalIsAnonymous()
    {
        return (path.length() <= 1 || path.charAt(1) == '|');
    }

    /**
     * Returns the name locating this component within its parent, or
     * returns null if there is none.  Notice that if looking up elements
     * by name in a type, you get the first element defintion when there
     * are multiple ones.  A full particle path can disambiguate.
     * 
     * This name will be qualified in a namespace if appropriate, but,
     * for example, local unqualified attributes will have a QName that
     * is unqualified.
     * 
     * Element, attributes, identity constraints, model/attribute groups,
     * and notations have names.
     */ 
    public QName getQName()
    {
        String localName = internalGetStringName();
        if (localName == null)
            return null;
        
        if (internalIsQualified())
            return new QName(namespace, localName);
        else
            return new QName(localName);
    }
    
    /**
     * Returns the signature string.
     */ 
    public String toString()
    {
        if (namespace.length() == 0)
            return path;
        
        return path + '@' + namespace;
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof XmlName)) return false;

        final XmlName xmlName = (XmlName) o;

        if (!namespace.equals(xmlName.namespace)) return false;
        if (!path.equals(xmlName.path)) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = namespace.hashCode();
        result = 29 * result + path.hashCode();
        return result;
    }
}

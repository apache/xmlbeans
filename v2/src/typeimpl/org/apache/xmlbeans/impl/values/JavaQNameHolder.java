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

package org.apache.xmlbeans.impl.values;

import org.apache.xmlbeans.impl.common.PrefixResolver;
import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.XMLChar;
import org.apache.xmlbeans.impl.common.QNameHelper;

import javax.xml.namespace.QName;
import org.apache.xmlbeans.impl.values.NamespaceContext;
   
public class JavaQNameHolder extends XmlObjectBase
{
    public JavaQNameHolder() {}

    public SchemaType schemaType()
        { return BuiltinSchemaTypeSystem.ST_QNAME; }

    private QName _value;

    protected int get_wscanon_rule()
        { return SchemaType.WS_PRESERVE; }
    
    // an ergonomic prefixer so that you can say stringValue() on a free-floating QName.
    private static final NamespaceManager PRETTY_PREFIXER = new PrettyNamespaceManager();
    
    private static class PrettyNamespaceManager implements NamespaceManager
    {
        public String find_prefix_for_nsuri(String nsuri, String suggested_prefix)
        {
            return QNameHelper.suggestPrefix(suggested_prefix);
        }
        public String getNamespaceForPrefix(String prefix)
        {
            return prefix;
        }
    }

    // SIMPLE VALUE ACCESSORS BELOW -------------------------------------------
    public String compute_text(NamespaceManager nsm)
    {
        if (nsm == null)
        {
            // we used to: throw new IllegalStateException("Cannot create QName prefix outside of a document");
            // but it's not nice to throw on stringValue()
            nsm = PRETTY_PREFIXER;
        }

// TODO - what I really need to do here is that if there is no
// namespace for this qname, then instead of finding the prefix for the
// uri, I should make a call to set the default namespace for the
// immediate context for this qname to be "".  
   
        String namespace = _value.getNamespaceURI();
        String localPart = _value.getLocalPart();

        if (namespace == null || namespace.length() == 0)
            return localPart;
        
        String prefix = nsm.find_prefix_for_nsuri( namespace, null );

        assert prefix != null;
        
        return prefix + ":" + localPart;
    }

    public static QName validateLexical(
        String v, ValidationContext context, PrefixResolver resolver)
    {
        QName name;
        
        try
        {
            name = parse(v, resolver);
        }
        catch ( XmlValueOutOfRangeException e )
        {
            context.invalid(e.getMessage());
            name = null;
        }

        return name;
    }
    
    private static QName parse(String v, PrefixResolver resolver)
    {
        String prefix, localname;
        int start;
        int end;
        for (end = v.length(); end > 0; end -= 1)
            if (!XMLChar.isSpace(v.charAt(end-1)))
                break;
        for (start = 0; start < end; start += 1)
            if (!XMLChar.isSpace(v.charAt(start)))
                break;

        int firstcolon = v.indexOf(':', start);
        if (firstcolon >= 0)
        {
            prefix = v.substring(start, firstcolon);
            localname = v.substring(firstcolon + 1, end);
        }
        else
        {
            prefix = "";
            localname = v.substring(start, end);
        }
        
        String uri =
            resolver == null ? null : resolver.getNamespaceForPrefix(prefix);
        
        if (uri == null)
        {
            if (prefix.length() > 0)
                throw new XmlValueOutOfRangeException("Can't resolve prefix: " + prefix);
                        
            uri = "";
        }

        return new QName( uri, localname );
    }
    
    protected void set_text(String s)
    {
        PrefixResolver resolver = NamespaceContext.getCurrent();

        if (resolver == null && has_store())
            resolver = get_store();
        
        _value = parse(s, resolver);
    }

    // BUGBUG - having prefix here may not work
    protected void set_QName(QName name)
    {
        assert name != null;
        
        // Sync force of creation of namesapce mapping ..
        
        if (has_store())
            get_store().find_prefix_for_nsuri( name.getNamespaceURI(), null );
        
        _value = name;
    }

    protected void set_xmlanysimple(XmlAnySimpleType value)
    {
        _value = parse(value.getStringValue(), NamespaceContext.getCurrent());
    }

    protected void set_nil() { _value = null; }

    // setters, getters (setter already handled via set_text)

    public QName qNameValue()
        { check_dated(); return _value; }

    // comparators
    protected boolean equal_to(XmlObject obj)
    {
        return _value.equals(((XmlObjectBase)obj).qNameValue());
    }

    protected int value_hash_code()
    {
        return _value.hashCode();
    }
}

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

package org.apache.xmlbeans.impl.validator;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.impl.common.Chars;
import org.apache.xmlbeans.impl.common.PrefixResolver;
import org.apache.xmlbeans.impl.common.ValidatorListener;
import org.apache.xmlbeans.impl.common.XmlWhitespace;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * Author: Cezar Andrei (cezar.andrei at bea.com)
 * Date: Feb 5, 2004
 */
public class ValidatorUtil
{
    private static class EventImpl
        implements ValidatorListener.Event
    {
        PrefixResolver _prefixResolver;
        Chars _text;

        EventImpl(PrefixResolver prefixResolver, Chars chars)
        {
            _prefixResolver = prefixResolver;
            _text = chars;
        }

        // can return null, used only to locate errors
        public XmlCursor getLocationAsCursor()
        {
            return null;
        }

        // fill up chars with the xsi:type attribute value if there is one othervise return false
        public boolean getXsiType(Chars chars) // BEGIN xsi:type
        {
            return false;
        }

        // fill up chars with xsi:nill attribute value if any
        public boolean getXsiNil(Chars chars) // BEGIN xsi:nil
        {
            return false;
        }

        public boolean getXsiLoc(Chars chars) // BEGIN xsi:schemaLocation
        {
            return false;
        }

        public boolean getXsiNoLoc(Chars chars) // BEGIN xsi:noNamespaceSchemaLocation
        {
            return false;
        }

        // On START and ATTR
        public QName getName()
        {
            return null;
        }

        // On TEXT and ATTR
        public void getText(Chars chars)
        {
            chars.string = _text.asString();
        }

        public void getText(Chars chars, int wsr)
        {
            chars.string = XmlWhitespace.collapse(
                    _text.asString(), wsr );
        }

        public boolean textIsWhitespace()
        {
            return false;
        }

        public String getNamespaceForPrefix(String prefix)
        {
            return _prefixResolver.getNamespaceForPrefix(prefix);
        }
    }

    public static boolean validateSimpleType ( SchemaType type, String value,
        Collection errors, PrefixResolver prefixResolver)
    {
        if (!type.isSimpleType() &&
                type.getContentType() != SchemaType.SIMPLE_CONTENT)
        {
            assert false;
            throw new RuntimeException( "Not a simple type" );
        }

        Validator validator =
                new Validator(
                        type, null, type.getTypeSystem(), null, errors);

        //make only one event at the beginning and than reuse it
        Chars text = new Chars();
        EventImpl ev = new EventImpl(prefixResolver, text);

        validator.nextEvent(ValidatorListener.BEGIN, ev);

        text.string = value;
        validator.nextEvent(ValidatorListener.TEXT, ev);

        validator.nextEvent(ValidatorListener.END, ev);

        return validator.isValid();
    }

//    public static void main(String[] args)
//    {
//        String value;
//        value = "  +1.2323 ";
//        System.out.println("float " + validateSimpleType(XmlFloat.type, value, null , null));
//        value = " +234  ";
//        System.out.println("posInt " + validateSimpleType(XmlPositiveInteger.type, value, null , null));
//        value = "2001-01-01";
//        System.out.println("IntOrDateUnion " + validateSimpleType(DocDocument.Doc.IntOrDateUnion.type, value, null , null));
//        value = "232321";
//        System.out.println("IntOrDateUnion " + validateSimpleType(DocDocument.Doc.IntOrDateUnion.type, value, null , null));
//    }
}

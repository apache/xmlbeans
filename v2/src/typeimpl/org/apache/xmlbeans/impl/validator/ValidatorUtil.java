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

        public javax.xml.stream.Location getLocation()
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

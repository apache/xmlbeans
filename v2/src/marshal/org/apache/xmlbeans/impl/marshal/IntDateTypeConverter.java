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

package org.apache.xmlbeans.impl.marshal;

import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDateBuilder;
import org.apache.xmlbeans.GDateSpecification;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;
import org.apache.xmlbeans.impl.util.XsTypeConverter;


/**
 * convert between schema date/time types and java.util.Date
 */
final class IntDateTypeConverter
    extends BaseSimpleTypeConverter
{
    private final int schemaType;

    /**
     *
     * @param schemaType  use codes from SchemaType
     */
    IntDateTypeConverter(int schemaType)
    {
        this.schemaType = schemaType;
    }

    protected Object getObject(UnmarshalResult context) throws XmlException
    {
        final GDate gdate = context.getGDateValue();
        return extractIntValue(gdate);
    }

    private Object extractIntValue(final GDateSpecification gdate)
    {
        final int val;
        //REVIEW: consider subclasses to avoid this switch
        switch (schemaType) {
            case SchemaType.BTC_G_DAY:
                val = gdate.getDay();
                break;
            case SchemaType.BTC_G_MONTH:
                val = gdate.getMonth();
                break;
            case SchemaType.BTC_G_YEAR:
                val = gdate.getYear();
                break;
            default:
                throw new AssertionError("inapplicable type: " + schemaType);
        }
        return new Integer(val);
    }

    public Object unmarshalAttribute(UnmarshalResult context) throws XmlException
    {
        final GDate gdate = context.getAttributeGDateValue();
        return extractIntValue(gdate);
    }

    public Object unmarshalAttribute(CharSequence lexical_value,
                                     UnmarshalResult result)
        throws XmlException
    {
        try {
            GDateSpecification gd =
                XsTypeConverter.getGDateValue(lexical_value, schemaType);
            return extractIntValue(gd);
        }
        catch (IllegalArgumentException e) {
            throw new InvalidLexicalValueException(e, result.getLocation());
        }

    }

    public CharSequence print(Object value, MarshalResult result)
        throws XmlException
    {
        final int i = ((Integer)value).intValue();
        //REVIEW: consider subclasses to avoid this switch
        try {
            GDateBuilder b = new GDateBuilder();
            switch (schemaType) {
                case SchemaType.BTC_G_DAY:
                    b.setDay(i);
                    break;
                case SchemaType.BTC_G_MONTH:
                    b.setMonth(i);
                    break;
                case SchemaType.BTC_G_YEAR:
                    b.setYear(i);
                    break;
                default:
                    throw new AssertionError("inapplicable type: " + schemaType);
            }
            b.setBuiltinTypeCode(schemaType);
            return b.toString();
        }
        catch (IllegalArgumentException iae) {
            throw new XmlException(iae);
        }
    }
}

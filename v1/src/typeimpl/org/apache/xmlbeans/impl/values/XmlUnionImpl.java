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

package org.apache.xmlbeans.impl.values;

import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.math.BigInteger;
import java.math.BigDecimal;

import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.StringEnumAbstractBase;
import org.apache.xmlbeans.GDateSpecification;
import org.apache.xmlbeans.GDurationSpecification;
import org.apache.xmlbeans.SimpleValue;

import org.apache.xmlbeans.impl.schema.SchemaTypeImpl;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.QNameHelper;

import javax.xml.namespace.QName;


/**
 * This class implements simple union types.
 *
 */
public class XmlUnionImpl extends XmlObjectBase implements XmlAnySimpleType
{
    public XmlUnionImpl(SchemaType type, boolean complex)
        { _schemaType = type; initComplexType(complex, false); }

    public SchemaType schemaType()
        { return _schemaType; }

    public SchemaType instanceType()
        { check_dated(); return _value == null ? null : ((SimpleValue)_value).instanceType(); }

    private SchemaType _schemaType;
    private XmlAnySimpleType _value; // underlying value
    private String _textvalue = ""; // textual value




    // SIMPLE VALUE ACCESSORS BELOW -------------------------------------------
    // gets raw text value

    protected String compute_text(NamespaceManager nsm)
        { return _textvalue; }

    protected boolean is_defaultable_ws(String v) {
        try {
            XmlAnySimpleType savedValue = _value;
            set_text(v);

            // restore the saved value
            _value = savedValue;

            return false;
        }
        catch (XmlValueOutOfRangeException e) {
            return true;
        }
    }

    protected void set_text(String s)
    {
        // first check against any patterns...
        if (!_schemaType.matchPatternFacet(s))
            throw new XmlValueOutOfRangeException();

        // save state for rollback
        String original = _textvalue;
        _textvalue = s;

        // iterate through the types, trying to create a type
        SchemaType[] members = _schemaType.getUnionConstituentTypes();
        assert(members != null);

        boolean pushed = false;
        // boolean wasstrict = set_strict(true); // tell types to complain ferverently about errors
        if (has_store())
        {
            NamespaceContext.push(new NamespaceContext(get_store()));
            pushed = true;
        }
        try
        {
            for (int i = 0; i < members.length; i++)
            {
                // From the point of view of the following call, "this" is a generic
                // XmlAnySimpleType implementation, for which only getText can be called.
                // (Note that "this" is not wrapped in the proxy object.)
                try
                {
                    XmlAnySimpleType newval = ((SchemaTypeImpl)members[i]).newValidatingValue(s);

                    // now we need to check against (enuemration) restrictions
                    if (!check(newval, _schemaType))
                        continue;

                    // found one that works!
                    _value = newval;
                    return;
                }
                catch (XmlValueOutOfRangeException e)
                {
                    continue;
                }
                catch (Exception e)
                {
                    throw new RuntimeException("Troublesome union exception caused by unexpected " + e, e);
                    // assert(false) : "Unexpected " + e;
                    // continue;
                }
            }
        }
        finally
        {
            if (pushed)
                NamespaceContext.pop();
            // set_strict(wasstrict);
        }


        // doesn't match any of the members; rollback and throw
        _textvalue = original;
        throw new XmlValueOutOfRangeException();
    }

    protected void set_nil()
    {
        _value = null;
        _textvalue = null;
    }

    protected int get_wscanon_rule() { return SchemaType.WS_PRESERVE; }


    // numerics
    public float floatValue()
        { check_dated(); return _value == null ? 0.0f : ((SimpleValue)_value).floatValue(); }

    public double doubleValue()
        { check_dated(); return _value == null ? 0.0 : ((SimpleValue)_value).doubleValue(); }

    public BigDecimal bigDecimalValue()
        { check_dated(); return _value == null ? null : ((SimpleValue)_value).bigDecimalValue(); }

    public BigInteger bigIntegerValue()
        { check_dated(); return _value == null ? null : ((SimpleValue)_value).bigIntegerValue(); }

    public byte byteValue()
        { check_dated(); return _value == null ? 0 : ((SimpleValue)_value).byteValue(); }

    public short shortValue()
        { check_dated(); return _value == null ? 0 : ((SimpleValue)_value).shortValue(); }

    public int intValue()
        { check_dated(); return _value == null ? 0 : ((SimpleValue)_value).intValue(); }

    public long longValue()
        { check_dated(); return _value == null ? 0 : ((SimpleValue)_value).longValue(); }


    // various
    public byte[] byteArrayValue()
        { check_dated(); return _value == null ? null : ((SimpleValue)_value).byteArrayValue(); }

    public boolean booleanValue()
        { check_dated(); return _value == null ? false : ((SimpleValue)_value).booleanValue(); }

    public Calendar calendarValue()
        { check_dated(); return _value == null ? null : ((SimpleValue)_value).calendarValue(); }

    public Date dateValue()
        { check_dated(); return _value == null ? null : ((SimpleValue)_value).dateValue(); }

    public GDate gDateValue()
        { check_dated(); return _value == null ? null : ((SimpleValue)_value).gDateValue(); }

    public GDuration gDurationValue()
        { check_dated(); return _value == null ? null : ((SimpleValue)_value).gDurationValue(); }

    public QName qNameValue()
        { check_dated(); return _value == null ? null : ((SimpleValue)_value).qNameValue(); }

    public List listValue()
        { check_dated(); return _value == null ? null : ((SimpleValue)_value).listValue(); }

    public List xlistValue()
        { check_dated(); return _value == null ? null : ((SimpleValue)_value).xlistValue(); }

    public StringEnumAbstractBase enumValue()
        { check_dated(); return _value == null ? null : ((SimpleValue)_value).enumValue(); }

    public String stringValue()
        { check_dated(); return _value == null ? null : _value.stringValue(); }

    /**
     * Returns true if the space of canonical lexical forms
     * of the first (source) type overlaps with the full lexical space
     * of the second (target) type. Both types must be primitives.
     */
    static boolean lexical_overlap(int source, int target)
    {
        // types are the same
        if (source == target)
            return true;

        // one of the types has the full lexical space
        if (source == SchemaType.BTC_ANY_SIMPLE ||
            target == SchemaType.BTC_ANY_SIMPLE ||
            source == SchemaType.BTC_STRING ||
            target == SchemaType.BTC_STRING ||
            source == SchemaType.BTC_ANY_URI ||
            target == SchemaType.BTC_ANY_URI)
            return true;

        switch (source)
        {
            case SchemaType.BTC_BOOLEAN: switch(target)
            {
                case SchemaType.BTC_QNAME:       // "true" is valid NcName and therefore QName
                case SchemaType.BTC_NOTATION:    // "true" is valid NCName
                    return true;
                default:
                    return false;
            }
            case SchemaType.BTC_BASE_64_BINARY: switch(target)
            {
                case SchemaType.BTC_BOOLEAN:     // "0" is valid boolean
                case SchemaType.BTC_HEX_BINARY:  // "0" is valid hex
                case SchemaType.BTC_QNAME:       // "a" is valid NcName and therefore QName
                case SchemaType.BTC_NOTATION:    // "a" is valid NcName
                case SchemaType.BTC_FLOAT:       // "0" is valid float
                case SchemaType.BTC_DOUBLE:      // "0" is valid double
                case SchemaType.BTC_DECIMAL:     // "0" is valid decimal
                case SchemaType.BTC_DURATION:    // "P1Y2M3DT10H30M" is both b64 and duration
                case SchemaType.BTC_G_YEAR:      // "1999" is valid year
                    return true;
                default:
                    return false;           // "-" and ":" cannot come from b64
            }
            case SchemaType.BTC_HEX_BINARY: switch(target)
            {
                case SchemaType.BTC_BOOLEAN:     // "0" is valid boolean
                case SchemaType.BTC_BASE_64_BINARY:  // "0" is valid b64
                case SchemaType.BTC_QNAME:       // "A" is valid NcName and therefore QName
                case SchemaType.BTC_NOTATION:    // "A" is valid NcName
                case SchemaType.BTC_FLOAT:       // "0" is valid float
                case SchemaType.BTC_DOUBLE:      // "0" is valid double
                case SchemaType.BTC_DECIMAL:     // "0" is valid decimal
                case SchemaType.BTC_G_YEAR:      // "1999" is valid year
                    return true;
                default:
                    return false;           // "-" and ":" cannot come from b64
            }
            case SchemaType.BTC_QNAME:
            case SchemaType.BTC_NOTATION: switch(target)
            {
                case SchemaType.BTC_BOOLEAN:     // "true" is valid boolean
                case SchemaType.BTC_BASE_64_BINARY:  // "a" is valid b64
                case SchemaType.BTC_HEX_BINARY:  // "a" is valid hex
                case SchemaType.BTC_QNAME:       // "A" is valid NcName and therefore QName
                case SchemaType.BTC_NOTATION:    // "A" is valid NcName and therefore QName
                case SchemaType.BTC_DURATION:    // "P1Y2M3DT10H30M" is both NcName and duration
                    return true;
                default:
                    return false;
            }
            case SchemaType.BTC_FLOAT:
            case SchemaType.BTC_DOUBLE:
            case SchemaType.BTC_DECIMAL:
            case SchemaType.BTC_G_YEAR: switch(target)
            {
                case SchemaType.BTC_BASE_64_BINARY: // "0" is valid b64
                case SchemaType.BTC_HEX_BINARY:  // "0" is valid hex
                case SchemaType.BTC_FLOAT:       // "0" is valid float
                case SchemaType.BTC_DOUBLE:      // "0" is valid double
                case SchemaType.BTC_DECIMAL:     // "0" is valid decimal
                case SchemaType.BTC_G_YEAR:      // "1999" is valid year
                    return true;
                default:
                    return false;
            }
            case SchemaType.BTC_DURATION: switch(target)
            {
                case SchemaType.BTC_QNAME:
                case SchemaType.BTC_NOTATION:
                case SchemaType.BTC_BASE_64_BINARY:
                    return true;
                default:
                    return false;
            }
            case SchemaType.BTC_DATE_TIME:
            case SchemaType.BTC_TIME:
            case SchemaType.BTC_DATE:
            case SchemaType.BTC_G_YEAR_MONTH:
            case SchemaType.BTC_G_MONTH_DAY:
            case SchemaType.BTC_G_DAY:
            case SchemaType.BTC_G_MONTH:
            default:
                return false;
        }
    }

    /**
     * True if the given schema type's logical type is a match for
     * the given category of java concepts.
     */

    private static final int JAVA_NUMBER = SchemaType.BTC_LAST_BUILTIN + 1;
    private static final int JAVA_DATE = SchemaType.BTC_LAST_BUILTIN + 2;
    private static final int JAVA_CALENDAR = SchemaType.BTC_LAST_BUILTIN + 3;
    private static final int JAVA_BYTEARRAY = SchemaType.BTC_LAST_BUILTIN + 4;
    private static final int JAVA_LIST = SchemaType.BTC_LAST_BUILTIN + 5;

    private static boolean logical_overlap(SchemaType type, int javacode)
    {
        // non-union types because it's being applied on irreducible union members!
        assert(type.getSimpleVariety() != SchemaType.UNION);

        if (javacode <= SchemaType.BTC_LAST_BUILTIN)
        {
            if (type.getSimpleVariety() != SchemaType.ATOMIC)
                return false;

            return (type.getPrimitiveType().getBuiltinTypeCode() == javacode);
        }

        switch (javacode)
        {
        case JAVA_NUMBER:
            {
                if (type.getSimpleVariety() != SchemaType.ATOMIC)
                    return false;

                switch (type.getPrimitiveType().getBuiltinTypeCode())
                {
                    case SchemaType.BTC_FLOAT:
                    case SchemaType.BTC_DOUBLE:
                    case SchemaType.BTC_DECIMAL:
                    case SchemaType.BTC_G_YEAR:
                    case SchemaType.BTC_G_MONTH:
                    case SchemaType.BTC_G_DAY:
                        return true;
                }
                return false;
            }
        case JAVA_DATE:
            {
                if (type.getSimpleVariety() != SchemaType.ATOMIC)
                    return false;

                switch (type.getPrimitiveType().getBuiltinTypeCode())
                {
                    case SchemaType.BTC_DATE_TIME:
                    case SchemaType.BTC_DATE:
                        return true;
                }
                return false;
            }
        case JAVA_CALENDAR:
            {
                if (type.getSimpleVariety() != SchemaType.ATOMIC)
                    return false;

                switch (type.getPrimitiveType().getBuiltinTypeCode())
                {
                    case SchemaType.BTC_DATE_TIME:
                    case SchemaType.BTC_DATE:
                    case SchemaType.BTC_TIME:
                    case SchemaType.BTC_G_YEAR_MONTH:
                    case SchemaType.BTC_G_MONTH_DAY:
                    case SchemaType.BTC_G_YEAR:
                    case SchemaType.BTC_G_MONTH:
                    case SchemaType.BTC_G_DAY:
                        return true;
                }
                return false;
            }
                
        case JAVA_BYTEARRAY:
            {
                if (type.getSimpleVariety() != SchemaType.ATOMIC)
                    return false;

                switch (type.getPrimitiveType().getBuiltinTypeCode())
                {
                    case SchemaType.BTC_BASE_64_BINARY:
                    case SchemaType.BTC_HEX_BINARY:
                        return true;
                }
                return false;
            }
        case JAVA_LIST:
            {
                return (type.getSimpleVariety() == SchemaType.LIST);
            }
        }

        assert(false) : "missing case";
        return false;
    }

    /**
     * Grabs a chained value of type st, creating and attaching
     * one if not present.
     */
    private void set_primitive(int typecode, Object val)
    {
        SchemaType[] members = _schemaType.getUnionConstituentTypes();
        assert(members != null);

        boolean pushed = false;
        if (has_store())
        {
            NamespaceContext.push(new NamespaceContext(get_store()));
            pushed = true;
        }
        try
        {
            outer: for (int i = 0; i < members.length; i++)
            {
                // candidates must be a logical match for the desired typecode
                if (logical_overlap(members[i], typecode))
                {
                    XmlAnySimpleType newval;

                    try
                    {
                        newval = members[i].newValue(val);
                    }
                    catch (XmlValueOutOfRangeException e)
                    {
                        // doesn't match this type even though logical categories
                        // line up (probably because of restriciton); try the next type.
                        continue outer;
                    }
                    catch (Exception e)
                    {
                        assert(false) : "Unexpected " + e;
                        continue outer;
                    }

                    /* TODO: rethink this - disabling for now.

                    // OK, now we've got a newval... We have to verify
                    // that lexically it doesn't overlap with previous types

                    String newvaltext = null;

                    inner: for (int j = 0; j < i; j++)
                    {
                        if (members[j].getSimpleVariety() == SchemaType.LIST ||
                            lexical_overlap(members[j].getPrimitiveType().getBuiltinTypeCode(),
                                            newval.schemaType().getPrimitiveType().getBuiltinTypeCode()))
                        {
                            // there is a preceding type that may lexically overlap with ours...
                            // if it lexically contains the string representation of our new
                            // proposed value, then it's impossible for the union to have our
                            // logical value (because it would have been masked) and throw an
                            // error.
                            if (newvaltext == null)
                                newvaltext = newval.stringValue();
                            try
                            {
                                // discard return value
                                members[i].newValue(newvaltext);

                                // oh bad, we succeeded. Our instance lexically looks like a
                                // previous type, and this isn't a valid value. Keep on hunting.
                                continue outer;
                            }
                            catch (XmlValueOutOfRangeException e)
                            {
                                // this is good: this error means that our value doesn't look like
                                // the other type.
                                continue inner;
                            }
                        }
                    }

                    */

                    // No lexical masking: we're OK
                    _value = newval;
                    _textvalue = _value.stringValue();
                    return;
                }
            }
        }
        finally
        {
            if (pushed)
                NamespaceContext.pop();
        }
    }

    // here are the setters

    protected void set_boolean(boolean v)
        { set_primitive(SchemaType.BTC_BOOLEAN, new Boolean(v)); }

    protected void set_byte(byte v)
        { set_primitive(JAVA_NUMBER, new Byte(v)); }
    protected void set_short(short v)
        { set_primitive(JAVA_NUMBER, new Short(v)); }
    protected void set_int(int v)
        { set_primitive(JAVA_NUMBER, new Integer(v)); }
    protected void set_long(long v)
        { set_primitive(JAVA_NUMBER, new Long(v)); }
    protected void set_float(float v)
        { set_primitive(JAVA_NUMBER, new Float(v)); }
    protected void set_double(double v)
        { set_primitive(JAVA_NUMBER, new Double(v)); }

    protected void set_ByteArray(byte[] b)
        { set_primitive(JAVA_BYTEARRAY, b); }
    protected void set_hex(byte[] b)
        { set_primitive(JAVA_BYTEARRAY, b); }
    protected void set_b64(byte[] b)
        { set_primitive(JAVA_BYTEARRAY, b); }
    protected void set_BigInteger(BigInteger v)
        { set_primitive(JAVA_NUMBER, v); }
    protected void set_BigDecimal(BigDecimal v)
        { set_primitive(JAVA_NUMBER, v); }
    protected void set_QName(QName v)
        { set_primitive(SchemaType.BTC_QNAME, v); }

    protected void set_Calendar(Calendar c)
        { set_primitive(JAVA_CALENDAR, c); }
    protected void set_Date(Date d)
        { set_primitive(JAVA_DATE, d); }
    protected void set_GDate(GDateSpecification d)
    {
        int btc = d.getBuiltinTypeCode();
        if (btc <= 0)
            throw new XmlValueOutOfRangeException();
        set_primitive(btc, d);
    }

    protected void set_GDuration(GDurationSpecification d)
        { set_primitive(SchemaType.BTC_DURATION, d); }

    protected void set_enum(StringEnumAbstractBase e)
        { set_primitive(SchemaType.BTC_STRING, e); }

    protected void set_list(List v)
        { set_primitive(JAVA_LIST, v); }


    protected void set_xmlfloat(XmlObject v)
        { set_primitive(SchemaType.BTC_FLOAT, v); }
    protected void set_xmldouble(XmlObject v)
        { set_primitive(SchemaType.BTC_DOUBLE, v); }
    protected void set_xmldecimal(XmlObject v)
        { set_primitive(SchemaType.BTC_DECIMAL, v); }
    protected void set_xmlduration(XmlObject v)
        { set_primitive(SchemaType.BTC_DURATION, v); }
    protected void set_xmldatetime(XmlObject v)
        { set_primitive(SchemaType.BTC_DATE_TIME, v); }
    protected void set_xmltime(XmlObject v)
        { set_primitive(SchemaType.BTC_TIME, v); }
    protected void set_xmldate(XmlObject v)
        { set_primitive(SchemaType.BTC_DATE, v); }
    protected void set_xmlgyearmonth(XmlObject v)
        { set_primitive(SchemaType.BTC_G_YEAR_MONTH, v); }
    protected void set_xmlgyear(XmlObject v)
        { set_primitive(SchemaType.BTC_G_YEAR, v); }
    protected void set_xmlgmonthday(XmlObject v)
        { set_primitive(SchemaType.BTC_G_MONTH_DAY, v); }
    protected void set_xmlgday(XmlObject v)
        { set_primitive(SchemaType.BTC_G_DAY, v); }
    protected void set_xmlgmonth(XmlObject v)
        { set_primitive(SchemaType.BTC_G_MONTH, v); }



    private static boolean check(XmlObject v, SchemaType sType)
    {
        XmlObject[] vals = sType.getEnumerationValues();
        if (vals != null)
        {
            for (int i = 0; i < vals.length; i++)
            {
                if (vals[i].valueEquals(v))
                    return true;
            }
            return false;
        }

        return true;
    }

    protected boolean equal_to(XmlObject xmlobj)
    {
        return _value.valueEquals(xmlobj);
    }

    protected int value_hash_code()
    {
        return _value.hashCode();
    }
    
    protected void validate_simpleval(String lexical, ValidationContext ctx)
    {
        try
        {
            check_dated();
        }
        catch (Exception e)
        {
            ctx.invalid("Union value " + lexical + " does not match any of the member types for " + QNameHelper.readable(schemaType()));
            return;
        }
        if (_value == null)
        {
            ctx.invalid("Union value " + lexical + " does not match any of the member types for " + QNameHelper.readable(schemaType()));
            return;
        }
        
        ((XmlObjectBase)_value).validate_simpleval(lexical,  ctx);
    }
    

}


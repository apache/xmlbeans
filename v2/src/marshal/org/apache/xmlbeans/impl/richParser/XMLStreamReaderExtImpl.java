package org.apache.xmlbeans.impl.richParser;

import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDateBuilder;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.impl.common.XMLChar;
import org.apache.xmlbeans.impl.common.XsTypeConverter;
import org.apache.xmlbeans.impl.util.Base64;
import org.apache.xmlbeans.impl.util.HexBin;
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Author: Cezar Andrei (cezar.andrei at bea.com)
 * Date: Nov 17, 2003
 */
public class XMLStreamReaderExtImpl
    implements XMLStreamReaderExt
{
    private final XMLStreamReader _xmlStream;
    private final CharSeqTrimWS _charSeq;

    public XMLStreamReaderExtImpl(XMLStreamReader xmlStream)
    {
        if (xmlStream==null)
            throw new IllegalArgumentException();

        _xmlStream = xmlStream;
        _charSeq = new CharSeqTrimWS(_xmlStream);
    }

    // XMLStreamReaderExt methods
    public String getStringValue()
        throws XMLStreamException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_PRESERVE);
        return _charSeq.toString();
    }

    public boolean getBooleanValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexBoolean(_charSeq);
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public byte getByteValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexByte(_charSeq);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public short getShortValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexShort(_charSeq);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public int getIntValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexInt(_charSeq);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public long getLongValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexLong(_charSeq);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public BigInteger getBigIntegerValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexInteger(_charSeq);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public BigDecimal getBigDecimalValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexDecimal(_charSeq);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public float getFloatValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexFloat(_charSeq);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public double getDoubleValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexDouble(_charSeq);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public InputStream getHexBinaryValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        String text = _charSeq.toString();
        byte[] buf = HexBin.decode(text.getBytes());
        if (buf!=null)
            return new ByteArrayInputStream(buf);
        else
            throw new InvalidLexicalValueException("invalid hexBinary value");
    }

    public InputStream getBase64Value()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        String text = _charSeq.toString();
        byte[] buf = Base64.decode(text.getBytes());
        if (buf!=null)
            return new ByteArrayInputStream(buf);
        else
            throw new InvalidLexicalValueException("invalid base64Binary value");
    }

    public XmlCalendar getCalendarValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return new GDateBuilder(_charSeq).getCalendar();
        }
        catch( IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public Date getDateValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return new GDateBuilder(_charSeq).getDate();
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public GDate getGDateValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexGDate(_charSeq);
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public GDuration getGDurationValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return new GDuration(_charSeq);
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public QName getQNameValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        return XsTypeConverter.lexQName(_charSeq, _xmlStream.getNamespaceContext());
    }

    public String getAttributeStringValue(int index) throws XMLStreamException
    {
        return _xmlStream.getAttributeValue(index);
    }

    public boolean getAttributeBooleanValue(int index) throws XMLStreamException
    {
        try
        {
            return XsTypeConverter.lexBoolean(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public byte getAttributeByteValue(int index) throws XMLStreamException
    {
        try
        {
            return XsTypeConverter.lexByte(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public short getAttributeShortValue(int index) throws XMLStreamException
    {
        try
        {
            return XsTypeConverter.lexShort(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public int getAttributeIntValue(int index) throws XMLStreamException
    {
        try
        {
            return XsTypeConverter.lexInt(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public long getAttributeLongValue(int index) throws XMLStreamException
    {
        try
        {
            return XsTypeConverter.lexLong(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public BigInteger getAttributeBigIntegerValue(int index) throws XMLStreamException
    {
        try
        {
            return XsTypeConverter.lexInteger(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public BigDecimal getAttributeBigDecimalValue(int index) throws XMLStreamException
    {
        try
        {
            return XsTypeConverter.lexDecimal(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public float getAttributeFloatValue(int index) throws XMLStreamException
    {
        try
        {
            return XsTypeConverter.lexFloat(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public double getAttributeDoubleValue(int index) throws XMLStreamException
    {
        try
        {
            return XsTypeConverter.lexDouble(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public InputStream getAttributeHexBinaryValue(int index) throws XMLStreamException
    {
        String text = _charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM).toString();
        byte[] buf = HexBin.decode(text.getBytes());
        if (buf!=null)
            return new ByteArrayInputStream(buf);
        else
            throw new InvalidLexicalValueException("invalid hexBinary value");
    }

    public InputStream getAttributeBase64Value(int index) throws XMLStreamException
    {
        String text = _charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM).toString();
        byte[] buf = Base64.decode(text.getBytes());
        if (buf!=null)
            return new ByteArrayInputStream(buf);
        else
            throw new InvalidLexicalValueException("invalid base64Binary value");
    }

    public XmlCalendar getAttributeCalendarValue(int index) throws XMLStreamException
    {
        try
        {
            return new GDateBuilder(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM)).
                getCalendar();
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public Date getAttributeDateValue(int index) throws XMLStreamException
    {
        try
        {
            return new GDateBuilder(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM))
                .getDate();
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public GDate getAttributeGDateValue(int index) throws XMLStreamException
    {
        try
        {
            return new GDate(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public GDuration getAttributeGDurationValue(int index) throws XMLStreamException
    {
        try
        {
            return new GDuration(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public QName getAttributeQNameValue(int index) throws XMLStreamException
    {
        return XsTypeConverter.lexQName(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM),
            _xmlStream.getNamespaceContext());
    }

    public String getAttributeStringValue(String uri, String local) throws XMLStreamException
    {
        return _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_PRESERVE).toString();
    }

    public boolean getAttributeBooleanValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexBoolean(cs);
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public byte getAttributeByteValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexByte(cs);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public short getAttributeShortValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexShort(cs);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public int getAttributeIntValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexInt(cs);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public long getAttributeLongValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexLong(cs);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public BigInteger getAttributeBigIntegerValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexInteger(cs);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public BigDecimal getAttributeBigDecimalValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexDecimal(cs);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public float getAttributeFloatValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexFloat(cs);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public double getAttributeDoubleValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexDouble(cs);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public InputStream getAttributeHexBinaryValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        String text = cs.toString();
        byte[] buf = HexBin.decode(text.getBytes());
        if (buf!=null)
            return new ByteArrayInputStream(buf);
        else
            throw new InvalidLexicalValueException("invalid hexBinary value");
    }

    public InputStream getAttributeBase64Value(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        String text = cs.toString();
        byte[] buf = Base64.decode(text.getBytes());
        if (buf!=null)
            return new ByteArrayInputStream(buf);
        else
            throw new InvalidLexicalValueException("invalid base64Binary value");
    }

    public XmlCalendar getAttributeCalendarValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return new GDateBuilder(cs).getCalendar();
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e);
        }
    }

    public Date getAttributeDateValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        return new GDateBuilder(cs).getDate();
    }

    public GDate getAttributeGDateValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        return new GDate(cs);
    }

    public GDuration getAttributeGDurationValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        return new GDuration(cs);
    }

    public QName getAttributeQNameValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        return XsTypeConverter.lexQName(cs, _xmlStream.getNamespaceContext());
    }

    /**
     * Only trims the XML whaitspace at edges, it should not be used for WS collapse
     * Used for int, short, byte
     */
    private static class CharSeqTrimWS
        implements CharSequence
    {
        final static int XMLWHITESPACE_PRESERVE = 1;
        final static int XMLWHITESPACE_TRIM = 2;

        private static int INITIAL_SIZE = 100;
        private char[] _buf = new char[INITIAL_SIZE];
        private int _start, _length = 0;
        private int _nonWSStart = 0;
        private int _nonWSEnd = 0;
        private String _toStringValue;
        private XMLStreamReader _xmlSteam;
        private boolean _supportForGetTextCharacters = true;

        CharSeqTrimWS(XMLStreamReader xmlSteam)
        {
            _xmlSteam = xmlSteam;
        }

        void reload(int style)
            throws XMLStreamException
        {
            _toStringValue = null;

            fillBuffer();

            if (style==XMLWHITESPACE_PRESERVE)
            {
                _nonWSStart = 0;
                _nonWSEnd = _length;
            }
            else if (style==XMLWHITESPACE_TRIM)
            {
                for (_nonWSStart=0; _nonWSStart<_length; _nonWSStart++)
                    if (!XMLChar.isSpace(_buf[_nonWSStart]))
                        break;
                for (_nonWSEnd=_length; _nonWSEnd>_nonWSStart; _nonWSEnd--)
                    if (!XMLChar.isSpace(_buf[_nonWSEnd-1]))
                        break;
            }
        }

        private void fillBuffer()
            throws XMLStreamException
        {
            _length = 0;

            if (_xmlSteam.getEventType() == XMLStreamReader.START_DOCUMENT)
                _xmlSteam.next();
            if (_xmlSteam.isStartElement())
                _xmlSteam.next();

            int depth = 0;
            String error = null;
            int eventType = _xmlSteam.getEventType();

            loop:
            while(true)
            {
                switch(eventType)
                {
                case XMLStreamReader.CDATA:
                case XMLStreamReader.CHARACTERS:
                case XMLStreamReader.SPACE:
                    if (depth==0)
                        addTextToBuffer();
                    break;

                case XMLStreamReader.ATTRIBUTE:
                case XMLStreamReader.COMMENT:
                case XMLStreamReader.DTD:
                case XMLStreamReader.ENTITY_DECLARATION:
                case XMLStreamReader.NAMESPACE:
                case XMLStreamReader.NOTATION_DECLARATION:
                case XMLStreamReader.PROCESSING_INSTRUCTION:
                case XMLStreamReader.START_DOCUMENT:
                    // ignore
                    break;

                case XMLStreamReader.END_DOCUMENT:
                    break loop;

                case XMLStreamReader.END_ELEMENT:
                    depth--;
                    if (depth<0)
                        break loop;
                    break;

                case XMLStreamReader.ENTITY_REFERENCE:
                    addEntityToBuffer();
                    break;

                case XMLStreamReader.START_ELEMENT:
                    depth++;
                    error = "Unexpected element '" + _xmlSteam.getName() + "' in text content.";
                    break;
                }
                eventType = _xmlSteam.next();
            }
            if (error!=null)
                throw new XMLStreamException(error);
        }

        private void addTextToBuffer()
        {
            int textLength = _xmlSteam.getTextLength();

            if (_length + textLength>_buf.length)
            {
                char[] newBuf = new char[_length + textLength];
                if (_length>0)
                    System.arraycopy(_buf, 0, newBuf, 0, _length);
                _buf = newBuf;
            }

            if (_supportForGetTextCharacters)
                try
                {
                    _length = _xmlSteam.getTextCharacters(0, _buf, _length, textLength);
                }
                catch(Exception e)
                {
                    _supportForGetTextCharacters = false;
                }

            if(!_supportForGetTextCharacters)
            {
                System.arraycopy(_xmlSteam.getTextCharacters(), _xmlSteam.getTextStart(), _buf, _length, textLength);
                _length = _length + textLength;
            }
        }

        private void addEntityToBuffer()
        {
            String text = _xmlSteam.getText();

            int textLength = text.length();

            if (_length + textLength>_buf.length)
            {
                char[] newBuf = new char[_length + textLength];
                if (_length>0)
                    System.arraycopy(_buf, 0, newBuf, 0, _length);
                _buf = newBuf;
            }

            text.getChars(0, text.length(), _buf, _length);
            _length = _length + text.length();
        }

        CharSequence reloadAtt(int index, int style)
            throws XMLStreamException
        {
            String value = _xmlSteam.getAttributeValue(index);
            int length = value.length();

            if (style==XMLWHITESPACE_PRESERVE)
            {
                return value;
            }
            else if (style==XMLWHITESPACE_TRIM)
            {
                int nonWSStart, nonWSEnd;
                for (nonWSStart=0; nonWSStart<length; nonWSStart++)
                    if (!XMLChar.isSpace(value.charAt(nonWSStart)))
                        break;
                for (nonWSEnd=length; nonWSEnd>nonWSStart; nonWSEnd--)
                    if (!XMLChar.isSpace(value.charAt(nonWSEnd-1)))
                        break;
                if (nonWSStart==0 && nonWSEnd==length)
                    return value;
                else
                    return value.subSequence(nonWSStart, nonWSEnd);
            }
            throw new IllegalStateException("unknown style");
        }

        CharSequence reloadAtt(String uri, String local, int style)
            throws XMLStreamException
        {
            String value = _xmlSteam.getAttributeValue(uri, local);
            int length = value.length();

            if (style==XMLWHITESPACE_PRESERVE)
            {
                return value;
            }
            else if (style==XMLWHITESPACE_TRIM)
            {
                for (_nonWSStart=0; _nonWSStart<_length; _nonWSStart++)
                    if (!XMLChar.isSpace(value.charAt(_nonWSStart)))
                        break;
                for (_nonWSEnd=_length; _nonWSEnd>_nonWSStart; _nonWSEnd--)
                    if (!XMLChar.isSpace(value.charAt(_nonWSEnd-1)))
                        break;
                if (_nonWSStart==0 && _nonWSEnd==_length)
                    return value;
                else
                    return value.subSequence(_nonWSStart, _nonWSEnd);
            }
            throw new IllegalStateException("unknown style");
        }

        public int length()
        {
            return _nonWSEnd - _nonWSStart;
        }

        public char charAt(int index)
        {
            // for each char, this has to be fast, using assert instead of if throw
            assert (index<_nonWSEnd-_nonWSStart && -1<index) :
                "Index " + index + " must be >-1 and <" + (_nonWSEnd - _nonWSStart);

            return _buf[_nonWSStart + index];
        }

        public CharSequence subSequence(int start, int end)
        {
            return new String(_buf, _nonWSStart + start, end - start);
        }

        public String toString()
        {
            if (_toStringValue!=null)
                return _toStringValue;

            _toStringValue = new String(_buf, _nonWSStart, _nonWSEnd - _nonWSStart);
            return _toStringValue;
        }
    }

    // XMLStreamReader methods
    public Object getProperty(String s)
        throws IllegalArgumentException
    {
        return _xmlStream.getProperty(s);
    }

    public int next()
        throws XMLStreamException
    {
        return _xmlStream.next();
    }

    public void require(int i, String s, String s1)
        throws XMLStreamException
    {
        _xmlStream.require(i, s, s1);
    }

    public String getElementText() throws XMLStreamException
    {
        return _xmlStream.getElementText();
    }

    public int nextTag() throws XMLStreamException
    {
        return _xmlStream.nextTag();
    }

    public boolean hasNext() throws XMLStreamException
    {
        return _xmlStream.hasNext();
    }

    public void close() throws XMLStreamException
    {
        _xmlStream.close();
    }

    public String getNamespaceURI(String s)
    {
        return _xmlStream.getNamespaceURI(s);
    }

    public boolean isStartElement()
    {
        return _xmlStream.isStartElement();
    }

    public boolean isEndElement()
    {
        return _xmlStream.isEndElement();
    }

    public boolean isCharacters()
    {
        return _xmlStream.isCharacters();
    }

    public boolean isWhiteSpace()
    {
        return _xmlStream.isWhiteSpace();
    }

    public String getAttributeValue(String s, String s1)
    {
        return _xmlStream.getAttributeValue(s, s1);
    }

    public int getAttributeCount()
    {
        return _xmlStream.getAttributeCount();
    }

    public QName getAttributeName(int i)
    {
        return _xmlStream.getAttributeName(i);
    }

    public String getAttributeNamespace(int i)
    {
        return _xmlStream.getAttributeNamespace(i);
    }

    public String getAttributeLocalName(int i)
    {
        return _xmlStream.getAttributeLocalName(i);
    }

    public String getAttributePrefix(int i)
    {
        return _xmlStream.getAttributePrefix(i);
    }

    public String getAttributeType(int i)
    {
        return _xmlStream.getAttributeType(i);
    }

    public String getAttributeValue(int i)
    {
        return _xmlStream.getAttributeValue(i);
    }

    public boolean isAttributeSpecified(int i)
    {
        return _xmlStream.isAttributeSpecified(i);
    }

    public int getNamespaceCount()
    {
        return _xmlStream.getNamespaceCount();
    }

    public String getNamespacePrefix(int i)
    {
        return _xmlStream.getNamespacePrefix(i);
    }

    public String getNamespaceURI(int i)
    {
        return _xmlStream.getNamespaceURI(i);
    }

    public NamespaceContext getNamespaceContext()
    {
        return _xmlStream.getNamespaceContext();
    }

    public int getEventType()
    {
        return _xmlStream.getEventType();
    }

    public String getText()
    {
        return _xmlStream.getText();
    }

    public char[] getTextCharacters()
    {
        return _xmlStream.getTextCharacters();
    }

    public int getTextCharacters(int i, char[] chars, int i1, int i2)
        throws XMLStreamException
    {
        return _xmlStream.getTextCharacters(i, chars, i1, i2);
    }

    public int getTextStart()
    {
        return _xmlStream.getTextStart();
    }

    public int getTextLength()
    {
        return _xmlStream.getTextLength();
    }

    public String getEncoding()
    {
        return _xmlStream.getEncoding();
    }

    public boolean hasText()
    {
        return _xmlStream.hasText();
    }

    public Location getLocation()
    {
        return _xmlStream.getLocation();
    }

    public QName getName()
    {
        return _xmlStream.getName();
    }

    public String getLocalName()
    {
        return _xmlStream.getLocalName();
    }

    public boolean hasName()
    {
        return _xmlStream.hasName();
    }

    public String getNamespaceURI()
    {
        return _xmlStream.getNamespaceURI();
    }

    public String getPrefix()
    {
        return _xmlStream.getPrefix();
    }

    public String getVersion()
    {
        return _xmlStream.getVersion();
    }

    public boolean isStandalone()
    {
        return _xmlStream.isStandalone();
    }

    public boolean standaloneSet()
    {
        return _xmlStream.standaloneSet();
    }

    public String getCharacterEncodingScheme()
    {
        return _xmlStream.getCharacterEncodingScheme();
    }

    public String getPITarget()
    {
        return _xmlStream.getPITarget();
    }

    public String getPIData()
    {
        return _xmlStream.getPIData();
    }
}

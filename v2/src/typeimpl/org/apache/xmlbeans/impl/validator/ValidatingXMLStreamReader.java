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
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.common.ValidatorListener;
import org.apache.xmlbeans.impl.common.XmlWhitespace;
import org.apache.xmlbeans.impl.common.QNameHelper;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.Location;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.StreamReaderDelegate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class is a wrapper over a generic XMLStreamReader that provides validation.
 * There are 3 cases:
 * <br/> 1) the XMLStreamReader represents a document, it contains only one element document
 *          - in this case the user schema type should be null or it should be a document SchemaType
 * <br/> 2) the XMLStreamReader represents an xml-fragment (content only) - must have at least one user type or xsi:type
 * <br/>     a) it has an xsi:type - if user schema type is available it has to be a base type of xsi:type
 * <br/>     b) it doesn't have xsi:type - user must provide a schema type
 *         otherwise will error and will not do validation
 * <br/> 3) the XMLStreamReader represents a global attribute - i.e. user schema type is null and only one attribute
 * <br/>
 *
 * @author Cezar Andrei (cezar.andrei at bea.com)
 * Date: Feb 13, 2004
 */
public class ValidatingXMLStreamReader
    extends StreamReaderDelegate
    implements XMLStreamReader
{
    private static final String URI_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    private static final QName XSI_TYPE = new QName(URI_XSI, "type");
    private static final QName XSI_NIL  = new QName(URI_XSI, "nil");
    private static final QName XSI_SL   = new QName(URI_XSI, "schemaLocation");
    private static final QName XSI_NSL  = new QName(URI_XSI, "noNamespaceSchemaLocation");

    private SchemaType _contentType;
    private SchemaTypeLoader _stl;
    private XmlOptions _options;
    private Collection _errorListener;
    protected Validator _validator;
    private final ElementEventImpl _elemEvent;
    private final AttributeEventImpl _attEvent;
    private final SimpleEventImpl _simpleEvent;

    private int _state;
    private final int STATE_FIRSTEVENT = 0;
    private final int STATE_VALIDATING = 1;
    private final int STATE_ATTBUFFERING = 2;
    private final int STATE_ERROR = 3;

    private List _attNamesList;
    private List _attValuesList;
    private SchemaType _xsiType;

    private int _depth;

    /**
     * Default constructor. Use init(...) to set the params.
     * See {@link #init}
     */
    public ValidatingXMLStreamReader()
    {
        super();
        _elemEvent = new ElementEventImpl();
        _attEvent = new AttributeEventImpl();
        _simpleEvent = new SimpleEventImpl();
    }

    /**
     * Used in case of reusing the same ValidatinXMLStreamReader object
     * @param xsr The stream to be validated
     * @param startWithCurrentEvent Validation will start if true with the current event or if false with the next event in the stream
     * @param contentType The schemaType of the content. This can be null for document and global Att validation
     * @param stl SchemaTypeLoader context of validation
     * @param options Validator options
     * @param errorListener Errors and warnings listener
     */
    public void init(XMLStreamReader xsr, boolean startWithCurrentEvent, SchemaType contentType,
                     SchemaTypeLoader stl, XmlOptions options, Collection errorListener)
    {
        setParent(xsr);
        _contentType = contentType;
        _stl = stl;
        _options = options;
        _errorListener = errorListener;
        _elemEvent.setXMLStreamReader(xsr);
        _attEvent.setXMLStreamReader(xsr);
        _simpleEvent.setXMLStreamReader(xsr);
        _validator = null;
        _state = STATE_FIRSTEVENT;
        if (_attNamesList!=null)
        {
            _attNamesList.clear();
            _attValuesList.clear();
        }
        _xsiType = null;
        _depth = 0;

        if (startWithCurrentEvent)
        {
            int evType = getEventType();
            validate_event(evType);
        }
    }

    private static class ElementEventImpl
        implements ValidatorListener.Event
    {
        private static final int BUF_LENGTH = 1024;
        private char[] _buf = new char[BUF_LENGTH];
        private int _length;
        private boolean _supportForGetTextCharacters = true;

        private XMLStreamReader _xmlStream;

        private void setXMLStreamReader(XMLStreamReader xsr)
        {
            _xmlStream = xsr;
        }

        // can return null, used only to locate errors
        public XmlCursor getLocationAsCursor()
        {
            return null;
        }

        public javax.xml.stream.Location getLocation()
        {
            return _xmlStream.getLocation();
        }

        // fill up chars with the xsi:type attribute value if there is one othervise return false
        public String getXsiType() // BEGIN xsi:type
        {
            return _xmlStream.getAttributeValue(URI_XSI, "type");
        }

        // fill up chars with xsi:nill attribute value if any
        public String getXsiNil() // BEGIN xsi:nil
        {
            return _xmlStream.getAttributeValue(URI_XSI, "nil");
        }

        // not used curently
        public String getXsiLoc() // BEGIN xsi:schemaLocation
        {
            return _xmlStream.getAttributeValue(URI_XSI, "schemaLocation");
        }

        // not used curently
        public String getXsiNoLoc() // BEGIN xsi:noNamespaceSchemaLocation
        {
            return _xmlStream.getAttributeValue(URI_XSI, "noNamespaceSchemaLocation");
        }

        // On START and ATTR
        public QName getName()
        {
            // avoid construction of a new QName object after the bug in getName() is fixed.
            QName qname = new QName(_xmlStream.getNamespaceURI(), _xmlStream.getLocalName());
            return qname;
        }

        // On TEXT and ATTR
        public String getText()
        {
            _length = 0;
            addTextToBuffer();
            return new String( _buf, 0, _length );
        }

        public String getText(int wsr)
        {
            return XmlWhitespace.collapse( _xmlStream.getText(), wsr );
        }

        public boolean textIsWhitespace()
        {
            return _xmlStream.isWhiteSpace();
        }

        public String getNamespaceForPrefix(String prefix)
        {
            return _xmlStream.getNamespaceURI(prefix);
        }

        private void addTextToBuffer()
        {
            int textLength = _xmlStream.getTextLength();
            ensureBufferLength(textLength);

            if (_supportForGetTextCharacters)
                try
                {
                    _length = _xmlStream.getTextCharacters(0, _buf, _length, textLength);
                }
                catch(Exception e)
                {
                    _supportForGetTextCharacters = false;
                }

            if(!_supportForGetTextCharacters)
            {
                System.arraycopy(_xmlStream.getTextCharacters(), _xmlStream.getTextStart(), _buf, _length, textLength);
                _length = _length + textLength;
            }
        }

        private void ensureBufferLength(int lengthToAdd)
        {
            if (_length + lengthToAdd>_buf.length)
            {
                char[] newBuf = new char[_length + lengthToAdd];
                if (_length>0)
                    System.arraycopy(_buf, 0, newBuf, 0, _length);
                _buf = newBuf;
            }
        }
    }

    private static final class AttributeEventImpl
        implements ValidatorListener.Event
    {
        private int _attIndex;
        private int _length;
        private XMLStreamReader _xmlStream;

        private void setXMLStreamReader(XMLStreamReader xsr)
        {
            _xmlStream = xsr;
        }

        // can return null, used only to locate errors
        public XmlCursor getLocationAsCursor()
        {
            return null;
        }

        public javax.xml.stream.Location getLocation()
        {
            return _xmlStream.getLocation();
        }

        // fill up chars with the xsi:type attribute value if there is one othervise return false
        public String getXsiType() // BEGIN xsi:type
        {
            throw new IllegalStateException();
        }

        // fill up chars with xsi:nill attribute value if any
        public String getXsiNil() // BEGIN xsi:nil
        {
            throw new IllegalStateException();
        }

        // not used curently
        public String getXsiLoc() // BEGIN xsi:schemaLocation
        {
            throw new IllegalStateException();
        }

        // not used curently
        public String getXsiNoLoc() // BEGIN xsi:noNamespaceSchemaLocation
        {
            throw new IllegalStateException();
        }

        // On START and ATTR
        public QName getName()
        {
            assert _xmlStream.isStartElement() : "Not on Start Element.";
            String uri = _xmlStream.getAttributeNamespace(_attIndex);
            QName qn = new QName(uri==null ? "" : uri, _xmlStream.getAttributeLocalName(_attIndex));
            //System.out.println("    Att QName: " + qn);
            return qn;
        }

        // On TEXT and ATTR
        public String getText()
        {
            assert _xmlStream.isStartElement() : "Not on Start Element.";
            return _xmlStream.getAttributeValue(_attIndex);
        }

        public String getText(int wsr)
        {
            assert _xmlStream.isStartElement() : "Not on Start Element.";
            return XmlWhitespace.collapse( _xmlStream.getAttributeValue(_attIndex), wsr );
        }

        public boolean textIsWhitespace()
        {
            throw new IllegalStateException();
        }

        public String getNamespaceForPrefix(String prefix)
        {
            assert _xmlStream.isStartElement() : "Not on Start Element.";
            return _xmlStream.getNamespaceURI(prefix);
        }

        private void setAttributeIndex(int attIndex)
        {
            _attIndex = attIndex;
        }
    }

    /**
     * This is used as implementation of Event for validating global attributes
     * and for pushing the buffered attributes
     */
    private static final class SimpleEventImpl
        implements ValidatorListener.Event
    {
        private String _text;
        private int _length;
        private QName  _qname;
        private XMLStreamReader _xmlStream;

        private void setXMLStreamReader(XMLStreamReader xsr)
        {
            _xmlStream = xsr;
        }

        // should return null, getLocation will be used, used only to locate errors
        public XmlCursor getLocationAsCursor()
        { return null; }

        public javax.xml.stream.Location getLocation()
        {
            return _xmlStream.getLocation();
        }

        // fill up chars with the xsi:type attribute value if there is one othervise return false
        public String getXsiType() // BEGIN xsi:type
        { return null; }

        // fill up chars with xsi:nill attribute value if any
        public String getXsiNil() // BEGIN xsi:nil
        { return null; }

        // not used curently
        public String getXsiLoc() // BEGIN xsi:schemaLocation
        { return null; }

        // not used curently
        public String getXsiNoLoc() // BEGIN xsi:noNamespaceSchemaLocation
        { return null; }

        // On START and ATTR
        public QName getName()
        { return _qname; }

        // On TEXT and ATTR
        public String getText()
        {
            return _text;
        }

        public String getText(int wsr)
        {
            return XmlWhitespace.collapse( _text, wsr );
        }

        public boolean textIsWhitespace()
        { return false; }

        public String getNamespaceForPrefix(String prefix)
        {
            return _xmlStream.getNamespaceURI(prefix);
        }
    }

    /* public methods in XMLStreamReader */

    public Object getProperty(String s) throws IllegalArgumentException
    {
        return super.getProperty(s);
    }

    public int next() throws XMLStreamException
    {
        int evType = super.next();
//        debugEvent(evType);

        validate_event(evType);

        return evType;
    }

    private void validate_event(int evType)
    {
        if (_state==STATE_ERROR)
            return;

        if (_depth<0)
            throw new IllegalArgumentException("ValidatingXMLStreamReader cannot go further than the subtree is was initialized on.");

        switch(evType)
        {
        case XMLEvent.START_ELEMENT:
            _depth++;
            if (_state == STATE_ATTBUFFERING)
                pushBufferedAttributes();

            if (_validator==null)
            {
                // avoid construction of a new QName object after the bug in getName() is fixed.
                QName qname = new QName(getNamespaceURI(), getLocalName());

                if (_contentType==null)
                    _contentType = typeForGlobalElement(qname);

                if (_state==STATE_ERROR)
                    break;

                initValidator(_contentType);
                _validator.nextEvent(Validator.BEGIN, _elemEvent);
            }

            _validator.nextEvent(Validator.BEGIN, _elemEvent);

            int attCount = getAttributeCount();
            for(int i=0; i<attCount; i++)
            {
                _attEvent.setAttributeIndex(i);
                QName qn = _attEvent.getName();
                if (isSpecialAttribute(qn))
                    continue;

                _validator.nextEvent(Validator.ATTR, _attEvent);
            }
            break;

        case XMLEvent.ATTRIBUTE:
            if (getAttributeCount()==0)
                break;

            if (_state == STATE_FIRSTEVENT || _state == STATE_ATTBUFFERING)
            {
                // buffer all Attributes
                for (int i=0; i<getAttributeCount(); i++)
                {
                    // avoid construction of a new QName object after the bug in getName() is fixed.
                    QName qname = new QName(getAttributeNamespace(i), getAttributeLocalName(i));

                    if (qname.equals(XSI_TYPE))
                    {
                        String xsiTypeValue = getAttributeValue(i);
                        String uri = super.getNamespaceURI(QNameHelper.getPrefixPart(xsiTypeValue));
                        QName xsiTypeQname = new QName(uri, QNameHelper.getLocalPart(xsiTypeValue));
                        _xsiType = _stl.findType(xsiTypeQname);
                    }

                    if (_attNamesList==null)
                    {
                        _attNamesList = new ArrayList();
                        _attValuesList = new ArrayList();
                    }
                    // skip xsi:type xsi:nil xsi:schemaLocation xsi:noNamespaceSchemaLocation
                    if (isSpecialAttribute(qname))
                        continue;

                    _attNamesList.add(qname);
                    _attValuesList.add(getAttributeValue(i));
                }
                _state = STATE_ATTBUFFERING;
            }
            else
                throw new IllegalStateException("ATT event must be only at the beggining of the stream.");

            break;

        case XMLEvent.END_ELEMENT:
        case XMLEvent.END_DOCUMENT:
            _depth--;
            if (_state == STATE_ATTBUFFERING)
                pushBufferedAttributes();

            _validator.nextEvent(Validator.END, _elemEvent);
            break;

        case XMLEvent.CDATA:
        case XMLEvent.CHARACTERS:
            if (_state == STATE_ATTBUFFERING)
                pushBufferedAttributes();

            if (_validator==null)
            {
                if (_contentType==null)
                {
                    if (isWhiteSpace()) // hack/workaround for avoiding errors for parsers that do not generate XMLEvent.SPACE
                        break;

                    addError("No content type provided for validation of a content model.");
                    _state = STATE_ERROR;
                    break;
                }
                initValidator(_contentType);
                _validator.nextEvent(Validator.BEGIN, _simpleEvent);
            }

            _validator.nextEvent(Validator.TEXT, _elemEvent);
            break;

        case XMLEvent.START_DOCUMENT:
            _depth++;
            break;

        case XMLEvent.COMMENT:
        case XMLEvent.DTD:
        case XMLEvent.ENTITY_DECLARATION:
        case XMLEvent.ENTITY_REFERENCE:
        case XMLEvent.NAMESPACE:
        case XMLEvent.NOTATION_DECLARATION:
        case XMLEvent.PROCESSING_INSTRUCTION:
        case XMLEvent.SPACE:
            //ignore
            break;

        default:
            throw new IllegalStateException("Unknown event type.");
        }
    }

    private void pushBufferedAttributes()
    {
        SchemaType validationType = null;

        if (_xsiType!=null)
        {
            if (_contentType==null)
            {
                validationType = _xsiType;
            }
            else
            {
                // we have both _xsiType and _contentType
                if (_contentType.isAssignableFrom(_xsiType))
                {
                    validationType = _xsiType;
                }
                else
                {
                    addError("Specified type '" + _contentType +
                        "' not compatible with found xsi:type '" + _xsiType + "'.");
                    _state = STATE_ERROR;
                    return;
                }
            }
        }
        else
        {
            if (_contentType != null)
            {
                validationType = _contentType;
            }
            else if (_attNamesList!=null)
            {
                // no xsi:type, no _contentType
                // this is the global attribute case
                validationType = _stl.findAttributeType((QName)_attNamesList.get(0));
                if (validationType==null)
                {
                    addError("A schema global element with name '" + _attNamesList.get(0) +
                        "' could not be found in the current schema type loader.");
                    _state = STATE_ERROR;
                    return;
                }
                // if _attNamesList.size() > 1 than the validator will add an error
            }
            else
            {
                addError("No content type provided for validation of a content model.");
                _state = STATE_ERROR;
                return;
            }
        }

        // here validationType is the right type, start pushing all acumulated attributes
        initValidator(validationType);
        _validator.nextEvent(Validator.BEGIN, _simpleEvent);

        for (int i=0; i<_attNamesList.size(); i++)
        {
            _simpleEvent._qname = (QName)_attNamesList.get(i);
            _simpleEvent._text = (String)_attValuesList.get(i);
            _validator.nextEvent(Validator.ATTR, _simpleEvent);
        }

        _state = STATE_VALIDATING;
    }

    private boolean isSpecialAttribute(QName qn)
    {
        if (qn.getNamespaceURI().equals(URI_XSI))
            return qn.getLocalPart().equals(XSI_TYPE.getLocalPart()) ||
                qn.getLocalPart().equals(XSI_NIL.getLocalPart()) ||
                qn.getLocalPart().equals(XSI_SL.getLocalPart()) ||
                qn.getLocalPart().equals(XSI_NSL.getLocalPart());

        return false;
    }

    /**
     * Initializes the validator for the given schemaType
     * @param schemaType
     */
    private void initValidator(SchemaType schemaType)
    {
        assert schemaType!=null;

        _validator = new Validator(schemaType, null, _stl, _options, _errorListener);
    }

    private SchemaType typeForGlobalElement(QName qname)
    {
        assert qname!=null;

        SchemaType docType = _stl.findDocumentType(qname);

        if (docType==null)
        {
            addError("Schema document type not found for element '" + qname + "'.");
            _state = STATE_ERROR;
        }
        return docType;
    }

    private void addError(String msg)
    {
        String source = null;
        Location location = null;
//        Location location = getLocation();

        if (location != null)
        {
            source = location.getPublicId();
            if (source==null)
                source = location.getSystemId();
            
            _errorListener.add(XmlError.forLocation(msg, source, location));
        }
        else
            _errorListener.add(XmlError.forMessage(msg));
    }

    /**
     * @return Returns the validation state up to this point.
     * NOTE: At least one START ELEMENT should have been consumed for a valid value to be returned.
     */
    public boolean isValid()
    {
        if ( _state==STATE_ERROR || _validator==null)
            return false;

        return _validator.isValid();
    }

//    /* for unit testing */
//    public static void main(String[] args) throws FileNotFoundException, XMLStreamException
//    {
//        ValidatingXMLStreamReader valXsr = new ValidatingXMLStreamReader();
//        for( int i = 0; i<args.length; i++)
//        {
//            validate(valXsr, args[i]);
//        }
//    }
//
//    private static void validate(ValidatingXMLStreamReader valXsr, String file)
//        throws XMLStreamException, FileNotFoundException
//    {
//        Collection errors = new ArrayList();
//        XMLStreamReader xsr = XMLInputFactory.newInstance().
//            createXMLStreamReader(new FileInputStream(new File(file)));
//        valXsr.init(xsr, null,
//            XmlBeans.typeLoaderForClassLoader(ValidatingXMLStreamReader.class.getClassLoader()),
//            null,
//            errors);
//
//        while( valXsr.hasNext() )
//        {
//            valXsr.next();
//        }
//
//        System.out.println("File '" + file + "' is: " + (valXsr.isValid() ? "Valid" : "INVALID") + "\t\t\t\t ----------");
//        for (Iterator i = errors.iterator(); i.hasNext(); )
//        {
//            XmlError err = (XmlError)i.next();
//            System.out.println("ERROR " + err.getSeverity() + " " + err.getLine() + ":" + err.getColumn() + " " +
//                err.getMessage() + " ");
//        }
//    }
//
//    private void debugEvent(int evType)
//    {
//        switch(evType)
//        {
//        case XMLEvent.START_ELEMENT:
//            System.out.println("SE     " + _elemEvent.getName());
//            break;
//        case XMLEvent.START_DOCUMENT:
//            System.out.println("SDoc");
//            break;
//        case XMLEvent.END_ELEMENT:
//            System.out.println("EE     " + _elemEvent.getName());
//            break;
//        case XMLEvent.END_DOCUMENT:
//            System.out.println("EDoc");
//            break;
//        case XMLEvent.SPACE:
//            System.out.println("SPACE");
//            break;
//        case XMLEvent.CDATA:
//            System.out.println("CDATA");
//            break;
//        case XMLEvent.CHARACTERS:
//            String c = _elemEvent.getText();
//            System.out.println("TEXT     " + c);
//            break;
//
//        case XMLEvent.ATTRIBUTE:      // global attributes
//            System.out.println("ATT     count: " + _elemEvent._xmlStream.getAttributeCount());
//            for(int i=0; i<_elemEvent._xmlStream.getAttributeCount(); i++)
//            {
//                System.out.println("\t\t" + _elemEvent._xmlStream.getAttributeNamespace(i) + ":" +
//                    _elemEvent._xmlStream.getAttributeLocalName(i) + "  =  " +
//                    _elemEvent._xmlStream.getAttributeValue(i));
//            }
//            break;
//        case XMLEvent.COMMENT:
//            System.out.println("COMMENT");
//            break;
//        case XMLEvent.DTD:
//            System.out.println("DTD");
//            break;
//        case XMLEvent.ENTITY_DECLARATION:
//            System.out.println("ENTITY_DECL");
//            break;
//        case XMLEvent.ENTITY_REFERENCE:
//            System.out.println("ENTITY_REF");
//            break;
//        case XMLEvent.NAMESPACE:
//            System.out.println("NS");
//            break;
//        case XMLEvent.NOTATION_DECLARATION:
//            System.out.println("NOTATION_DECL");
//            break;
//        case XMLEvent.PROCESSING_INSTRUCTION:
//            System.out.println("PI");
//            break;
//        }
//    }
}

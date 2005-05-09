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

package org.apache.xmlbeans.impl.store;

import java.lang.reflect.Method;
import java.lang.ref.SoftReference;

import org.apache.xmlbeans.impl.common.EncodingMap;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.common.XMLNameHelper;
import org.apache.xmlbeans.impl.store.Splay.Finish;
import org.apache.xmlbeans.impl.values.NamespaceManager;
import org.apache.xmlbeans.impl.values.XmlStore;
import org.apache.xmlbeans.impl.values.TypeStoreFactory;
import org.apache.xmlbeans.QNameCache;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.XmlBookmark;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlLineNumber;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.XmlSaxHandler;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.apache.xmlbeans.xml.stream.Attribute;
import org.apache.xmlbeans.xml.stream.AttributeIterator;
import org.apache.xmlbeans.xml.stream.CharacterData;
import org.apache.xmlbeans.xml.stream.Location;
import org.apache.xmlbeans.xml.stream.ProcessingInstruction;
import org.apache.xmlbeans.xml.stream.Space;
import org.apache.xmlbeans.xml.stream.StartDocument;
import org.apache.xmlbeans.xml.stream.StartElement;
import org.apache.xmlbeans.xml.stream.XMLEvent;
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.apache.xmlbeans.xml.stream.XMLName;
import org.apache.xmlbeans.xml.stream.XMLStreamException;

import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public final class Root extends Finish implements XmlStore
{
    public Root ( SchemaTypeLoader stl, SchemaType type, XmlOptions options )
    {
        super( ROOT );

        assert stl != null;

        _schemaTypeSystem = stl;

        _leftOnly = true;

        _props = new DocProps();

        _doc = new Doc( this, null );

        _leftSplay = _doc;
        _doc._parentSplay = this;
        adjustCdocBeginLeft( _doc.getCdocBegin() );

        _text = new Text();

        SchemaType sType = null;

        options = XmlOptions.maskNull( options );
        
        if (options.hasOption( XmlOptions.DOCUMENT_TYPE ))
            sType = (SchemaType) options.get( XmlOptions.DOCUMENT_TYPE );

        if (sType == null)
            sType = type;

        if (sType == null)
            sType = XmlObject.type;

        _validateOnSet = options.hasOption( XmlOptions.VALIDATE_ON_SET );

        _factory = (TypeStoreFactory) options.get( TypeStoreFactory.KEY );

        _doc.setType( this, sType );
    }

    public static XmlStore newStore ( SchemaTypeLoader stl, SchemaType type, XmlOptions options )
    {
        return new Root( stl, type, options );
    }

    public XmlDocumentProperties documentProperties ( )
    {
        return _props;
    }

    public XmlCursor createCursor ( )
    {
        assert validate();
        return new Cursor( this, _doc );
    }

    Container getContainer ( ) { return _doc; }

    boolean validateOnSet ( ) { return _validateOnSet; }

    public XmlObject getObject ( )
    {
        Type t = _doc.peekType();

        return t == null ? null : t.getXmlObject();
    }

    public SchemaTypeLoader getSchemaTypeLoader ( )
    {
        return _schemaTypeSystem;
    }

    boolean isEmpty ( )
    {
        return _leftSplay == _doc && _doc._rightSplay == null && _doc.isValid();
    }

    boolean isLeftOnly ( ) { return _leftOnly; }

    void ensureEmpty ( )
    {
        if (!isEmpty())
            _doc.removeContent( this, true );

        assert isEmpty();
        assert validate();
        assert _leftOnly;
        assert getCchLeft() == 0;
        assert _text.length() == 0;
        assert getCdocBeginLeft() == 1;
        assert _doc != null && _leftSplay == _doc;
    }

    void updateCch ( Splay s, int deltaCch )
    {
        assert !s.isRoot();

        if (deltaCch != 0)
        {
            s.splay( this, this );

            s.adjustCch( deltaCch );
            adjustCchLeft( deltaCch );
        }
    }

    int getCp ( Splay s )
    {
        //
        // A left only tree has the nice property that _cchLeft is
        // also the cp!
        //

        if (!_leftOnly && s != this)
            s.splay( this, this );

        return s.getCchLeft();
    }

    int getDocBeginIndex ( Splay s )
    {
        //
        // A left only tree has the nice property that cdocBeginLeft is
        // also the pos!
        //

        if (!_leftOnly)
            s.splay( this, this );

        return s.getCdocBeginLeft();
    }
    
    Begin findNthBegin ( Splay parent, QName name, QNameSet set, int n )
    {
        // only one of (set or name) is not null
        // or both are null for a wildcard
        assert ( name == null || set == null );
        assert n >= 0;

        if (parent == null || parent.isLeaf())
            return null;

        int da = _nthCache_A.distance( parent, name, set, n );
        int db = _nthCache_B.distance( parent, name, set, n );

        Begin b =
            da <= db
                ? _nthCache_A.fetch( parent, name, set, n )
                : _nthCache_B.fetch( parent, name, set, n );

        if (da == db)
        {
            nthCache temp = _nthCache_A;
            _nthCache_A = _nthCache_B;
            _nthCache_B = temp;
        }

        return b;
    }

    int count ( Container parent, QName name, QNameSet set )
    {
        Splay s = findNthBegin( parent, name, set, 0 );

        if (s == null)
            return 0;

        int n = 0;

        for ( ; ; s = s.nextSplay() )
        {
            if (s.isFinish())
                break;

            if (!s.isBegin())
                continue;

            if (set == null)
            {
                if (s.getName().equals(name))
                    n++;
            }
            else
            {
                if (set.contains(s.getName()))
                    n++;
            }

            s = s.getFinishSplay();
        }

        return n;
    }

    /**
     * Set up strong type information based on this schema type system.
     */

    private boolean namespacesSame ( QName n1, QName n2 )
    {
        if (n1 == n2)
            return true;

        if (n1 == null || n2 == null)
            return false;

        if (n1.getNamespaceURI() == n2.getNamespaceURI())
            return true;

        if (n1.getNamespaceURI() == null || n2.getNamespaceURI() == null)
            return false;

        return n1.getNamespaceURI().equals( n2.getNamespaceURI() );
    }

    private void addNamespace ( StringBuffer sb, QName name )
    {
        if (name.getNamespaceURI() == null)
            sb.append( "<no namespace>" );
        else
        {
            sb.append( "\"" );
            sb.append( name.getNamespaceURI() );
            sb.append( "\"" );
        }
    }

    XmlObject autoTypedDocument (
        SchemaType factoryType, XmlOptions options )
            throws XmlException
    {
        // The type in the options has highest precidence because it is
        // supplied by the user.
        
        SchemaType overrideType =
            (SchemaType) XmlOptions.safeGet( options, XmlOptions.DOCUMENT_TYPE );

        // precedence is given to the override above all
        
        SchemaType theType = overrideType;

        // Document and attribute types have no name
        
        if (theType == null &&
                (factoryType == null ||
                    (!factoryType.isDocumentType() &&
                        !factoryType.isAttributeType())))
        {
            // infer type from xsi:type
            QName typeName = _doc.getXsiTypeName( this );

            SchemaType sniffedType =
                typeName == null
                    ? null
                    : _schemaTypeSystem.findType( typeName );

            if (factoryType == null ||
                    factoryType.isAssignableFrom( sniffedType ))
            {
                theType = sniffedType;
            }
        }

        // todo:
        // use the following when implementing subst groups:
        //     factoryType == null || expectedType.isDocumentType()

        if (factoryType == null || factoryType.isDocumentType())
        {
            if (theType == null)
            {
                // infer type based on root elt

                QName docElemName = null;

                XmlCursor c = createCursor();

                if (c.toFirstChild() && !c.toNextSibling())
                    docElemName = c.getName();

                c.dispose();

                if (docElemName != null)
                    theType = _schemaTypeSystem.findDocumentType( docElemName );

                // verify elt inheritance when implementing subst groups
                if (factoryType != null && theType != null)
                {
                    QName factoryElemName = factoryType.getDocumentElementName();

                    if (!factoryElemName.equals(docElemName) &&
                        !factoryType.isValidSubstitution(docElemName))

                        throw new XmlException("Element " + QNameHelper.pretty(docElemName) +
                            " is not a valid " + QNameHelper.pretty(factoryElemName) +
                            " document or a valid substitution.");
                }

            }

            if (theType == null)
            {
                // infer type based on root attr

                QName attrName = null;

                XmlCursor c = createCursor();

                if (c.toFirstAttribute() && !c.toNextAttribute())
                    attrName = c.getName();

                c.dispose();

                if (attrName != null)
                    theType = _schemaTypeSystem.findAttributeType( attrName );
            }
        }

        // sniffing doesn't say anything: assume the expected type
        if (theType == null)
            theType = factoryType;

        // Still nothing: the no type.
        if (theType == null)
            theType = XmlBeans.NO_TYPE;

        // assign type
        _doc.setType( this, theType );

        // todo: Have a similar attribute type check
        if (factoryType != null)
        {
            if (theType.isDocumentType())
                verifyDocumentType( theType.getDocumentElementName() );
            else if (theType.isAttributeType())
                verifyAttributeType( theType.getAttributeTypeAttributeName() );
        }

        //
        // If a type was passed in, usually from generated code which needs the
        // type to be something specific because of a pending cast, then check
        // the resulting type and throw a nice exception.
        //

        if (factoryType != null && !factoryType.isAssignableFrom(theType))
        {
            /*
            System.out.println("Factory type = " + factoryType);
            System.out.println("The type = " + theType);
            System.out.println("basetype of the type = " + theType.getBaseType());
            */

            throw
                new XmlException( "XML object is not of type " + factoryType );
        }

        return getObject();
    }

    private void verifyDocumentType ( QName docElemName )
        throws XmlException
    {
        XmlCursor c = createCursor();

        try
        {
            StringBuffer sb = null;

            if (!c.toFirstChild() || c.toNextSibling())
            {
                sb = new StringBuffer();

                sb.append( "The document is not a " );
                sb.append( QNameHelper.pretty( docElemName ) );

                if (c.currentTokenType().isStartdoc())
                    sb.append( ": no document element" );
                else
                    sb.append( ": multiple document elements" );
            }
            else
            {
                QName name = c.getName();

                if (!name.equals( docElemName ))
                {
                    sb = new StringBuffer();

                    sb.append( "The document is not a " );
                    sb.append( QNameHelper.pretty( docElemName ) );

                    if (docElemName.getLocalPart().equals( name.getLocalPart() ))
                    {
                        sb.append( ": document element namespace mismatch " );
                        sb.append( "expected " );
                        addNamespace( sb, docElemName );
                        sb.append( " got " );
                        addNamespace(sb,  name );
                    }
                    else if (namespacesSame( docElemName, name ))
                    {
                        sb.append( ": document element local name mismatch " );
                        sb.append( "expected " + docElemName.getLocalPart() );
                        sb.append( " got " + name.getLocalPart() );
                    }
                    else
                    {
                        sb.append( ": document element mismatch " );
                        sb.append( "got " );
                        sb.append( QNameHelper.pretty( name ) );
                    }
                }
            }

            if (sb != null)
            {
                XmlError err = XmlError.forCursor(sb.toString(), c.newCursor());
                throw new XmlException( err.toString(), null, err );
            }
        }
        finally
        {
            c.dispose();
        }
    }

    private void verifyAttributeType ( QName attrName )
        throws XmlException
    {
        XmlCursor c = createCursor();

        try
        {
            StringBuffer sb = null;

            if (!c.toFirstAttribute() || c.toNextAttribute())
            {
                sb = new StringBuffer();

                sb.append( "The document is not a " );
                sb.append( QNameHelper.pretty( attrName ) );

                if (c.currentTokenType().isStartdoc())
                    sb.append( ": no attributes" );
                else
                    sb.append( ": multiple attributes" );
            }
            else
            {
                QName name = c.getName();

                if (!name.equals( attrName ))
                {
                    sb = new StringBuffer();

                    sb.append( "The document is not a " );
                    sb.append( QNameHelper.pretty( attrName ) );

                    if (attrName.getLocalPart().equals( name.getLocalPart() ))
                    {
                        sb.append( ": attribute namespace mismatch " );
                        sb.append( "expected " );
                        addNamespace( sb, attrName );
                        sb.append( " got " );
                        addNamespace(sb,  name );
                    }
                    else if (namespacesSame( attrName, name ))
                    {
                        sb.append( ": attribute local name mismatch " );
                        sb.append( "expected " + attrName.getLocalPart() );
                        sb.append( " got " + name.getLocalPart() );
                    }
                    else
                    {
                        sb.append( ": attribute element mismatch " );
                        sb.append( "got " );
                        sb.append( QNameHelper.pretty( name ) );
                    }
                }
            }

            if (sb != null)
            {
                XmlError err = XmlError.forCursor(sb.toString(), c.newCursor());
                throw new XmlException( err.toString(), null, err );
            }
        }
        finally
        {
            c.dispose();
        }
    }

    //
    //
    //

    private static ThreadLocal tl_SaxLoaders =
        new ThreadLocal()
        {
            protected Object initialValue()
            {
                return new SoftReference(createSaxLoader());
            }
        };

    private static SaxLoader createSaxLoader ()
    {
        SaxLoader sl = PiccoloSaxLoader.newInstance();

        if (sl == null)
            sl = DefaultSaxLoader.newInstance();

        if (sl == null)
            throw new RuntimeException( "Can't find an XML parser" );

        return sl;
    }

    private static SaxLoader getSaxLoader ( )
    {
        SoftReference softRef = (SoftReference)tl_SaxLoaders.get();
        SaxLoader saxLoader = (SaxLoader) softRef.get();
        if (saxLoader==null)
        {
            saxLoader = createSaxLoader();
            tl_SaxLoaders.set(new SoftReference(saxLoader));
        }
        return saxLoader;

    }
    

    private static class PiccoloSaxLoader extends SaxLoader
    {
        public static SaxLoader newInstance ( )
        {
            try
            {
                Class pc = Class.forName( "com.bluecast.xml.Piccolo" );
                
                XMLReader xr = (XMLReader) pc.newInstance();

                Method m_getEncoding     = pc.getMethod( "getEncoding", null );
                Method m_getVersion      = pc.getMethod( "getVersion", null );
                Method m_getStartLocator = pc.getMethod( "getStartLocator", null );

                Locator startLocator =
                    (Locator) m_getStartLocator.invoke( xr, null );

                return new PiccoloSaxLoader( xr, startLocator, m_getEncoding, m_getVersion );
            }
            catch ( ClassNotFoundException e )
            {
                return null;
            }
            catch ( Exception e )
            {
                throw new RuntimeException( e.getMessage(), e );
            }
        }
        
        protected void postLoad ( Root r )
        {
            try
            {
                r._props.setEncoding( (String) _m_getEncoding.invoke( _xr, null ) );
                r._props.setVersion ( (String) _m_getVersion .invoke( _xr, null ) );
            }
            catch ( Exception e )
            {
                throw new RuntimeException( e.getMessage(), e );
            }
        }
        
        private PiccoloSaxLoader (
            XMLReader xr, Locator startLocator, Method m_getEncoding, Method m_getVersion )
        {
            super( xr, startLocator );

            _m_getEncoding = m_getEncoding;
            _m_getVersion = m_getVersion;
        }

        private Method _m_getEncoding;
        private Method _m_getVersion;
    }
    
    private static class DefaultSaxLoader extends SaxLoader
    {
        public static SaxLoader newInstance ( )
        {
            try
            {
                return
                    new DefaultSaxLoader(
                        SAXParserFactory.newInstance().newSAXParser().getXMLReader() );
            }
            catch ( Throwable e )
            {
                throw new RuntimeException( e.getMessage(), e );
            }
        }

        private DefaultSaxLoader ( XMLReader xr )
        {
            super( xr, null );
        }
    }

    private static class SaxLoader
        implements ContentHandler, LexicalHandler, ErrorHandler, EntityResolver
    {
        protected SaxLoader ( XMLReader xr, Locator startLocator )
        {
            _xr = xr;
            _startLocator = startLocator;

            if (xr != null)
            {
                try
                {
                    xr.setFeature( "http://xml.org/sax/features/namespace-prefixes", true );
                    xr.setFeature( "http://xml.org/sax/features/namespaces", true );
                    xr.setFeature( "http://xml.org/sax/features/validation", false );


                    xr.setProperty( "http://xml.org/sax/properties/lexical-handler", this );
                    xr.setContentHandler( this );
                    xr.setErrorHandler( this );
                    xr.setEntityResolver( this );
                }
                catch ( Throwable e )
                {
                    throw new RuntimeException( e.getMessage(), e );
                }
            }
        }

        protected void setContext ( LoadContext context, XmlOptions options )
        {
            _context = context;

            _wantLineNumbers =
                XmlOptions.maskNull(
                    options ).hasOption( XmlOptions.LOAD_LINE_NUMBERS ) &&
                    _startLocator != null;
        }

        protected void postLoad ( Root r )
        {
        }

        public void load ( Root r, InputSource inputSource, XmlOptions options )
            throws IOException, XmlException
        {
            LoadContext context = new LoadContext( r, options );
            
            setContext( context, options );
            
            try
            {
                assert r.disableStoreValidation();

                _xr.parse( inputSource );

                postLoad( r );

//                // Piccolo specific access to encoding and version
//                _props.setEncoding( piccolo.getEncoding() );
//                _props.setVersion( piccolo.getVersion() );

                context.finish();

                r.associateSourceName( options );
            }
            catch ( XmlRuntimeException e )
            {
                context.abort();
                throw new XmlException( e );
            }
            catch ( SAXParseException e )
            {
                context.abort();

                XmlError err =
                    XmlError.forLocation(
                        e.getMessage(),
                        (String) XmlOptions.safeGet( options, XmlOptions.DOCUMENT_SOURCE_NAME ),
                        e.getLineNumber(), e.getColumnNumber(), -1 );

                throw new XmlException( err.toString(), e, err );
            }
            catch ( SAXException e )
            {
                context.abort();
                
                XmlError err = XmlError.forMessage( e.getMessage() );
                
                throw new XmlException( err.toString(), e, err );
            }
            catch ( RuntimeException e )
            {
                context.abort();
                throw e;
            }
            finally
            {
                assert r.enableStoreValidation();
            }
        }

        // Sax ContentHandler

        public void startDocument ( ) throws SAXException
        {
        }

        public void endDocument ( ) throws SAXException
        {
            // Set context to null
            // This prevents the handler (which is held in TLS from keeping
            // the entire document in memory
            _context = null;
        }

        public void startElement (
            String namespaceURI, String localName,
            String qName, Attributes atts )
                throws SAXException
        {
            if (localName.length() == 0)
                localName = qName;

            // Out current parser (Piccolo) does not error when a
            // namespace is used and not defined.  Check for these here

            if (qName.indexOf( ':' ) >= 0 && namespaceURI.length() == 0)
            {
                XmlError err =
                    XmlError.forMessage(
                        "Use of undefined namespace prefix: " +
                            qName.substring( 0, qName.indexOf( ':' ) ));

                throw new XmlRuntimeException( err.toString(), null, err );
            }

            _context.begin( localName, namespaceURI );

            // BUGBUG - do more of the following to get line number for
            // as many parts of the XML as we can
            if (_wantLineNumbers)
            {
                _context.lineNumberAnnotation(
                    _startLocator.getLineNumber(),
                    _startLocator.getColumnNumber(),
                    -1 );
            }

            for ( int i = 0, len = atts.getLength() ; i < len ; i++ )
            {
                String aqn = atts.getQName( i );

                if (aqn.equals( "xmlns" ))
                {
                    _context.xmlns( "", atts.getValue( i ) );
                }
                else if (aqn.startsWith( "xmlns:" ))
                {
                    String prefix = aqn.substring( 6 );

                    if (prefix.length() == 0)
                    {
                        XmlError err =
                            XmlError.forMessage( "Prefix not specified", XmlError.SEVERITY_ERROR );

                        throw new XmlRuntimeException( err.toString(), null, err );
                    }

                    String uri = atts.getValue( i );

                    if (uri.length() == 0)
                    {
                        XmlError err =
                            XmlError.forMessage(
                                "Prefix can't be mapped to no namespace: " + prefix,
                                XmlError.SEVERITY_ERROR );

                        throw new XmlRuntimeException( err.toString(), null, err );
                    }

                    _context.xmlns( prefix, uri );
                }
                else
                {
                    String attrUri = atts.getURI( i );
                    String attrLocal = atts.getLocalName( i );

                    if (attrLocal.length() == 0)
                        attrLocal = aqn;

// given the doc <a x:y='z'/>, piccolo will report the uri of the y
// attribute as 'x'!  Bad Piccolo.  Thus, I can't perform the undefined
// prefix check here.
//
//                    if (aqn.indexOf( ':' ) >= 0 && attrUri.length() == 0)
//                    {
//                        XmlError err =
//                            new CursorXmlError(
//                                "Use of undefined namespace prefix: " +
//                                    aqn.substring( 0, aqn.indexOf( ':' ) ),
//                                XmlError.SEVERITY_ERROR,
//                                null );
//
//                        throw
//                            new XmlRuntimeException(
//                                err.toString(), null, err );
//                    }

                    _context.attr( attrLocal, attrUri, atts.getValue( i ) );
                }
            }
        }

        public void endElement (
            String namespaceURI, String localName, String qName )
                throws SAXException
        {
            _context.end();
        }
        public void characters ( char ch[], int start, int length )

            throws SAXException
        {
            _context.text( ch, start, length );
        }

        public void ignorableWhitespace ( char ch[], int start, int length )
            throws SAXException
        {
            _context.text( ch, start, length );
        }

        public void comment ( char ch[], int start, int length )
            throws SAXException
        {
            _context.comment( ch, start, length );
        }

        public void processingInstruction ( String target, String data )
            throws SAXException
        {
            _context.procinst( target, data );
        }

        public void startDTD ( String name, String publicId, String systemId )
            throws SAXException
        {
            _context.doctype( name, publicId, systemId );
        }

        public void endDTD ( ) throws SAXException
        {
        }

        // Error Handling
        public void fatalError ( SAXParseException e ) throws SAXException
        {
            throw e;
        }

        public void error ( SAXParseException e ) throws SAXException
        {
            XmlError err =
                XmlError.forMessage( "Error: " + e.getMessage(), XmlError.SEVERITY_ERROR );

            throw new XmlRuntimeException( err.toString(), null, err );
        }

        public void warning ( SAXParseException e ) throws SAXException
        {
            // Throw away warings for now
        }

        // Entity Resolver
        public InputSource resolveEntity( String publicId, String systemId )
        {
            // System.out.println("public id = " + publicId);
            // System.out.println("system id = " + systemId);

            return new InputSource( new StringReader( "" ) );
        }

        public void setDocumentLocator ( Locator locator )
        {
            _locator = locator;
        }

        public void startPrefixMapping ( String prefix, String uri )
            throws SAXException
        {
            if (beginsWithXml( prefix ) &&
                ! ( "xml".equals( prefix ) && _xml1998Uri.equals( uri ) ))
            {
                XmlError err =
                    XmlError.forMessage(
                        "Prefix can't begin with XML: " + prefix,
                        XmlError.SEVERITY_ERROR );

                throw
                    new XmlRuntimeException(
                        err.toString(), null, err );
            }
        }

        // Ignored
        public void endPrefixMapping ( String prefix ) throws SAXException {}
        public void skippedEntity ( String name ) throws SAXException {}
        public void startCDATA ( ) throws SAXException { }
        public void endCDATA ( ) throws SAXException { }
        public void startEntity ( String name ) throws SAXException { }
        public void endEntity ( String name ) throws SAXException { }

        protected XMLReader _xr;
        
        private Locator     _locator;
        private LoadContext _context;
        private boolean     _wantLineNumbers;
        private Locator     _startLocator;
    }
    
    //
    //
    //

    public XmlObject loadXml ( InputStream in, SchemaType type, XmlOptions options )
        throws IOException, XmlException
    {
        String encodingOverride =
            (String) XmlOptions.safeGet( options, XmlOptions.CHARACTER_ENCODING );

        if (encodingOverride != null)
        {
            String javaEncoding = EncodingMap.getIANA2JavaMapping( encodingOverride );

            if (javaEncoding == null)
                javaEncoding = encodingOverride;

            return loadXml( new InputStreamReader( in, javaEncoding ), type, options );
        }

        return loadXml( new InputSource( in ), type, options );
    }

    public XmlObject loadXml ( Reader r, SchemaType type, XmlOptions options )
        throws IOException, XmlException
    {
        return loadXml( new InputSource( r ), type, options );
    }

    public XmlObject loadXml ( InputSource is, SchemaType type, XmlOptions options )
        throws IOException, XmlException
    {
        is.setSystemId( "file://" );

        getSaxLoader().load( this, is, options );

        return autoTypedDocument( type, options );
    }
    
    public XmlObject loadXml ( String s, SchemaType type, XmlOptions options )
        throws XmlException
    {
        Reader r = new StringReader( s );

        try
        {
            return loadXml( r, type, options );
        }
        catch ( IOException e )
        {
            assert false: "StringReader should not throw IOException";
            throw new XmlException( e.getMessage(), e );
        }
        finally
        {
            try { r.close(); } catch ( IOException e ) { }
        }
    }

    private void associateSourceName ( XmlOptions options )
    {
        String sourceName =
            (String) XmlOptions.safeGet(
                options, XmlOptions.DOCUMENT_SOURCE_NAME );

        if (sourceName != null)
            _props.setSourceName( sourceName );
    }

    //
    // XmlSaxHandler is returned to a user so that user may obtain the content
    // and lexical handlers to push content and then get the XmlObject at the
    // end of the parse push.
    //

    private class XmlSaxHandlerImpl extends SaxLoader implements XmlSaxHandler
    {
        XmlSaxHandlerImpl ( SchemaType type, XmlOptions options )
        {
            super( null, null );
            
            assert isEmpty();

            _options = options;
            _type = type;

            _context = new LoadContext( Root.this, options );

            setContext( _context, options );
        }

        public ContentHandler getContentHandler ( )
        {
            return _context == null ? null : this;
        }

        public LexicalHandler getLexicalHandler ( )
        {
            return _context == null ? null : this;
        }

        public XmlObject getObject ( ) throws XmlException
        {
            if (_context == null)
                return null;

            _context.finish();

            _context = null;

            Root.this.associateSourceName( _options );

            return Root.this.autoTypedDocument( _type, _options );
        }

        private LoadContext _context;
        private SchemaType  _type;
        private XmlOptions  _options;
    }

    public XmlSaxHandler newSaxHandler ( SchemaType type, XmlOptions options )
    {
        return new XmlSaxHandlerImpl( type, options );
    }

    //
    //
    //

    private void newParseSax ( InputSource in, XmlOptions options )
    {
        LoadContext context = new LoadContext( this, options );
    }

    //
    // Helper object for creating documents in a "load" kinda way
    //

    static final class LoadContext
    {
        LoadContext ( Root root, XmlOptions options )
        {
            assert root != null;

            _options = options = XmlOptions.maskNull( options );

            _qnameCache = XmlBeans.getQNameCache();

            if (options.hasOption( XmlOptions.LOAD_REPLACE_DOCUMENT_ELEMENT ))
            {
                QName name = (QName) options.get( XmlOptions.LOAD_REPLACE_DOCUMENT_ELEMENT );

                if (name != null && name.getLocalPart().length() == 0)
                {
                    throw
                        new IllegalArgumentException(
                            "Load Replace Document Element: Invalid name, local part empty" );
                }
                
                _discardDocElem = true;
                _replaceDocElem = name;
            }

            _stripWhitespace = options.hasOption(XmlOptions.LOAD_STRIP_WHITESPACE);
            _stripComments = options.hasOption(XmlOptions.LOAD_STRIP_COMMENTS);
            _stripProcinsts = options.hasOption(XmlOptions.LOAD_STRIP_PROCINSTS);

            _substituteNamespaces =
                (Map) options.get(XmlOptions.LOAD_SUBSTITUTE_NAMESPACES);

            _additionalNamespaces =
                (Map) options.get(XmlOptions.LOAD_ADDITIONAL_NAMESPACES);

            _root = root;
            _root.ensureEmpty();

            _lastNonAttr = _root._doc;
            _lastSplay = _root._doc;
            _lastPos = 0;

            _frontier = _root._doc;
        }

        private Root getRoot ( )
        {
            return _root;
        }

        private int getCp ( Splay s )
        {
            assert _root.isLeftOnly();
            assert dv > 0 || s.getCpSlow() == s.getCchLeft();
            return s.getCchLeft();
        }

        private void adjustCch ( Splay s, int delta )
        {
            assert _root.isLeftOnly();

            s.adjustCch( delta );

            // There may be attrs after this splay which need their cchLeft's
            // to be updated.  To avoid splaying, update them by hand.

            for ( s = s.nextSplay() ; s != null ; s = s.nextSplay() )
            {
                assert s.isAttr() || s.isRoot();
                s.adjustCchLeft( delta );
            }
        }

        private void insert ( Splay s )
        {
            assert !_finished;
            assert s.getCch() == 0;

            _root.insertSplay( s, _root._leftSplay );

            _lastSplay = s;
            _lastPos = 0;

            if (!s.isAttr())
                _lastNonAttr = s;
        }

        private void insert ( Splay s, char[] buf, int off, int cch )
        {
            assert !_finished;
            assert s.getCch() == 0;
            insert( s );
            _root._text.insert( getCp( s ), buf, off, cch );
            adjustCch( s, cch );
        }

        private void insert ( Splay s, String text )
        {
            assert !_finished;
            assert s.getCch() == 0;
            insert( s );
            _root._text.insert( getCp( s ), text );
            adjustCch( s, text.length() );
        }

        private void stripLeadingWhitespace ( )
        {
            int cchAfter = _lastNonAttr.getCchAfter();

            if (cchAfter > 0)
            {
                int cch = cchAfter;

                int cpAfter =
                    _lastNonAttr.getCpForPos(
                        _root, _lastNonAttr.getPosAfter() );

                int off = _root._text.unObscure( cpAfter, cch );

                for ( ; cch > 0 ; cch-- )
                {
                    if (!isWhiteSpace( _root._text._buf[ off + cch - 1 ]))
                        break;
                }

                int delta = cch - cchAfter;

                if (delta < 0)
                {
                    _root._text.remove( cpAfter + cchAfter + delta, - delta );
                    _lastNonAttr.adjustCchAfter( delta );
                    adjustCch( _lastNonAttr, delta );
                }
            }
        }

        void abort ( )
        {
            // The shit must have hit the fan ...

            // Close all elements

            while ( _frontier != _root._doc )
                end();

            try
            {
                finish();
            }
            catch ( XmlException e )
            {
                assert false;
            }
        }

        void finish ( ) throws XmlException
        {
            if (_stripWhitespace)
                stripLeadingWhitespace();

            // TODO: deal with unterminated begins here

            if (_frontier != _root._doc)
                throw new XmlException( "Document not ended" );

            assert _root._leftOnly;

            _finished = true;

            _lastSplay = _root;
            _lastPos = 0;

            if (_options.hasOption(XmlOptions.LOAD_TRIM_TEXT_BUFFER))
                _root._text.trim();

            // If we have additional namespaces, add them now, making sure we
            // done over ride exisitng ones.

            if (_additionalNamespaces != null)
            {
                Splay s = _root._doc;

                while ( !s.isRoot() && !s.isBegin() )
                    s = s.nextSplay();

                if (s.isBegin())
                {
                    java.util.Iterator i =
                        _additionalNamespaces.keySet().iterator();

                    while ( i.hasNext() )
                    {
                        String prefix = (String) i.next();

                        // Usually, this is the predefined xml namespace
                        if (prefix.toLowerCase().startsWith( "xml" ))
                            continue;

                        String namespace =
                            (String) _additionalNamespaces.get( prefix );

                        if (s.namespaceForPrefix( prefix, false ) == null)
                        {
                            _root.insertSingleSplaySansSplayInLeftOnlyTree(
                                new Xmlns( new QName( namespace, prefix ) ),
                                s );

                            // The above insert should not splay the tree
                            assert _root.isLeftOnly();
                        }
                    }
                }
            }

            // For most of loading, I don't invalidate the document
            // version because nothing should be sensitive to it while
            // loading.  When finished loading, bump it.

            _root.invalidateVersion();
            
            assert _root.isLeftOnly();
        }

        private QName checkName ( String local, String uri )
        {
            if (_substituteNamespaces != null)
            {
                String substituteUri =
                    (String) _substituteNamespaces.get( uri );

                if (substituteUri != null)
                    return _qnameCache.getName( substituteUri, local );
            }

            return _qnameCache.getName( uri, local );
        }

        private QName checkName ( QName name )
        {
            if (_substituteNamespaces != null)
            {
                String substituteUri =
                    (String)
                        _substituteNamespaces.get( name.getNamespaceURI() );

                if (substituteUri != null)
                {
                    name =
                        _qnameCache.getName(
                            substituteUri, name.getLocalPart() );
                }
            }

            return name;
        }

        private QName checkNameAttr ( String local, String uri )
        {
            if (_substituteNamespaces != null && uri.length() > 0)
            {
                String substituteUri =
                    (String) _substituteNamespaces.get( uri );

                if (substituteUri != null)
                    return _qnameCache.getName( substituteUri, local );
            }

            return _qnameCache.getName( uri, local );
        }

        private QName checkNameAttr ( QName name )
        {
            if (_substituteNamespaces != null && name.getNamespaceURI().length() > 0 )
            {
                String substituteUri =
                    (String)
                        _substituteNamespaces.get( name.getNamespaceURI() );

                if (substituteUri != null)
                {
                    name =
                        _qnameCache.getName(
                            substituteUri, name.getLocalPart() );
                }
            }

            return name;
        }

        void doctype ( String name, String publicID, String systemID )
        {
            _root._props.setDoctypeName( name );
            _root._props.setDoctypePublicId( publicID );
            _root._props.setDoctypeSystemId( systemID );
        }

        private void insertBegin ( QName name )
        {
            if (_stripWhitespace)
                stripLeadingWhitespace();

            if (_frontier.isDoc() && !_docElemDiscarded &&
                    (_discardDocElem || isXmlFragment( name )))
            {
                _docElemDiscarded = true;

                if (_replaceDocElem == null)
                {
                    // Remove all content up to now because the
                    // document element is to be removed, and I dont
                    // want that content to mix with the real content.
                    
                    _root.ensureEmpty();
                    _lastNonAttr = _root._doc;
                    _lastSplay = _root._doc;
                    _lastPos = 0;
                    _frontier = _root._doc;
                    
                    return;
                }

                name = _replaceDocElem;
            }

            insert( _frontier = new Begin( name, _frontier ) );
        }

        void begin ( String local, String uri )
        {
            insertBegin( checkName( local, uri ) );
        }

        void begin ( QName name )
        {
            insertBegin( checkName( name ) );
        }

        void end ( )
        {
            if (_stripWhitespace)
                stripLeadingWhitespace();

            if (_frontier.isDoc())
            {
                if (!_docElemDiscarded)
                    throw new IllegalStateException( "Too many end elements" );
            }
            else
            {
                assert !_finished;
                assert _frontier.isBegin();
                assert !_frontier.isLeaf();

                if (_lastNonAttr == _frontier)
                {
                    _lastSplay = _frontier;
                    _lastPos = 1 + _frontier.getCch();
                    _frontier.toggleIsLeaf();
                    int cch = _frontier.getCchAfter();
                    _frontier.adjustCchAfter( - cch );
                }
                else
                {
                    Begin b = (Begin) _frontier;
                    End e = new End( b );
                    b._end = e;
                    insert( e );
                }

                _frontier = _frontier.getContainer();
            }
        }

        void attr ( QName name, char[] buf, int off, int cch )
        {
            insert( new Attr( checkNameAttr( name ) ), buf, off, cch );
        }

        void attr ( QName name, String value )
        {
            insert( new Attr( checkNameAttr( name ) ), value );
        }

        void attr ( String local, String uri, String value )
        {
            insert( new Attr( checkNameAttr( local, uri ) ), value );
        }

        private boolean discardXmlns ( QName name )
        {
            return
                _docElemDiscarded && _frontier.isDoc() &&
                    name.getNamespaceURI().equals( _openFragUri );
        }

        void xmlns ( String prefix, String uri )
        {
            QName name = checkName( prefix, uri );

            if (!discardXmlns( name ))
                insert( new Xmlns( name ) );
        }

        void xmlns ( QName name )
        {
            name = checkName( name );

            if (!discardXmlns( name ))
                insert( new Xmlns( name ) );
        }

        void comment ( char[] buf, int off, int cch )
        {
            if (!_stripComments)
                insert( new Comment(), buf, off, cch );
        }

        void comment ( String value )
        {
            if (!_stripComments)
                insert( new Comment(), value );
        }

        void procinst ( String target, char[] buf, int off, int cch )
        {
            if (!_stripProcinsts)
                insert( new Procinst( target ), buf, off, cch );
        }

        void procinst ( String target, String value )
        {
            if (!_stripProcinsts)
                insert( new Procinst( target ), value );
        }

        //
        // returns the cp where new text should go.
        //

        int preText ( )
        {
            assert !_finished;

            return getCp( _lastNonAttr ) + _lastNonAttr.getCch();
        }

        //
        // New text has been placed, adjust...
        //

        void postText ( int cp, int cch )
        {
            if (_stripWhitespace && _lastNonAttr.getCchAfter() == 0)
            {
                int off = _root._text.unObscure( cp, cch );

                int i = 0;

                while ( i < cch && isWhiteSpace( _root._text._buf[ off + i ] ) )
                    i++;

                if (i > 0)
                {
                    _root._text.remove( cp, i );
                    cch -= i;
                }
            }

            if (cch > 0)
            {
                _lastSplay = _lastNonAttr;
                _lastPos = _lastNonAttr.getEndPos();
                _lastNonAttr.adjustCchAfter( cch );

                adjustCch( _lastNonAttr, cch );
            }
        }

        void text ( char[] buf, int off, int cch )
        {
            assert !_finished;

            int start = off;
            int end = off + cch;

            if (_stripWhitespace && _lastNonAttr.getCchAfter() == 0)
            {
                while ( start < end && isWhiteSpace( buf[ start ] ) )
                    start++;
            }

            int cchText = end - start;

            if (cchText > 0)
            {
                _lastSplay = _lastNonAttr;
                _lastPos = _lastNonAttr.getEndPos();

                _root._text.insert(
                    getCp( _lastNonAttr ) + _lastNonAttr.getCch(),
                    buf, start, cchText );

                _lastNonAttr.adjustCchAfter( cchText );
                adjustCch( _lastNonAttr, cchText );
            }
        }

        void text ( String text )
        {
            assert !_finished;

            int start = 0;
            int end = text.length();

            if (_stripWhitespace && _lastNonAttr.getCchAfter() == 0)
            {
                while ( start < end && isWhiteSpace( text.charAt( start ) ) )
                    start++;
            }

            int cchText = end - start;

            if (cchText > 0)
            {
                _lastSplay = _lastNonAttr;
                _lastPos = _lastNonAttr.getEndPos();

                _root._text.insert(
                    getCp( _lastNonAttr ) + _lastNonAttr.getCch(),
                    text, start, cchText );

                _lastNonAttr.adjustCchAfter( cchText );
                adjustCch( _lastNonAttr, cchText );
            }
        }

        // Annotates the last thing inserted
        void annotate ( XmlBookmark xmlBookmark )
        {
            new Annotation( _root, xmlBookmark ).
                set( _lastSplay, _lastPos );
        }

        void lineNumberAnnotation ( int line, int column, int offset )
        {
            annotate( new XmlLineNumber( line, column, offset ) );
        }

        void lineNumberAnnotation ( XMLEvent xe )
        {
            Location loc = xe.getLocation();

            if (loc != null)
            {
                lineNumberAnnotation(
                    loc.getLineNumber(), loc.getColumnNumber(), -1 );
            }
        }

        void javelinAnnotation ( XmlCursor.XmlBookmark ja )
        {
            if (ja != null)
               annotate( ja );
        }

        private Root       _root;
        private Splay      _lastNonAttr;
        private Container  _frontier;
        private Splay      _lastSplay;
        private int        _lastPos;
        private boolean    _finished;
        private boolean    _discardDocElem;
        private QName      _replaceDocElem;
        private boolean    _stripWhitespace;
        private boolean    _stripComments;
        private boolean    _stripProcinsts;
        private boolean    _docElemDiscarded;
        private Map        _substituteNamespaces;
        private Map        _additionalNamespaces;
        private QNameCache _qnameCache;
        private XmlOptions _options;
    }

    private void loadNodeChildren ( Node n, LoadContext context )
    {
        for ( Node c = n.getFirstChild() ; c != null ; c = c.getNextSibling() )
            loadNode( c, context );
    }

    private void loadNode ( Node n, LoadContext context )
    {
        switch ( n.getNodeType() )
        {
        case Node.DOCUMENT_NODE :
        case Node.DOCUMENT_FRAGMENT_NODE :
        case Node.ENTITY_REFERENCE_NODE :
        {
            loadNodeChildren( n, context );

            break;
        }
        case Node.ELEMENT_NODE :
        {
            String localName = n.getLocalName();

            if (localName == null)
                localName = n.getNodeName();
            
            context.begin( localName, n.getNamespaceURI() );

            NamedNodeMap attrs = n.getAttributes();

            for ( int i = 0 ; i < attrs.getLength() ; i++ )
            {
                Node a = attrs.item( i );

                String uri = a.getNamespaceURI();
                String local = a.getLocalName();
                String value = a.getNodeValue();

                if (local == null)
                    local = a.getNodeName();

                if (uri != null && uri.equals( _xmlnsUri ))
                {
                    if (local.equals( "xmlns" ))
                        context.xmlns( "", value );
                    else
                        context.xmlns( local, value );
                }
                else
                {
                    context.attr( local, uri, value );
                }
            }

            loadNodeChildren( n, context );

            context.end();

            break;
        }
        case Node.TEXT_NODE :
        case Node.CDATA_SECTION_NODE :
        {
            context.text( n.getNodeValue() );
            break;
        }
        case Node.COMMENT_NODE :
        {
            context.comment( n.getNodeValue() );
            break;
        }
        case Node.PROCESSING_INSTRUCTION_NODE :
        {
            context.procinst( n.getNodeName(), n.getNodeValue() );
            break;
        }
        case Node.DOCUMENT_TYPE_NODE :
        case Node.ENTITY_NODE :
        case Node.NOTATION_NODE :
        case Node.ATTRIBUTE_NODE :
        {
            throw new RuntimeException( "Unexpected node" );
        }
        }
    }

    public XmlObject loadXml ( Node node, SchemaType type, XmlOptions options )
        throws XmlException
    {
        LoadContext context = new LoadContext( this, options );

        loadNode( node, context );

        associateSourceName( options );

        return autoTypedDocument( type, options );
    }

    public XmlObject loadXml (
        XMLInputStream xis, SchemaType type, XmlOptions options )
            throws XMLStreamException, XmlException
    {
        loadXmlInputStream( xis, options );

        return autoTypedDocument( type, options );
    }

    public void loadXmlInputStream ( XMLInputStream xis, XmlOptions options )
        throws XMLStreamException, XmlException
    {
        options = XmlOptions.maskNull( options );

        XMLEvent x = xis.peek();
        
        if (x != null && x.getType() == XMLEvent.START_ELEMENT)
        {
            Map nsMap = ((StartElement) x).getNamespaceMap();

            if (nsMap != null && nsMap.size() > 0)
            {
                Map namespaces = new HashMap();
                
                namespaces.putAll( nsMap );

                options = new XmlOptions( options );

                options.put( XmlOptions.LOAD_ADDITIONAL_NAMESPACES, namespaces );
            }
        }

        LoadContext context = new LoadContext( this, options );

        boolean lineNums = options.hasOption( XmlOptions.LOAD_LINE_NUMBERS );

        events:
        for ( XMLEvent xe = xis.next() ; xe != null ; xe = xis.next() )
        {
            switch ( xe.getType() )
            {
            case XMLEvent.START_DOCUMENT :
                // BUGBUG (ericvas) 12258 - FIXED below
                StartDocument doc = (StartDocument) xe;

//                context.doctype(
//                    doc.getname().getLocalName(),
//                    "", doc.getPublicId()

                _props.setDoctypeSystemId( doc.getSystemId() );
//                _publicID = doc.getPublicId();
                _props.setEncoding( doc.getCharacterEncodingScheme() );
                _props.setVersion( doc.getVersion() );
                _standAlone = doc.isStandalone();

                if (lineNums)
                    context.lineNumberAnnotation( xe );

                break;

            case XMLEvent.END_DOCUMENT :
                if (lineNums)
                    context.lineNumberAnnotation( xe );

                break events;

            case XMLEvent.NULL_ELEMENT :
                if (!xis.hasNext())
                    break events;
                break;

            case XMLEvent.START_ELEMENT :
                context.begin( XMLNameHelper.getQName( xe.getName() ) );

                if (lineNums)
                    context.lineNumberAnnotation( xe );

                for ( AttributeIterator ai = ((StartElement) xe).getAttributes()
                      ; ai.hasNext() ; )
                {
                    Attribute attr = ai.next();

                    context.attr(
                        XMLNameHelper.getQName( attr.getName() ),
                        attr.getValue() );
                }

                for ( AttributeIterator ai = ((StartElement) xe).getNamespaces()
                      ; ai.hasNext() ; )
                {
                    Attribute attr = ai.next();

                    XMLName name = attr.getName();
                    String local = name.getLocalName();

                    if (name.getPrefix() == null && local.equals( "xmlns" ))
                        local = "";

                    context.xmlns( local, attr.getValue() );
                }

                break;

            case XMLEvent.END_ELEMENT :
                context.end();

                if (lineNums)
                    context.lineNumberAnnotation( xe );

                break;

            case XMLEvent.SPACE :
                if (((Space) xe).ignorable())
                    break;

                // Fall through

            case XMLEvent.CHARACTER_DATA :
                CharacterData cd = (CharacterData) xe;

                if (cd.hasContent())
                {
                    context.text( cd.getContent() );

                    if (lineNums)
                        context.lineNumberAnnotation( xe );
                }

                break;

            case XMLEvent.COMMENT :
                org.apache.xmlbeans.xml.stream.Comment comment =
                    (org.apache.xmlbeans.xml.stream.Comment) xe;

                if (comment.hasContent())
                {
                    context.comment( comment.getContent() );

                    if (lineNums)
                        context.lineNumberAnnotation( xe );
                }

                break;

            case XMLEvent.PROCESSING_INSTRUCTION :
                ProcessingInstruction procInstr = (ProcessingInstruction) xe;

                context.procinst( procInstr.getTarget(), procInstr.getData() );

                if (lineNums)
                    context.lineNumberAnnotation( xe );

                break;

            // These are ignored
            case XMLEvent.ENTITY_REFERENCE :
            case XMLEvent.START_PREFIX_MAPPING :
            case XMLEvent.END_PREFIX_MAPPING :
            case XMLEvent.CHANGE_PREFIX_MAPPING :
            case XMLEvent.XML_EVENT :
                break;

            default :
                throw new RuntimeException(
                    "Unhandled xml event type: " + xe.getTypeAsString() );
            }
        }

        context.finish();

        associateSourceName( options );

        assert validate();
        assert isLeftOnly();
    }

    public static void dump ( XmlObject x )
    {
        dump( x, System.out );
    }

    public static void dump ( XmlObject x, PrintStream ps )
    {
        XmlCursor xc = x.newCursor();
        Root r = ((Cursor) xc).getRoot();
        r.dump( ps, null, ((Cursor) xc).getSplay(), false );
        xc.dispose();
    }

    public static void dump ( XmlCursor xc )
    {
        dump( xc, System.out );
    }

    public static void dump ( XmlCursor xc, PrintStream ps )
    {
        Root r = ((Cursor) xc).getRoot();
        r.dump( ps, null, ((Cursor) xc).getSplay(), false );
    }

    //
    //
    //

    private void dumpText ( PrintStream ps, int cp, int cch, Splay s, int p )
    {
        if (cp >= 0 && cp + cch <= _text.length())
            dumpString( ps, _text.fetch( cp, cch ), s, p );
        else
            ps.print( "[string: cp=" + cp + ", cch=" + cch + " ]" );
    }

    private void dumpText ( PrintStream ps, int cp, int cch )
    {
        if (cp >= 0 && cp + cch <= _text.length())
            dumpString( ps, _text.fetch( cp, cch ), null, 0 );
        else
            ps.print( "[string: cp=" + cp + ", cch=" + cch + " ]" );
    }

    private void dumpString ( PrintStream ps, String s )
    {
        dumpString( ps, s, null, 0 );
    }

    private void dumpGoobersInline ( PrintStream ps, Splay s, int p )
    {
        for ( Goober g = s.firstGoober() ; g != null ;
              g = s.nextGoober( g ) )
        {
            if (g.getKind() == CURSOR && g.getPos() == p)
            {
                CursorGoober cg = (CursorGoober) g;

                ps.print( "[" + ((CursorGoober) g).getDebugId() + "]" );
            }
        }
    }

    private void dumpString ( PrintStream ps, String str, Splay s, int p )
    {
        for ( int i = 0 ; i < str.length() ; i++ )
        {
            if (s != null)
                dumpGoobersInline( ps, s, i + p );

            if (i == 36)
            {
                ps.print( "..." );
                break;
            }

            char ch = str.charAt( i );

            if (ch == '\n')
                ps.print( "\\n" );
            else if (ch == '\r')
                ps.print( "\\r" );
            else if (ch == '\t')
                ps.print( "\\t" );
            else if (ch == '\"')
                ps.print( "\\\"" );
            else
                ps.print( ch );
        }
    }

    private void dumpGoobers ( PrintStream ps, Goober g )
    {
        if (g == null)
            return;

        if (g.getKind() == AGGREGATE)
        {
            ps.print( " {" );
            dumpGoobers( ps, g._goobers );
            ps.print( "}" );
        }
        else
            ps.print( " - " + g.getKindName() + " pos: " + g.getPos() );

        if (g.getKind() == CURSOR)
            ps.print( " id: " + ((CursorGoober) g).getDebugId() );

        if (g._next != g._parent._goobers)
            dumpGoobers( ps, g._next );
    }

    private void dumpSplayTree( Splay s, PrintStream ps )
    {
        if (s == null)
            return;

        if (s._rightSplay != null)
            dumpSplayTree( s._rightSplay, ps );

        for ( Splay t = s._parentSplay ; t != null ; t = t._parentSplay )
            ps.print( "  " );

        ps.print( "[" + s.getDebugId() );
        ps.print( " c:" + s.getCchLeft() + "(" + s.getCch() + ")");
        ps.print( " b:" + s.getCdocBeginLeft() + "(" + s.getCdocBegin() + ")");

        if (s._parentSplay != null)
        {
            ps.print( " p:" + s._parentSplay.getDebugId() );
            ps.print( s._parentSplay._leftSplay == s ? "L" : "R" );
        }

        ps.println( "]" );

        if (s._leftSplay != null)
            dumpSplayTree( s._leftSplay, ps );
    }

    public static void dump ( XmlCursor c, boolean verbose )
    {
        ((Cursor) c).dump( verbose );
    }

    public void dump ( )
    {
        dump( System.out, null, this, false );
    }

    public void dump ( boolean verbose )
    {
        dump( System.out, null, this, verbose );
    }

    public void dump ( PrintStream ps )
    {
        dump( ps, null, this, false );
    }

    public void dump ( PrintStream ps, String msg, Object src, boolean verbose )
    {
        // Under IntelliJ, if you call a function from the expression
        // evaluator and it produces too much output, IntelliJ will not
        // show you the output of the function.  So, I dump to a file
        // here as well and the given print stream!

        File f = new File( "c:\\" );

        if (f.exists())
        {
            f = new File( f, "xbean.dmp" );

            OutputStream os = null;
            PrintStream ps2 = null;

            try
            {
                os = new FileOutputStream( f );
                ps2 = new PrintStream( os );

                doDump( ps2, msg, src, verbose );
            }
            catch ( Throwable t )
            {
                if (ps2 != null)
                {
                    ps2.println( "Exception during dump." );
                    t.printStackTrace( ps2 );
                }
            }

            if (os != null)
            {
                try { os.close(); } catch ( Exception e ) { }
            }
        }

        try
        {
            doDump( ps, msg, src, verbose );
        }
        catch ( Throwable t )
        {
            System.out.println( "Exception during dump." );
            t.printStackTrace( System.out );
        }
    }

    static class DumpNsManager implements NamespaceManager
    {
        DumpNsManager ( PrintStream ps )
        {
            this.ps = ps;
        }

        public String find_prefix_for_nsuri ( String uri, String suggestion )
        {
            String prefix = "debug_prefix_" + i++;

            ps.print( " [find_prefix_for_nsuri: " );
            ps.print( "\"" + uri + "\" (" + suggestion + ") -> " + prefix );
            ps.print( "]" );

            return prefix;
        }

        public String getNamespaceForPrefix ( String prefix )
        {
            if (prefix != null && prefix.equals( "xml" ))
                return _xml1998Uri;

            String uri = "debug_ns_" + i++;

            ps.print( " [lookup_nsuri_for_prefix: " );
            ps.print( prefix + " -> \"" + uri + "\"" );
            ps.print( "]" );

            return uri;
        }

        PrintStream ps;

        private int i = 1;
    }

    public void doDump (
        PrintStream ps, String msg, Object src, boolean verbose )
    {
        if (!_assertEnabled)
        {
            ps.println( "No dump produced (assert needs to be enabled)" );
            return;
        }

        NamespaceManager nsm = new DumpNsManager( ps );

        if (msg != null)
            ps.println( msg );
        else
            ps.println( "Dump:" );

        if (src != null)
        {
            if (src instanceof Cursor)
            {
                ps.println(
                    "  from cursor " + ((CursorGoober)((Cursor) src)._data._goober).getDebugId() );
            }
            else if (src instanceof Splay)
                ps.println( "  from splay " + ((Splay) src).getDebugId() );
            else
                ps.println( "  from src " + src );
        }

        int depth = 0;

        for ( Splay s = _doc ; s != null ; s = s.nextSplay() )
        {
            int kind = s.getKind();

            String ids = "" + s.getDebugId();
            ids = "        ".substring( 0, 6 - ids.length() ) + ids;
            ps.print( ids + ": " );

            if (kind == END)
                depth--;

            if (!s.isRoot())
            {
                for ( int i = 0 ; i < depth ; i++ )
                    ps.print( "  " );
            }

            dumpGoobersInline( ps, s, 0 );

            switch ( kind )
            {
            case DOC :
                ps.print( "DOC" );
                break;

            case BEGIN :
            {
                Begin b = (Begin) s;

                if (!b.isLeaf())
                    depth++;

                ps.print( "<" );
                ps.print( s.getName().toString() );
                ps.print( ">" );

                if (b.isLeaf())
                {
                    if (b.getCchValue() > 0)
                    {
                        ps.print( "\"" );
                        dumpText( ps, s.getCpSlow(), b.getCchValue(), s, 1 );
                        ps.print( "\"" );
                    }

                    dumpGoobersInline( ps, s, s.getPosLeafEnd() );

                    ps.print( "</" );
                    ps.print( s.getName().getLocalPart() );
                    ps.print( "/>" );
                }

                break;
            }

            case ATTR :
            {
                if (s.isXmlns())
                {
                    ps.print( "#xmlns:" );
                    ps.print( s.getName().getLocalPart() );
                    ps.print( "=\"" );
                    ps.print( s.getName().getNamespaceURI() );
                    ps.print( "\"" );
                }
                else
                {
                    ps.print( "@" );
                    ps.print( s.getName() );
                    ps.print( "=\"" );
                    dumpText( ps, s.getCpSlow(), s.getCchValue() );
                    ps.print( "\"" );
                }

                break;
            }

            case END :
            {
                End es = (End) s;
                Begin b = es._begin;

                ps.print( "</" );
                ps.print( b.getName().getLocalPart() );
                ps.print( "/>" );

                break;
            }

            case COMMENT :
            {
                if (s.isFragment())
                {
                    ps.print( "FRAG" );
                }
                else
                {
                    ps.print( "<!--" );
                    dumpText( ps, s.getCpSlow(), s.getCchValue() );
                    ps.print( "-->" );
                }
                break;
            }

            case PROCINST :
            {
                ps.print( "<?" );
                ps.print( s.getLocal() );
                ps.print( " " );
                dumpText( ps, s.getCpSlow(), s.getCchValue() );
                ps.print( "?>" );
                break;
            }

            case ROOT :
            {
                ps.print( "ROOT" );
                break;
            }

            default:
                throw new RuntimeException( "Unknown splay kind" );
            }

            if (s.getCchAfter() > 0)
            {
                ps.print( "\"" );

                dumpText(
                    ps, s.getCpSlow() + s.getCchValue(), s.getCchAfter(),
                    s, s.getPosAfter() );

                ps.print( "\"" );
            }

            dumpGoobersInline( ps, s, s.getEndPos() );

            if (s.isInvalid())
            {
                String value = s.peekType().build_text( nsm );

                ps.print( " [invalid:" );
                ps.print( "\"" );
                dumpString( ps, value );
                ps.print( "\"" );
                ps.print( "]" );
            }

            dumpGoobers( ps, s._goobers );

            if (verbose)
            {
                switch ( kind )
                {
                case BEGIN :
                {
                    Begin b = (Begin) s;

                    if (b.getFinish() != null)
                    {
                        ps.print( " [end: " );
                        ps.print( b.getFinish().getDebugId() );
                        ps.print( "]" );
                    }

                    if (b.getContainer() != null)
                    {
                        ps.print(
                            " [container: " +
                                b.getContainer().getDebugId() + "]" );
                    }

                    break;
                }

                case END :
                {
                    End es = (End) s;
                    Begin b = es._begin;

                    ps.print( " [begin: " + b.getDebugId() + "]" );

                    break;
                }
                }
            }

            ps.println();
        }

        ps.println();

        if (getCchLeft() > _text.length())
        {
            ps.println(
                "Text buffer has too few characters (" +
                    (getCchLeft() - _text.length()) + ")" );
        }
        else if (getCchLeft() < _text.length())
        {
            ps.println(
                "Text buffer has too many characters (" +
                    (_text.length() - getCchLeft()) + "): " );

            dumpString(
                ps,
                _text.fetch( getCchLeft(), _text.length() - getCchLeft() ) );
        }

        if (verbose)
        {
            ps.println( "Splay tree:" );

            ps.println( "  isLeftOnly: " + _leftOnly );
            ps.println( "  version: " + getVersion() );
            ps.println( "  first: " + _doc.getDebugId() );

            ps.println();
            dumpSplayTree( this, ps );
            ps.println();
            ps.println();
        }

        ps.println();
    }

    /**
     * Insert a splay (or splays) into the tree.  This is a low level
     * operation, and operates only on the splay aspect of the tree.
     * Any invalidation maintenance must be handled by callers.
     */

    void insertSplay ( Splay s, Splay a )
    {
        assert s != null;
        assert !s.isRoot();
        assert a != null;
        assert Root.dv > 0 || validateSplayTree();
        assert !a.isRoot();
        assert Root.dv > 0 || a.getRootSlow() == this;
        assert s._parentSplay == null;
        assert s._rightSplay == null;
        assert s._leftSplay != null || s.getCchLeft() == 0;
        assert s._leftSplay != null || s.getCdocBeginLeft() == 0;

        int cch = s.getCch();
        int cbegin = s.getCdocBegin();

        if (s._leftSplay != null)
        {
            cch += s.getCchLeft();
            cbegin += s.getCdocBeginLeft();
            _leftOnly = false;
        }

        if (_leftOnly && a == _leftSplay)
        {
            s._leftSplay = _leftSplay;
            s._parentSplay = this;

            assert s.getCchLeft() == 0;
            s.adjustCchLeft(
                _leftSplay.getCchLeft() + _leftSplay.getCch() );

            assert s.getCdocBeginLeft() == 0;
            s.adjustCdocBeginLeft(
                _leftSplay.getCdocBeginLeft() + _leftSplay.getCdocBegin() );

            _leftSplay._parentSplay = s;
            _leftSplay = s;

            adjustCchLeft( cch );
            adjustCdocBeginLeft( cbegin );
        }
        else if (_leftOnly && cch == 0 && cbegin == 0)
        {
            //
            // If the splay to insert has no cch or cbegin, then when
            // the tree is left only children, I need not splay.
            //

            s._parentSplay = a._parentSplay;
            s._leftSplay = a;
            a._parentSplay = s;
            s._parentSplay._leftSplay = s;

            s.adjustCchLeft( a.getCchLeft() + a.getCch() );

            s.adjustCdocBeginLeft(
                a.getCdocBeginLeft() + a.getCdocBegin() );
        }
        else
        {
            Splay p;

            if (a._rightSplay == null)
            {
                (p = a)._rightSplay = s;
                _leftOnly = false;
            }
            else
            {
                for ( p = a._rightSplay ; p._leftSplay != null ; )
                    p = p._leftSplay;

                p._leftSplay = s;
            }

            s._parentSplay = p;

            for ( p = s ; ; )
            {
                Splay t = p._parentSplay;

                if (t == null)
                    break;

                if (t._leftSplay == p)
                {
                    t.adjustCchLeft( cch );
                    t.adjustCdocBeginLeft( cbegin );
                }

                p = t;
            }

            s.splay( this, this );
        }

        assert validateSplayTree();
    }

    /**
     * Special insert to insert a single splay into a leftonly tree without
     * causing the tree to go non left only.  Because this does not splay,
     * doing this too many times can be very inefficient.
     */
    
    void insertSingleSplaySansSplayInLeftOnlyTree ( Splay s, Splay a )
    {
        assert _leftOnly;
        assert s._rightSplay == null;
        assert s._leftSplay == null;

        s._leftSplay = a;
        s._parentSplay = a._parentSplay;
        a._parentSplay._leftSplay = s;
        a._parentSplay = s;

        s.adjustCchLeft( a.getCchLeft() + a.getCch() );
        s.adjustCdocBeginLeft( a.getCdocBeginLeft() + a.getCdocBegin() );
        
        int cch = s.getCch();
        int cbegin = s.getCdocBegin();
        
        for ( Splay p = s._parentSplay ; p != null ; p = p._parentSplay )
        {
            p.adjustCchLeft( cch );
            p.adjustCdocBeginLeft( cbegin );
        }
    }
            
    /**
     * Remove [ first, last ) splays from total sequence:
     *
     *     A - first - B - x - last - C
     *
     * where first, last and x are individual splays and A, B and C are
     * multiple, intervening splays.  Return the result with x at the top
     * and first his left child.  This allows x to have the splay
     * characteristics for the whole removed tree.
     */

    Splay removeSplays ( Splay first, Splay last )
    {
        assert validateSplayTree();
        assert first != last;;
        assert !first.isRoot();
        assert !first.isDoc();
        assert Root.dv > 0 || first.getRootSlow() == this;
        assert Root.dv > 0 || last.getRootSlow() == this;
        assert Root.dv > 0 || first.compareSlow( last ) == -1;
        assert !last.isRoot() || last == this;

        Splay x = last.prevSplay();

        if (x == first)
            return x.removeSplay( this );

        // Make the dog leg!

        if (last != this)
            last.splay( this, this );

        x.splay( this, last );
        first.splay( this, x );

        assert this == last || this._leftSplay == last;
        assert last._leftSplay == x;
        assert x._leftSplay == first;
        assert x._rightSplay == null;
        assert first._leftSplay != null;

        int firstCchLeft = first.getCchLeft();
        int firstCbeginLeft = first.getCdocBeginLeft();

        int deltaCchLeft = firstCchLeft - last.getCchLeft();
        int deltaCbeginLeft = firstCbeginLeft - last.getCdocBeginLeft();

        last.adjustCchLeft( deltaCchLeft );
        last.adjustCdocBeginLeft( deltaCbeginLeft );

        if (last != this)
        {
            adjustCchLeft( deltaCchLeft );
            adjustCdocBeginLeft( deltaCbeginLeft );
        }

        x.adjustCchLeft( - firstCchLeft );
        x.adjustCdocBeginLeft( - firstCbeginLeft );

        first.adjustCchLeft( - firstCchLeft );
        first.adjustCdocBeginLeft( - firstCbeginLeft );

        assert first.getCchLeft() == 0;
        assert first.getCdocBeginLeft() == 0;

        assert x.getCchLeft() + deltaCchLeft + x.getCch() == 0;
        assert x.getCdocBeginLeft() + deltaCbeginLeft + x.getCdocBegin() == 0;

        first._leftSplay._parentSplay = last;
        last._leftSplay = first._leftSplay;
        x._parentSplay = null;

        first._leftSplay = null;
        first._parentSplay = null;

        assert validateSplayTree();

        return x;
    }

    public static synchronized boolean disableStoreValidation ( )
        { dv++; return true; }

    public static synchronized boolean enableStoreValidation  ( )
        { dv--; return true; }

    public static int dv;

    {
        if (!"true".equals( System.getProperty( "treeasserts" )))
            disableStoreValidation();
    }

    public boolean validate ( )
    {
        if (dv != 0)
            return true;

        try
        {
            return doValidate();
        }
        catch ( RuntimeException rte )
        {
            System.out.println( "Document invalid: " + rte.getMessage() );
            rte.printStackTrace();
            dump( true );
            throw rte;
        }
    }

    //
    // Recursive descent validation of the content of the document
    //
    // Grammer for valid content of a document
    //
    // <doc>        ::= DOC <attributes> <content> ROOT
    // <attributes> ::= ( ATTR )*
    // <content>    ::= ( ( COMMENT | PROCINST | <element>) )*
    // <element>    ::= ( LEAF  <attributes> ) |
    //                    ( BEGIN <attributes> <content> END )
    //

    private Splay validateAttributes ( Splay s )
    {
        while ( s.getKind() == ATTR )
            s = s.nextSplay();

        return s;
    }

    private Splay validateContent ( Splay s )
    {
        for ( ; ; )
        {
            int k = s.getKind();

            if (k == COMMENT || k == PROCINST)
                s = s.nextSplay();
            else
            {
                Splay q = s;
                s = validateElement( s );

                if (q == s)
                    break;
            }
        }

        return s;
    }

    private Splay validateElement ( Splay s )
    {
        if (s.getKind() == BEGIN)
        {
            Splay b = s;

            s = validateAttributes( s.nextSplay() );

            if (!b.isLeaf())
            {
                s = validateContent( s );

                assert s.isEnd(): "Missing END, splay: " + s.getDebugId();

                s = s.nextSplay();
            }
        }

        return s;
    }

    private boolean validateDoc ( )
    {
        Splay s = _doc.nextSplay();

        s = validateAttributes( s );

        s = validateContent( s );

        assert s.isRoot(): "Expected root";

        return true;
    }

    private static class ValidateContext
    {
        int _numSplayTypes;
        int _cInvalidatableTypes;
        int _cElemOrderSensitiveTypes;
    }

    private void validateGoober ( Goober g, Splay s, ValidateContext context )
    {
        if (g instanceof Type)
        {
            assert g.getKind() == TYPE;
            assert s.isTypeable();

            context._numSplayTypes++;

            Type type = (Type) g;

            if (type.uses_invalidate_value())
                context._cInvalidatableTypes++;

            if (type.is_child_element_order_sensitive())
                context._cElemOrderSensitiveTypes++;
        }
        else
        {
            assert g.getKind() != TYPE;
        }

        assert g.getSplay() == s;
        assert g.getRoot() == this;
        assert g.getPos() >= 0;
        assert g.getPos() <= s.getMaxPos();
    }

    private boolean doValidate ( )
    {
        assert validateDoc();
        assert validateSplayTree();
        assert getCchLeft() == _text.length();

        validateChangeListenerState();

// TODO: Validate that is the root type is null, there are no types in
// tree at all

// TODO: Check the invarient that if there is a Type anywhere
// in the document, that there is an existing chain of types
// all the way to the root and is_ok_element_user or is_ok_attribute_user,
// depending on the case, returns ok.

        ValidateContext context = new ValidateContext();

        Container recentContainer = null;

        for ( Splay s = _doc ; s != null ; s = s.nextSplay() )
        {
            if (s.isRoot())
            {
                assert s.getCchAfter() == 0;
            }
            else if (s.isBegin())
            {
                Begin b = (Begin) s;

                assert b.getName() != null;

                assert
                    b.getFinish() == null ||
                        b.getFinish().getContainer() == b;

                if (b.isLeaf())
                {
                    assert b.getFinish() == null;
                }
                else
                {
                    assert b.getFinish() != null;
                    assert b.getFinish().getContainer() == b;
                    assert b.getCchValue() == 0;
                }
            }
            else if (s.isAttr())
            {
                assert s.getName() != null;
                assert s.getCchAfter() == 0;
            }

            if (s.isProcinst())
            {
                assert s.getName().getNamespaceURI() != null;
                assert s.getName().getNamespaceURI().length() == 0;
            }

            if (s.isContainer())
            {
                assert s.getContainer() == recentContainer;

                if (!s.isLeaf())
                    recentContainer = (Container) s;
            }
            else if (s.isFinish())
            {
                assert s.getContainer() == recentContainer;
                recentContainer = recentContainer.getContainer();
            }

            context._numSplayTypes = 0;

            for ( Goober g = s.firstGoober() ; g != null ;
                  g = s.nextGoober( g ) )
            {
                validateGoober( g, s, context );
            }

            if (isInvalid())
            {
                assert s.isTypeable();
                assert s.peekType() != null;

                assert context._numSplayTypes == 1;

                if (s.isDoc())
                {
                    assert s.getCchAfter() == 0;
                    Splay n = s.nextNonAttrSplay();

                    assert n.isRoot();
                }
                else if (s.isBegin())
                {
                    assert s.isLeaf();
                    assert s.getCchValue() == 0;
                }
                else if (s.isAttr())
                {
                    assert s.getCchValue() == 0;
                }
            }
            else
            {
                assert context._numSplayTypes <= 1;
            }
        }

        assert context._cInvalidatableTypes == _cInvalidatableTypes;
        assert context._cElemOrderSensitiveTypes ==_cElemOrderSensitiveTypes;

        // TODO: Validate indices

        return true;
    }

    boolean validateSplayTree ( )
    {
        if (dv != 0)
            return true;

        try
        {
            return doValidateSplayTree();
        }
        catch ( RuntimeException rte )
        {
            System.out.println( "Splay tree invalid: " + rte );

            dump( true );

            throw rte;
        }
    }

    private static class ValidateStats extends HashMap
    {
        int getCch ( Splay s )
        {
            return getStats( s )._cch;
        }

        void adjustCch ( Splay s, int cchDelta )
        {
            Stats stats = getStats( s );
            stats._cch += cchDelta;
        }

        int getCbegin ( Splay s )
        {
            return getStats( s )._cbegin;
        }

        void adjustCbegin ( Splay s, int cbeginDelta )
        {
            Stats stats = getStats( s );
            stats._cbegin += cbeginDelta;
        }

        private Stats getStats ( Splay s )
        {
            Stats stats = (Stats) get( s );

            if (stats == null)
                put( s, stats = new Stats() );

            return stats;
        }

        private static class Stats
        {
            public int _cch;
            public int _cbegin;
        }
    }

    boolean doValidateSplayTree ( )
    {
        assertAssertEnabled();

        ValidateStats stats = new ValidateStats();

        assert _parentSplay == null;
        assert _rightSplay  == null;
        assert _doc._leftSplay == null;

        Splay s = this;

        if (s._leftSplay != null)
        {
            s = s._leftSplay;

            while ( s != null && s._leftSplay != null )
                s = s._leftSplay;
        }

        assert s == _doc;

        loop:
        for ( ; ; )
        {
            if (s._leftSplay == null && s._rightSplay == null)
            {
                stats.adjustCch( s, s.getCch() );
                stats.adjustCbegin( s, s.getCdocBegin() );
            }

            if (s._rightSplay != null)
            {
                for ( s = s._rightSplay ; s._leftSplay != null ; )
                    s = s._leftSplay;
            }
            else
            {
                for ( ; ; )
                {
                    Splay p = s._parentSplay;

                    if (p == null)
                        break loop;

                    if (p._rightSplay == null || p._rightSplay == s)
                    {
                        s = p;

                        stats.adjustCch( s, s.getCch() );
                        stats.adjustCbegin( s, s.getCdocBegin() );

                        if (s._leftSplay != null)
                        {
                            stats.adjustCch( s, stats.getCch( s._leftSplay ) );

                            stats.adjustCbegin(
                                s, stats.getCbegin( s._leftSplay ) );
                        }

                        if (s._rightSplay != null)
                        {
                            stats.adjustCch( s, stats.getCch( s._rightSplay ) );

                            stats.adjustCbegin(
                                s, stats.getCbegin( s._rightSplay ) );
                        }
                    }
                    else
                    {
                        assert p._leftSplay == s;
                        assert p._rightSplay != null;

                        s = p._rightSplay;

                        while ( s._leftSplay != null )
                            s = s._leftSplay;

                        break;
                    }
                }
            }
        }

        for ( s = _doc ; s != null ; s = s.nextSplay() )
        {
            assert !_leftOnly || s._rightSplay == null: "" + s.getDebugId();
            assert s._leftSplay  == null || s._leftSplay ._parentSplay == s;
            assert s._rightSplay == null || s._rightSplay._parentSplay == s;

            if (s._leftSplay == null)
            {
                assert s.getCchLeft() == 0;
                assert s.getCdocBeginLeft() == 0;
            }
            else
            {
                assert
                    s.getCchLeft() == stats.getCch( s._leftSplay ):
                        "" + s.getDebugId();

                assert s.getCdocBeginLeft() == stats.getCbegin( s._leftSplay );
            }
        }

        return true;
    }

    //
    //
    //

    interface ChangeListener
    {
        void changeNotification ( );
    }

    private static class ChangeClient
    {
        ChangeListener _listener;
        ChangeClient   _next;
    }

    long getVersion ( )
    {
        return __version;
    }

    void invalidateVersion ( )
    {
        assert _changeClients == null;

        __version++;

        assert (_debugChangeVersion = __version) == 0 || true;
    }

    // Use this *only* if you know what you are doing!
    void restoreVersion ( long oldVersion )
    {
        assert __version >= oldVersion;
        __version = oldVersion;
        assert (_debugChangeVersion = __version) == 0 || true;
    }

    boolean validateChangeStarted ( )
    {
        assert _changeClients == null;
        return _changeClients == null;
    }

    boolean validateChangeListenerState ( )
    {
        assert _debugChangeVersion == __version;
        return true;
    }

    void registerForChange ( ChangeListener listener )
    {
        assert validateChangeListenerState();

        // See if this listener is the first one on the list.  Easy but not
        // totally complete  optimization

        if (_changeClients != null && _changeClients._listener == listener)
            return;

        ChangeClient client = new ChangeClient();

        client._next = _changeClients;
        client._listener = listener;
        _changeClients = client;
    }

    void startChange ( )
    {
        assert validateChangeListenerState();

        long currentVersion = 0;

        assert (currentVersion = __version) == 0 || true;

        while ( _changeClients != null )
        {
            _changeClients._listener.changeNotification();
            _changeClients = _changeClients._next;
        }

        assert currentVersion == __version;
    }

    //
    // Document properties
    //

    static class DocProps
        extends XmlDocumentProperties
    {
        private HashMap _map = new HashMap();

        public Object put ( Object key, Object value )
        {
            return _map.put( key, value );
        }

        public Object get ( Object key )
        {
            return _map.get( key );
        }

        public Object remove ( Object key )
        {
            return _map.remove( key );
        }
    }

    class nthCache
    {
        private boolean namesSame ( QName pattern, QName name )
        {
            return pattern == null || pattern.equals( name );
        }

        private boolean setsSame ( QNameSet patternSet, QNameSet set)
        {
            // value equality is probably too expensive. Since the use case
            // involves QNameSets that are generated by the compiler, we
            // can use identity comparison.
            return patternSet != null && patternSet == set;
        }

        private boolean nameHit(QName namePattern,  QNameSet setPattern, QName name)
        {
            if (setPattern == null)
                return namesSame(namePattern, name);
            else
                return setPattern.contains(name);
        }

        private boolean cacheSame (QName namePattern,  QNameSet setPattern)
        {
            return setPattern == null ? namesSame(namePattern, _name) :
                setsSame(setPattern, _set);
        }

        int distance ( Splay parent, QName name, QNameSet set, int n )
        {
            assert n >= 0;

            if (_version != Root.this.getVersion())
                return Integer.MAX_VALUE - 1;

            if (parent != _parent || !cacheSame(name, set))
                return Integer.MAX_VALUE;

            return n > _n ? n - _n : _n - n;
        }

        Begin fetch ( Splay parent, QName name, QNameSet set, int n )
        {
            assert n >= 0;

            if (_version != Root.this.getVersion() || _parent != parent ||
                  ! cacheSame(name, set) || n == 0)
            {
                _version = Root.this.getVersion();
                _parent = parent;
                _name = name;
                _child = null;
                _n = -1;

                if (!parent.isLeaf())
                {
                    loop:
                    for ( Splay s = parent.nextSplay() ; ; s = s.nextSplay() )
                    {
                        switch ( s.getKind() )
                        {
                        case END  :
                        case ROOT : break loop;

                        case BEGIN :
                            if (nameHit( name, set, s.getName() ))
                            {
                                _child = s;
                                _n = 0;
                                break loop;
                            }

                            s = s.getFinishSplay();
                            break;
                        }
                    }
                }
            }

            if (_n < 0)
                return null;

            if (n > _n)
            {
                while ( n > _n )
                {
                    for ( Splay s = _child.getFinishSplay().nextSplay() ; ;
                          s = s.nextSplay() )
                    {
                        if (s.isFinish())
                            return null;

                        if (s.isBegin())
                        {
                            if (nameHit( name, set, s.getName() ))
                            {
                                _child = s;
                                _n++;
                                break;
                            }

                            s = s.getFinishSplay();
                        }
                    }
                }
            }
            else if (n < _n)
            {
                while ( n < _n )
                {
                    Splay s = _child;

                    for ( ; ; )
                    {
                        s = s.prevSplay();

                        if (s.isLeaf() || s.isEnd())
                        {
                            if (s.isEnd())
                                s = s.getContainer();

                            if (nameHit( name, set, s.getName() ))
                            {
                                _child = s;
                                _n--;
                                break;
                            }
                        }
                        else if (s.isContainer())
                            return null;
                    }
                }
            }

            return (Begin) _child;
        }

        private long     _version;
        private Splay    _parent;
        private QName    _name;
        private QNameSet _set;
        
        private Splay _child;
        private int   _n;
    }

    //
    // Da fields.  Da Bears.  Ditka is God.
    //

    boolean _leftOnly;
    Doc     _doc;
    Text    _text;
    boolean _validateOnSet;

    //
    // Document version.  These numbers get incremented when the document
    // changes in a variety of ways.
    //

    private long __version = 1;
    private long _debugChangeVersion = 1; // Debug only

    private ChangeClient _changeClients;

    DocProps _props;

    //
    // XmlInputStream
    //

    boolean _standAlone;

    //
    //
    //

    final SchemaTypeLoader _schemaTypeSystem;

    int _cInvalidatableTypes;
    int _cElemOrderSensitiveTypes;

    //
    //
    //

    nthCache _nthCache_A = new nthCache();
    nthCache _nthCache_B = new nthCache();
    TypeStoreFactory _factory;
}

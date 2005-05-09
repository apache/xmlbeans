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

import org.apache.xmlbeans.impl.common.Chars;
import org.apache.xmlbeans.impl.common.EncodingMap;
import org.apache.xmlbeans.impl.common.GenericXmlInputStream;
import org.apache.xmlbeans.impl.common.ValidatorListener;
import org.apache.xmlbeans.impl.common.XmlEventBase;
import org.apache.xmlbeans.impl.common.XmlNameImpl;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.store.Splay.Container;
import org.apache.xmlbeans.impl.store.Splay.Xmlns;
import org.apache.xmlbeans.impl.values.NamespaceManager;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ConcurrentModificationException;
import java.lang.ref.SoftReference;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.apache.xmlbeans.xml.stream.Attribute;
import org.apache.xmlbeans.xml.stream.AttributeIterator;
import org.apache.xmlbeans.xml.stream.ChangePrefixMapping;
import org.apache.xmlbeans.xml.stream.CharacterData;
import org.apache.xmlbeans.xml.stream.Comment;
import org.apache.xmlbeans.xml.stream.EndDocument;
import org.apache.xmlbeans.xml.stream.EndElement;
import org.apache.xmlbeans.xml.stream.EndPrefixMapping;
import org.apache.xmlbeans.xml.stream.Location;
import org.apache.xmlbeans.xml.stream.ProcessingInstruction;
import org.apache.xmlbeans.xml.stream.StartDocument;
import org.apache.xmlbeans.xml.stream.StartElement;
import org.apache.xmlbeans.xml.stream.StartPrefixMapping;
import org.apache.xmlbeans.xml.stream.XMLEvent;
import org.apache.xmlbeans.xml.stream.XMLName;
import org.apache.xmlbeans.xml.stream.XMLStreamException;

public abstract class Saver implements NamespaceManager
{
    //
    //
    //
    
    private final Object monitor()
    {
        return _root;
    }

    Saver ( Root r, Splay s, int p, XmlOptions options )
    {
        assert Root.dv > 0 || s.getRootSlow() == r;

        // Input s and p must be normalized already
        assert p < s.getEndPos();

        _root = r;
        _top = _splay = s;
        _pos = p;
        _version = r.getVersion();
        _sb = new StringBuffer();
        _attrs = new LinkedHashMap();
        _attrNames = new HashSet();
        _firstPush = true;
        

        // Initialize the state of the namespaces
        _namespaceStack = new ArrayList();
        _uriMap = new HashMap();
        _prefixMap = new HashMap();

        // Stops the synthesis of this namspace and make for better
        // roundtripping 
        addMapping( "xml", Splay._xml1998Uri );

        // Check for implicit namespaces
        
        options = XmlOptions.maskNull( options );

        if (options.hasOption( XmlOptions.SAVE_IMPLICIT_NAMESPACES ))
        {
            Map m = (Map) options.get( XmlOptions.SAVE_IMPLICIT_NAMESPACES );
            
            for ( Iterator i = m.keySet().iterator() ; i.hasNext() ; )
            {
                String prefix = (String) i.next();
                addMapping( prefix, (String) m.get( prefix ) );
            }
        }

        if (options.hasOption( XmlOptions.SAVE_SUGGESTED_PREFIXES ))
            _suggestedPrefixes = (Map) options.get( XmlOptions.SAVE_SUGGESTED_PREFIXES);
        
        // If the default prefix has not been mapped, do so now
        
        if (getNamespaceForPrefix( "" ) == null)
        {
            _initialDefaultUri = new String( "" );
            addMapping( "", _initialDefaultUri );
        }

        _saveNamespacesFirst = options.hasOption( XmlOptions.SAVE_NAMESPACES_FIRST );

        if (_prettyPrint = options.hasOption( XmlOptions.SAVE_PRETTY_PRINT ))
        {
            _prettyIndent = 2;

            if (options.hasOption( XmlOptions.SAVE_PRETTY_PRINT_INDENT ))
            {
                _prettyIndent =
                    ((Integer) options.get( XmlOptions.SAVE_PRETTY_PRINT_INDENT )).intValue();
            }

            if (options.hasOption( XmlOptions.SAVE_PRETTY_PRINT_OFFSET ))
            {
                _prettyOffset =
                    ((Integer) options.get( XmlOptions.SAVE_PRETTY_PRINT_OFFSET )).intValue();
            }
        }

        if (options.hasOption( XmlOptions.SAVE_AGGRESSIVE_NAMESPACES ) &&
                !(this instanceof SynthNamespaceSaver))
        {
            SynthNamespaceSaver saver =
                new SynthNamespaceSaver( r, s, p, options );

            while ( saver.process() )
                ;

            if (!saver._synthNamespaces.isEmpty())
                _preComputedNamespaces = saver._synthNamespaces;
        }
        
        _useDefaultNamespace =
            options.hasOption( XmlOptions.SAVE_USE_DEFAULT_NAMESPACE );

        if (options.hasOption( XmlOptions.SAVE_FILTER_PROCINST ))
        {
            _filterProcinst =
                (String) options.get( XmlOptions.SAVE_FILTER_PROCINST );
        }

        if (options.hasOption( XmlOptions.SAVE_USE_OPEN_FRAGMENT ))
            _fragment = Splay._openuriFragment;
        else
            _fragment = Splay._xmlFragment;

        // Outer overrides inner
        
        _inner =
            options.hasOption( XmlOptions.SAVE_INNER ) &&
                !options.hasOption( XmlOptions.SAVE_OUTER );
        
        if (_inner && !_top.isDoc())
            _synthElem = _fragment;
        
        else if (options.hasOption( XmlOptions.SAVE_SYNTHETIC_DOCUMENT_ELEMENT ))
        {
            _fragment = _synthElem =
                (QName) options.get( XmlOptions.SAVE_SYNTHETIC_DOCUMENT_ELEMENT );

            if (_synthElem == null)
                throw new IllegalArgumentException( "Null synthetic element" );
        }
        
        _preProcess = true;
    }

    protected final void checkVersion ( )
    {
        if (_version != _root.getVersion())
            throw new ConcurrentModificationException( "Document changed during save" );
    }

    protected final Root getRoot ( ) { return _root; }
    protected final Map  getUriMap ( ) { return _uriMap; }
    protected final Map  getPrefixMap ( ) { return _prefixMap; }

    // emitContainer will process leaf contents and the text after a
    // start.  All other text is processed with emitTextAfter.

    protected abstract void emitXmlnsFragment ( Splay s );
    protected abstract void emitAttrFragment ( Splay s );
    protected abstract void emitTextFragment ( Splay s, int p, int cch );
    protected abstract void emitCommentFragment ( Splay s );
    protected abstract void emitProcinstFragment ( Splay s );

    protected abstract void emitDocType(
        String doctypeName, String publicID, String systemID );
    
    protected abstract void emitComment ( Splay s );
    protected abstract void emitTextAfter ( Splay s, int p, int cch );
    protected abstract void emitEnd ( Splay s, QName name );
    protected abstract void emitProcinst ( Splay s );
    protected abstract void emitContainer ( Container c, QName name );

    // Called when a synthetic prefix is created.
    
    protected void syntheticNamespace (
        String prefix, String uri, boolean considerCreatingDefault ) { }

    /*
     * It is vital that the saver does not modify the tree in the process of
     * saving.  So, when there is invalid content or attr values which need
     * new namespace/prefix mappings, I compute the content/values by passing
     * the NamespaceManager implemented by the saver which can cons up mappings
     * just for the purposes of saving without modifying the tree by adding
     * Xmlns splays.
     *
     * Thus, I must not, as a byproduct of saving, validate any content/values
     * into the tree which call back for namespace mappings which need to be
     * created.
     *
     * Also, I need to compute the valid text (if a splay is invalid) before
     * I have closed the attribute list of the enclosing container because
     * I will have to persist out these temporary meppings before they are
     * referenced by the values which need them.
     */

    final String text ( )
    {
        assert _text != null;
        
        return _text.toString();
    }

    final boolean noText ( )
    {
        assert _text != null || _sb.length() == 0;
        assert _text == null || _text == _sb;
        
        return _text == null;
    }

    final void clearText ( )
    {
        _text = null;
        _sb.delete( 0, _sb.length() );
    }

    final static void spaces ( StringBuffer sb, int offset, int count )
    {
        while ( count-- > 0 )
            sb.insert( offset, ' ' );
    }

    final static void trim ( StringBuffer sb )
    {
        int i;

        for ( i = 0 ; i < sb.length() ; i++ )
            if (!Splay.isWhiteSpace( sb.charAt( i ) ))
                break;

        sb.delete( 0, i );

        for ( i = sb.length() ; i > 0 ; i-- )
            if (!Splay.isWhiteSpace( sb.charAt( i - 1 ) ))
                break;

        sb.delete( i, sb.length() );
    }

    // Call process until it returns false to save everything.

    protected final boolean process ( )
    {
        synchronized (monitor())
        {
            checkVersion();
    
            if (_preProcess)
            {
                _preProcess = false;
    
                Splay s = _splay;
                int   p = _pos;
    
                _splay = null;
                _pos = 0;
    
                // Check for position right before end token.  Effectively there
                // is nothing to save here, so save out the empty fragment.
    
                if ((p == 0 && s.isFinish()) ||
                      (s.isLeaf() && p == s.getPosLeafEnd()))
                {
                    assert _splay == null;
                    processTextFragment( null, 0, 0 );
                    return true;
                }
    
                // Here, if p > 0, then we're saving some text
    
                if (p > 0)
                {
                    assert !s.isLeaf() || p != s.getPosLeafEnd();
                    processTextFragment( s, p, s.getPostCch( p ) );
                    assert _splay == null;
                    return true;
                }
    
                if (_inner && (s.isAttr() || s.isComment() || s.isProcinst()))
                {
                    processTextFragment( s, 0, s.getCchValue() );
                    return true;
                }
    
                if (s.isXmlns())
                {
                    processXmlnsFragment( s );
                    return true;
                }
                
                if (s.isAttr())
                {
                    processAttrFragment( s );
                    return true;
                }
                
                if (s.isComment())
                {
                    processCommentFragment( s );
                    return true;
                }
                
                if (s.isProcinst())
                {
                    if (_filterProcinst != null &&
                          s.getLocal().equals( _filterProcinst ))
                    {
                        processTextFragment( null, 0, 0 );
                        return true;
                    }
                    else
                        processProcinstFragment( s );
    
                    return true;
                }
    
                assert s.isContainer();
    
                _splay = s;
                _endSplay = s.isContainer() ? s.getFinishSplay() : s;
            }
    
            // I need to break the processing of a splay into two parts.  The
            // first part is the processing of the splay and the second part is
            // the post processing of the splay.  In particular, the post
            // processing, among other things, pops the mapping stack.  This
            // is done so stuff 'saved' during the first part (the pre-process)
            // can use the mapping stack.  When the next process comes around,
            // the mapping stack will then be updated.
    
            if (_postPop)
            {
                popMappings();
                _postPop = false;
            }
    
            if (_postProcess)
            {
                assert _splay != null;
    
                boolean emitted = false;
    
                if (_splay == _endSplay)
                    _splay = null;
                else
                {
                    Splay s = _splay;
    
                    _splay = _splay.nextNonAttrSplay();
    
                    if (_skipContainerFinish)
                    {
                        assert s.isBegin() && !s.isLeaf() && s.getCchAfter() ==0;
                        assert _splay.isFinish();
    
                        _splay = _splay == _endSplay ? null : _splay.nextSplay();
                    }
    
                    if (_skipContainerFinish || s.isLeaf())
                    {
                        assert !_postPop;
                        _postPop = true;
                    }
    
                    if (!s.isDoc())
                    {
                        assert noText();
    
                        int cchAfter = s.getCchAfter();
    
                        if (_prettyPrint)
                        {
                            _text = _sb;
    
                            if (cchAfter > 0)
                            {
                                Root r = getRoot();
    
                                r._text.fetch(
                                    _text,
                                    s.getCpForPos( r, s.getPosAfter() ), cchAfter );
    
                                trim( _text );
                            }
    
                            Container stop = (Container) _top;
    
                            if (!stop.isDoc())
                                stop = stop.getContainer();
    
                            Container c = s.getContainer( s.getPosAfter() );
    
                            if (_text.length() > 0)
                            {
                                Container p = c;
    
                                for ( ; p != stop ; p = p.getContainer() )
                                    spaces( _text, 0, _prettyIndent );
    
                                if (_prettyIndent >= 0)
                                {
                                    _text.insert( 0, _newLine );
                                    spaces( _text, 1, _prettyOffset );
                                }
                            }
    
                            if (_prettyIndent >= 0)
                            {
                                _text.append( _newLine );
                                spaces( _text, _text.length(), _prettyOffset );
                            }
    
                            Container p = c;
    
                            if (s.nextNonAttrSplay().isEnd())
                                p = p.getContainer();
    
                            for ( ; p != null && p != stop; p = p.getContainer() )
                                spaces( _text, _text.length(), _prettyIndent );
                        }
    
                        if (_text == null ? cchAfter > 0 : _text.length() > 0)
                        {
                            emitTextAfter( s, s.getPosAfter(), cchAfter );
                            emitted = true;
                        }
    
                        clearText();
                    }
                }
    
                _postProcess = false;
    
                // Make sure I only return false if there is *really* nothing more
                // to process
    
                if (emitted)
                    return true;
    
                if (_postPop)
                {
                    popMappings();
                    _postPop = false;
                }
            }
    
            if (_splay == null)
                return false;
    
            if (_version != getRoot().getVersion())
                throw new IllegalStateException( "Document changed" );
    
            _skipContainerFinish = false;
    
            switch ( _splay.getKind() )
            {
            case Splay.DOC :
            case Splay.BEGIN :
            {
                processContainer( (Container) _splay );
                break;
            }
            case Splay.ROOT :
            case Splay.END  :
            {
                processEnd( _splay );
                break;
            }
            case Splay.COMMENT :
            {
                emitComment( _splay );
                break;
            }
            case Splay.PROCINST :
            {
                if (_filterProcinst == null ||
                        !_splay.getLocal().equals( _filterProcinst ))
                {
                    emitProcinst( _splay );
                }
                break;
            }
            case Splay.ATTR :
            default :
            {
                assert false: "Unexpected splay kind " + _splay.getKind();
                return false;
            }
            }
    
            _postProcess = true;
    
            return true;
        }
    }

    private final void processContainer ( Container c )
    {
        assert c.isDoc() || c.isBegin();

        QName name =
            _synthElem != null && c == _top
                ? _synthElem
                : c.isBegin() ? c.getName() : null;

        String nameUri = name == null ? null : name.getNamespaceURI();

        // See if there is a doctype to save out

        if (c.isDoc() )
        {
            String systemId = _root._props.getDoctypeSystemId();
            String docTypeName = _root._props.getDoctypeName();
            
            if (systemId != null || docTypeName != null)
            {
                if (docTypeName == null && name != null)
                    docTypeName = name.getLocalPart();

                if (docTypeName == null)
                {
                    XmlCursor xc = _root.createCursor();
                    
                    if (xc.toFirstChild())
                        docTypeName = xc.getName().getLocalPart();
                    
                    xc.dispose();
                }

                if (docTypeName == null && _fragment != null)
                    docTypeName = _fragment.getLocalPart();

                String publicId = _root._props.getDoctypePublicId();
                
                emitDocType( docTypeName, publicId, systemId );
            }
        }

        // Add a new entry to the frontier.  If this element has a name
        // which has no namespace, then we must make sure that pushing
        // the mappings causes the default namespace to be empty

        boolean ensureDefaultEmpty = name != null && nameUri.length() == 0;

        pushMappings( c, ensureDefaultEmpty );

        //
        // There are four things which use mappings:
        //
        //   1) The element name
        //   2) The element value (qname based)
        //   3) Attribute names
        //   4) The attribute values (qname based)
        //

        // 1) The element name (not for starts)

        if (name != null)
            ensureMapping( nameUri, null, !ensureDefaultEmpty, false );

        assert noText();

        if (c.isInvalid())
        {
            // 2) The element value
            _text = _sb.append( c.peekType().build_text( this ) );
        }

        _attrs.clear();
        _attrNames.clear();

        for ( Splay s = c.nextSplay() ; s.isAttr() ; s = s.nextSplay() )
        {
            if (s.isNormalAttr() &&
                    (_wantDupAttrs || !_attrNames.contains( s.getName() )))
            {
                _attrNames.add( s.getName() );
                
                // 3) Attribute name
                ensureMapping( s.getUri(), null, false, true );

                String invalidValue = null;
                
                if (s.isInvalid())
                    invalidValue = s.peekType().build_text( this ); // #4

                _attrs.put( s, invalidValue );
            }
        }

        // emitContainer handles text only for leaves and starts

        if (_prettyPrint && (c.isDoc() || c.isLeaf()))
        {
            if (_text == null)
            {
                Root r = getRoot();

                r._text.fetch(
                    _text = _sb,
                    r.getCp( c ),
                    c.isLeaf() ? c.getCchValue() : c.getCch() );
            }

            trim( _text );

            if (c.isDoc())
            {
                spaces( _text, 0, _prettyOffset );

                if (_text.length() > _prettyOffset)
                {
                    if (_prettyIndent >= 0)
                    {
                        _text.insert( 0, _newLine );
                        spaces( _text, 1, _prettyOffset );
                        _text.append( _newLine );
                        spaces( _text, _text.length(), _prettyOffset );
                    }
                }
            }
        }

        // derived savers may want to test the stuff being stored to
        // see if is well formed or not in order to save out a
        // fragment.  Do this here when emitting the first container if
        // that container is the root (if not the root, then we are
        // well formed)

        if (_wantFragTest && name == null)
        {
            if ((_text != null && !Splay.isWhiteSpace( _text )) ||
                  !c.isAfterWhiteSpace( getRoot() ))
            {
                _needsFrag = true;
            }
            else
            {
                assert !c.isLeaf();
                
                Splay s = c.nextSplay();

                // Check for leaf anyways -- sometimes it happens!
                if (c.isLeaf() || s.isAttr() || hasMappings())
                    _needsFrag = true;
                else
                {
                    boolean sawBegin = false;
                    Splay cEnd = c.getFinishSplay();

                    for ( ; s != cEnd ; s = s.nextSplay() )
                    {
                        if (s.isBegin())
                        {
                            if (sawBegin)
                            {
                                _needsFrag = true;
                                break;
                            }

                            sawBegin = true;
                            s = s.getFinishSplay();
                        }

                        if (!s.isAfterWhiteSpace( getRoot() ))
                        {
                            _needsFrag = true;
                            break;
                        }
                    }

                    if (!sawBegin)
                        _needsFrag = true;
                }
            }
        }

        // If I am doing aggressive namespaces and we're emitting a
        // container which can contain content, add the namespaces
        // we've computed.  Basically, I'm making sure the pre-computed
        // namespaces are mapped on the first container which has a name.

        if (_preComputedNamespaces != null && (name != null || _needsFrag))
        {
            for ( Iterator i = _preComputedNamespaces.keySet().iterator() ; i.hasNext() ; )
            {
                String uri = (String) i.next();
                
                ensureMapping(
                    uri, null,
                    _preComputedNamespaces.get( uri ) != null && !ensureDefaultEmpty, false );
            }

            // Set to null so we do this once at the top
            _preComputedNamespaces = null;
        }

        if (_wantFragTest)
        {
            // See if I need to gen a fragment for the document

            assert name != null || (c.isDoc() && _synthElem == null);

            if (name == null)
            {
                if (_needsFrag)
                {
                    name = _fragment;
                    ensureFragmentNamespace();
                    _docElem = name;
                }
            }
            else if (c.isDoc())
                _docElem = name;
        }

        emitContainer( c, name );

        clearText();
    }

    private void processEnd ( Splay s )
    {
        Container c = s.getContainer();
        
        QName name =
            _synthElem != null && c == _top
                ? _synthElem
                : c.isBegin() ? c.getName() : null;
        
        if (_wantFragTest && name == null)
        {
            boolean isRoot = s.isRoot();

            if (!isRoot || _docElem != null)
            {
                name = 
                    isRoot
                        ? _docElem
                        : (s.isEnd() ? s.getContainer() : s ).getName();
            }
        }

        emitEnd( _splay, name );

        assert !_postPop;

        _postPop = true;
    }

    private final void pushFragmentMappings ( Splay s )
    {
        // mask the initial frame to hide default mappings
        
        pushMappings(
            s == null ? null : s.getContainer(),
            _fragment.getNamespaceURI().length() == 0 );
        
        ensureFragmentNamespace();
    }

    private final void processXmlnsFragment ( Splay s )
    {
        // Saving a default xmlns mapping is dangerous, just spit out empty
        // text
        
        if (s.getLocal().length() == 0)
        {
            pushFragmentMappings( null );
            _text = _sb;
            emitTextFragment( s, 0, 0 );
        }
        else
        {
            pushFragmentMappings( null );
            ensureMapping( s.getUri(), s.getLocal(), false, true );
            emitXmlnsFragment( s );
        }
    }

    private final void processCommentFragment ( Splay s )
    {
        pushFragmentMappings( null );
        emitCommentFragment( s );
    }

    private final void processProcinstFragment ( Splay s )
    {
        pushFragmentMappings( null );
        emitProcinstFragment( s );
    }
    
    /**
     * This is called only when a single attr is being saved.  It is not called
     * as a consequence of saving out a container.
     */

    private final void processAttrFragment ( Splay s )
    {
        assert s.isNormalAttr();

        pushFragmentMappings( s );

        ensureMapping( s.getUri(), null, false, true );

        assert noText();

        if (s.isInvalid())
        {
            // TODO - pass StringBuffer to buildText to save object creation
            _text = _sb.append( s.peekType().build_text( this ) );
        }

        emitAttrFragment( s );

        clearText();
    }

    /**
     * This is called only when a single chunk of text is to be saved.
     * It is not called as a consequence of saving out a container.
     */

    private final void processTextFragment ( Splay s, int p, int cch )
    {
        // Only need to save ancestor namespace mappings if there could be a
        // qname here.  If the text is of length 0, then there is no qname.

        Splay c = null;
        
        if ((_text != null && _text.length() > 0) || (s != null && cch > 0))
            c = s.getContainer( p );
        
        pushFragmentMappings( c );

        emitTextFragment( s, p, cch );
    }

    //
    // Layout of namespace stack:
    //
    //    URI Undo
    //    URI Rename
    //    Prefix Undo
    //    Mapping
    //

    boolean hasMappings ( )
    {
        int i = _namespaceStack.size();

        return i > 0 && _namespaceStack.get( i - 1 ) != null;
    }

    void iterateMappings ( )
    {
        _currentMapping = _namespaceStack.size();

        while ( _currentMapping > 0 &&
                  _namespaceStack.get( _currentMapping - 1 ) != null )
        {
            _currentMapping -= 8;
        }
    }

    boolean hasMapping ( )
    {
        return _currentMapping < _namespaceStack.size();
    }

    void nextMapping ( )
    {
        _currentMapping += 8;
    }

    String mappingPrefix ( )
    {
        assert hasMapping();
        return (String) _namespaceStack.get( _currentMapping + 6 );
    }

    String mappingUri ( )
    {
        assert hasMapping();
        return (String) _namespaceStack.get( _currentMapping + 7 );
    }

    String mappingPrevPrefixUri ( )
    {
        assert hasMapping();
        return (String) _namespaceStack.get( _currentMapping + 5 );
    }

    //
    //
    //

    private final void pushMappings ( Container c, boolean ensureDefaultEmpty )
    {
        _namespaceStack.add( null );

        for ( ; c != null ; c = c.getContainer() )
        {
            namespaces:
            for ( Splay s = c.nextSplay() ; s.isAttr() ; s = s.nextSplay() )
            {
                if (s.isXmlns())
                {
                    Xmlns x = (Xmlns) s;
                    String prefix = x.getLocal();
                    String uri = x.getUri();

                    if (ensureDefaultEmpty &&
                            prefix.length() == 0 && uri.length() > 0)
                    {
                        continue;
                    }

                    // Make sure the prefix is not already mapped in
                    // this frame

                    for ( iterateMappings() ; hasMapping() ; nextMapping() )
                        if (mappingPrefix().equals( prefix ))
                            continue namespaces;

                    addMapping( prefix, uri );
                }
            }

            // Push all ancestors the first time
            
            if (!_firstPush)
                break;
        }

        if (ensureDefaultEmpty)
        {
            String defaultUri = (String) _prefixMap.get( "" );

            // I map the default to "" at the very beginning
            assert defaultUri != null;

            if (defaultUri.length() > 0)
                addMapping( "", "" );
        }

        _firstPush = false;
    }

    private final void dumpMappings ( )
    {
        for ( int i = _namespaceStack.size() ; i > 0 ; )
        {
            if (_namespaceStack.get( i - 1 ) == null)
            {
                System.out.println( "----------------" );
                i--;
                continue;
            }

            System.out.print( "Mapping: " );
            System.out.print( _namespaceStack.get( i - 2 ) );
            System.out.print( " -> " );
            System.out.print( _namespaceStack.get( i - 1 ) );
            System.out.println();

            System.out.print( "Prefix Undo: " );
            System.out.print( _namespaceStack.get( i - 4 ) );
            System.out.print( " -> " );
            System.out.print( _namespaceStack.get( i - 3 ) );
            System.out.println();

            System.out.print( "Uri Rename: " );
            System.out.print( _namespaceStack.get( i - 5 ) );
            System.out.print( " -> " );
            System.out.print( _namespaceStack.get( i - 6 ) );
            System.out.println();

            System.out.print( "UriUndo: " );
            System.out.print( _namespaceStack.get( i - 7 ) );
            System.out.print( " -> " );
            System.out.print( _namespaceStack.get( i - 8 ) );
            System.out.println();

            System.out.println();

            i -= 8;
        }
    }

    private final void addMapping ( String prefix, String uri )
    {
        assert uri != null;
        assert prefix != null;

        // If the prefix being mapped here is already mapped to a uri,
        // that uri will either go out of scope or be mapped to another
        // prefix.

        String renameUri = (String) _prefixMap.get( prefix );
        String renamePrefix = null;

        if (renameUri != null)
        {
            // See if this prefix is already mapped to this uri.  If
            // so, then add to the stack, but there is nothing to rename
        
            if (renameUri.equals( uri ))
                renameUri = null;
            else
            {
                int i = _namespaceStack.size();

                while ( i > 0 )
                {
                    if (_namespaceStack.get( i - 1 ) == null)
                    {
                        i--;
                        continue;
                    }

                    if (_namespaceStack.get( i - 7 ).equals( renameUri ))
                    {
                        renamePrefix = (String) _namespaceStack.get( i - 8 );

                        if (renamePrefix == null ||
                              !renamePrefix.equals( prefix ))
                        {
                            break;
                        }
                    }

                    i -= 8;
                }

                assert i > 0;
            }
        }

        _namespaceStack.add( _uriMap.get( uri ) );
        _namespaceStack.add( uri );

        if (renameUri != null)
        {
            _namespaceStack.add( _uriMap.get( renameUri ) );
            _namespaceStack.add( renameUri );
        }
        else
        {
            _namespaceStack.add( null );
            _namespaceStack.add( null );
        }

        _namespaceStack.add( prefix );
        _namespaceStack.add( _prefixMap.get( prefix ) );

        _namespaceStack.add( prefix );
        _namespaceStack.add( uri );

        _uriMap.put( uri, prefix );
        _prefixMap.put( prefix, uri );

        if (renameUri != null)
            _uriMap.put( renameUri, renamePrefix );
    }

    private final void popMappings ( )
    {
        for ( ; ; )
        {
            int i = _namespaceStack.size();

            if (i == 0)
                break;

            if (_namespaceStack.get( i - 1 ) == null)
            {
                _namespaceStack.remove( i - 1 );
                break;
            }

            Object oldUri = _namespaceStack.get( i - 7 ); 
            Object oldPrefix = _namespaceStack.get( i - 8 ); 

            if (oldPrefix == null) 
                _uriMap.remove( oldUri ); 
            else 
                _uriMap.put( oldUri, oldPrefix ); 

            oldPrefix = _namespaceStack.get( i - 4 ); 
            oldUri = _namespaceStack.get( i - 3 ); 

            if (oldUri == null) 
                _prefixMap.remove( oldPrefix ); 
            else 
                _prefixMap.put( oldPrefix, oldUri ); 

            String uri = (String) _namespaceStack.get( i - 5 );

            if (uri != null)
                _uriMap.put( uri, _namespaceStack.get( i - 6 ) );

            // Hahahahahaha -- :-(
            _namespaceStack.remove( i - 1 );
            _namespaceStack.remove( i - 2 );
            _namespaceStack.remove( i - 3 );
            _namespaceStack.remove( i - 4 );
            _namespaceStack.remove( i - 5 );
            _namespaceStack.remove( i - 6 );
            _namespaceStack.remove( i - 7 );
            _namespaceStack.remove( i - 8 );
        }
    }

    protected final String getUriMapping ( String uri )
    {
        assert _uriMap.get( uri ) != null;
        return (String) _uriMap.get( uri );
    }

    protected final boolean tryPrefix ( String prefix )
    {
        if (prefix == null || Splay.beginsWithXml( prefix ))
            return false;

        String existingUri = (String) _prefixMap.get( prefix );

        // If the prefix is currently mapped, then try another prefix.  A
        // special case is that of trying to map the default prefix ("").
        // Here, there always exists a default mapping.  If this is the
        // mapping we found, then remap it anyways. I use != to compare
        // strings because I want to test for the specific initial default
        // uri I added when I initialized the saver.

        if (existingUri != null &&
              (prefix.length() > 0 || existingUri != _initialDefaultUri))
        {
            return false;
        }

        return true;
    }

    protected final String ensureMapping (
        String uri, String candidatePrefix,
        boolean considerCreatingDefault, boolean mustHavePrefix )
    {
        assert uri != null;
        assert candidatePrefix == null || candidatePrefix.length() > 0;

        // Can be called for no-namespaced things

        if (uri.length() == 0)
            return null;

        String prefix = (String) _uriMap.get( uri );

        if (prefix != null && (prefix.length() > 0 || !mustHavePrefix))
            return prefix;

        //
        // I try prefixes from a number of places, in order:
        //
        //  1) What was passed in
        //  2) The optional suggestions (for uri's)
        //  3) The default mapping is allowed
        //  4) ns#++
        //
        
        if (candidatePrefix == null || !tryPrefix( candidatePrefix ))
        {
            if (_suggestedPrefixes != null &&
                    _suggestedPrefixes.containsKey( uri ) &&
                        tryPrefix( (String) _suggestedPrefixes.get( uri ) ))
            {
                candidatePrefix = (String) _suggestedPrefixes.get( uri );
            }
            else if (considerCreatingDefault && _useDefaultNamespace && tryPrefix( "" ))
                candidatePrefix = "";
            else
            {
                String basePrefix = QNameHelper.suggestPrefix( uri );
                candidatePrefix = basePrefix;
                
                for ( int i = 1 ; ; i++ )
                {
                    if (tryPrefix( candidatePrefix ))
                        break;
                    
                    candidatePrefix = basePrefix + i;
                }
            }
        }

        assert candidatePrefix != null;

        syntheticNamespace( candidatePrefix, uri, considerCreatingDefault );

        addMapping( candidatePrefix, uri );

        return candidatePrefix;
    }

    public final String find_prefix_for_nsuri ( String uri, String prefix )
    {
        assert uri != null;
        assert prefix == null || prefix.length() > 0;

        boolean emptyUri = uri.length() == 0;
        
        return ensureMapping( uri, prefix, emptyUri, !emptyUri );
    }

    public final String getNamespaceForPrefix ( String prefix )
    {
        if (prefix != null && prefix.equals( "xml" ))
            return Splay._xml1998Uri;

        return (String) _prefixMap.get( prefix );
    }

    protected final String ensureFragmentNamespace ( )
    {
        if (_fragment.getNamespaceURI().length() == 0)
            return "";
        
        return ensureMapping( _fragment.getNamespaceURI(), "frag", false, false );
    }

    /**
     * A Saver which records synthetic namespaces
     */
    
    static final class SynthNamespaceSaver extends Saver
    {
        LinkedHashMap _synthNamespaces = new LinkedHashMap();
        
        SynthNamespaceSaver ( Root r, Splay s, int p, XmlOptions options )
        {
            super( r, s, p, options );
        }
        
        protected void syntheticNamespace (
            String prefix, String uri, boolean considerCreatingDefault )
        {
            _synthNamespaces.put( uri, considerCreatingDefault ? "useDefault" : null );
        }
        
        protected void emitXmlnsFragment ( Splay s ) { }
        protected void emitAttrFragment ( Splay s ) { }
        protected void emitTextFragment ( Splay s, int p, int cch ) { }
        protected void emitCommentFragment ( Splay s ) { }
        protected void emitProcinstFragment ( Splay s ) { }
        protected void emitComment ( Splay s ) { }
        protected void emitTextAfter ( Splay s, int p, int cch ) { }
        protected void emitEnd ( Splay s, QName name ) { }
        protected void emitProcinst ( Splay s ) { }
        protected void emitContainer ( Container c, QName name ) { }
        
        protected void emitDocType(
            String doctypeName, String publicID, String systemID ) { }
    }

    /**
     * A Saver which generates characters.
     */

    static final class TextSaver extends Saver
    {
        TextSaver (
            Root r, Splay s, int p, XmlOptions options, String encoding )
        {
            super( r, s, p, options );

            _wantFragTest = true;

            if (encoding != null)
            {
                String version = r._props.getVersion();

                if (version == null)
                    version = "1.0";
                
                emit( "<?xml version=\"" );
                emit( version );
                emit( "\" encoding=\"" + encoding + "\"?>" + _newLine );
            }
        }

        protected void emitContainer ( Container c, QName name )
        {
            if (c.isBegin())
            {
                emitContainerHelper( c, name, null, null, false );

                if (c.isLeaf())
                {
                    int cch = _text == null ? c.getCchValue() : _text.length();

                    if (cch > 0)
                    {
                        emit( '>' );

                        if (_text == null)
                            emit( getRoot().getCp( c ), cch );
                        else
                            emit( _text );

                        entitizeContent();

                        emit( "</" );
                        emitName( name );
                    }
                    else
                        emit( '/' );
                }
                else
                {
                    assert !c.isLeaf();

                    if (c.getCchAfter() == 0 && c.nextNonAttrSplay().isEnd())
                    {
                        emit( '/' );
                        _skipContainerFinish = true;
                    }
                }

                emit( '>' );
            }
            else
            {
                assert c.isDoc();

                if (name != null)
                    emitContainerHelper( c, name, null, null, true );

                if (_text == null)
                    emit( getRoot().getCp( c ), c.getCch() );
                else
                    emit( _text );

                entitizeContent();
            }
        }

        private void emitAttrHelper ( Splay s, String invalidValue )
        {
            assert s.isNormalAttr();
            
            emit( ' ' );
            emitName( s.getName() );
            emit( "=\"" );

            if (invalidValue != null)
                emit( invalidValue );
            else
                emit( getRoot().getCp( s ), s.getCch() );

            entitizeAttrValue();

            emit( '"' );
        }

        private void emitNamespacesHelper ( )
        {
            for ( iterateMappings() ; hasMapping() ; nextMapping() )
            {
                emit( ' ' );
                emitXmlns( mappingPrefix(), mappingUri() );
            }
        }
                                     
        private void emitContainerHelper (
            Container c, QName name,
            Splay extraAttr, StringBuffer extraAttrText,
            boolean close )
        {
            assert name != null;

            emit( '<' );
            emitName( name );

            if (_saveNamespacesFirst)
                emitNamespacesHelper();

            if (c != null)
            {
                for ( Iterator i = _attrs.keySet().iterator() ; i.hasNext() ; )
                {
                    Splay s = (Splay) i.next();
                    emitAttrHelper( s, (String) _attrs.get( s ) );
                }
            }

            if (extraAttr != null)
            {
                emitAttrHelper(
                    extraAttr,
                    extraAttrText == null ? null : extraAttrText.toString() );
            }

            if (!_saveNamespacesFirst)
                emitNamespacesHelper();

            if (close)
                emit( '>' );
        }

        protected void emitText ( Splay s, int p, int cch )
        {
            emit( s.getCpForPos( getRoot(), p ), cch );
            entitizeContent();
        }

        protected void emitTextAfter ( Splay s, int p, int cch )
        {
            if (_text == null)
                emitText( s, p, cch );
            else
            {
                emit( _text );
                entitizeContent();
            }
        }

        protected void emitTextFragment ( Splay s, int p, int cch )
        {
            emitContainerHelper( null, _fragment, null, null, false );

            if (_text != null)
            {
                if (_text.length() > 0)
                {
                    emit( ">" );
                    emit( _text );
                    emitEndHelper( _fragment );
                    return;
                }
            }
            else if (s != null)
            {
                if (cch > 0)
                {
                    emit( ">" );
                    emitText( s, p, cch );
                    emitEndHelper( _fragment );
                    return;
                }
            }
            
            emit( "/>" );
        }

        protected void emitAttrFragment ( Splay s )
        {
            emitContainerHelper( null, _fragment, s, _text, false );
            emit( "/>" );
        }

        protected void emitXmlnsFragment ( Splay s )
        {
            emitContainerHelper( null, _fragment, null, null, false );
            emit( "/>" );
        }

        protected void emitCommentFragment ( Splay s )
        {
            emitContainerHelper( null, _fragment, null, null, true );
            emitComment( s );
            emitEndHelper( _fragment );
        }

        protected void emitProcinstFragment ( Splay s )
        {
            emitContainerHelper( null, _fragment, null, null, true );
            emitProcinst( s );
            emitEndHelper( _fragment );
        }

        protected void emitXmlns ( String prefix, String uri )
        {
            assert prefix != null;
            assert uri != null;

            emit( "xmlns" );

            if (prefix.length() > 0)
            {
                emit( ":" );
                emit( prefix );
            }

            emit( "=\"" );

            // TODO - must encode uri properly

            emit( uri );
            entitizeAttrValue();

            emit( '"' );
        }

        protected void emitEndHelper ( QName name )
        {
            emit( "</" );
            emitName( name );
            emit( '>' );
        }

        protected void emitEnd ( Splay s, QName name )
        {
            if (name != null)
                emitEndHelper( name );
        }

        private void emitLiteral ( String literal )
        {
            if (literal.indexOf( "\"" ) < 0)
            {
                emit( "\"" );
                emit( literal );
                emit( "\"" );
            }
            else
            {
                emit( "'" );
                emit( literal );
                emit( "'" );
            }
        }

        protected void emitDocType(
            String doctypeName, String publicID, String systemID )
        {
            assert doctypeName != null;
            
            emit( "<!DOCTYPE " );
            emit( doctypeName );

            if (publicID == null && systemID != null)
            {
                emit( " SYSTEM " );
                emitLiteral( systemID );
            }
            else if (publicID != null)
            {
                emit( " PUBLIC " );
                emitLiteral( publicID );
                emit( " " );
                emitLiteral( systemID );
            }

            emit( ">" + _newLine );
        }
        
        protected void emitComment ( Splay s )
        {
            assert s.isComment();
            emit( "<!--" );
            emit( getRoot().getCp( s ), s.getCchValue() );
            entitizeComment();
            emit( "-->" );
        }

        protected void emitProcinst ( Splay s )
        {
            assert s.isProcinst();
            emit( "<?" );
            // TODO - encoding issues here?
            emit( s.getLocal() );

            if (s.getCchValue() > 0)
            {
                emit( " " );
                emit( getRoot().getCp( s ), s.getCchValue() );
                entitizeProcinst();
            }

            emit( "?>" );
        }

        /**
         * Ensure that there are at least cch chars available in the buffer.
         * Return the actual number of characters available.  If less than cch,
         * then that's all which will ever become available.
         */

        private int ensure ( int cch )
        {
            // Even if we're asked to ensure nothing, still try to ensure
            // atleast one character so we can determine if we're at the
            // end of the stream.

            if (cch <= 0)
                cch = 1;

            int available = getAvailable();

            for ( ; available < cch ; available = getAvailable() )
                if (!process())
                    break;

            assert available == getAvailable();

            if (available == 0)
                return 0;

            return available;
        }

        private void emitName ( QName name )
        {
            assert name != null;

            String uri = name.getNamespaceURI();

            assert uri != null;

            if (uri.length() != 0)
            {
                String prefix = getUriMapping( uri );

                if (prefix.length() > 0)
                {
                    emit( prefix );
                    emit( ":" );
                }
            }

            assert name.getLocalPart().length() > 0;

            emit( name.getLocalPart() );
        }

        private boolean preEmit ( int cch )
        {
            assert cch >= 0;
            
            _lastEmitCch = cch;

            if (cch == 0)
                return true;

            if (_free < cch)
                resize( cch, -1 );

            assert cch <= _free;

            int used = getAvailable();

            // if we are about to emit and there is noting in the buffer, reset
            // the buffer to be at the beginning so as to not grow it anymore
            // than needed.
            
            if (used == 0)
            {
                assert _in == _out;
                assert _free == _buf.length;
                _in = _out = 0;
            }

            _lastEmitIn = _in;

            _free -= cch;
            
            assert _free >= 0;

            return false;
        }

        private void emit ( String s )
        {
            int cch = s == null ? 0 : s.length();

            if (preEmit( cch ))
                return;

            int chunk;

            if (_in <= _out || cch < (chunk = _buf.length - _in))
            {
                s.getChars( 0, cch, _buf, _in );
                _in += cch;
            }
            else
            {
                s.getChars( 0, chunk, _buf, _in );
                s.getChars( chunk, cch, _buf, 0 );
                _in = (_in + cch) % _buf.length;
            }
        }

        private void emit ( StringBuffer sb )
        {
            int cch = sb == null ? 0 : sb.length();

            if (preEmit( cch ))
                return;

            int chunk;

            if (_in <= _out || cch < (chunk = _buf.length - _in))
            {
                sb.getChars( 0, cch, _buf, _in );
                _in += cch;
            }
            else
            {
                sb.getChars( 0, chunk, _buf, _in );
                sb.getChars( chunk, cch, _buf, 0 );
                _in = (_in + cch) % _buf.length;
            }
        }

        private void emit ( int cp, int cch )
        {
            emit(
                getRoot()._text._buf,
                getRoot()._text.unObscure( cp, cch ),
                cch );
        }

        private void emit ( char ch )
        {
            preEmit( 1 );

            _buf[ _in ] = ch;

            _in = (_in + 1) % _buf.length;
        }

        private void emit ( char[] buf, int off, int cch )
        {
            assert cch >= 0;

            if (preEmit( cch ))
                return;

            int chunk;

            if (_in <= _out || cch < (chunk = _buf.length - _in))
            {
                System.arraycopy( buf, off, _buf, _in, cch );
                _in += cch;
            }
            else
            {
                System.arraycopy( buf, off, _buf, _in, chunk );
                System.arraycopy( buf, off + chunk, _buf, 0, cch - chunk );
                _in = (_in + cch) % _buf.length;
            }
        }

        private void entitizeContent ( )
        {
            if (_lastEmitCch == 0)
                return;

            int i = _lastEmitIn;
            final int n = _buf.length;

            boolean hasOutOfRange = false;
            
            int count = 0;
            for ( int cch = _lastEmitCch ; cch > 0 ; cch-- )
            {
                char ch = _buf[ i ];

                if (ch == '<' || ch == '&')
                    count++;
                else if (isBadChar( ch ))
                    hasOutOfRange = true;

                if (++i == n)
                    i = 0;
            }

            if (count == 0 && !hasOutOfRange)
                return;

            i = _lastEmitIn;

            //
            // Heuristic for knowing when to save out stuff as a CDATA.
            //

            if (_lastEmitCch > 32 && count > 5 &&
                  count * 100 / _lastEmitCch > 1)
            {
                boolean lastWasBracket = _buf[ i ] == ']';

                i = replace( i, "<![CDATA[" + _buf[ i ] );

                boolean secondToLastWasBracket = lastWasBracket;

                lastWasBracket = _buf[ i ] == ']';

                if (++i == _buf.length)
                    i = 0;

                for ( int cch = _lastEmitCch ; cch > 0 ; cch-- )
                {
                    char ch = _buf[ i ];

                    if (ch == '>' && secondToLastWasBracket && lastWasBracket)
                        i = replace( i, "&gt;" );
                    else if (isBadChar( ch ))
                        i = replace( i, "?" );
                    else
                        i++;

                    secondToLastWasBracket = lastWasBracket;
                    lastWasBracket = ch == ']';

                    if (i == _buf.length)
                        i = 0;
                }

                emit( "]]>" );
            }
            else
            {
                for ( int cch = _lastEmitCch ; cch > 0 ; cch-- )
                {
                    char ch = _buf[ i ];

                    if (ch == '<')
                        i = replace( i, "&lt;" );
                    else if (ch == '&')
                        i = replace( i, "&amp;" );
                    else if (isBadChar( ch ))
                        i = replace( i, "?" );
                    else
                        i++;

                    if (i == _buf.length)
                        i = 0;
                }
            }
        }

        private void entitizeAttrValue ( )
        {
            if (_lastEmitCch == 0)
                return;

            int i = _lastEmitIn;

            for ( int cch = _lastEmitCch ; cch > 0 ; cch-- )
            {
                char ch = _buf[ i ];

                if (ch == '<')
                    i = replace( i, "&lt;" );
                else if (ch == '&')
                    i = replace( i, "&amp;" );
                else if (ch == '"')
                    i = replace( i, "&quot;" );
                else
                    i++;

                if (i == _buf.length)
                    i = 0;
            }
        }

        private void entitizeComment ( )
        {
            if (_lastEmitCch == 0)
                return;

            int i = _lastEmitIn;

            boolean lastWasDash = false;

            for ( int cch = _lastEmitCch ; cch > 0 ; cch-- )
            {
                char ch = _buf[ i ];

                if (isBadChar( ch ))
                    i = replace( i, "?" );
                else if (ch == '-')
                {
                    if (lastWasDash)
                    {
                        // Replace "--" with "- " to make well formed
                        i = replace( i, " " );
                        lastWasDash = false;
                    }
                    else
                    {
                        lastWasDash = true;
                        i++;
                    }
                }
                else
                {
                    lastWasDash = false;
                    i++;
                }

                if (i == _buf.length)
                    i = 0;
            }

            // Because I have only replaced chars with single chars,
            // _lastEmitIn will still be ok

            if (_buf[ _lastEmitIn + _lastEmitCch - 1 ] == '-')
                i = replace( _lastEmitIn + _lastEmitCch - 1, " " );
        }

        private void entitizeProcinst ( )
        {
            if (_lastEmitCch == 0)
                return;

            int i = _lastEmitIn;

            boolean lastWasQuestion = false;

            for ( int cch = _lastEmitCch ; cch > 0 ; cch-- )
            {
                char ch = _buf[ i ];

                if (isBadChar( ch ))
                    i = replace( i, "?" );

                if (ch == '>')
                {
    // TODO - Had to convert to a space here ... imples not well formed XML
                    if (lastWasQuestion)
                        i = replace( i, " " );
                    else
                        i++;

                    lastWasQuestion = false;
                }
                else
                {
                    lastWasQuestion = ch == '?';
                    i++;
                }

                if (i == _buf.length)
                    i = 0;
            }
        }


        /**
         * Test if a character is valid in xml character content. See
         * http://www.w3.org/TR/REC-xml#NT-Char
         */
        
        private boolean isBadChar ( char ch )
        {
            return ! (
                (ch >= 0x20 && ch <= 0xD7FF ) ||
                (ch >= 0xE000 && ch <= 0xFFFD) ||
                (ch >= 0x10000 && ch <= 0x10FFFF) ||
                (ch == 0x9) || (ch == 0xA) || (ch == 0xD)
                );
        }

        private int replace ( int i, String replacement )
        {
            assert replacement.length() > 0;

            int dCch = replacement.length() - 1;

            if (dCch == 0)
            {
                _buf[ i ] = replacement.charAt( 0 );
                return (i + 1) % _buf.length;
            }

            assert _free >= 0;

            if (dCch > _free)
                i = resize( dCch, i );

            assert _free >= 0;

            assert _free >= dCch;
            assert getAvailable() > 0;

            if (_out > _in && i >= _out)
            {
                System.arraycopy( _buf, _out, _buf, _out - dCch, i - _out );
                _out -= dCch;
                i -= dCch;
            }
            else
            {
                if (_in + dCch >= _buf.length) {
                  System.arraycopy(_buf, _out, _buf, 0, _in - _out);
                  i -= _out;
                  _in -= _out;
                  _out = 0;
                }

                assert i < _in;
                System.arraycopy( _buf, i, _buf, i + dCch, _in - i );
                _in += dCch;
            }

            replacement.getChars( 0, dCch + 1, _buf, i );

            _free -= dCch;
            
            assert _free >= 0;

            return (i + dCch + 1) % _buf.length;
        }

        int getAvailable ( )
        {
            return _buf == null ? 0 : _buf.length - _free;
        }

        /**
         * Make sure there is enough room for cch chars in the buffer
         */

        private int resize ( int cch, int i )
        {
            assert _free >= 0;
            assert cch > 0;
            assert cch > _free;

            int newLen = _buf == null ? _initialBufSize : _buf.length * 2;
            int used = getAvailable();

            while ( newLen - used < cch )
                newLen *= 2;

            char[] newBuf = new char [ newLen ];

            if (used > 0)
            {
                if (_in > _out)
                {
                    assert i == -1 || (i >= _out && i < _in);
                    System.arraycopy( _buf, _out, newBuf, 0, used );
                    i -= _out;
                }
                else
                {
                    assert i == -1 || (i >= _out || i < _in);
                    System.arraycopy( _buf, _out, newBuf, 0, used - _in );
                    System.arraycopy( _buf, 0, newBuf, used - _in, _in );
                    i = i >= _out ? i - _out : i + _out;
                }
                
                _out = 0;
                _in = used;
                _free += newBuf.length - _buf.length;
            }
            else
            {
                _free += newBuf.length;
                assert _in == 0 && _out == 0;
                assert i == -1;
            }

            _buf = newBuf;

            assert _free >= 0;

            return i;
        }

        public int read ( )
        {
            if (ensure( 1 ) == 0)
                return -1;

            assert getAvailable() > 0;

            int ch = _buf[ _out ];

            _out = (_out + 1) % _buf.length;
            _free++;

            return ch;
        }

        public int read ( char[] cbuf, int off, int len )
        {
            // Check for end of stream even if there is no way to return
            // characters because the Reader doc says to return -1 at end of
            // stream.

            int n;

            if ((n = ensure( len )) == 0)
                return -1;

            if (cbuf == null || len <= 0)
                return 0;

            if (n < len)
                len = n;

            if (_out < _in)
            {
                System.arraycopy( _buf, _out, cbuf, off, len );
            }
            else
            {
                int chunk = _buf.length - _out;

                if (chunk >= len)
                    System.arraycopy( _buf, _out, cbuf, off, len );
                else
                {
                    System.arraycopy( _buf, _out, cbuf, off, chunk );
                    System.arraycopy( _buf, 0, cbuf, off + chunk, len - chunk );
                }
            }

            _out = (_out + len) % _buf.length;
            _free += len;

            assert _free >= 0;

            return len;
        }

        public int write ( Writer writer, int cchMin )
        {
            while ( getAvailable() < cchMin)
            {
                if (!process())
                    break;
            }

            int charsAvailable = getAvailable();

            if (charsAvailable > 0)
            {
                // I don't want to deal with the circular cases

                assert _out == 0;

                try
                {
                    writer.write( _buf, 0, charsAvailable );
                    writer.flush();
                }
                catch ( IOException e )
                {
                    throw new RuntimeException( e );
                }

                _free += charsAvailable;
                
                assert _free >= 0;
                
                _in = 0;
            }

            return charsAvailable;
        }

        /**
         * Ensure all text and return it as a string.
         */

        public String saveToString ( )
        {
            // We're gonna build a string.  Instead of using StringBuffer, may
            // as well use my buffer here.  Fill the whole sucker up and
            // create a String!

            while ( process() )
                ;

            assert _out == 0;

            int available = getAvailable();

            return available == 0 ? "" : new String( _buf, _out, available );
        }

        private static final int _initialBufSize = 4096;

        private int _lastEmitIn;
        private int _lastEmitCch;

        private int    _free;
        private int    _in;
        private int    _out;
        private char[] _buf;
    }

    /**
     * A Reader which exposes the text of a part of the tree.
     */

    static final class TextReader extends Reader
    {
        TextReader ( Root r, Splay s, int p, XmlOptions options )
        {
            _textSaver = new TextSaver( r, s, p, options, null );
        }

        public void close ( ) throws IOException { }

        public boolean ready ( ) throws IOException { return true; }

        public int read ( ) throws IOException
        {
            return _textSaver.read();
        }

        public int read ( char[] cbuf ) throws IOException
        {
            return _textSaver.read( cbuf, 0, cbuf == null ? 0 : cbuf.length );
        }

        public int read ( char[] cbuf, int off, int len ) throws IOException
        {
            return _textSaver.read( cbuf, off, len );
        }

        private TextSaver _textSaver;
    }

    /**
     *
     */

    static final class InputStreamSaver extends InputStream
    {
        InputStreamSaver (
            Root r, Splay s, int p, XmlOptions options )
        {
            options = XmlOptions.maskNull(options);
            _byteBuffer = new OutputStreamImpl();

            String encoding = null;
            
            if (r._props.getEncoding() != null)
            {
                encoding =
                    EncodingMap.getIANA2JavaMapping( r._props.getEncoding() );
            }

            if (options.hasOption(XmlOptions.CHARACTER_ENCODING))
            {
                encoding =
                    (String) options.get(XmlOptions.CHARACTER_ENCODING);
            }

            if (encoding != null)
            {
                String ianaEncoding =
                    EncodingMap.getJava2IANAMapping( encoding );

                if (ianaEncoding != null)
                    encoding = ianaEncoding;
            }

            if (encoding == null)
                encoding = EncodingMap.getJava2IANAMapping( "UTF8" );

            String javaEncoding = EncodingMap.getIANA2JavaMapping( encoding );

            if (javaEncoding == null)
            {
                throw
                    new IllegalStateException(
                        "Unknown encoding: " + encoding );
            }

            try
            {
                _converter = new OutputStreamWriter( _byteBuffer, javaEncoding);
            }
            catch ( UnsupportedEncodingException e )
            {
                throw new RuntimeException( e );
            }

            _textSaver = new TextSaver( r, s, p, options, encoding );
        }

        public int read ( )
        {
            return _byteBuffer.read();
        }

        public int read ( byte[] bbuf, int off, int len )
        {
            return _byteBuffer.read ( bbuf, off, len );
        }

        private int ensure ( int cbyte )
        {
            // Even if we're asked to ensure nothing, still try to ensure
            // atleast one byte so we can determine if we're at the
            // end of the stream.

            if (cbyte <= 0)
                cbyte = 1;

            int bytesAvailable = _byteBuffer.getAvailable();

            for ( ; bytesAvailable < cbyte ;
                  bytesAvailable = _byteBuffer.getAvailable() )
            {
                if (_textSaver.write( _converter, 2048 ) < 2048)
                    break;
            }

            bytesAvailable = _byteBuffer.getAvailable();

            if (bytesAvailable == 0)
                return 0;

            return bytesAvailable;
        }

        private final class OutputStreamImpl extends OutputStream
        {
            int read ( )
            {
                if (InputStreamSaver.this.ensure( 1 ) == 0)
                    return -1;

                assert getAvailable() > 0;

                int bite = _buf[ _out ];

                _out = (_out + 1) % _buf.length;
                _free++;

                return bite;
            }

            int read ( byte[] bbuf, int off, int len )
            {
                // Check for end of stream even if there is no way to return
                // characters because the Reader doc says to return -1 at end of
                // stream.

                int n;

                if ((n = ensure( len )) == 0)
                    return -1;

                if (bbuf == null || len <= 0)
                    return 0;

                if (n < len)
                    len = n;

                if (_out < _in)
                {
                    System.arraycopy( _buf, _out, bbuf, off, len );
                }
                else
                {
                    int chunk = _buf.length - _out;

                    if (chunk >= len)
                        System.arraycopy( _buf, _out, bbuf, off, len );
                    else
                    {
                        System.arraycopy( _buf, _out, bbuf, off, chunk );

                        System.arraycopy(
                            _buf, 0, bbuf, off + chunk, len - chunk );
                    }
                }

                _out = (_out + len) % _buf.length;
                _free += len;

                return len;
            }

            int getAvailable ( )
            {
                return _buf == null ? 0 : _buf.length - _free;
            }

            public void write ( int bite )
            {
                if (_free == 0)
                    resize( 1 );

                assert _free > 0;

                _buf[ _in ] = (byte) bite;

                _in = (_in + 1) % _buf.length;
                _free--;
            }

            public void write ( byte[] buf, int off, int cbyte )
            {
                assert cbyte >= 0;

                if (cbyte == 0)
                    return;

                if (_free < cbyte)
                    resize( cbyte );

                if (_in == _out)
                {
                    assert getAvailable() == 0;
                    assert _free == _buf.length - getAvailable();
                    _in = _out = 0;
                }

                int chunk;

                if (_in <= _out || cbyte < (chunk = _buf.length - _in))
                {
                    System.arraycopy( buf, off, _buf, _in, cbyte );
                    _in += cbyte;
                }
                else
                {
                    System.arraycopy( buf, off, _buf, _in, chunk );

                    System.arraycopy(
                        buf, off + chunk, _buf, 0, cbyte - chunk );

                    _in = (_in + cbyte) % _buf.length;
                }

                _free -= cbyte;
            }

            void resize ( int cbyte )
            {
                assert cbyte > _free;

                int newLen = _buf == null ? _initialBufSize : _buf.length * 2;
                int used = getAvailable();

                while ( newLen - used < cbyte )
                    newLen *= 2;

                byte[] newBuf = new byte [ newLen ];

                if (used > 0)
                {
                    if (_out == _in)
                        System.arraycopy( _buf, 0, newBuf, 0, used );
                    else if (_in > _out)
                        System.arraycopy( _buf, _out, newBuf, 0, used );
                    else
                    {
                        System.arraycopy(
                            _buf, _out, newBuf, 0, used - _in );

                        System.arraycopy(
                            _buf, 0, newBuf, used - _in, _in );
                    }

                    _out = 0;
                    _in = used;
                    _free += newBuf.length - _buf.length;
                }
                else
                {
                    _free += newBuf.length;
                    assert _in == 0 && _out == 0;
                }

                _buf = newBuf;
            }

            private static final int _initialBufSize = 4096;

            int    _free;
            int    _in;
            int    _out;
            byte[] _buf;
        }

        private OutputStreamImpl   _byteBuffer;
        private TextSaver          _textSaver;
        private OutputStreamWriter _converter;
    }

    /**
     *
     */

    static final class XmlInputStreamSaver extends Saver
    {
        XmlInputStreamSaver ( Root r, Splay s, int p, XmlOptions options )
        {
            super( r, s, p, options );
        }

        XMLEvent dequeue ( ) throws XMLStreamException
        {
            if (_out == null && !process())
                return null;

            if (_out == null)
                return null;

            XmlEventImpl e = _out;

            if ((_out = _out._next) == null)
                _in = null;

            return e;
        }

        private void enqueue ( XmlEventImpl e )
        {
            assert e._next == null;

            if (_in == null)
            {
                assert _out == null;
                _out = _in = e;
            }
            else
            {
                _in._next = e;
                _in = e;
            }
        }

        //
        //
        //

        protected void emitXmlnsFragment ( Splay s )
        {
            throw new IllegalStateException( "Can't stream an attribute" );
        }

        protected void emitText ( Splay s, int p, int cch )
        {
            assert cch > 0;
            enqueue( new CharacterDataImpl( getRoot(), s, p, null ) );
        }

        protected void emitTextFragment ( Splay s, int p, int cch )
        {
            // BUGBUG - there could be namespaces pushed for this text, not
            // sure how to communicate those, however

            if (_text != null)
            {
                if (_text.length() > 0)
                    emitText( s, p, cch );
                
                return;
            }
            
            if (s != null && cch > 0)
                emitText( s, p, cch );
        }

        protected void emitTextAfter ( Splay s, int p, int cch )
        {
            if (_text == null)
                emitText( s, p, cch );
            else
                enqueue( new CharacterDataImpl( getRoot(), s, -1, text() ) );
        }

        protected void emitEndPrefixMappings ( )
        {
            Root r = getRoot();

            for ( iterateMappings() ; hasMapping() ; nextMapping() )
            {
                String prevPrefixUri = mappingPrevPrefixUri();

                if (prevPrefixUri == null)
                    enqueue( new EndPrefixMappingImpl( r, mappingPrefix() ) );
                else
                {
                    enqueue(
                        new ChangePrefixMappingImpl(
                            r, mappingPrefix(), mappingUri(), prevPrefixUri ) );
                }
            }
        }

        protected void emitEnd ( Splay s, QName name )
        {
            Root r = getRoot();

            if (s.isRoot())
                enqueue( new EndDocumentImpl( r, s ) );
            else if (s.isEnd())
                enqueue( new EndElementImpl( r, s, name, getUriMap() ) );
            else
            {
                assert s.isLeaf();
                enqueue( new EndElementImpl( r, s, name, getUriMap() ) );
            }

            emitEndPrefixMappings();
        }

        protected void emitCommentFragment ( Splay s )
        {
            emitComment( s );
        }

        protected void emitProcinstFragment ( Splay s )
        {
            emitProcinst( s );
        }

        protected void emitDocType(
            String doctypeName, String publicID, String systemID )
        {
        }
        
        protected void emitComment ( Splay s )
        {
            enqueue( new CommentImpl( getRoot(), s ) );
        }

        protected void emitProcinst ( Splay s )
        {
            enqueue( new ProcessingInstructionImpl( getRoot(), s ) );
        }

        protected void emitAttrFragment ( Splay s )
        {
            throw new IllegalStateException( "Can't stream an attribute" );
        }

        protected void emitContainer ( Container c, QName name )
        {
            Root r = getRoot();

            for ( iterateMappings() ; hasMapping() ; nextMapping() )
            {
                enqueue(
                    new StartPrefixMappingImpl(
                        r, mappingPrefix(), mappingUri() ) );
            }

            if (c.isDoc())
                enqueue( new StartDocumentImpl( r, c ) );

            if (name != null)
            {
                enqueue(
                    new StartElementImpl( r, c, name, this ) );
            }

            if (c.isDoc())
            {
                assert c.isDoc();

                if (_text != null)
                {
                    if (_text.length() > 0)
                        enqueue( new CharacterDataImpl( r, c, -1, text() ) );
                }
                else if (c.getCch() > 0)
                    enqueue( new CharacterDataImpl( r, c, 1, null ) );
            }
            else if (c.isLeaf())
            {
                if (_text != null)
                {
                    if (_text.length() > 0)
                        enqueue( new CharacterDataImpl( r, c, -1, text() ));
                }
                else if (c.getCchValue() > 0)
                    enqueue( new CharacterDataImpl( r, c, 1, null ) );

                enqueue( new EndElementImpl( r, c, name, getUriMap() ) );

                emitEndPrefixMappings();
            }
        }


        //
        //
        //

        private static XMLName computeName ( QName n, Map uriMap )
        {
            String uri = n.getNamespaceURI();

            if (uri.length() == 0)
                uri = null;

            assert n.getLocalPart().length() > 0;

            // The following assert may fire if someone computes a name
            // of an element/attr too late (after other events have been
            // enqueued and the uri map has been updated.  I check later
            // to make sure we don't crash, however.

            assert uri == null || uriMap.containsKey( uri ) : "Problem uri " + uri;

            String prefix = null;

            if (uri != null)
            {
                prefix = (String) uriMap.get( uri );

                if (prefix != null && prefix.length() == 0)
                    prefix = null;
            }

            return new XmlNameImpl( uri, n.getLocalPart(), prefix );
        }

        private static abstract class XmlEventImpl extends XmlEventBase
        {
            public Object monitor()
            {
                return _root;
            }
            
            XmlEventImpl ( int type, Root r, Splay s )
            {
                super( type );
                _root = r;
                _splay = s;
                _version = _root.getVersion();
            }

            public XMLName getName ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    return null;
                }
            }

            public XMLName getSchemaType ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    throw new RuntimeException( "NYI" );
                }
            }

            public boolean hasName ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    return false;
                }
            }

            public final Location getLocation ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    // TODO - perhaps I can save a location goober sometimes?
                    return null;
                }
            }

            protected final void checkVersion ( )
            {
                if (_version != _root.getVersion())
                    throw new ConcurrentModificationException( "Document changed" );
            }

            protected final Root getRoot ( )
            {
                return _root;
            }

            protected final Splay getSplay ( )
            {
                return _splay;
            }

            private Root  _root;
            private Splay _splay;
            private long  _version;

            XmlEventImpl _next;
        }

        private static class StartDocumentImpl
            extends XmlEventImpl implements StartDocument
        {
            StartDocumentImpl ( Root r, Splay s )
            {
                super( XMLEvent.START_DOCUMENT, r, s );
            }

            public String getSystemId ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    return getRoot()._props.getDoctypeSystemId();
                }
            }

            public String getCharacterEncodingScheme ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    return getRoot()._props.getEncoding();
                }
            }

            public boolean isStandalone ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    return getRoot()._standAlone;
                }
            }

            public String getVersion ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    return getRoot()._props.getVersion();
                }
            }
        }

        private static class StartElementImpl
            extends XmlEventImpl implements StartElement
        {
            StartElementImpl ( Root r, Splay s, QName name, Saver saver )
            {
                super( XMLEvent.START_ELEMENT, r, s );

                _name = computeName( name, saver.getUriMap() );

                _prefixMap = saver.getPrefixMap();

                AttributeImpl lastAttr = null;

                for ( Iterator i = saver._attrs.keySet().iterator() ;
                      i.hasNext() ; )
                {
                    Splay a = (Splay) i.next();
                    
                    AttributeImpl attr =
                        new NormalAttributeImpl(
                            r, a, (String) saver._attrs.get( a ),
                            saver.getUriMap() );
                    
                    if (_attributes == null)
                        _attributes = attr;
                    else
                        lastAttr._next = attr;

                    lastAttr = attr;
                }

                lastAttr = null;

                for ( saver.iterateMappings() ;
                      saver.hasMapping() ; saver.nextMapping() )
                {
                    AttributeImpl attr =
                        new XmlnsAttributeImpl(
                            r, saver.mappingPrefix(), saver.mappingUri(),
                            saver.getUriMap() );

                    if (_namespaces == null)
                        _namespaces = attr;
                    else
                        lastAttr._next = attr;

                    lastAttr = attr;
                }
            }

            public boolean hasName()
            {
                synchronized (monitor())
                {
                    checkVersion();
                    return true;
                }
            }

            public XMLName getName ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    return _name;
                }
            }

            public AttributeIterator getAttributes ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
    
                    return
                        new AttributeIteratorImpl( getRoot(), _attributes, null );
                }
            }

            public AttributeIterator getNamespaces ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
    
                    return
                        new AttributeIteratorImpl( getRoot(), null, _namespaces );
                }
            }

            public AttributeIterator getAttributesAndNamespaces ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
    
                    return
                        new AttributeIteratorImpl(
                            getRoot(), _attributes, _namespaces );
                }
            }

            public Attribute getAttributeByName ( XMLName xmlName )
            {
                synchronized (monitor())
                {
                    checkVersion();
    
                    for ( AttributeImpl a = _attributes ; a != null ; a = a._next )
                    {
                        if (xmlName.equals( a.getName() ))
                            return a;
                    }
    
                    return null;
                }
            }

            public String getNamespaceUri ( String prefix )
            {
                synchronized (monitor())
                {
                    checkVersion();
    
                    return
                        (String) _prefixMap.get( prefix == null ? "" : prefix );
                }
            }

            public Map getNamespaceMap ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
    
                    return _prefixMap;
                }
            }

            private static class AttributeIteratorImpl
                implements AttributeIterator
            {
                public Object monitor()
                {
                    return _root;
                }
                
                AttributeIteratorImpl (
                    Root r, AttributeImpl attributes, AttributeImpl namespaces )
                {
                    _root = r;
                    _version = r.getVersion();
                    _attributes = attributes;
                    _namespaces = namespaces;
                }

                public Attribute next ( )
                {
                    synchronized (monitor())
                    {
                        checkVersion();
    
                        AttributeImpl attr = null;
    
                        if (_attributes != null)
                        {
                            attr = _attributes;
                            _attributes = attr._next;
                        }
                        else if (_namespaces != null)
                        {
                            attr = _namespaces;
                            _namespaces = attr._next;
                        }
    
                        return attr;
                    }
                }

                public boolean hasNext ( )
                {
                    synchronized (monitor())
                    {
                        checkVersion();
    
                        return _attributes != null || _namespaces != null;
                    }
                }

                public Attribute peek ( )
                {
                    synchronized (monitor())
                    {
                        checkVersion();
    
                        if (_attributes != null)
                            return _attributes;
                        else if (_namespaces != null)
                            return _namespaces;
    
                        return null;
                    }
                }

                public void skip ( )
                {
                    synchronized (monitor())
                    {
                        checkVersion();
    
                        if (_attributes != null)
                            _attributes = _attributes._next;
                        else if (_namespaces != null)
                            _namespaces = _namespaces._next;
                    }
                }

                private final void checkVersion ( )
                {
                    if (_version != _root.getVersion())
                        throw new IllegalStateException( "Document changed" );
                }

                private Root          _root;
                private long          _version;
                private AttributeImpl _attributes;
                private AttributeImpl _namespaces;
            }

            private static abstract class AttributeImpl implements Attribute
            {
                public Object monitor()
                {
                    return _root;
                }
                
                AttributeImpl ( Root r )
                {
                    _root = r;
                    _version = r.getVersion();
                }

                public XMLName getName ( )
                {
                    synchronized (monitor())
                    {
                        checkVersion();
                        return _name;
                    }
                }

                public String getType ( )
                {
                    synchronized (monitor())
                    {
                        checkVersion();
    // TODO - Make sure throwing away this DTD info is ok.
    // Is there schema info which can return more useful info?
                        return "CDATA";
                    }
                }

                public XMLName getSchemaType ( )
                {
                    synchronized (monitor())
                    {
                        checkVersion();
    // TODO - Can I return something reasonable here?
                        return null;
                    }
                }

                protected final void checkVersion ( )
                {
                    if (_version != _root.getVersion())
                        throw new IllegalStateException( "Document changed" );
                }

                AttributeImpl _next;

                protected XMLName _name;

                protected Root  _root;
                private long  _version;
            }

            private static class XmlnsAttributeImpl extends AttributeImpl
            {
                XmlnsAttributeImpl (
                    Root r, String prefix, String uri, Map uriMap )
                {
                    super( r );

                    _uri = uri;

                    String local;

                    if (prefix.length() == 0)
                    {
                        prefix = null;
                        local = "xmlns";
                    }
                    else
                    {
                        local = prefix;
                        prefix = "xmlns";
                    }

                    _name = new XmlNameImpl( null, local, prefix );
                }

                public String getValue ( )
                {
                    synchronized (monitor())
                    {
                        checkVersion();
                        return _uri;
                    }
                }

                private String _uri;
            }

            private static class NormalAttributeImpl extends AttributeImpl
            {
                NormalAttributeImpl (
                    Root r, Splay s, String value, Map uriMap )
                {
                    super( r );
                    assert s.isNormalAttr();
                    _splay = s;
                    _value = value;
                    _name = computeName( s.getName(), uriMap );
                }

                public String getValue ( )
                {
                    synchronized (monitor())
                    {
                        checkVersion();
    
                        return _value != null ? _value : _splay.getText( _root );
                    }
                }

                private String _value; // If invalid in the store
                private Splay _splay;
            }

            private XMLName _name;
            private Map     _prefixMap;

            private AttributeImpl _attributes;
            private AttributeImpl _namespaces;
        }

        private static class StartPrefixMappingImpl
            extends XmlEventImpl implements StartPrefixMapping
        {
            StartPrefixMappingImpl ( Root r, String prefix, String uri )
            {
                super( XMLEvent.START_PREFIX_MAPPING, r, null );

                _prefix = prefix;
                _uri = uri;
            }

            public String getNamespaceUri ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    return _uri;
                }
            }

            public String getPrefix ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    return _prefix;
                }
            }

            private String _prefix, _uri;
        }

        private static class ChangePrefixMappingImpl
            extends XmlEventImpl implements ChangePrefixMapping
        {
            ChangePrefixMappingImpl (
                Root r, String prefix, String oldUri, String newUri )
            {
                super( XMLEvent.CHANGE_PREFIX_MAPPING, r, null );

                _oldUri = oldUri;
                _newUri = newUri;
                _prefix = prefix;
            }

            public String getOldNamespaceUri ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    return _oldUri;
                }
            }

            public String getNewNamespaceUri ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    return _newUri;
                }
            }

            public String getPrefix ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    return _prefix;
                }
            }

            private String _oldUri, _newUri, _prefix;
        }

        private static class EndPrefixMappingImpl
            extends XmlEventImpl implements EndPrefixMapping
        {
            EndPrefixMappingImpl ( Root r, String prefix )
            {
                super( XMLEvent.END_PREFIX_MAPPING, r, null );
                _prefix = prefix;
            }

            public String getPrefix ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    return _prefix;
                }
            }

            private String _prefix;
        }

        private static class CharacterDataImpl
            extends XmlEventImpl implements CharacterData
        {
            CharacterDataImpl ( Root r, Splay s, int p, String charData )
            {
                super( XMLEvent.CHARACTER_DATA, r, s );

                assert p > 0 || (charData != null && charData.length() > 0);

                _pos = p;
                _charData = charData;
            }

            public String getContent ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
    
                    Splay s = getSplay();
    
                    if (_pos == -1)
                        return _charData;
    
                    Root r = getRoot();
    
                    return
                        r._text.fetch(
                            s.getCpForPos( r, _pos ), s.getPostCch( _pos ) );
                }
            }

            public boolean hasContent ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    return true;
                }
            }

            private int    _pos;
            private String _charData;
        }

        private static class EndElementImpl
            extends XmlEventImpl implements EndElement
        {
            EndElementImpl ( Root r, Splay s, QName name, Map uriMap )
            {
                super( XMLEvent.END_ELEMENT, r, s );

                assert s.isLeaf() || s.isEnd();

                if (name == null)
                {
                    if (s.isEnd())
                        s = s.getContainer();
                    
                    name = s.getName();
                }

                _name = computeName( name, uriMap );
            }

            public boolean hasName ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    return true;
                }
            }

            public XMLName getName ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    return _name;
                }
            }

            private XMLName _name;
        }

        private static class EndDocumentImpl
            extends XmlEventImpl implements EndDocument
        {
            EndDocumentImpl ( Root r, Splay s )
            {
                super( XMLEvent.END_DOCUMENT, r, s );
            }
        }

        private static class CommentImpl
            extends XmlEventImpl implements Comment
        {
            CommentImpl ( Root r, Splay s )
            {
                super( XMLEvent.COMMENT, r, s );
                assert s.isComment();
            }

            public String getContent ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
    
                    Splay s = getSplay();
                    return s.getCch() == 0 ? null : s.getText( getRoot() );
                }
            }

            public boolean hasContent ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
    
                    return getSplay().getCch() > 0;
                }
            }
        }

        private static class ProcessingInstructionImpl
            extends XmlEventImpl implements ProcessingInstruction
        {
            ProcessingInstructionImpl ( Root r, Splay s )
            {
                super( XMLEvent.PROCESSING_INSTRUCTION, r, s );
                assert s.isProcinst();
            }

            public String getTarget ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    return getSplay().getLocal();
                }
            }

            public String getData ( )
            {
                synchronized (monitor())
                {
                    checkVersion();
                    Splay s = getSplay();
                    return s.getCch() == 0 ? null : s.getText( getRoot() );
                }
            }
        }

        private XmlEventImpl _in, _out;
    }

    static final class XmlInputStreamImpl extends GenericXmlInputStream
    {
        XmlInputStreamImpl ( Root r, Splay s, int p, XmlOptions options )
        {
            _xmlInputStreamSaver =
                new XmlInputStreamSaver( r, s, p, options );

            // Make the saver grind away just a bit to throw any exceptions
            // related to the inability to create a stream on this xml
            
            _xmlInputStreamSaver.process();
        }

        protected XMLEvent nextEvent ( ) throws XMLStreamException
        {
            return _xmlInputStreamSaver.dequeue();
        }

        private XmlInputStreamSaver _xmlInputStreamSaver;
    }

    //
    //
    //

    static final class ValidatorSaver
        extends Saver implements ValidatorListener.Event
    {
        ValidatorSaver (
            Root r, Splay s, int p,
            XmlOptions options, ValidatorListener vEventSink )
        {
            super( r, s, p, options );

            _wantDupAttrs = true;

            assert p == 0;

            _startSplay = s;
            _vEventSink = vEventSink;

            while ( process() )
                ;  // Empty
        }

        protected void emitXmlnsFragment ( Splay s )
        {
            throw new IllegalStateException();
        }

        protected void emitTextFragment ( Splay s, int p, int cch )
        {
            throw new IllegalStateException();
        }

        protected void emitCommentFragment ( Splay s )
        {
            throw new IllegalStateException();
        }

        protected void emitProcinstFragment ( Splay s )
        {
            throw new IllegalStateException();
        }

        protected void emitTextAfter ( Splay s, int p, int cch )
        {
            if (_text == null)
            {
                assert cch > 0;
                emitEvent( ValidatorListener.TEXT, s, p, null, s, p );
            }
            else
                emitEvent( ValidatorListener.TEXT, s, p, null, text() );
        }

        protected void emitEnd ( Splay s, QName name )
        {
            emitEvent( ValidatorListener.END, s, 0 );
        }

        protected void emitDocType(
            String doctypeName, String publicID, String systemID )
        {
        }
        
        protected void emitComment ( Splay s )
        {
            if (s.getCchAfter() > 0)
                emitEvent( ValidatorListener.TEXT, s, 0, null, s, 1 );
        }

        protected void emitProcinst ( Splay s )
        {
            if (s.getCchAfter() > 0)
                emitEvent( ValidatorListener.TEXT, s, 0, null, s, 1 );
        }

        protected void emitAttrFragment ( Splay s )
        {
            emitEvent( ValidatorListener.BEGIN, s, 0, null );

            if (_text != null)
            {
                if (_text.length() > 0)
                    emitEvent( ValidatorListener.TEXT, s, 0, null, text() );
            }
            else if (s.getCch() > 0)
                emitEvent( ValidatorListener.TEXT, s, 0, null, s, 0 );

            emitEvent( ValidatorListener.END, s, 0 );
        }

        protected void emitContainer ( Container c, QName name )
        {
            assert _xsiNoLoc == null;
            assert _xsiLoc == null;
            assert _xsiType == null;
            assert _xsiNil == null;

            for ( Splay s = c.nextSplay() ; s.isAttr() ; s = s.nextSplay() )
            {
                if (s.isXsiAttr())
                {
                    String local = s.getLocal();

                    if (local.equals( "type" ))
                        _xsiType = s;
                    else if (local.equals( "nil" ))
                        _xsiNil = s;
                    else if (local.equals( "schemaLocation" ))
                        _xsiLoc = s;
                    else if (local.equals( "noNamespaceSchemaLocation" ))
                        _xsiNoLoc = s;
                }
            }

            emitEvent(
                ValidatorListener.BEGIN, c, 0,
                c == _startSplay ? null : name );

            _xsiNoLoc = _xsiLoc = _xsiType = _xsiNil = null;

            for ( Iterator i = _attrs.keySet().iterator() ; i.hasNext() ; )
            {
                Splay s = (Splay) i.next();
                
                if (s.isXsiAttr())
                {
                    String local = s.getLocal();

                    if (local.equals( "type" ) ||
                        local.equals( "nil" ) ||
                        local.equals( "schemaLocation" ) ||
                        local.equals( "noNamespaceSchemaLocation" ))
                    {
                        continue;
                    }
                }

                String invalidAttrValue = (String) _attrs.get( s );

                if (invalidAttrValue == null)
                {
                    emitEvent(
                        ValidatorListener.ATTR, s, 0, s.getName(), s, 0 );
                }
                else
                {
                    emitEvent(
                        ValidatorListener.ATTR, s, 0, s.getName(),
                        invalidAttrValue );
                }
            }

            emitEvent( ValidatorListener.ENDATTRS, c, 0 );

            if (c.isDoc())
            {
                assert c.isDoc();

                if (_text != null)
                {
                    if (_text.length() > 0)
                        emitEvent( ValidatorListener.TEXT, c, 1, null, text() );
                }
                else if (c.getCch() > 0)
                    emitEvent( ValidatorListener.TEXT, c, 1, null, c, 1 );
            }
            else if (c.isLeaf())
            {
                int cch = _text != null ? _text.length() : c.getCchValue();

                if (cch > 0)
                {
                    if (_text != null)
                    {
                        emitEvent(
                            ValidatorListener.TEXT, c, 1, null, text() );
                    }
                    else
                    {
                        emitEvent(
                            ValidatorListener.TEXT, c, 1, null, c, 1 );
                    }
                }

                emitEvent( ValidatorListener.END, c, c.getPosLeafEnd() );
            }
        }

        protected void emitEvent ( int kind, Splay sLoc, int pLoc )
            { emitEvent( kind, sLoc, pLoc, null, null, 0, null ); }

        protected void emitEvent ( int kind, Splay sLoc, int pLoc, QName name )
            { emitEvent( kind, sLoc, pLoc, name, null, 0, null ); }

        protected void emitEvent (
            int kind, Splay sLoc, int pLoc, QName name, String text )
                { emitEvent ( kind, sLoc, pLoc, name, null, 0, text ); }

        protected void emitEvent (
            int kind, Splay sLoc, int pLoc, QName name, Splay sText, int pText )
                { emitEvent ( kind, sLoc, pLoc, name, sText, pText, null ); }

        protected void emitEvent (
            int kind, Splay sLoc, int pLoc, QName name,
            Splay sText, int pText, String text )
        {
            if (kind == ValidatorListener.TEXT && _emittedText)
                return;

            boolean hasText = text != null || sText != null;

            assert
                !hasText ||
                    (kind == ValidatorListener.ATTR ||
                        kind == ValidatorListener.TEXT);

            assert kind != ValidatorListener.ATTR || hasText;
            assert kind != ValidatorListener.TEXT || hasText;

            assert kind != ValidatorListener.ATTR || name != null;

            _name = name;
            _sText = sText;
            _pText = pText;
            _eventText = text;
            _hasText = hasText;
            _sLoc = sLoc;
            _pLoc = pLoc;

            _vEventSink.nextEvent( kind, this );

            _emittedText = kind == ValidatorListener.TEXT;
        }

        //
        //
        //

        public XmlCursor getLocationAsCursor ( )
        {
            checkVersion();
            return new Cursor( getRoot(), _sLoc, _pLoc );
        }

        public boolean getXsiType ( Chars chars )
        {
            if (_xsiType == null)
                return false;

            setChars( chars, PRESERVE, null, _xsiType, 0 );

            return true;
        }

        public boolean getXsiNil ( Chars chars )
        {
            if (_xsiNil == null)
                return false;


            setChars( chars, PRESERVE, null, _xsiNil, 0 );

            return true;
        }

        public boolean getXsiLoc ( Chars chars )
        {
            if (_xsiLoc == null)
                return false;

            setChars( chars, PRESERVE, null, _xsiLoc, 0 );

            return true;
        }

        public boolean getXsiNoLoc ( Chars chars )
        {
            if (_xsiNoLoc == null)
                return false;

            setChars( chars, PRESERVE, null, _xsiNoLoc, 0 );

            return true;
        }

        public QName getName ( )
        {
            return _name;
        }

        private void setChars (
            Chars chars, int wsr, String string, Splay sText, int pText )
        {
            assert string != null || sText != null;

            checkVersion();

            Root r = getRoot();

            chars.buffer = null;
            chars.string = null;

            if (string != null)
            {
                chars.string = string;
            }
            else if (pText == 0)
            {
                chars.buffer = r._text._buf;
                chars.length = sText.getCch();

                chars.offset =
                    r._text.unObscure(
                        r.getCp( sText ), chars.length );
            }
            else if (pText == 1 && sText.isLeaf())
            {
                chars.buffer = r._text._buf;
                chars.length = sText.getCchValue();

                chars.offset =
                    r._text.unObscure(
                        r.getCp( sText ), chars.length );
            }
            else
            {
                assert pText == sText.getPosAfter();

                boolean moreText = false;

                for ( Splay t = sText.nextNonAttrSplay() ; ;
                      t = t.nextSplay() )
                {
                    if (!t.isComment() && !t.isProcinst())
                        break;

                    if (t.getCchAfter() > 0)
                    {
                        moreText = true;
                        break;
                    }
                }

                if (!moreText)
                {
                    chars.buffer = r._text._buf;
                    chars.length = sText.getCchAfter();

                    chars.offset =
                        r._text.unObscure(
                            sText.getCpForPos( r, pText ),
                            chars.length );
                }
                else
                {
                    StringBuffer sb = new StringBuffer();

                    int cch = sText.getCchAfter();

                    int off =
                        r._text.unObscure(
                            sText.getCpForPos( r, pText ), cch );

                    sb.append( r._text._buf, off, cch );

                    for ( Splay t = sText.nextNonAttrSplay() ; ;
                          t = t.nextSplay() )
                    {
                        if (!t.isComment() && !t.isProcinst())
                            break;

                        if (t.getCchAfter() > 0)
                        {
                            cch = t.getCchAfter();

                            off =
                                r._text.unObscure(
                                    t.getCpForPos( r, 1 ), cch );

                            sb.append( r._text._buf, off, cch );
                        }
                    }

                    chars.length = sb.length();
                    chars.buffer = new char [ chars.length ];
                    chars.offset = 0;

                    sb.getChars( 0, chars.length, chars.buffer, 0 );
                }
            }

            if (wsr != PRESERVE)
            {
                // TODO - this is quick, dirty and very inefficient
                //        make it faster!

                String str = chars.asString();
                StringBuffer sb = new StringBuffer();
                int state = -1, nSpaces = 0, cch = str.length();

                for ( int i = 0 ; i < cch ; i++ )
                {
                    char ch = str.charAt( i );

                    if (ch == '\n' || ch == '\r' || ch == '\t')
                        ch = ' ';

                    if (wsr == COLLAPSE)
                    {
                        if (ch == ' ')
                        {
                            if (state == -1)
                                continue;

                            nSpaces++;

                            continue;
                        }

                        if (nSpaces > 1)
                            nSpaces = 1;

                        for ( ; nSpaces > 0 ; nSpaces-- )
                            sb.append( ' ' );

                        state = 0;
                    }

                    sb.append( ch );
                }

                chars.string = sb.toString();
                chars.buffer = null;
            }
        }

        public void getText ( Chars chars )
        {
            getText( chars, PRESERVE );
        }

        public void getText ( Chars chars, int wsr )
        {
            if (!_hasText)
                throw new RuntimeException( "No text for this event");

            setChars( chars, wsr, _eventText, _sText, _pText );
        }

        // TODO - rather expensive to make Chars and getText and get
        // String
        public boolean textIsWhitespace ( )
        {
            Chars chars = new Chars();
            getText( chars );
            String s = chars.asString();

            for ( int i = 0 ; i < s.length() ; i++ )
            {
                switch ( s.charAt( i ) )
                {
                    case ' ':
                    case '\n':
                    case '\r':
                    case '\t':
                        break;

                    default :
                        return false;
                }
            }

            return true;
        }

        private ValidatorListener _vEventSink;
        private Splay             _startSplay;
        private boolean           _emittedText;

        private QName   _name;
        private Splay   _xsiType;
        private Splay   _xsiNil;
        private Splay   _xsiLoc;
        private Splay   _xsiNoLoc;
        private String  _eventText;
        private Splay   _sText;
        private int     _pText;
        private Splay   _sLoc;
        private int     _pLoc;
        private boolean _hasText;
    }

    //
    //
    //

    static final class SaxSaver extends Saver
    {
        SaxSaver (
            Root r, Splay s, int p, XmlOptions options,
            ContentHandler contentHandler, LexicalHandler lexicalhandler )
                throws SAXException
        {
            super( r, s, p, options );

            _attributes = new AttributesImpl();
            
            _wantFragTest = true;
            
            _contentHandler = contentHandler;
            _lexicalhandler = lexicalhandler;

//            _contentHandler.setDocumentLocator( hhmmmm  );
            
            _contentHandler.startDocument();

            try
            {
                while ( process() )
                    ;
            }
            catch ( SaverSAXException e )
            {
                throw e._saxException;
            }
            
            _contentHandler.endDocument();
        }

        private class SaverSAXException extends RuntimeException
        {
            SaverSAXException ( SAXException e )
            {
                _saxException = e;
            }
            
            SAXException _saxException;
        }

        private void throwSaxException ( SAXException e )
        {
            throw new SaverSAXException( e );
        }

        protected void emitContainer ( Container c, QName name )
        {
            if (c.isBegin())
            {
                emitContainerHelper( c, name, null, null );

                if (c.isLeaf())
                {
                    int cch = _text == null ? c.getCchValue() : _text.length();

                    if (cch > 0)
                    {
                        if (_text == null)
                            emitCharacters( c, 0, cch );
                        else
                            emitCharacters( _text );
                    }
                    
                    emitEndElement( c.getName() );
                }
            }
            else
            {
                assert c.isDoc();

                if (name != null)
                    emitContainerHelper( c, name, null, null );

                if (_text == null)
                    emitCharacters( c, 0, c.getCch() );
                else
                    emitCharacters( _text );
            }
        }

        private void emitAttrHelper ( Splay s, String value )
        {
            assert s.isNormalAttr();
            
            String local = s.getLocal();
            String uri = s.getUri();

            _attributes.addAttribute(
                s.getUri(), s.getLocal(),
                getPrefixedName( s.getName() ),
                "CDATA",
                value == null ? s.getText( getRoot() ) : value );
        }

        private void addNamespaceAttr ( String prefix, String uri )
        {
            try
            {
                _contentHandler.startPrefixMapping( prefix, uri );
            }
            catch ( SAXException e )
            {
                throwSaxException( e );
            }

            if (prefix.length() == 0)
                _attributes.addAttribute( "", "", "xmlns", "CDATA", uri );
            else
            {
                _attributes.addAttribute(
                    "", "", "xmlns:" + prefix, "CDATA", uri );
            }
        }
        
        private void emitNamespacesHelper ( )
        {
            for ( iterateMappings() ; hasMapping() ; nextMapping() )
                addNamespaceAttr( mappingPrefix(), mappingUri() );
        }

        private void emitContainerHelper (
            Container c, QName name,
            Splay extraAttr, StringBuffer extraAttrText )
        {
            assert name != null;
            
            _attributes.clear();

            if (_saveNamespacesFirst)
                emitNamespacesHelper();

            for ( Iterator i = _attrs.keySet().iterator() ; i.hasNext() ; )
            {
                Splay s = (Splay) i.next();
                emitAttrHelper( s, (String) _attrs.get( s ) );
            }

            if (extraAttr != null)
            {
                emitAttrHelper(
                    extraAttr,
                    extraAttrText == null ? null : extraAttrText.toString() );
            }

            if (!_saveNamespacesFirst)
                emitNamespacesHelper();
            
            emitElement( name, getPrefixedName( name ) );
        }

        private void emitCharacters ( char[] buf, int off, int cch )
        {
            try
            {
                _contentHandler.characters( buf, off, cch );
            }
            catch ( SAXException e )
            {
                throwSaxException( e );
            }
        }
        
        private void emitCharacters ( Splay s, int p, int cch )
        {
            emitCharacters( s.getCpForPos( getRoot(), p ), cch );
        }
        
        private void emitCharacters ( int cp, int cch )
        {
            if (cch == 0)
                return;

            emitCharacters(
                getRoot()._text._buf, getRoot()._text.unObscure( cp, cch ),
                cch );
        }
        
        private void emitCharacters ( StringBuffer sb )
        {
            if (sb.length() == 0)
                return;
            
            String text = sb.toString(); // Inefficient, use a shared char[]
            
            emitCharacters( text.toCharArray(), 0, text.length() );
        }
        
        private void emitEndElement ( QName name )
        {
            try
            {
                _contentHandler.endElement(
                    name.getNamespaceURI(), name.getLocalPart(),
                    getPrefixedName( name ) );
            }
            catch ( SAXException e )
            {
                throwSaxException( e );
            }
            
            for ( iterateMappings() ; hasMapping() ; nextMapping() )
            {
                try
                {
                    _contentHandler.endPrefixMapping( mappingPrefix() );
                }
                catch ( SAXException e )
                {
                    throwSaxException( e );
                }
            }
        }
        
        private void endNamespaces ( )
        {
            try
            {
                for ( int i = 0 ; i < _attributes.getLength() ; i ++ )
                {
                    String qn = _attributes.getQName( i );
                    
                    if (!qn.startsWith( "xmlns" ))
                        continue;

                    int j = qn.indexOf( ':' );

                    if (j >= 0)
                    {
                        _contentHandler.endPrefixMapping(
                            qn.substring( j + 1 ) );
                    }
                    else
                        _contentHandler.endPrefixMapping( "" );
                }
            }
            catch ( SAXException e )
            {
                throwSaxException( e );
            }
        }
                
        private String getPrefixedName ( QName name )
        {
            String ns = name.getNamespaceURI();
            String lp = name.getLocalPart();

            if (ns.length() == 0)
                return lp;

            String prefix = getUriMapping( ns );

            if (prefix.length() == 0)
                return lp;

            return prefix + ":" + lp;
        }

        private void emitElement ( QName name, String prefixedName )
        {
            try
            {
                _contentHandler.startElement(
                    name.getNamespaceURI(), name.getLocalPart(),
                    getPrefixedName( name ), _attributes );
            }
            catch ( SAXException e )
            {
                throwSaxException( e );
            }
        }

        protected void emitEnd ( Splay s, QName name )
        {
            if (name != null)
                emitEndElement( name );
        }

        protected void emitTextAfter ( Splay s, int p, int cch )
        {
            if (_text == null)
                emitCharacters( s, p, cch );
            else
                emitCharacters( _text );
        }

        protected void emitDocType(
            String doctypeName, String publicID, String systemID )
        {
            if (_lexicalhandler != null)
            {
                try
                {
                    _lexicalhandler.startDTD( doctypeName, publicID, systemID );
                    _lexicalhandler.endDTD();
                }
                catch ( SAXException e )
                {
                    throwSaxException( e );
                }
            }
        }
        
        protected void emitComment ( Splay s )
        {
            if (_lexicalhandler != null)
            {
                int cp = getRoot().getCp( s );
                int cch = s.getCchValue();

                try
                {
                    _lexicalhandler.comment(
                        getRoot()._text._buf,
                        getRoot()._text.unObscure( cp, cch ),
                        cch );
                }
                catch ( SAXException e )
                {
                    throwSaxException( e );
                }
            }
        }

        protected void emitProcinst ( Splay s )
        {
            try
            {
                _contentHandler.processingInstruction(
                    s.getLocal(), s.getText( getRoot() ) );
            }
            catch ( SAXException e )
            {
                throwSaxException( e );
            }
        }

        protected void emitTextFragment ( Splay s, int p, int cch )
        {
            emitContainerHelper( null, _fragment, null, null );
            
            if (_text != null)
            {
                if (_text.length() > 0)
                    emitCharacters( _text );
            }
            else if (s != null && cch > 0)
                emitCharacters( s, p, cch );

            emitEndElement( _fragment );
        }

        protected void emitXmlnsFragment ( Splay s )
        {
            emitContainerHelper( null, _fragment, null, null );
            emitEndElement( _fragment );
        }

        protected void emitAttrFragment ( Splay s )
        {
            emitContainerHelper( null, _fragment, s, _text );
            emitEndElement( _fragment );
        }

        protected void emitCommentFragment ( Splay s )
        {
            emitContainerHelper( null, _fragment, null, null );
            emitComment( s );
            emitEndElement( _fragment );
        }

        protected void emitProcinstFragment ( Splay s )
        {
            emitContainerHelper( null, _fragment, null, null );
            emitProcinst( s );
            emitEndElement( _fragment );
        }
        
        private AttributesImpl _attributes;

        private SAXException _saxException;
        
        private ContentHandler _contentHandler;
        private LexicalHandler _lexicalhandler;
    }

            
    //
    //
    //

    private static ThreadLocal _threadDocumentBuilderFactory =
        new ThreadLocal()
        {
            protected Object initialValue()
            {
                return new SoftReference(createDocumentBuilder());
            }
        };

    public static DocumentBuilder createDocumentBuilder()
    {
        try
        {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }
        catch ( ParserConfigurationException e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    public static DocumentBuilder getThreadLocalDocumentBuilder() {

        SoftReference softRef = (SoftReference)_threadDocumentBuilderFactory.get();
        DocumentBuilder documentBuilder = (DocumentBuilder) softRef.get();
        if (documentBuilder==null)
        {
            documentBuilder = createDocumentBuilder();
            _threadDocumentBuilderFactory.set(new SoftReference(documentBuilder));
        }
        return documentBuilder;
    }

    static final class DomSaver extends Saver
    {
        DomSaver ( Root r, Splay s, int p, boolean createDoc, XmlOptions options )
        {
            super( r, s, p, options );
            _createDoc = createDoc;
        }

        Node exportDom ( )
            throws Exception
        {
            // TODO - add an options which specifies a Document with which
            // to create the fragment

            _doc = getThreadLocalDocumentBuilder().newDocument();

            Node result;

            if (_createDoc)
            {
                result = _currentNode = _doc;
            }
            else
            {
                DocumentFragment frag = _doc.createDocumentFragment();
                result = _currentNode = frag;
            }

            while ( process() )
                ;

            return result;
        }

        protected void emitContainer ( Container c, QName name )
        {
            Root r = getRoot();

            if (c.isDoc())
            {
                if (hasMappings())
                {
                    throw new IllegalStateException(
                        "Namespace attribute not associated with an element" );
                }

                for ( Splay s = c.nextSplay() ; s.isAttr() ; s = s.nextSplay() )
                {
                    if (s.isNormalAttr())
                    {
                        throw new IllegalStateException(
                            "Attribute not associated with an element" );
                    }
                }

                String text = null;

                if (_text != null)
                {
                    if (_text.length() > 0)
                        text = text();
                }
                else if (c.getCch() > 0)
                    text = r._text.fetch( 0, c.getCch() );

                if (text != null && _currentNode != _doc)
                {
                    _currentNode.insertBefore(
                        _doc.createTextNode( text ), null );
                }
            }
            else
            {
                assert c.isBegin();

                String qname = c.getLocal();

                if (c.getUri().length() > 0)
                {
                    String prefix = getUriMapping( c.getUri() );

                    if (prefix.length() > 0)
                        qname = prefix + ":" + qname;
                }

                Element e = _doc.createElementNS( c.getUri(), qname );

                _currentNode.insertBefore( e, null );

                for ( iterateMappings() ; hasMapping() ; nextMapping() )
                {
                    String prefix = mappingPrefix();

                    if (prefix.length() == 0)
                        qname = "xmlns";
                    else
                        qname = "xmlns:" + prefix;

                    e.setAttributeNS( Splay._xmlnsUri, qname, mappingUri() );
                }

                for ( Iterator i = _attrs.keySet().iterator() ; i.hasNext() ; )
                {
                    Splay s = (Splay) i.next();
                    
                    qname = s.getLocal();

                    if (s.getUri().length() > 0)
                    {
                        String prefix = getUriMapping( s.getUri() );

                        if (prefix.length() > 0)
                            qname = prefix + ":" + qname;
                    }

                    String invalidAttrValue = (String) _attrs.get( s );

                    e.setAttributeNS(
                        s.getUri(), qname,
                        invalidAttrValue == null
                            ? s.getText( r )
                            : invalidAttrValue );
                }

                if (c.isLeaf())
                {
                    String text = null;

                    if (_text != null)
                    {
                        if (_text.length() > 0)
                            text = text();
                    }
                    else if (c.getCchValue() > 0)
                    {
                        text =
                            r._text.fetch(
                                c.getCpForPos( r, 1 ), c.getCchValue() );
                    }

                    if (text != null)
                        e.insertBefore( _doc.createTextNode( text ), null );
                }
                else
                {
                    _currentNode = e;
                }
            }
        }

        protected void emitEnd ( Splay s, QName name )
        {
            _currentNode = _currentNode.getParentNode();
        }

        protected void emitTextAfter ( Splay s, int p, int cch )
        {
            assert cch > 0;

            Root r = getRoot();

            String text = null;

            if (_text != null)
            {
                if (_text.length() > 0)
                    text = text();
            }
            else if (cch > 0)
                text = r._text.fetch( s.getCpForPos( r, p ), cch );

            if (_currentNode != _doc)
                _currentNode.insertBefore( _doc.createTextNode( text ), null );
        }

        protected void emitDocType(
            String doctypeName, String publicID, String systemID )
        {
        }
                
        protected void emitComment ( Splay s )
        {
            Root r = getRoot();

            _currentNode.insertBefore(
                _doc.createComment(
                    r._text.fetch(
                        s.getCpForPos( r, 0 ), s.getCchValue() ) ), null );
        }

        protected void emitProcinst ( Splay s )
        {
            Root r = getRoot();

            _currentNode.insertBefore(
                _doc.createProcessingInstruction(
                    s.getLocal(),
                    r._text.fetch(
                        s.getCpForPos( r, 0 ), s.getCchValue() ) ), null );
        }

        protected void emitXmlnsFragment ( Splay s )
        {
            throw new IllegalStateException(
                "Cannot create a node for a namespace attribute" );
        }

        protected void emitAttrFragment ( Splay s )
        {
            throw new IllegalStateException(
                "Cannot create a node for a attribute" );
        }

        protected void emitTextFragment ( Splay s, int p, int cch )
        {
            // BUGBUG - there could be namespaces pushed for this text, but not
            // sure how to represent them here....  I should really put the
            // fragment logic in the base saver and have the base saver
            // synthesize well formed output...
            
            if (s != null)
            {
                Root r = getRoot();

                _currentNode.insertBefore(
                    _doc.createTextNode(
                    _text != null
                        ? _text.toString()
                        : s != null
                            ? r._text.fetch( s.getCpForPos( r, p ), cch )
                                : "" ),
                    null );
            }
        }

        protected void emitCommentFragment ( Splay s )
        {
            Root r = getRoot();

            _currentNode.insertBefore(
                _doc.createComment(
                    r._text.fetch(
                        s.getCpForPos( r, 0 ), s.getCchValue() ) ), null );
        }

        protected void emitProcinstFragment ( Splay s )
        {
            Root r = getRoot();

            _currentNode.insertBefore(
                _doc.createProcessingInstruction(
                    s.getLocal(),
                    r._text.fetch(
                        s.getCpForPos( r, 0 ), s.getCchValue() ) ), null );
        }

        Document _doc;
        Node     _currentNode;
        boolean  _createDoc;
    }

    //
    //
    //

    protected StringBuffer  _text;
    protected StringBuffer  _sb;
    protected boolean       _skipContainerFinish;
    protected LinkedHashMap _attrs;
    
    private HashSet _attrNames;

    private final boolean _inner;
    private final Root    _root;
    private final Splay   _top;
    private final long    _version;

    protected boolean _wantDupAttrs;
    protected boolean _wantFragTest;
    protected boolean _needsFrag;
    protected QName   _fragment;
    protected QName   _docElem;
    
    protected QName _synthElem;

    protected boolean _saveNamespacesFirst;
    protected boolean _useDefaultNamespace;

    private boolean _prettyPrint;
    private int     _prettyIndent;
    private int     _prettyOffset;

    private Splay   _splay;
    private int     _pos;
    private boolean _preProcess;
    private boolean _postProcess;
    private boolean _postPop;
    private Splay   _endSplay;

    private ArrayList _namespaceStack;
    private int       _currentMapping;
    private HashMap   _uriMap;
    private HashMap   _prefixMap;
    private boolean   _firstPush;
    private String    _initialDefaultUri;
    
    private HashMap _preComputedNamespaces;
    private String  _filterProcinst;
    private Map     _suggestedPrefixes;

    protected static String  _newLine;

    static
    {
        _newLine = System.getProperty( "line.separator" );
        if (_newLine == null)
            _newLine = "\n";
    }
}

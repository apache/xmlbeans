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

package org.apache.xmlbeans.impl.newstore2;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlOptions;

import org.apache.xmlbeans.impl.common.QNameHelper;

import java.io.Writer;
import java.io.Reader;
import java.io.IOException;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ConcurrentModificationException;

abstract class Saver
{
    static final int NONE     = Cur.NONE;
    static final int ROOT     = Cur.ROOT;
    static final int ELEM     = Cur.ELEM;
    static final int ATTR     = Cur.ATTR;
    static final int COMMENT  = Cur.COMMENT;
    static final int PROCINST = Cur.PROCINST;
    static final int TEXT     = Cur.TEXT;

    protected abstract boolean emitContainer (
        SaveCur c, QName name, ArrayList attrNames, ArrayList attrValues );
    
    protected abstract void emitFinish    ( SaveCur c, QName name );
    protected abstract void emitText      ( SaveCur c );
    protected abstract void emitComment   ( SaveCur c );
    protected abstract void emitProcinst  ( SaveCur c );

    protected void syntheticNamespace ( String prefix, String uri, boolean considerDefault ) { }

    Saver ( Cur c, XmlOptions options )
    {
        options = XmlOptions.maskNull( options );

        _locale = c._locale;
        _version = _locale.version();

        _cur = createSaveCur( c, options  );

        _namespaceStack = new ArrayList();
        _uriMap = new HashMap();
        _prefixMap = new HashMap();

        _attrNames = new ArrayList();
        _attrValues = new ArrayList ();

        _newLine = System.getProperty( "line.separator" );

        if (_newLine == null)
            _newLine = "\n";

        // Stops the synthesis of this namspace and make for better
        // roundtripping 
        addMapping( "xml", Locale._xml1998Uri );

        if (options.hasOption( XmlOptions.SAVE_IMPLICIT_NAMESPACES ))
        {
            Map m = (Map) options.get( XmlOptions.SAVE_IMPLICIT_NAMESPACES );
            
            for ( Iterator i = m.keySet().iterator() ; i.hasNext() ; )
            {
                String prefix = (String) i.next();
                addMapping( prefix, (String) m.get( prefix ) );
            }
        }

        // If the default prefix has not been mapped, do so now
        
        if (getNamespaceForPrefix( "" ) == null)
        {
            _initialDefaultUri = new String( "" );
            addMapping( "", _initialDefaultUri );
        }
        
        if (options.hasOption( XmlOptions.SAVE_AGGRESSIVE_NAMESPACES ) &&
                !(this instanceof SynthNamespaceSaver))
        {
            SynthNamespaceSaver saver = new SynthNamespaceSaver( c, options );

            while ( saver.process() )
                ;

            if (!saver._synthNamespaces.isEmpty())
                _preComputedNamespaces = saver._synthNamespaces;
        }
        
        _useDefaultNamespace =
            options.hasOption( XmlOptions.SAVE_USE_DEFAULT_NAMESPACE );

        _saveNamespacesFirst = options.hasOption( XmlOptions.SAVE_NAMESPACES_FIRST );
        
        if (options.hasOption( XmlOptions.SAVE_SUGGESTED_PREFIXES ))
            _suggestedPrefixes = (Map) options.get( XmlOptions.SAVE_SUGGESTED_PREFIXES);

        if (options.hasOption( XmlOptions.SAVE_FILTER_PROCINST ))
        {
            _filterProcinst =
                (String) options.get( XmlOptions.SAVE_FILTER_PROCINST );
        }
    }

    private SaveCur createSaveCur ( Cur c, XmlOptions options )
    {
        QName synthName = (QName) options.get( XmlOptions.SAVE_SYNTHETIC_DOCUMENT_ELEMENT );

        QName fragName = synthName;

        if (fragName == null)
        {
            fragName = 
                options.hasOption( XmlOptions.SAVE_USE_OPEN_FRAGMENT )
                    ? Locale._openuriFragment
                    : Locale._xmlFragment;
        }

        boolean saveInner = 
            options.hasOption( XmlOptions.SAVE_INNER ) &&
                !options.hasOption( XmlOptions.SAVE_OUTER );

        Cur start = c.tempCur();
        Cur end   = c.tempCur();

        SaveCur cur = null;

        int k = c.kind();

        switch ( k )
        {
        case ROOT :
        {
            positionToInner( c, start, end );

            if (isFragment( start, end ))
            {
                positionToInner( c, start, end );
                cur = new FragSaveCur( start, end, fragName );
            }
            else if (synthName != null)
            {
                positionToInner( c, start, end );
                cur = new FragSaveCur( start, end, synthName );
            }
            else
                cur = new DocSaveCur( c );

            break;
        }
        
        case ELEM :
        {
            if (saveInner)
            {
                positionToInner( c, start, end );
                boolean isFrag = isFragment( start, end );
                cur = new FragSaveCur( start, end, isFragment( start, end ) ? fragName : synthName);
            }
            else if (synthName != null)
            {
                positionToInner( c, start, end );
                cur = new FragSaveCur( start, end, synthName );
            }
            else
            {
                start.moveToCur( c );
                end.moveToCur( c );
                end.toEnd();
                end.next();

                cur = new FragSaveCur( start, end, null );
            }

            break;
        }
        }

        if (cur == null)
        {
            assert k < 0 || k == ATTR || k == COMMENT || k == PROCINST || k == TEXT;

            if (k < 0)
            {
                start.moveToCur( c );
                end.moveToCur( c );
            }
            else if (k == TEXT)
            {
                start.moveToCur( c );
                end.moveToCur( c );
                end.next();
            }
            else if (saveInner)
            {
                start.moveToCur( c );
                start.next();
                
                end.moveToCur( c );
                end.toEnd();
            }
            else if (k == ATTR)
            {
                start.moveToCur( c );
                end.moveToCur( c );
            }
            else
            {
                start.moveToCur( c );
                end.moveToCur( c );
                end.toEnd();
                end.next();
            }

            cur = new FragSaveCur( start, end, fragName );
        }

        if (options.hasOption( XmlOptions.SAVE_PRETTY_PRINT ))
            cur = new PrettySaveCur( cur, options );

        start.release();
        end.release();

        return cur;
    }

    private void positionToInner ( Cur c, Cur start, Cur end )
    {
        assert c.isContainer();

        start.moveToCur( c );

        if (!start.toFirstAttr())
            start.next();

        end.moveToCur( c );
        end.toEnd();
    }

    private static boolean isFragment ( Cur start, Cur end )
    {
        assert !end.isAttr();

        int numDocElems = 0;
        
        while ( ! start.isSamePos( end ) )
        {
            int k = start.kind();

            if (k == ATTR)
                break;
            
            if (k == TEXT && !Locale.isWhiteSpace( start.getString( -1 )))
                break;

            if (k == ELEM && ++numDocElems > 1)
                break;

            // Move to next token

            assert k != ATTR;
            
            if (k == ELEM)
                start.toEnd();
            
            start.next();
        }
        
        return !start.isSamePos( end );
    }

    protected boolean saveNamespacesFirst ( )
    {
        return _saveNamespacesFirst;
    }

    private final void checkVersion ( )
    {
        if (_version != _locale.version())
            throw new ConcurrentModificationException( "Document changed during save" );
    }

    protected final boolean process ( )
    {
        assert _locale.entered();
        
        checkVersion();

        if (_postPop)
        {
            popMappings();
            _postPop = false;
        }

        if (_postProcess)
        {
            int k = _cur.kind();
            
            if (k == -ROOT)
                _done = true;
            else
            {
                switch ( k )
                {
                case ROOT :
                case ELEM :
                {
                    if (_skipContainer)
                    {
                        assert _cur.isElem();
                        _cur.toEnd();
                    }

                    _cur.next();
                    
                    break;
                }
                    
                case - ROOT :
                case - ELEM :
                case TEXT :
                    _cur.next();
                    break;
                    
                case COMMENT :
                case PROCINST :
                    _cur.toEnd();
                    _cur.next();
                    break;
                                               
                default : throw new RuntimeException( "Unexpected kind" );
                }
            }

            if (_postPop)
            {
                popMappings();
                _postPop = false;
            }
        }
        
        if (_done)
        {
            _cur.release();
            _cur = null;
            
            return false;
        }

        checkVersion();

        switch ( _cur.kind() )
        {
            case   ROOT : case   ELEM : { _skipContainer = processContainer(); break; }
            case - ROOT : case - ELEM : { processFinish(); _postPop = true;    break; }
            case TEXT                 : { emitText( _cur );                    break; }
            case COMMENT              : { emitComment( _cur );                 break; }
            case PROCINST             : { processProcinst();                   break; }

            default : throw new RuntimeException( "Unexpected kind" );
        }

        _postProcess = true;

        return true;
    }

    private final boolean processContainer ( )
    {
        assert _cur.isContainer();
        assert !_cur.isRoot() || _cur.getName() == null;

        QName name = _cur.getName();

        String nameUri = name == null ? null : name.getNamespaceURI();
        
        // TODO - check for doctype to save out here

        ;

        // Add a new entry to the frontier.  If this element has a name
        // which has no namespace, then we must make sure that pushing
        // the mappings causes the default namespace to be empty

        boolean ensureDefaultEmpty = name != null && nameUri.length() == 0;

        pushMappings( _cur, ensureDefaultEmpty );

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

        //
        //
        //

        _attrNames.clear();
        _attrValues.clear();

        _cur.push();

        attrs:
        for ( boolean A = _cur.toFirstAttr() ; A ; A = _cur.toNextAttr() )
        {
            if (_cur.isNormalAttr())
            {
                QName attrName = _cur.getName();

                _attrNames.add( attrName );
                               
                for ( int i = _attrNames.size() - 2 ; i >= 0 ; i-- )
                {
                    if (_attrNames.get( i ).equals( attrName ))
                    {
                        _attrNames.remove( _attrNames.size() - 1 );
                        continue attrs;
                    }
                }

                _attrValues.add( _cur.getAttrValue() );
                
                ensureMapping( attrName.getNamespaceURI(), null, false, true );
            }
        }
        
        _cur.pop();

        // If I am doing aggressive namespaces and we're emitting a
        // container which can contain content, add the namespaces
        // we've computed.  Basically, I'm making sure the pre-computed
        // namespaces are mapped on the first container which has a name.

        if (_preComputedNamespaces != null && name != null)
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

        return emitContainer( _cur, name, _attrNames, _attrValues );
    }

    private final void processFinish ( )
    {
        QName name = _cur.getName();
        
        emitFinish( _cur, name );
        
        _postPop = true;
    }
    
    private final void processProcinst ( )
    {
        if (_filterProcinst == null || !_cur.getName().getLocalPart().equals( _filterProcinst ))
            emitProcinst( _cur );
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

    private final void pushMappings ( SaveCur c, boolean ensureDefaultEmpty )
    {
        assert c.isContainer();
        
        _namespaceStack.add( null );

        c.push();

        namespaces:
        for ( boolean A = c.toFirstAttr() ; A ; A = c.toNextAttr() )
        {
            if (c.isXmlns())
            {
                String prefix = c.getXmlnsPrefix();
                String uri = c.getXmlnsUri();

                if (!ensureDefaultEmpty || prefix.length() > 0 || uri.length() == 0)
                    addNewFrameMapping( prefix, uri );
            }
        }

        c.pop();

        if (c.isRoot() && _cur._ancestorNamespaces != null)
        {
            for ( int i = 0 ; i < _cur._ancestorNamespaces.size() ; i += 2 )
            {
                String prefix = (String) _cur._ancestorNamespaces.get( i );
                String uri    = (String) _cur._ancestorNamespaces.get( i + 1 );
                
                if (!ensureDefaultEmpty || prefix.length() > 0 || uri.length() == 0)
                    addNewFrameMapping( prefix, uri );
            }
        }
        
        if (ensureDefaultEmpty)
        {
            String defaultUri = (String) _prefixMap.get( "" );

            // I map the default to "" at the very beginning
            assert defaultUri != null;

            if (defaultUri.length() > 0)
                addMapping( "", "" );
        }
    }

    private final void addNewFrameMapping ( String prefix, String uri )
    {
        // Make sure the prefix is not already mapped in this frame

        for ( iterateMappings() ; hasMapping() ; nextMapping() )
            if (mappingPrefix().equals( prefix ))
                return;;

        addMapping( prefix, uri );
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

                        if (renamePrefix == null || !renamePrefix.equals( prefix ))
                            break;
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
    
    private final String ensureMapping (
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
    
    protected final String getUriMapping ( String uri )
    {
        assert _uriMap.get( uri ) != null;
        return (String) _uriMap.get( uri );
    }

    private final boolean tryPrefix ( String prefix )
    {
        if (prefix == null || Locale.beginsWithXml( prefix ))
            return false;

        String existingUri = (String) _prefixMap.get( prefix );

        // If the prefix is currently mapped, then try another prefix.  A
        // special case is that of trying to map the default prefix ("").
        // Here, there always exists a default mapping.  If this is the
        // mapping we found, then remap it anyways. I use != to compare
        // strings because I want to test for the specific initial default
        // uri I added when I initialized the saver.

        if (existingUri != null && (prefix.length() > 0 || existingUri != _initialDefaultUri))
            return false;

        return true;
    }

    public final String getNamespaceForPrefix ( String prefix )
    {
        assert !prefix.equals( "xml" ) || _prefixMap.get( prefix ).equals( Locale._xml1998Uri );
        
        return (String) _prefixMap.get( prefix );
    }

    //
    //
    //
    
    static final class SynthNamespaceSaver extends Saver
    {
        LinkedHashMap _synthNamespaces = new LinkedHashMap();
        
        SynthNamespaceSaver ( Cur c, XmlOptions options )
        {
            super( c, options );
        }
        
        protected void syntheticNamespace (
            String prefix, String uri, boolean considerCreatingDefault )
        {
            _synthNamespaces.put( uri, considerCreatingDefault ? "useDefault" : null );
        }
        
        protected boolean emitContainer (
            SaveCur c, QName name, ArrayList attrNames, ArrayList attrValues ) { return false; }
        
        protected void emitFinish    ( SaveCur c, QName name ) { }
        protected void emitText      ( SaveCur c ) { }
        protected void emitComment   ( SaveCur c ) { }
        protected void emitProcinst  ( SaveCur c ) { }
    }

    //
    //
    //

    static final class TextSaver extends Saver
    {
        TextSaver ( Cur c, XmlOptions options, String encoding )
        {
            super( c, options );

            if (encoding != null)
            {
                XmlDocumentProperties props = Locale.getDocProps( c );
                
                String version = props == null ? null : props.getVersion();

                if (version == null)
                    version = "1.0";
                
                emit( "<?xml version=\"" );
                emit( version );
                emit( "\" encoding=\"" + encoding + "\"?>" + _newLine );
            }
        }
        
        protected boolean emitContainer (
            SaveCur c, QName name, ArrayList attrNames, ArrayList attrValues )
        {
            boolean skipElem = false;
            
            if (c.isElem())
            {
                emitContainerHelper( name, false, attrNames, attrValues );

                assert c.isContainer();

                if (!c.hasChildren() && !c.hasText())
                {
                    emit( '/' );
                    skipElem = true;
                }
                
                emit( '>' );
            }
            else
            {
                assert c.isRoot();

                if (name != null)
                    emitContainerHelper( name, true, attrNames, attrValues );
            }

            return skipElem;
        }
        
        protected void emitFinish ( SaveCur c, QName name )
        {
            if (name != null)
                emitFinishHelper( name );
        }
        
        protected void emitFinishHelper ( QName name )
        {
            assert name != null;
            
            emit( "</" );
            emitName( name );
            emit( '>' );
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

        private void emitNamespacesHelper ( )
        {
            for ( iterateMappings() ; hasMapping() ; nextMapping() )
            {
                emit( ' ' );
                emitXmlns( mappingPrefix(), mappingUri() );
            }
        }
                                     
        private void emitAttrHelper ( QName attrName, String attrValue )
        {
            emit( ' ' );
            emitName( attrName );
            emit( "=\"" );
            emit( attrValue );
            entitizeAttrValue();
            emit( '"' );
        }

        private void emitContainerHelper (
            QName name, boolean close, ArrayList attrNames, ArrayList attrValues )
        {
            assert name != null;

            emit( '<' );
            emitName( name );

            if (saveNamespacesFirst())
                emitNamespacesHelper();

            for ( int i = 0 ; i < attrNames.size() ; i++ )
                emitAttrHelper( (QName) attrNames.get( i ), (String) attrValues.get( i ) );

            if (!saveNamespacesFirst())
                emitNamespacesHelper();

            if (close)
                emit( '>' );
        }
        
        protected void emitText ( SaveCur c )
        {
            assert c.isText();
            
            emit( c );

            entitizeContent();
        }
        
        protected void emitComment ( SaveCur c )
        {
            assert c.isComment();

            emit( "<!--" );
            emitValue( c );
            entitizeComment();
            emit( "-->" );
        }

        protected void emitProcinst ( SaveCur c )
        {
            assert c.isProcinst();
            
            emit( "<?" );
            
            // TODO - encoding issues here?
            emit( c.getName().getLocalPart() );

            if (c.hasText())
            {
                emit( " " );
                emitValue( c );
                entitizeProcinst();
            }
            
            emit( "?>" );
        }

        //
        //
        //
        
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

        private void emit ( char ch )
        {
            preEmit( 1 );

            _buf[ _in ] = ch;

            _in = (_in + 1) % _buf.length;
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
        
        private void emit ( SaveCur c )
        {
            if (c.isText())
            {
                Object src = c.getChars();
                int cch = c._cchSrc;
                
                if (preEmit( cch ))
                    return;

                int chunk;

                if (_in <= _out || cch < (chunk = _buf.length - _in))
                {
                    CharUtil.getChars( _buf, _in, src, c._offSrc, cch );
                    _in += cch;
                }
                else
                {
                    CharUtil.getChars( _buf, _in, src, c._offSrc, chunk );
                    CharUtil.getChars( _buf, 0, src, c._offSrc + chunk, cch - chunk );
                    _in = (_in + cch) % _buf.length;
                }
            }
            else
                preEmit( 0 );
        }
        
        private void emitValue ( SaveCur c )
        {
            assert !c.isText() && !c.isAttr() && c.kind() > 0 && !c.hasChildren();

            c.push();
            c.next();

            emit( c );

            c.pop();
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
                return i + 1;
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
                assert i < _in;
                System.arraycopy( _buf, i, _buf, i + dCch, _in - i );
                _in += dCch;
            }

            replacement.getChars( 0, dCch + 1, _buf, i );

            _free -= dCch;
            
            assert _free >= 0;

            return i + dCch + 1;
        }
        //
        //
        //

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

        int getAvailable ( )
        {
            return _buf == null ? 0 : _buf.length - _free;
        }

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

        //
        //
        //

        private static final int _initialBufSize = 4096;

        private int _lastEmitIn;
        private int _lastEmitCch;

        private int    _free;
        private int    _in;
        private int    _out;
        private char[] _buf;
    }
    
    static final class TextReader extends Reader
    {
        TextReader ( Cur c, XmlOptions options )
        {
            _textSaver = new TextSaver( c, options, null );
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
    
    //
    //
    //

    private static abstract class SaveCur
    {
        boolean isRoot       ( ) { return kind() == ROOT;     }
        boolean isElem       ( ) { return kind() == ELEM;     }
        boolean isAttr       ( ) { return kind() == ATTR;     }
        boolean isText       ( ) { return kind() == TEXT;     }
        boolean isComment    ( ) { return kind() == COMMENT;  }
        boolean isProcinst   ( ) { return kind() == PROCINST; }
        boolean isFinish     ( ) { return Cur.kindIsFinish( kind() ); }
        boolean isContainer  ( ) { return Cur.kindIsContainer( kind() ); }
        boolean isNormalAttr ( ) { return kind() == ATTR && !isXmlns(); }

        abstract void release ( );
        
        abstract int kind ( );
        
        abstract QName  getName ( );
        abstract String getXmlnsPrefix ( );
        abstract String getXmlnsUri ( );
        
        abstract boolean isXmlns ( );
        
        abstract boolean hasChildren  ( );
        abstract boolean hasText      ( );
        
        abstract boolean toFirstAttr ( );
        abstract boolean toNextAttr ( );
        abstract String  getAttrValue ( );
        
        abstract boolean next  ( );
        abstract void    toEnd ( );

        abstract void push ( );
        abstract void pop ( );

        abstract Object getChars ( );

        ArrayList _ancestorNamespaces;

        int _offSrc;
        int _cchSrc;
    }
    
    // TODO - saving a fragment need to take namesapces from root and
    // reflect them on the document element
    
    private static final class DocSaveCur extends SaveCur
    {
        DocSaveCur ( Cur c )
        {
            assert c.isRoot();
            _cur = c.weakCur( this );
        }

        void release ( )
        {
            _cur.release();
            _cur = null;
        }
        
        int kind ( ) { return _cur.kind(); }
        
        QName  getName        ( ) { return _cur.getName(); }
        String getXmlnsPrefix ( ) { return _cur.getXmlnsPrefix(); }
        String getXmlnsUri    ( ) { return _cur.getXmlnsUri(); }
        
        boolean isXmlns       ( ) { return _cur.isXmlns();     }
        
        boolean hasChildren   ( ) { return _cur.hasChildren(); }
        boolean hasText       ( ) { return _cur.hasText();     }
        
        boolean toFirstAttr   ( ) { return _cur.toFirstAttr(); }
        boolean toNextAttr    ( ) { return _cur.toNextAttr();  }
        String  getAttrValue  ( ) { assert _cur.isAttr(); return _cur.getValueString(); }
        
        void    toEnd         ( ) { _cur.toEnd();              }
        boolean next          ( ) { return _cur.next();        }
        
        void push ( )         { _cur.push(); }
        void pop  ( )         { _cur.pop(); }

        
        Object getChars ( )
        {
            Object o = _cur.getChars( -1 );
            
            _offSrc = _cur._offSrc;
            _cchSrc = _cur._cchSrc;

            return o;
        }
        
        private Cur _cur;
    }

    private static final class FragSaveCur extends SaveCur
    {
        FragSaveCur ( Cur start, Cur end, QName synthElem )
        {
            _saveAttr = start.isAttr() && start.isSamePos( end );

            _cur = start.weakCur( this );
            _end = end.weakCur( this );

            _elem = synthElem;
            
            _state = ROOT_START;
            
            _stateStack = new int [ 8 ];

            start.push();
            computeAncestorNamespaces( start );
            start.pop();
        }

        private void computeAncestorNamespaces ( Cur c )
        {
            _ancestorNamespaces = new ArrayList();
            
            while ( c.toParentRaw() )
            {
                if (c.toFirstAttr())
                {
                    do
                    {
                        if (c.isXmlns())
                        {
                            _ancestorNamespaces.add( c.getXmlnsPrefix() );
                            _ancestorNamespaces.add( c.getXmlnsUri() );
                        }
                    }
                    while ( c.toNextAttr() );
                    
                    c.toParent();
                }
            }
        }
        
        //
        //
        //
        
        void release ( )
        {
            _cur.release();
            _cur = null;

            _end.release();
            _end = null;
        }

        int kind ( )
        {
            switch ( _state )
            {
            case ROOT_START : return  ROOT;
            case ELEM_START : return  ELEM;
            case ELEM_END   : return -ELEM;
            case ROOT_END   : return -ROOT;
            }
            
            assert _state == CUR;
            
            return _cur.kind();
        }
        
        QName getName ( )
        {
            switch ( _state )
            {
            case ROOT_START :
            case ROOT_END   : return null;
            case ELEM_START : 
            case ELEM_END   : return _elem;
            }
            
            assert _state == CUR;

            return _cur.getName();
        }
        
        String getXmlnsPrefix ( )
        {
            assert _state == CUR && _cur.isAttr();
            return _cur.getXmlnsPrefix();
        }
        
        String getXmlnsUri ( )
        {
            assert _state == CUR && _cur.isAttr();
            return _cur.getXmlnsUri();
        }
        
        boolean isXmlns ( )
        {
            assert _state == CUR && _cur.isAttr();
            return _cur.isXmlns();
        }
        
        boolean hasChildren ( )
        {
            boolean hasChildren = false;
            
            if (isContainer())
            {
                push();
                next();

                if (!isText() && !isFinish())
                    hasChildren = true;
                
                pop();
            }

            return hasChildren;
        }
        
        boolean hasText ( )
        {
            boolean hasText = false;
            
            if (isContainer())
            {
                push();
                next();

                if (isText())
                    hasText = true;
                
                pop();
            }

            return hasText;
        }
        
        Object getChars ( )
        {
            assert _state == CUR && _cur.isText();

            Object src = _cur.getChars( -1 );
            
            _offSrc = _cur._offSrc;
            _cchSrc = _cur._cchSrc;

            return src;
        }
        
        boolean next ( )
        {
            switch ( _state )
            {
            case ROOT_START :
            {
                _state = _elem == null ? CUR : ELEM_START;
                return true;
            }

            case ELEM_START :
            {
                if (_saveAttr)
                    _state = ELEM_END;
                else
                {
                    if (_cur.isAttr())
                    {
                        _cur.toParent();
                        _cur.next();
                    }

                    if (_cur.isSamePos( _end ))
                        _state = ELEM_END;
                    else
                        _state = CUR;
                }

                return true;
            }

            case CUR :
            {
                assert !_cur.isAttr();

                _cur.next();

                if (_cur.isSamePos( _end ))
                    _state = _elem == null ? ROOT_END : ELEM_END;

                return true;
            }
            
            case ELEM_END :
            {
                _state = ROOT_END;
                return true;
            }
            }

            assert _state == ROOT_END;
            
            return false;
        }
        
        void toEnd ( )
        {
            switch ( _state )
            {
            case ROOT_START : _state = ROOT_END; return;
            case ELEM_START : _state = ELEM_END; return;
            case ROOT_END   :
            case ELEM_END   : return;
            }

            assert _state == CUR && !_cur.isAttr() && !_cur.isText();

            _cur.toEnd();
        }
        
        boolean toFirstAttr ( )
        {
            switch ( _state )
            {
            case ROOT_END   :
            case ELEM_END   : 
            case ROOT_START : return false;
            case CUR        : return _cur.toFirstAttr();
            }
            
            assert _state == ELEM_START;

            if (!_cur.isAttr())
                return false;
            
            _state = CUR;
                
            return true;
        }
        
        boolean toNextAttr ( )
        {
            assert _state == CUR;
            return !_saveAttr && _cur.toNextAttr();
        }
        
        String getAttrValue ( )
        {
            assert _state == CUR && _cur.isAttr();
            return _cur.getValueString();
        }

        void push ( )
        {
            if (_stateStackSize == _stateStack.length)
            {
                int[] newStateStack = new int [ _stateStackSize * 2 ];
                System.arraycopy( _stateStack, 0, newStateStack, 0, _stateStackSize );
                _stateStack = newStateStack;
            }

            _stateStack [ _stateStackSize++ ] = _state;
            _cur.push();
        }

        void pop ()
        {
            _cur.pop();
            _state = _stateStack [ --_stateStackSize ];
        }
        
        //
        //
        //

        private Cur _cur;
        private Cur _end;

        private QName _elem;

        private boolean _saveAttr;
        
        private static final int ROOT_START = 1;
        private static final int ELEM_START = 2;
        private static final int ROOT_END   = 3;
        private static final int ELEM_END   = 4;
        private static final int CUR        = 5;
        
        private int _state;

        private int[] _stateStack;
        private int   _stateStackSize;
    }
    
    private static final class PrettySaveCur extends SaveCur
    {
        PrettySaveCur ( SaveCur c, XmlOptions options )
        {
            _sb = new StringBuffer();
            _stack = new ArrayList();
            
            _cur = c;

            assert options != null;

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

        void release ( ) { _cur.release(); }
        
        int kind ( ) { return _txt == null ? _cur.kind() : TEXT; }
        
        QName  getName        ( ) { assert _txt == null; return _cur.getName(); }
        String getXmlnsPrefix ( ) { assert _txt == null; return _cur.getXmlnsPrefix(); }
        String getXmlnsUri    ( ) { assert _txt == null; return _cur.getXmlnsUri(); }
        
        boolean isXmlns       ( ) { return _txt == null ? _cur.isXmlns()      : false; }
        
        boolean hasChildren   ( ) { return _txt == null ? _cur.hasChildren() : false; }
        boolean hasText       ( ) { return _txt == null ? _cur.hasText()     : false; }
        
        boolean toFirstAttr   ( ) { assert _txt == null; return _cur.toFirstAttr(); }
        boolean toNextAttr    ( ) { assert _txt == null; return _cur.toNextAttr(); }
        String  getAttrValue  ( ) { throw new RuntimeException( "Not impl" ); }
        
        void toEnd ( ) { assert _txt == null; _cur.toEnd(); }
        
        boolean next ( )
        {
            if (_txt != null)
            {
                assert _txt.length() > 0;
                assert !_cur.isText();
                _txt = null;
                
                return true;
            }

            int kind = kind();

            if (!_cur.next())
                return false;

            _sb.delete( 0, _sb.length() );

            if (kind == ROOT || kind == ELEM)
            {
                if (_cur.isText())
                {
                    Object src = _cur.getChars();
                    CharUtil.getString( _sb, _cur._offSrc, _cur._cchSrc );
                    _cur.next();
                }

                trim( _sb );
                
                if (_sb.length() > 0)
                    _txt = _sb.toString();

            }
            else if (kind < 0 && kind != -ATTR)
            {
            }

            return true;
        }
        
        void push ( ) { _cur.push(); _stack.add( _txt ); }
        
        void pop ( ) { _cur.pop(); _txt = (String) _stack.remove( _stack.size() - 1 ); }
        
        Object getChars ( )
        {
            if (_txt != null)
            {
                _offSrc = 0;
                _cchSrc = _txt.length();
                return _txt;
            }
            
            Object o = _cur.getChars();
            
            _offSrc = _cur._offSrc;
            _cchSrc = _cur._cchSrc;

            return o;
        }
        
        final static void trim ( StringBuffer sb )
        {
            int i;

            for ( i = 0 ; i < sb.length() ; i++ )
                if (!Locale.isWhiteSpace( sb.charAt( i ) ))
                    break;

            sb.delete( 0, i );

            for ( i = sb.length() ; i > 0 ; i-- )
                if (!Locale.isWhiteSpace( sb.charAt( i - 1 ) ))
                    break;

            sb.delete( i, sb.length() );
        }

        private SaveCur _cur;

        private int _prettyIndent;
        private int _prettyOffset;

        private String       _txt;
        private StringBuffer _sb;
        
        private ArrayList    _stack;
    }
    
    //
    //
    //

    private final Locale _locale;
    private final long   _version;
    
    private SaveCur _cur;

    private boolean _postProcess;
    private boolean _postPop;
    private boolean _done;
    private boolean _skipContainer;

    private Map     _suggestedPrefixes;
    private boolean _useDefaultNamespace;
    private HashMap _preComputedNamespaces;
    private boolean _saveNamespacesFirst;
    private String  _filterProcinst;

    private ArrayList _attrNames;
    private ArrayList _attrValues;

    private ArrayList _namespaceStack;
    private int       _currentMapping;
    private HashMap   _uriMap;
    private HashMap   _prefixMap;
    private String    _initialDefaultUri;

    protected String _newLine;
}
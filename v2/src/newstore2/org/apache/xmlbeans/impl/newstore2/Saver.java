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

import java.util.ConcurrentModificationException;

import org.apache.xmlbeans.XmlOptions;

import java.io.Writer;
import java.io.Reader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;

abstract class Saver
{
    static final int NONE     = Cur.NONE;
    static final int ROOT     = Cur.ROOT;
    static final int ELEM     = Cur.ELEM;
    static final int ATTR     = Cur.ATTR;
    static final int COMMENT  = Cur.COMMENT;
    static final int PROCINST = Cur.PROCINST;
    static final int TEXT     = Cur.TEXT;

    protected abstract void emitContainer ( Cur c, QName name );
    protected abstract void emitFinish    ( Cur c, QName name );

    Saver ( Cur c, XmlOptions options )
    {
        _locale = c._locale;
        _version = _locale.version();

        _cur = c.weakCur( this );
        _preProcess = true;

        _namespaceStack = new ArrayList();
        _uriMap = new HashMap();
        _prefixMap = new HashMap();
        _firstPush = true;
        
        // Stops the synthesis of this namspace and make for better
        // roundtripping 
        addMapping( "xml", Locale._xml1998Uri );


        // TODO - check for implicit namespaces here

        
        // If the default prefix has not been mapped, do so now
        
        if (getNamespaceForPrefix( "" ) == null)
        {
            _initialDefaultUri = new String( "" );
            addMapping( "", _initialDefaultUri );
        }
        
        // TODO - establish _synthName
    }

    protected final void checkVersion ( )
    {
        if (_version != _locale.version())
            throw new ConcurrentModificationException( "Document changed during save" );
    }

    protected final boolean process ( )
    {
        assert _locale.entered();
        
        checkVersion();

        if (_preProcess)
        {
            assert _cur != null;

            _preProcess = false;

            _done = true;

            if (!_cur.isContainer())
                throw new RuntimeException( "Not implemented" );

            assert _cur.isContainer();

            _done = false;

            _top = _cur.weakCur( this );
        }

        if (_postPop)
        {
            popMappings();
            _postPop = false;
        }

        if (_postProcess)
        {
            if (_cur.isAtEndOf( _top ))
                _done = true;
            else
            {
                switch ( _cur.kind() )
                {
                case ROOT    : case ELEM     : _cur.nextNonAttr();        break;
                case - ROOT  : case - ELEM   : _cur.next();               break;
                case COMMENT : case PROCINST : _cur.toEnd(); _cur.next(); break;
                                               
                default : throw new RuntimeException( "Unexpected kind" );
                }

                if (_skipContainerFinish)
                {
                    assert _cur.isFinish();

                    if (_cur.isAtEndOf( _top ))
                        _done = true;
                    else
                        _cur.next();

                    _postPop = true;
                }

                if (_cur.isText())
                {
                    throw new RuntimeException( "Not implemented" );
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
            _cur.release();      _cur = null;
            Cur.release( _top ); _top = null;
            
            return false;
        }

        checkVersion();

        _skipContainerFinish = false;

        switch ( _cur.kind() )
        {
            case  ROOT   : case  ELEM : { processContainer();               break; }
            case - ROOT : case - ELEM : { processFinish(); _postPop = true; break; }
            case COMMENT              : { throw new RuntimeException( "Not implemented" ); }
            case PROCINST             : { throw new RuntimeException( "Not implemented" ); }

            default : throw new RuntimeException( "Unexpected kind" );
        }

        _postProcess = true;

        return true;
    }

    private final void processContainer ( )
    {
        assert _cur.isContainer();
        assert !_cur.isRoot() || _cur.getName() == null;
        
        QName name = _synthElem != null && _cur.isSamePos( _top ) ? _synthElem : _cur.getName();

        String nameUri = name == null ? null : name.getNamespaceURI();
        
        // TODO - check for doctype to save out here

        ;

        // Add a new entry to the frontier.  If this element has a name
        // which has no namespace, then we must make sure that pushing
        // the mappings causes the default namespace to be empty

        boolean ensureDefaultEmpty = name != null && nameUri.length() == 0;

        pushMappings( _cur, ensureDefaultEmpty );

        // todo - do _wantFragTest thingy here ...
    }

    private final void processFinish ( )
    {
        QName name = _synthElem != null && _cur.isAtEndOf( _top ) ? _synthElem : _cur.getName();
        
        // todo - do _wantFragTest thingy here ...
        
        emitFinish( _cur, name );
        
        _postPop = true;
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

    private final void pushMappings ( Cur container, boolean ensureDefaultEmpty )
    {
        assert container.isContainer();
        
        _namespaceStack.add( null );

        Cur c = container.tempCur();
        
        for ( boolean C = true ; C ; C = c.toParentRaw() )
        {
            Cur a = c.tempCur();

            namespaces:
            for ( boolean A = a.toFirstAttr() ; A ; A = a.toNextAttr() )
            {
                if (a.isXmlns())
                {
                    String prefix = a.getXmlnsPrefix();
                    String uri = a.getValueString();
                    
                    if (ensureDefaultEmpty && prefix.length() == 0 && uri.length() > 0)
                        continue;
                    
                    // Make sure the prefix is not already mapped in this frame

                    for ( iterateMappings() ; hasMapping() ; nextMapping() )
                        if (mappingPrefix().equals( prefix ))
                            continue namespaces;

                    addMapping( prefix, uri );
                }
            }

            a.release();

            // Push all ancestors the first time
            
            if (!_firstPush)
                break;
        }

        c.release();

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
    
    public final String getNamespaceForPrefix ( String prefix )
    {
        assert !prefix.equals( "xml" ) || _prefixMap.get( prefix ).equals( Locale._xml1998Uri );
        
        return (String) _prefixMap.get( prefix );
    }

    //
    //
    //

    static final class TextSaver extends Saver
    {
        TextSaver ( Cur c, XmlOptions options, String encoding )
        {
            super( c, options );

            // TODO - do something with encoding here
        }
        
        protected void emitContainer ( Cur c, QName name )
        {
            throw new RuntimeException( "Not implemented" );
        }
        
        protected void emitFinish ( Cur c, QName name )
        {
            throw new RuntimeException( "Not implemented" );
        }

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

    private final Locale _locale;
    private final long   _version;
    
    private Cur _cur;
    private Cur _top;

    private boolean _preProcess;
    private boolean _postProcess;
    private boolean _postPop;
    private boolean _done;
    private boolean _skipContainerFinish;

    private QName _synthElem;

    private ArrayList _namespaceStack;
    private int       _currentMapping;
    private boolean   _firstPush;
    private HashMap   _uriMap;
    private HashMap   _prefixMap;
    private String    _initialDefaultUri;
}
package org.apache.xmlbeans.impl.newstore2;

import javax.xml.namespace.QName;

import java.util.ConcurrentModificationException;

import org.apache.xmlbeans.XmlOptions;

import java.io.Writer;
import java.io.Reader;
import java.io.IOException;

import java.util.ArrayList;

class Saver
{
    Saver ( Cur c, XmlOptions options )
    {
        _locale = c._locale;
        _version = _locale.version();

        _cur = c.weakCur( this );

        _namespaceStack = new ArrayList();
        
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

            _last = _cur.weakCur( this );
            _last.toEnd();

            _top = _cur.weakCur( this );
        }

        if (_postProcess)
        {
            if (_cur.isSamePosition( _last ))
                _done = true;
            else
            {
                
            }
        }
        
        if (_done)
        {
            _cur.release();
            _cur = null;

            if (_last != null) { _last.release(); _last = null; }
            if (_top  != null) { _top.release();  _top = null;  }
            
            return false;
        }

        checkVersion();

        int k = _cur.kind();

        assert
            k == Cur.ROOT || k == Cur.ELEM
                || k == Cur.COMMENT || k == Cur.PROCINST ||
                    k == - Cur.ROOT || k == - Cur.ELEM;
                
        switch ( k )
        {
            case Cur.ROOT :
            case Cur.ELEM :
            {
                processContainer();
                break;
            }
            
            case - Cur.ROOT :
            case - Cur.ELEM :
            {
                processFinish();
                break;
            }
            
            case Cur.COMMENT :
            {
                throw new RuntimeException( "Not implemented" );
            }
            
            case Cur.PROCINST :
            {
                throw new RuntimeException( "Not implemented" );
            }
        }

        _postProcess = true;

        return true;
    }

    private final void processContainer ( )
    {
        assert _cur.isContainer();
        assert !_cur.isRoot() || _cur.getName() == null;
        
        QName name =
            _synthElem != null && _cur.isSamePosition( _top ) ? _synthElem : _cur.getName();

        String nameUri = name == null ? null : name.getNamespaceURI();
        
        // TODO - check for doctype to save out here

        ;

        // Add a new entry to the frontier.  If this element has a name
        // which has no namespace, then we must make sure that pushing
        // the mappings causes the default namespace to be empty

        boolean ensureDefaultEmpty = name != null && nameUri.length() == 0;

        pushMappings( _cur, ensureDefaultEmpty );
    }

    private final void processFinish ( )
    {
        throw new RuntimeException( "Not implemented" );
    }

    private final void pushMappings ( Cur container, boolean ensureDefaultEmpty )
    {
        assert container.isContainer();
        
        _namespaceStack.add( null );

        Cur c = container.tempCur();
        
        for ( boolean cont = true ; cont ; cont = c.toParentRaw() )
        {
        }

        c.release();


        

//        for ( ; c != null ; c = c.getContainer() )
//        {
//            namespaces:
//            for ( Splay s = c.nextSplay() ; s.isAttr() ; s = s.nextSplay() )
//            {
//                if (s.isXmlns())
//                {
//                    Xmlns x = (Xmlns) s;
//                    String prefix = x.getLocal();
//                    String uri = x.getUri();
//
//                    if (ensureDefaultEmpty &&
//                            prefix.length() == 0 && uri.length() > 0)
//                    {
//                        continue;
//                    }
//
//                    // Make sure the prefix is not already mapped in
//                    // this frame
//
//                    for ( iterateMappings() ; hasMapping() ; nextMapping() )
//                        if (mappingPrefix().equals( prefix ))
//                            continue namespaces;
//
//                    addMapping( prefix, uri );
//                }
//            }
//
//            // Push all ancestors the first time
//            
//            if (!_firstPush)
//                break;
//        }
//
//        if (ensureDefaultEmpty)
//        {
//            String defaultUri = (String) _prefixMap.get( "" );
//
//            // I map the default to "" at the very beginning
//            assert defaultUri != null;
//
//            if (defaultUri.length() > 0)
//                addMapping( "", "" );
//        }
//
//        _firstPush = false;
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
    private Cur _last;

    private boolean _preProcess;
    private boolean _postProcess;
    private boolean _done;

    private QName _synthElem;
    private Cur   _top;

    private ArrayList _namespaceStack;
}
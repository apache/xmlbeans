package org.apache.xmlbeans.impl.newstore2;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

import org.apache.xmlbeans.impl.newstore2.DomImpl.Dom;
import org.apache.xmlbeans.impl.newstore2.DomImpl.TextNode;
import org.apache.xmlbeans.impl.newstore2.DomImpl.CdataNode;
import org.apache.xmlbeans.impl.newstore2.DomImpl.SaajTextNode;
import org.apache.xmlbeans.impl.newstore2.DomImpl.SaajCdataNode;

import javax.xml.namespace.QName;

final class Locale implements DOMImplementation
{
    Locale ( )
    {
        _noSync = true;
        _tempFrames = new Cur [ _numTempFramesLeft = 8 ];
        _charUtil = CharUtil.getThreadLocalCharUtil();
    }

    public long version ( )
    {
        return _versionAll;
    }

    public QName makeQName ( String uri, String localPart )
    {
        assert localPart != null && localPart.length() > 0;
        // TODO - make sure name is a well formed name?

        return new QName( uri, localPart );
    }

    public QName makeQName ( String uri, String local, String prefix )
    {
        return new QName( uri, local, prefix );
    }

    QName makeQualifiedQName ( String uri, String qname )
    {
        assert qname != null && qname.length() > 0;

        int i = qname.indexOf( ':' );

        return i < 0
            ? new QName( uri, qname )
            : new QName( uri, qname.substring( i + 1 ), qname.substring( 0, i ) );
    }

    Cur tempCur ( )
    {
        Cur c = getCur( Cur.TEMP );

        if (c._tempFrame < 0)
        {
            assert _numTempFramesLeft < _tempFrames.length;
                
            int frame = _tempFrames.length - _numTempFramesLeft - 1;

            assert frame >= 0 && frame < _tempFrames.length;

            c._nextTemp = _tempFrames[ frame ];
            _tempFrames[ frame ] = c;
        
            c._tempFrame = frame;
        }
        
        return c;
    }

    private Cur getCur ( int curKind )
    {
        assert curKind == Cur.TEMP || curKind == Cur.PERM || curKind == Cur.WEAK;
        assert _curPool == null || _curPoolCount > 0;
        
        Cur c = _curPool;
        
        if (c == null)
        {
            c = new Cur( this );
            c._state = Cur.POOLED;
            c._tempFrame = -1;
            c._pos = -1;
        }
        else
        {
            _curPool = c.listRemove( _curPool );
            _curPoolCount--;
        }

        assert c._prev == null && c._next == null;
        assert c._xobj == null;
        assert c._pos == -1;
        assert curKind == Cur.TEMP;

        c._curKind = curKind;

        c._state = Cur.UNEMBEDDED;
        
        _unembedded = c.listInsert( _unembedded );

        return c;
    }

    public TextNode createTextNode ( )
    {
        return _saaj == null ? new TextNode( this ) : new SaajTextNode( this );
    }

    public CdataNode createCdataNode ( )
    {
        return _saaj == null ? new CdataNode( this ) : new SaajCdataNode( this );
    }

    void enter ( )
    {
        assert _numTempFramesLeft >= 0;
        
        if (--_numTempFramesLeft <= 0)
        {
            Cur[] newTempFrames = new Cur [ (_numTempFramesLeft = _tempFrames.length) * 2 ];
            System.arraycopy( _tempFrames, 0, newTempFrames, 0, _tempFrames.length );
            _tempFrames = newTempFrames;
        }

        
        
//        if (++_numTempFrames >= _tempFrames.length)
//        {
//            Cursor[] newTempFrames = new Cursor [ _tempFrames.length * 2 ];
//            System.arraycopy( _tempFrames, 0, newTempFrames, 0, _tempFrames.length );
//            _tempFrames = newTempFrames;
//        }
//
//        if (++_entryCount > 1000)
//        {
//            _entryCount = 0;
//
//            if (_refQueue != null)
//            {
//                for ( ; ; )
//                {
//                    Ref ref = (Ref) _refQueue.poll();
//
//                    if (ref == null)
//                        break;
//
//                    ref._cur.release();
//                }
//            }
//        }
    }
    
    void exit ( )
    {
        assert _numTempFramesLeft >= 0;

        int frame = _tempFrames.length - ++_numTempFramesLeft;

        Cur c = _tempFrames [ frame ];

        _tempFrames [ frame ] = null;
        
        while ( c != null )
        {
            assert c._tempFrame == frame;

            Cur next = c._nextTemp;

            c._nextTemp = null;
            c._tempFrame = -1;

            c.release();

            c = next;
        }
        
        
        
//        assert _numTempFrames > 0;
//
//        _numTempFrames--;
//
//        Cur c = _tempFrames[ _numTempFrames ];
//        
//        _tempFrames[ _numTempFrames ] = null;
//
//        while ( c != null )
//        {
//            assert c._tempFrame == _numTempFrames;
//
//            Cur next = c._nextTemp;
//
//            c._nextTemp = null;
//            c._tempFrame = -1;
//
//            c.release();
//
//            c = next;
//        }
    }
    
    //
    //
    //

    boolean noSync ( )
    {
        return _noSync;
    }
    
    //
    // DOMImplementation methods
    //

    public Document createDocument ( String uri, String qname, DocumentType doctype )
    {
        return DomImpl._domImplementation_createDocument( this, uri, qname, doctype );
    }

    public DocumentType createDocumentType ( String qname, String publicId, String systemId )
    {
        throw new RuntimeException( "Not implemented" );
//        return DomImpl._domImplementation_createDocumentType( this, qname, publicId, systemId );
    }

    public boolean hasFeature ( String feature, String version )
    {
        throw new RuntimeException( "Not implemented" );
//        return DomImpl._domImplementation_hasFeature( this, feature, version );
    }

    //
    //
    //

    private boolean _noSync;

    private int   _numTempFramesLeft;
    private Cur[] _tempFrames;

    Cur _curPool;
    int _curPoolCount;

    Cur _unembedded;
    
    long _versionAll;
    long _versionSansText;
    
    CharUtil _charUtil;
    
    static Saaj _saaj;
    
    Dom _ownerDoc;
}
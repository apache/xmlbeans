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

package org.apache.xmlbeans.impl.newstore;

import org.apache.xmlbeans.impl.newstore.pub.store.Gcur;

public final class Fcur // extends Gcur
{
}



// package store.xptr;
// 
// import store.DomImpl.Dom;
// import store.DomImpl.CharNode;
// import store.DomImpl;
// 
// import org.w3c.dom.Node;
// 
// import javax.xml.namespace.QName;
// 
// import java.util.ArrayList;
// import java.util.HashMap;
// 
// import java.io.File;
// import java.io.IOException;
// import java.io.RandomAccessFile;
// 
// import java.nio.ByteBuffer;
// import java.nio.channels.FileChannel;
// 
// import org.w3c.dom.Node;
// import org.w3c.dom.Attr;
// import org.w3c.dom.CDATASection;
// import org.w3c.dom.Comment;
// import org.w3c.dom.ProcessingInstruction;
// import org.w3c.dom.DocumentFragment;
// import org.w3c.dom.Document;
// import org.w3c.dom.Element;
// import org.w3c.dom.EntityReference;
// import org.w3c.dom.EntityReference;
// import org.w3c.dom.Text;
// import org.w3c.dom.DocumentType;
// import org.w3c.dom.NodeList;
// import org.w3c.dom.DOMImplementation;
// import org.w3c.dom.NamedNodeMap;
// 
// public class Fptr extends Xptr
// {
//     protected void _create ( int kind )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
// 
//     protected void _setName ( QName name )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected boolean _isPositionedAt ( Xptr xAs )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected Xptr _getEmbedded ( )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected boolean _isPositioned ( )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected void _release ( )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected void _dispose ( )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected void _moveTo ( Xptr xTo )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected void _moveTo ( Dom  dTo )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected boolean _toParent ( boolean raw )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected boolean _toFirstChild ( )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected boolean _toLastChild ( )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected boolean _toPrevSibling ( )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected boolean _toNextSibling ( )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected int _cchValue ( )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected int _cchAfter ( )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected boolean _hasChildren ( )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected boolean _atSameNode ( Xptr xAs )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected Dom _getDom ( )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected CharNode _getValueCharNodes ( )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected CharNode _getAfterCharNodes ( )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected void _setValueCharNodes ( CharNode nodes )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected void _setAfterCharNodes ( CharNode nodes )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
// 
//     protected void _remove ( )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected void _insert ( Xptr newSibling )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected void _append ( Xptr newChild )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected void _insertValueChars ( int i, Object src, int off, int cch )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected void _insertAfterChars ( int i, Object src, int off, int cch )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected String _getValueString ( int i, int cch )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected String _getAfterString ( int i, int cch )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected void _removeValueChars ( int i,int cch )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     protected void _removeAfterChars ( int i,int cch )
//     {
//         throw new RuntimeException( "Not implemented" );
//     }
//     
//     //
//     //
//     //
// 
//     private Fptr ( Fmaster m )
//     {
//         super( m );
//         
//         _master = m;
//         _index = m.nextPtrIndex( this );
//     }
// 
//     public static Master newMaster ( )
//     {
//         return new Fmaster();
//     }
// 
//     private static final class Fmaster extends Master implements DOMImplementation
//     {
//         Fmaster ( )
//         {
//             try
//             {
//                 _file = File.createTempFile( "xmlbean", ".heap" );
//                 // System.out.println( "XmlBeans file: " + _file.toString() );
//                 _file.deleteOnExit();
//                 _raf = new RandomAccessFile( _file, "rw" );
//             }
//             catch ( Exception e )
//             {
//                 throw new RuntimeException( e.getMessage(), e );
//             }
//             
//             _free = 4;
// 
//             _ptrs = new Fptr [ 16 ];
//             _nextPtrs = 0;
//             
//             _qnames = new ArrayList();
//             _qnameCache = new HashMap();
//         }
//         
//         protected void finalize ( )
//         {
//             try
//             {
//                 if (_raf  != null) _raf.close();
//                 if (_file != null) _file.delete();
//             }
//             catch ( IOException e )
//             {
//                 throw new RuntimeException( e.getMessage(), e );
//             }
//         }
// 
//         private int allocate ( int size )
//         {
//             assert size > 0 && size % 4 == 0;
//             assert _free % 4 == 0;
//             
//             if (_buf == null || _buf.capacity() - _free < size)
//             {
//                 int newCapacity = _buf == null ? 1024 * 1024 : _buf.capacity();
// 
//                 while (newCapacity - _free < size)
//                     newCapacity += 2;
// 
//                 try
//                 {
//                     _buf = _raf.getChannel().map( FileChannel.MapMode.READ_WRITE, 0, newCapacity );
//                 }
//                 catch ( Exception e )
//                 {
//                     throw new RuntimeException( e.getMessage(), e );
//                 }
//             }
// 
//             int offset = _free;
//             
//             _free += size;
// 
//             return offset;
//         }
// 
//         public Xptr newPtr ( )
//         {
//             return new Fptr( this );
//         }
// 
//         QName getQName ( int i )
//         {
//             assert i >= 0 && i < _qnames.size();
//             return (QName) _qnames.get( i );
//         }
// 
//         int getQName ( QName name )
//         {
//             Integer integer = (Integer) _qnameCache.get( name );
// 
//             if (integer == null)
//             {
//                 integer = new Integer( _qnames.size() );
//                 _qnames.add( name );
//             }
// 
//             return integer.intValue();
//         }
//         
//         int nextPtrIndex ( Fptr fptr )
//         {
//             // TODO - maintain a free list for these ...
// 
//             assert _nextPtrs >= 0 || _nextPtrs < _ptrs.length;
// 
//             int i;
//             
//             if (_ptrs[ _nextPtrs ] == null)
//                 i = _nextPtrs;
//             else
//             {
//                 for ( i = 0 ; i < _ptrs.length ; i++ )
//                     if (_ptrs[ i ] == null)
//                         break;
// 
//                 if (i == _ptrs.length)
//                 {
//                     Fptr newPtrs[] = new Fptr [ _ptrs.length * 2 ];
//                     System.arraycopy( _ptrs, 0, newPtrs, 0, _ptrs.length );
//                     i = _ptrs.length;
//                     _ptrs = newPtrs;
//                 }
//             }
// 
//             assert _ptrs[ i ] == null;
// 
//             _ptrs[ i ] = fptr;
// 
//             _nextPtrs = (i + 1) % _ptrs.length;
//             
//             return i;
//         }
// 
//         void dispose ( Fptr fx )
//         {
//             assert _ptrs[ fx._index ] == fx;
//             _ptrs[ fx._index ] = null;
//         }
//         
//         Fptr getPtr ( int i )
//         {
//             assert i >= 0 && i < _ptrs.length && _ptrs[ i ] != null;
// 
//             return _ptrs[ i ];
//         }
// 
//         public Document createDocument (
//             String namespaceURI, String qualifiedName, DocumentType doctype )
//         {
//             return
//                 DomImpl._domImplementation_createDocument(
//                     this, namespaceURI, qualifiedName, doctype );
//         }
// 
//         public DocumentType createDocumentType (
//             String qualifiedName, String publicId, String systemId )
//         {
//             return
//                 DomImpl._domImplementation_createDocumentType(
//                     this, qualifiedName, publicId, systemId );
//         }
// 
//         public boolean hasFeature ( String feature, String version )
//         {
//             return DomImpl._domImplementation_hasFeature( this, feature, version );
//         }
//         
//         private File             _file;
//         private RandomAccessFile _raf;
//         private ByteBuffer       _buf;
//         private int              _free;
// 
//         private Fptr [ ]         _ptrs;
//         private int              _nextPtrs;
// 
//         private ArrayList        _qnames;
//         private HashMap          _qnameCache;
//     }
// 
// 
// //    //
// //    // Abstract implementations
// //    //
// //
// //    protected void dispose ( )
// //    {
// //        assert _offset == 0;
// //        _master.dispose( this );
// //        _master = null;
// //    }
// //    
// //    protected Xptr getEmbedded ( )
// //    {
// //        throw new RuntimeException( "Not implemented" );
// //    }
// //    
// //    protected void insertEmbedded ( )
// //    {
// //        assert _offset > 0;
// //        
// //        int headIndex = getField( EMBEDHEAD );
// //        
// //        Xptr head = headIndex == 0 ? null : _master.getPtr( headIndex );
// //
// //        head = listInsert( head, EMBEDDED );
// //        
// //        setField( EMBEDHEAD, ((Fptr) head)._index );
// //    }
// //
// //    protected void removeEmbedded ( )
// //    {
// //        assert _offset > 0;
// //        
// //        int headIndex = getField( EMBEDHEAD );
// //
// //        assert headIndex > 0;
// //
// //        Xptr newHead = listRemove( _master.getPtr( headIndex ) );
// //        
// //        setField( EMBEDHEAD, ((Fptr) newHead)._index );
// //    }
// //
// //    protected void moveToNode ( Dom d )
// //    {
// //        set( ((Fptr) d)._offset );
// //        
// //    }
// //    
// //    protected void moveToNode ( Xptr xTo )
// //    {
// //        if (xTo == null)
// //            set( 0 );
// //        else
// //        {
// //            Fptr fxTo = (Fptr) xTo;
// //            set( fxTo._offset );
// //        }
// //    }
// //
// //    public boolean parentNode ( boolean raw )
// //    {
// //        int off = getField( PARENT );
// //
// //        if (off == 0)
// //            return false;
// //
// //        set( off );
// //
// //        return true;
// //    }
// //    
// //    public boolean firstChildNode ( )
// //    {
// //        int off = firstChild();
// //
// //        if (off == 0)
// //            return false;
// //
// //        set( off );
// //
// //        return true;
// //    }
// //    
// //    public boolean lastChildNode ( )
// //    {
// //        throw new RuntimeException( "Not implemented" );
// //    }
// //    
// //    public boolean nextSiblingNode ( )
// //    {
// //        int off = getField( NEXT_SIBLING );
// //
// //        if (off == 0)
// //            return false;
// //
// //        set( off );
// //
// //        return true;
// //    }
// //    
// //    public boolean prevSiblingNode ( )
// //    {
// //        int off = getField( PREV_SIBLING );
// //
// //        if (off == 0)
// //            return false;
// //
// //        set( off );
// //
// //        return true;
// //    }
// //    
// //    protected String getText ( int pos, int cch )
// //    {
// //        throw new RuntimeException( "Not implemented" );
// //    }
// //    
// //    protected void insertText ( int pos, Object src, int off, int cch )
// //    {
// //        throw new RuntimeException( "Not implemented" );
// //    }
// //    
// //    protected void removeText ( int pos, int cch )
// //    {
// //        throw new RuntimeException( "Not implemented" );
// //    }
// //    
// //    protected void removeNode ( )
// //    {
// //        throw new RuntimeException( "Not implemented" );
// //    }
// //    
// //    protected void appendNode ( Xptr child )
// //    {
// //        throw new RuntimeException( "Not implemented" );
// //    }
// //    
// //    protected void insertNode ( Xptr sibling )
// //    {
// //        throw new RuntimeException( "Not implemented" );
// //    }
// //    
// //    protected void pushNode ( int pos )
// //    {
// //        assert pos >= 0 && _offset > 0;
// //        _stack = _master.push( null, ((long) _offset) << 32 + pos, _stack );
// //    }
// //
// //    protected void popNode ( Object obj, long num )
// //    {
// //        assert obj == null;
// //        set( (int) (num >> 32) );
// //    }
// //
// //    protected boolean atSameNode ( Xptr xAs )
// //    {
// //        Fptr fx = (Fptr) xAs;
// //        return _master == fx._master && _offset == fx._offset;
// //    }
// //    
// //    public boolean hasChildren ( )
// //    {
// //        return firstChild() != 0;
// //    }
// //    
// //    public int cchValue ( )
// //    {
// //        int off = getField( VALUE );
// //        
// //        return off == 0 ? 0 : getInt( off );
// //    }
// //    
// //    public int cchAfter ( )
// //    {
// //        int off = getField( AFTER );
// //        
// //        return off == 0 ? 0 : getInt( off );
// //    }
// //    
// //    public void createNode ( int kind )
// //    {
// //        int bits = kind;
// //        
// //        int off = _master.allocate( MAX );
// //
// //        setInt( off + BITS, bits );
// //
// //        set( off );
// //    }
// //    
// //    public Dom domNode ( )
// //    {
// //        assert !isPooled() && _offset > 0;
// //
// //        return domNode( _offset );
// //    }
// //    
// //    public CharNode getCharNodes ( boolean after )
// //    {
// //        throw new RuntimeException( "Not implemented" );
// //    }
// //
// //    public void setCharNodes ( boolean after, CharNode nodes )
// //    {
// //        throw new RuntimeException( "Not implemented" );
// //    }
// //    
// //    protected Dom domNode ( int off )
// //    {
// //        assert off > 0;
// //        
// //        int headIndex = getInt( off + EMBEDHEAD );
// //
// //        Fptr fp = headIndex == 0 ? null : _master.getPtr( headIndex );
// //
// //        for ( ; fp != null ; fp = (Fptr) fp._next )
// //            if (fp instanceof Dom)
// //                break;
// //
// //        if (fp == null)
// //        {
// //            switch ( _kind  )
// //            {
// //                case DOMDOC   : fp = new DocumentFptr     ( _master ); break;
// //                case DOMFRAG  : fp = new DocumentFragFptr ( _master ); break;
// //                case ELEM     : fp = new ElementFptr      ( _master ); break;
// //                case XMLNS    :
// //                case ATTR     : fp = new AttrFptr         ( _master ); break;
// //                case COMMENT  : fp = new CommentFptr      ( _master ); break;
// //                case PROCINST : fp = new ProcInstFptr     ( _master ); break;
// //
// //                default : throw new RuntimeException( "Unexpected kind" );
// //            }
// //
// //            // DOM Fptr's are not put onto any list here because they do not need to be embedded
// //            // because they will always has a pos of 0 and will simply travel with this node
// //            // (offset)
// //
// //            // BUGBUG - this will not work - need to create a phantom
// //            // reference which points to the object which implemenbts the dom
// //            // node.
// //
// //            fp.set( off );
// //            fp.insertEmbedded();
// //        }
// //
// //        return (Dom) fp;
// //    }
// //
// //    protected QName name ( )
// //    {
// //        assert !isPooled() && _offset > 0;
// //        int i = getField( NAME );
// //        return i == 0 ? null : _master.getQName( i );
// //    }
// //
// //    protected void setName ( QName name )
// //    {
// // //        assert name == null || hasName();
// // //
// // //        setInt( off + NAME, _master.getQName( name ) );
// //
// //        throw new RuntimeException( "Not impl" );
// //    }
// //    
// //    //
// //    //
// //    //
// //    
// //    private Fptr ( Fmaster m )
// //    {
// //        super( m );
// //        
// //        _master = m;
// //        _index = m.nextPtrIndex( this );
// //    }
// //
// //    private static final int BITS         =  0 * 4;
// //    private static final int NAME         =  1 * 4;
// //    private static final int PARENT       =  2 * 4;
// //    private static final int FIRST_CHILD  =  3 * 4;
// //    private static final int LAST_CHILD   =  4 * 4;
// //    private static final int NEXT_SIBLING =  5 * 4;
// //    private static final int PREV_SIBLING =  6 * 4;
// //    private static final int VALUE        =  7 * 4;
// //    private static final int AFTER        =  8 * 4;
// //    private static final int EMBEDHEAD    =  9 * 4;
// //    private static final int MAX          = 10 * 4;
// //
// //    protected int getInt ( int offset )
// //    {
// //        return _master._buf.getInt( offset );
// //    }
// //    
// //    private void setInt ( int offset, int value )
// //    {
// //        _master._buf.putInt( offset, value );
// //    }
// //    
// //    protected int getField ( int field )
// //    {
// //        return getInt( _offset + field );
// //    }
// //    
// //    private void setField ( int field, int value )
// //    {
// //        setInt( _offset + field, value );
// //    }
// //
// //    private void set ( int off )
// //    {
// //        if (off != _offset)
// //        {
// //            _offset = off;
// //            _kind = off == 0 ? -1 : getField( BITS );
// //        }
// //    }
// //
// //    private int firstChild ( )
// //    {
// //        int off = getInt( _offset + FIRST_CHILD );
// //
// //        if (off == 0)
// //            return 0;
// //        
// //        for ( ; ; )
// //        {
// //            if (getInt( off + BITS ) >> 1 != ATTR)
// //                return off;
// //
// //            off = getInt( off + NEXT_SIBLING );
// //
// //            if (off == 0)
// //                return 0;
// //        }
// //    }
// //    
// //    //
// //    // DOM
// //    //
// //
// //    private static abstract class NodeFptr extends Fptr implements Dom
// //    {
// //        NodeFptr ( Fmaster m ) { super( m ); }
// //        
// //        public Master master ( )
// //        {
// //            return _master;
// //        }
// //
// //        public Xptr tempPtr ( )
// //        {
// //            Xptr x = master().tempPtr();
// //            moveTo( x );
// //            return x;
// //        }
// //        
// //        public int nodeType ( )
// //        {
// //            return DomImpl.domNodeType( getField( BITS ) );
// //        }
// //
// //        public Dom altParent ( )
// //        {
// //            return _altParent;
// //        }
// //        
// //        public Dom firstChild  ( )       { return DomImpl.node_getFirstChild ( this ); }
// //        public Dom nextSibling ( )       { return DomImpl.node_getNextSibling( this ); }
// //        public Dom parent      ( )       { return DomImpl.node_getParentNode( this );  }
// //        public Dom remove      ( )       { return DomImpl.remove( this );              }
// //        public Dom insert      ( Dom b ) { return DomImpl.insert( this, b );           }
// //        public Dom append      ( Dom p ) { return DomImpl.append( this, p );           }
// //
// //        public Node appendChild ( Node newChild ) { return DomImpl._node_appendChild( this, newChild ); }
// //        public Node cloneNode ( boolean deep ) { return DomImpl._node_cloneNode( this, deep ); }
// //        public NamedNodeMap getAttributes ( ) { return DomImpl._node_getAttributes( this ); }
// //        public NodeList getChildNodes ( ) { return DomImpl._node_getChildNodes( this ); }
// //        public Node getParentNode ( ) { return DomImpl._node_getParentNode( this ); }
// //        public Node removeChild ( Node oldChild ) { return DomImpl._node_removeChild( this, oldChild ); }
// //        public Node getFirstChild ( ) { return DomImpl._node_getFirstChild( this ); }
// //        public Node getLastChild ( ) { return DomImpl._node_getLastChild( this ); }
// //        public String getLocalName ( ) { return DomImpl._node_getLocalName( this ); }
// //        public String getNamespaceURI ( ) { return DomImpl._node_getNamespaceURI( this ); }
// //        public Node getNextSibling ( ) { return DomImpl._node_getNextSibling( this ); }
// //        public String getNodeName ( ) { return DomImpl._node_getNodeName( this ); }
// //        public short getNodeType ( ) { return DomImpl._node_getNodeType( this ); }
// //        public String getNodeValue ( ) { return DomImpl._node_getNodeValue( this ); }
// //        public Document getOwnerDocument ( ) { return DomImpl._node_getOwnerDocument( this ); }
// //        public String getPrefix ( ) { return DomImpl._node_getPrefix( this ); }
// //        public Node getPreviousSibling ( ) { return DomImpl._node_getPreviousSibling( this ); }
// //        public boolean hasAttributes ( ) { return DomImpl._node_hasAttributes( this ); }
// //        public boolean hasChildNodes ( ) { return DomImpl._node_hasChildNodes( this ); }
// //        public Node insertBefore ( Node newChild, Node refChild ) { return DomImpl._node_insertBefore( this, newChild, refChild ); }
// //        public boolean isSupported ( String feature, String version ) { return DomImpl._node_isSupported( this, feature, version ); }
// //        public void normalize ( ) { DomImpl._node_normalize( this ); }
// //        public Node replaceChild ( Node newChild, Node oldChild ) { return DomImpl._node_replaceChild( this, newChild, oldChild ); }
// //        public void setNodeValue ( String nodeValue ) { DomImpl._node_setNodeValue( this, nodeValue ); }
// //        public void setPrefix ( String prefix ) { DomImpl._node_setPrefix( this, prefix ); }
// //
// //        Dom _altParent;
// //    }
// //    
// //    private static class DocumentFptr extends NodeFptr implements Document
// //    {
// //        DocumentFptr ( Fmaster m ) { super( m ); }
// //        public Attr createAttribute ( String name ) { return DomImpl._document_createAttribute( this, name ); }
// //        public Attr createAttributeNS ( String namespaceURI, String qualifiedName ) { return DomImpl._document_createAttributeNS( this, namespaceURI, qualifiedName ); }
// //        public CDATASection createCDATASection ( String data ) { return DomImpl._document_createCDATASection( this, data ); }
// //        public Comment createComment ( String data ) { return DomImpl._document_createComment( this, data ); }
// //        public DocumentFragment createDocumentFragment ( ) { return DomImpl._document_createDocumentFragment( this ); }
// //        public Element createElement ( String tagName ) { return DomImpl._document_createElement( this, tagName ); }
// //        public Element createElementNS ( String namespaceURI, String qualifiedName ) { return DomImpl._document_createElementNS( this, namespaceURI, qualifiedName ); }
// //        public EntityReference createEntityReference ( String name ) { return DomImpl._document_createEntityReference( this, name ); }
// //        public ProcessingInstruction createProcessingInstruction ( String target, String data ) { return DomImpl._document_createProcessingInstruction( this, target, data ); }
// //        public Text createTextNode ( String data ) { return DomImpl._document_createTextNode( this, data ); }
// //        public DocumentType getDoctype ( ) { return DomImpl._document_getDoctype( this ); }
// //        public Element getDocumentElement ( ) { return DomImpl._document_getDocumentElement( this ); }
// //        public Element getElementById ( String elementId ) { return DomImpl._document_getElementById( this, elementId ); }
// //        public NodeList getElementsByTagName ( String tagname ) { return DomImpl._document_getElementsByTagName( this, tagname ); }
// //        public NodeList getElementsByTagNameNS ( String namespaceURI, String localName ) { return DomImpl._document_getElementsByTagNameNS( this, namespaceURI, localName ); }
// //        public DOMImplementation getImplementation ( ) { return DomImpl._document_getImplementation( this ); }
// //        public Node importNode ( Node importedNode, boolean deep ) { return DomImpl._document_importNode( this, importedNode, deep ); }
// //    }
// //    
// //    private static class DocumentFragFptr extends NodeFptr implements DocumentFragment
// //    {
// //        DocumentFragFptr ( Fmaster m ) { super( m ); }
// //    }
// //
// //    private static class ElementFptr extends NodeFptr implements Element
// //    {
// //        ElementFptr ( Fmaster m ) { super( m ); }
// //        public String getAttribute ( String name ) { return DomImpl._element_getAttribute( this, name ); }
// //        public Attr getAttributeNode ( String name ) { return DomImpl._element_getAttributeNode( this, name ); }
// //        public Attr getAttributeNodeNS ( String namespaceURI, String localName ) { return DomImpl._element_getAttributeNodeNS( this, namespaceURI, localName ); }
// //        public String getAttributeNS ( String namespaceURI, String localName ) { return DomImpl._element_getAttributeNS( this, namespaceURI, localName ); }
// //        public NodeList getElementsByTagName ( String name ) { return DomImpl._element_getElementsByTagName( this, name ); }
// //        public NodeList getElementsByTagNameNS ( String namespaceURI, String localName ) { return DomImpl._element_getElementsByTagNameNS( this, namespaceURI, localName ); }
// //        public String getTagName ( ) { return DomImpl._element_getTagName( this ); }
// //        public boolean hasAttribute ( String name ) { return DomImpl._element_hasAttribute( this, name ); }
// //        public boolean hasAttributeNS ( String namespaceURI, String localName ) { return DomImpl._element_hasAttributeNS( this, namespaceURI, localName ); }
// //        public void removeAttribute ( String name ) { DomImpl._element_removeAttribute( this, name ); }
// //        public Attr removeAttributeNode ( Attr oldAttr ) { return DomImpl._element_removeAttributeNode( this, oldAttr ); }
// //        public void removeAttributeNS ( String namespaceURI, String localName ) { DomImpl._element_removeAttributeNS( this, namespaceURI, localName ); }
// //        public void setAttribute ( String name, String value ) { DomImpl._element_setAttribute( this, name, value ); }
// //        public Attr setAttributeNode ( Attr newAttr ) { return DomImpl._element_setAttributeNode( this, newAttr ); }
// //        public Attr setAttributeNodeNS ( Attr newAttr ) { return DomImpl._element_setAttributeNodeNS( this, newAttr ); }
// //        public void setAttributeNS ( String namespaceURI, String qualifiedName, String value ) { DomImpl._element_setAttributeNS( this, namespaceURI, qualifiedName, value ); }
// //    }
// //    
// //    private static class AttrFptr extends NodeFptr implements Attr
// //    {
// //        AttrFptr ( Fmaster m ) { super( m ); }
// //        public String getName ( ) { return DomImpl._attr_getName( this ); }
// //        public Element getOwnerElement ( ) { return DomImpl._attr_getOwnerElement( this ); }
// //        public boolean getSpecified ( ) { return DomImpl._attr_getSpecified( this ); }
// //        public String getValue ( ) { return DomImpl._attr_getValue( this ); }
// //        public void setValue ( String value ) { DomImpl._attr_setValue( this, value ); }
// //    }
// //    
// //    private static class ProcInstFptr extends NodeFptr implements ProcessingInstruction
// //    {
// //        ProcInstFptr ( Fmaster m ) { super( m ); }
// //        public String getData ( ) { return DomImpl._processingInstruction_getData( this ); }
// //        public String getTarget ( ) { return DomImpl._processingInstruction_getTarget( this ); }
// //        public void setData ( String data ) { DomImpl._processingInstruction_setData( this, data ); }
// //    }
// //    
// //    private static class CommentFptr extends NodeFptr implements Comment
// //    {
// //        CommentFptr ( Fmaster m ) { super( m ); }
// //        public void appendData ( String arg ) { DomImpl._comment_appendData( this, arg ); }
// //        public void deleteData ( int offset, int count ) { DomImpl._comment_deleteData( this, offset, count ); }
// //        public String getData ( ) { return DomImpl._comment_getData( this ); }
// //        public int getLength ( ) { return DomImpl._comment_getLength( this ); }
// //        public void insertData ( int offset, String arg ) { DomImpl._comment_insertData( this, offset, arg ); }
// //        public void replaceData ( int offset, int count, String arg ) { DomImpl._comment_replaceData( this, offset, count, arg ); }
// //        public void setData ( String data ) { DomImpl._comment_setData( this, data ); }
// //        public String substringData ( int offset, int count ) { return DomImpl._comment_substringData( this, offset, count ); }
// //    }
// //    
// //    //
// //    //
// //    //
// //    
//     protected Fmaster _master;
//     private   int     _index;
//     private   int     _offset;
// }
 

/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights 
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer. 
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:  
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must 
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written 
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache 
*    XMLBeans", nor may "Apache" appear in their name, without prior 
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems 
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package org.apache.xmlbeans.impl.newstore;

import javax.xml.stream.XMLStreamReader;

import javax.xml.namespace.QName;

import java.util.ArrayList;

import javax.xml.namespace.QName;

import java.io.PrintStream;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.DOMImplementation;

import org.apache.xmlbeans.impl.newstore.Cursor;
import org.apache.xmlbeans.impl.newstore.CharUtil;
import org.apache.xmlbeans.impl.newstore.SaajImpl;
import org.apache.xmlbeans.impl.newstore.Jsr173;
import org.apache.xmlbeans.impl.newstore.pub.store.Locale;
import org.apache.xmlbeans.impl.newstore.pub.store.Cur;
import org.apache.xmlbeans.impl.newstore.pub.store.Dom;
import org.apache.xmlbeans.impl.newstore.pub.store.Dom.CharNode;
import org.apache.xmlbeans.impl.newstore.pub.store.Dom.TextNode;
import org.apache.xmlbeans.impl.newstore.pub.store.Dom.CdataNode;

import org.apache.xmlbeans.XmlCursor;

import javax.xml.soap.SOAPElement;

public final class DomImpl
{
    public static final int ELEMENT   = Node.ELEMENT_NODE;
    public static final int ATTR      = Node.ATTRIBUTE_NODE;
    public static final int TEXT      = Node.TEXT_NODE;
    public static final int CDATA     = Node.CDATA_SECTION_NODE;
    public static final int ENTITYREF = Node.ENTITY_REFERENCE_NODE;
    public static final int ENTITY    = Node.ENTITY_NODE;
    public static final int PROCINST  = Node.PROCESSING_INSTRUCTION_NODE;
    public static final int COMMENT   = Node.COMMENT_NODE;
    public static final int DOCUMENT  = Node.DOCUMENT_NODE;
    public static final int DOCTYPE   = Node.DOCUMENT_TYPE_NODE;
    public static final int DOCFRAG   = Node.DOCUMENT_FRAGMENT_NODE;
    public static final int NOTATION  = Node.NOTATION_NODE;

    //
    // Handy dandy Dom exceptions
    //

    static class HierarchyRequestErr extends DOMException
    {
        HierarchyRequestErr ( ) { this( "This node isn't allowed there" ); }
        HierarchyRequestErr ( String message ) { super( HIERARCHY_REQUEST_ERR, message ); }
    }
    
    static class WrongDocumentErr extends DOMException
    {
        WrongDocumentErr ( ) { this( "Nodes do not belong to the same document" ); }
        WrongDocumentErr ( String message ) { super( WRONG_DOCUMENT_ERR, message ); }
    }
    
    static class NotFoundErr extends DOMException
    {
        NotFoundErr ( ) { this( "Node not found" ); }
        NotFoundErr ( String message ) { super( NOT_FOUND_ERR, message ); }
    }

    static class NamespaceErr extends DOMException
    {
        NamespaceErr ( ) { this( "Namespace error" ); }
        NamespaceErr ( String message ) { super( NAMESPACE_ERR, message ); }
    }

    static class NoModificationAllowedErr extends DOMException
    {
        NoModificationAllowedErr ( ) { this( "No modification allowed error" ); }
        NoModificationAllowedErr ( String message ) { super( NO_MODIFICATION_ALLOWED_ERR, message ); }
    }
    
    static class InuseAttributeError extends DOMException
    {
        InuseAttributeError ( ) { this( "Attribute currently in use error" ); }
        InuseAttributeError ( String message ) { super( INUSE_ATTRIBUTE_ERR, message ); }
    }
    
    static class IndexSizeError extends DOMException
    {
        IndexSizeError ( ) { this( "Index Size Error" ); }
        IndexSizeError ( String message ) { super( INDEX_SIZE_ERR, message ); }
    }

    static class NotSupportedError extends DOMException
    {
        NotSupportedError ( ) { this( "This operation is not supported" ); }
        NotSupportedError ( String message ) { super( NOT_SUPPORTED_ERR, message ); }
    }

    //
    //
    //

//    public interface Dom
//    {
//        Master master    ( );
//        int    nodeType  ( );
//        Xcur   tempCur   ( );
//
//        String name      ( );
//        QName  qName     ( );
//
//        void dump ( PrintStream o );
//        void dump ( );
//
//        // These will simply delegate to DomImpl non-gateway methods
//        Dom firstChild  ( );
//        Dom nextSibling ( );
//        Dom parent      ( );
//        Dom remove      ( );
//        Dom insert      ( Dom b );
//        Dom append      ( Dom p );
//    }

    //
    //
    //

    public static Dom parent ( Dom d )
    {
        return node_getParentNode( d );
    }

    public static Dom firstChild ( Dom d )
    {
        return node_getFirstChild( d );
    }
    
    public static Dom nextSibling ( Dom d )
    {
        return node_getNextSibling( d );
    }

    public static Dom append ( Dom n, Dom p )
    {
        return node_insertBefore( p, n, null );
    }

    public static Dom insert ( Dom n, Dom b )
    {
        return node_insertBefore( parent( b ), n, b );
    }

    public static Dom remove ( Dom n )
    {
        Dom p = parent( n );

        if (p != null)
            node_removeChild( p, n );

        return n;
    }

    static String nodeKindName ( int t )
    {
        switch ( t )
        {
        case ATTR      : return "attribute";
        case CDATA     : return "cdata section";
        case COMMENT   : return "comment";
        case DOCFRAG   : return "document fragment";
        case DOCUMENT  : return "document";
        case DOCTYPE   : return "document type";
        case ELEMENT   : return "element";
        case ENTITY    : return "entity";
        case ENTITYREF : return "entity reference";
        case NOTATION  : return "notation";
        case PROCINST  : return "processing instruction";
        case TEXT      : return "text";
                                           
        default : throw new RuntimeException( "Unknown node type" );
        }
    }

    private static String isValidChild ( Dom parent, Dom child )
    {
        int pk = parent.nodeType();
        int ck = child.nodeType();

        switch ( pk )
        {
        case DOCUMENT :
        {
            switch ( ck )
            {
            case ELEMENT :
            {
                if (document_getDocumentElement( parent ) != null)
                    return "Documents may only have a maximum of one document element";

                return null;
            }
            case DOCTYPE :
            {
                if (document_getDoctype( parent ) != null)
                    return "Documents may only have a maximum of one document type node";
                
                return null;
            }
            case PROCINST :
            case COMMENT  :
                return null;
            }

            break;
        }

        case ATTR :
        {
            if (ck == TEXT || ck == ENTITYREF)
                return null;

            // TODO -- traverse the entity tree, making sure that there are
            // only entity refs and text nodes in it.
        }
            
        case DOCFRAG   :
        case ELEMENT   :
        case ENTITY    :
        case ENTITYREF :
        {
            switch ( ck )
            {
            case ELEMENT :
            case ENTITYREF:
            case CDATA :
            case TEXT :
            case COMMENT :
            case PROCINST :
                return null;
            }

            break;
        }

        case CDATA :
        case TEXT :
        case COMMENT :
        case PROCINST :
        case DOCTYPE :
        case NOTATION :
            return nodeKindName( pk ) + " nodes may not have any children";
        }

        return
            nodeKindName( pk ) + " nodes may not have " +
                nodeKindName( ck ) + " nodes as children";
    }

    private static void validateNewChild ( Dom parent, Dom child )
    {
        String msg = isValidChild( parent, child );

        if (msg != null)
            throw new HierarchyRequestErr( msg );

        if (parent == child)
            throw new HierarchyRequestErr( "New child and parent are the same node" );

        while ( (child = parent( child )) != null )
        {
            // TODO - use read only state on a node to know if it is under an
            // entity ref
            
            if (child.nodeType() == ENTITYREF)
                throw new NoModificationAllowedErr( "Entity reference trees may not be modified" );

            if (child == parent)
                throw new HierarchyRequestErr( "New child is an ancestor node of the parent node" );
        }
    }

    private static void removeNode ( Dom n )
    {
        Cur cTo = n.tempCur();
        Cur cFrom = n.tempCur();

        cFrom.toEnd();
        cFrom.next();

        CharNode fromNodes = cFrom.getCharNodes();

        if (fromNodes != null)
            cTo.setCharNodes( CharNode.appendNodes( cTo.getCharNodes(), fromNodes ) );

        cTo.moveNode( null );

        cTo.release();
        cFrom.release();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Document _domImplementation_createDocument (
        Locale l, String u, String n, DocumentType t )
    {
        Document d;

        if (l.noSync())         { l.enter(); try { d = domImplementation_createDocument( l, u, n, t ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { d = domImplementation_createDocument( l, u, n, t ); } finally { l.exit(); } }

        return d;
    }

    public static Document domImplementation_createDocument (
        Locale l, String namespaceURI, String qualifiedName, DocumentType doctype )
    {
        Cur c = l.tempCur();
        
        c.createRoot( Cur.DOMDOC );

        Dom d = c.getDom();
        
        c.next();
        
        c.createElement( l.makeQualifiedQName( namespaceURI, qualifiedName ) );
        
        if (doctype != null)
            throw new RuntimeException( "Not impl" );

        c.release();
        
        return (Document) d;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static DocumentType _domImplementation_createDocumentType (
        Locale l, String qname, String publicId, String systemId )
    {
        throw new RuntimeException( "Not impl" );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static boolean _domImplementation_hasFeature (
        Locale l, String feature, String version )
    {
        throw new RuntimeException( "Not impl" );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static DOMImplementation _document_getImplementation ( Dom d )
    {
        return (DOMImplementation) d.locale();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Element _document_createElementNS ( Dom d, String uri, String qname )
    {
        Locale l = d.locale();

        Dom e;

        if (l.noSync())         { l.enter(); try { e = document_createElementNS( d, uri, qname ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { e = document_createElementNS( d, uri, qname ); } finally { l.exit(); } }

        return (Element) e;
    }

    public static Dom document_createElementNS ( Dom d, String uri, String qname )
    {
        // TODO - validate the name here
        Locale l = d.locale();
        
        Cur c = l.tempCur();
        
        c.createElement( l.makeQualifiedQName( uri, qname ) );

        Dom e = c.getDom();
        
        c.release();
        
        return e;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Element _document_createElement ( Dom d, String name )
    {
        Locale l = d.locale();

        Dom e;

        if (l.noSync())         { l.enter(); try { e = document_createElement( d, name ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { e = document_createElement( d, name ); } finally { l.exit(); } }

        return (Element) e;
    }

    public static Dom document_createElement ( Dom d, String name )
    {
        // TODO - validate the name here
        
        Locale l = d.locale();

        Cur c = l.tempCur();

        c.createElement( l.makeQualifiedQName( "", name ) );

        Dom e = c.getDom();

        c.release();

        return e;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Text _document_createTextNode ( Dom d, String data )
    {
        return (Text) document_createTextNode( d, data );
    }

    public static CharNode document_createTextNode ( Dom d, String data )
    {
        TextNode t = d.locale().createTextNode();

        if (data == null)
            data = "";

        t._src = data;
        t._off = 0;
        t._cch = data.length();

        return t;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Comment _document_createComment ( Dom d, String data )
    {
        Locale l = d.locale();

        Dom c;

        if (l.noSync())         { l.enter(); try { c = document_createComment( d, data ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { c = document_createComment( d, data ); } finally { l.exit(); } }

        return (Comment) c;
    }
    
    public static Dom document_createComment ( Dom d, String data )
    {
        Locale l = d.locale();

        Cur c = l.tempCur();

        c.createComment();

        Dom comment = c.getDom();

        if (data != null)
        {
            c.next();
            c.insertChars( data, 0, data.length() );
        }

        c.release();

        return comment;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static DocumentFragment _document_createDocumentFragment ( Dom d )
    {
        Locale l = d.locale();

        Dom f;

        if (l.noSync())         { l.enter(); try { f = document_createDocumentFragment( d ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { f = document_createDocumentFragment( d ); } finally { l.exit(); } }

        return (DocumentFragment) f;
    }
    
    public static Dom document_createDocumentFragment ( Dom d )
    {
        Cur c = d.locale().tempCur();

        c.createRoot( Cur.DOMFRAG );

        Dom f = c.getDom();
        
        c.release();

        return f;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Attr _document_createAttribute ( Dom d, String name )
    {
        Locale l = d.locale();

        Dom a;

        if (l.noSync())         { l.enter(); try { a = document_createAttribute( d, name ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { a = document_createAttribute( d, name ); } finally { l.exit(); } }

        return (Attr) a;
    }

    public static Dom document_createAttribute ( Dom d, String name )
    {
        // TODO - validate the name here

        Locale l = d.locale();

        Cur c = l.tempCur();

        c.createAttr( l.makeQualifiedQName( "", name ) );

        Dom e = c.getDom();

        c.release();

        return e;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Attr _document_createAttributeNS ( Dom d, String uri, String qname )
    {
        Locale l = d.locale();

        Dom a;

        if (l.noSync())         { l.enter(); try { a = document_createAttributeNS( d, uri, qname ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { a = document_createAttributeNS( d, uri, qname ); } finally { l.exit(); } }

        return (Attr) a;
    }
    
    public static Dom document_createAttributeNS ( Dom d, String uri, String qname )
    {
        // TODO - validate the name here

        Locale l = d.locale();

        Cur c = l.tempCur();

        c.createAttr( l.makeQualifiedQName( uri, qname ) );

        Dom e = c.getDom();

        c.release();

        return e;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    
    public static Element _document_getDocumentElement ( Dom d )
    {
        Locale l = d.locale();

        Dom e;

        if (l.noSync())         { l.enter(); try { e = document_getDocumentElement( d ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { e = document_getDocumentElement( d ); } finally { l.exit(); } }

        return (Element) e;
    }

    public static Dom document_getDocumentElement ( Dom d )
    {
        for ( d = firstChild( d ) ; d != null ; d = nextSibling( d ) )
        {
            if (d.nodeType() == ELEMENT)
                return d;
        }

        return null;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static DocumentType _document_getDoctype ( Dom n )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public static Dom document_getDoctype ( Dom n )
    {
        throw new RuntimeException( "Not implemented" );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Document _node_getOwnerDocument ( Dom n )
    {
        Locale l = n.locale();

        Dom d;

        if (l.noSync())         { l.enter(); try { d = node_getOwnerDocument( n ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { d = node_getOwnerDocument( n ); } finally { l.exit(); } }

        return (Document) d;
    }
    
    public static Dom node_getOwnerDocument ( Dom n )
    {
        Locale l = n.locale();

        if (l._ownerDoc == null)
        {
            Cur c = l.tempCur();
            c.createRoot( Cur.DOMDOC );
            l._ownerDoc = c.getDom();
            c.release();
        }

        return l._ownerDoc;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static String _element_getTagName ( Dom e )
    {
        return e.name();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Attr _element_getAttributeNode ( Dom e, String name )
    {
        return (Attr) _attributes_getNamedItem( e, name );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Attr _element_getAttributeNodeNS ( Dom e, String uri, String local )
    {
        return (Attr) _attributes_getNamedItemNS( e, uri, local );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Attr _element_setAttributeNode ( Dom e, Attr newAttr )
    {
        return (Attr) _attributes_setNamedItem( e, newAttr );
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Attr _element_setAttributeNodeNS ( Dom e, Attr newAttr )
    {
        return (Attr) _attributes_setNamedItemNS( e, newAttr );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static String _element_getAttribute ( Dom e, String name )
    {
        Node a = _attributes_getNamedItem( e, name );
        return a == null ? "" : a.getNodeValue();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static String _element_getAttributeNS ( Dom e, String uri, String local )
    {
        Node a = _attributes_getNamedItemNS( e, uri, local );
        return a == null ? "" : a.getNodeValue();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static boolean _element_hasAttribute ( Dom e, String name )
    {
        return _attributes_getNamedItem( e, name ) != null;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static boolean _element_hasAttributeNS ( Dom e, String uri, String local )
    {
        return _attributes_getNamedItemNS( e, uri, local ) != null;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static void _element_removeAttribute ( Dom e, String name )
    {
        try
        {
            _attributes_removeNamedItem( e, name );
        }
        catch ( NotFoundErr ex )
        {
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static void _element_removeAttributeNS ( Dom e, String uri, String local )
    {
        try
        {
            _attributes_removeNamedItemNS( e, uri, local );
        }
        catch ( NotFoundErr ex )
        {
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Attr _element_removeAttributeNode ( Dom e, Attr oldAttr )
    {
        if (oldAttr.getOwnerElement() != e)
            throw new NotFoundErr( "Attribute to remove does not belong to this element" );

        return (Attr) _attributes_removeNamedItem( e, oldAttr.getNodeName() );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static void _element_setAttribute ( Dom e, String name, String value )
    {
        // TODO - validate all attr/element names in all apprpraite
        // methdos

        Locale l = e.locale();

        if (l.noSync())         { l.enter(); try { element_setAttribute( e, name, value ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { element_setAttribute( e, name, value ); } finally { l.exit(); } }
    }
    
    public static void element_setAttribute ( Dom e, String name, String value )
    {
        Dom a = attributes_getNamedItem( e, name );

        if (a == null)
        {
            a = document_createAttribute( node_getOwnerDocument( e ), name );
            attributes_setNamedItem( e, a );
        }
        
        node_setNodeValue( a, value );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static void _element_setAttributeNS ( Dom e, String uri, String local, String value )
    {
        Locale l = e.locale();

        if (l.noSync())         { l.enter(); try { element_setAttributeNS( e, uri, local, value ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { element_setAttributeNS( e, uri, local, value ); } finally { l.exit(); } }
    }
    
    public static void element_setAttributeNS ( Dom e, String uri, String local, String value )
    {
        Dom a = attributes_getNamedItemNS( e, uri, local );

        if (a == null)
        {
            a = document_createAttributeNS( node_getOwnerDocument( e ), uri, local);
            attributes_setNamedItemNS( e, a );
        }

        node_setNodeValue( a, value );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static NodeList _element_getElementsByTagName ( Dom e, String name )
    {
        Locale l = e.locale();

        NodeList nl;

        if (l.noSync())         { l.enter(); try { nl = element_getElementsByTagName( e, name ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { nl = element_getElementsByTagName( e, name ); } finally { l.exit(); } }

        return nl;

    }
    public static NodeList element_getElementsByTagName ( Dom e, String name )
    {
        return new ElementsByTagNameNodeList( e, name );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static NodeList _element_getElementsByTagNameNS ( Dom e, String uri, String local )
    {
        Locale l = e.locale();

        NodeList nl;

        if (l.noSync())         { l.enter(); try { nl = element_getElementsByTagNameNS( e, uri, local ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { nl = element_getElementsByTagNameNS( e, uri, local ); } finally { l.exit(); } }

        return nl;
    }
    
    public static NodeList element_getElementsByTagNameNS ( Dom e, String uri, String local )
    {
        return new ElementsByTagNameNSNodeList( e, uri, local );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _node_removeChild ( Dom p, Node child )
    {
        Locale l = p.locale();

        if (child == null)
            throw new NotFoundErr( "Child to remove is null" );

        Dom c;
        
        if (!(child instanceof Dom) || (c = (Dom) child).locale() != l)
            throw new WrongDocumentErr( "Child to remove is from another document" );

        Dom d;

        if (l.noSync())         { l.enter(); try { d = node_removeChild( p, c ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { d = node_removeChild( p, c ); } finally { l.exit(); } }

        return (Node) d;
    }

    public static Dom node_removeChild ( Dom parent, Dom child )
    {
        if (parent( child ) != parent)
            throw new NotFoundErr( "Child to remove is not a child of given parent" );
        
        switch ( child.nodeType() )
        {
        case DOCUMENT :
        case DOCFRAG :
        case ATTR :
            throw new IllegalStateException();
            
        case ELEMENT :
        case PROCINST :
        case COMMENT :
            removeNode( child );
            break;

        case TEXT :
        case CDATA :
        {
            Cur c = child.tempCur();
            
            CharNode nodes = c.getCharNodes();

            CharNode cn = (CharNode) child;

            assert cn._src instanceof Dom;

            cn._src = c.moveChars( null, cn._cch );
            cn._off = c._offSrc;
            cn._cch = c._cchSrc;
            
            c.setCharNodes( CharNode.remove( nodes, cn ) );

            c.release();

            break;
        }
            
        case ENTITYREF :
            throw new RuntimeException( "Not impl" );
            
        case ENTITY :
        case DOCTYPE :
        case NOTATION :
            throw new RuntimeException( "Not impl" );
            
        default : throw new RuntimeException( "Unknown kind" );
        }

        return child;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _node_replaceChild ( Dom p, Node newChild, Node oldChild )
    {
        Locale l = p.locale();

        if (newChild == null)
            throw new NotFoundErr( "Child to add is null" );

        Dom nc;

        if (!(newChild instanceof Dom) || (nc = (Dom) newChild).locale() != l)
            throw new WrongDocumentErr( "Child to add is from another document" );

        Dom oc = null;

        if (!(oldChild instanceof Dom) || (oc = (Dom) oldChild).locale() != l)
            throw new WrongDocumentErr( "Chidl to replace is from another document" );

        Dom d;

        if (l.noSync())         { l.enter(); try { d = node_insertBefore( p, nc, oc ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { d = node_insertBefore( p, nc, oc ); } finally { l.exit(); } }

        return (Node) d;
        
    }
    
    public static Dom node_replaceChild ( Dom p, Dom newChild, Dom oldChild )
    {
        node_insertBefore( p, newChild, oldChild );
        node_removeChild( p, oldChild );

        return oldChild;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _node_insertBefore ( Dom p, Node newChild, Node refChild )
    {
        Locale l = p.locale();

        if (newChild == null)
            throw new NotFoundErr( "Child to add is null" );

        Dom nc;
        
        if (!(newChild instanceof Dom) || (nc = (Dom) newChild).locale() != l)
            throw new WrongDocumentErr( "Child to add is from another document" );

        Dom rc = null;

        if (refChild != null)
        {
            if (!(refChild instanceof Dom) || (rc = (Dom) refChild).locale() != l)
                throw new WrongDocumentErr( "Reference child is from another document" );
        }

        Dom d;

        if (l.noSync())         { l.enter(); try { d = node_insertBefore( p, nc, rc ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { d = node_insertBefore( p, nc, rc ); } finally { l.exit(); } }

        return (Node) d;
    }

    public static Dom node_insertBefore ( Dom p, Dom nc, Dom rc )
    {
        assert nc != null;

        if (rc != null && parent( rc ) != p)
            throw new NotFoundErr( "RefChild is not a child of this node" );

        // TODO - obey readonly status of a substree

        int nck = nc.nodeType();

        if (nck == DOCFRAG)
        {
            for ( Dom c = firstChild( nc ) ; c != null ; c = nextSibling( c ) )
                validateNewChild( p, c );

            for ( Dom c = firstChild( nc ) ; c != null ;  )
            {
                Dom n = nextSibling( c );
                insert( c, rc );
                c = n;
            }
            
            return nc;
        }

        //
        // Make sure the new child is allowed here
        //

        validateNewChild( p, nc );

        //
        // Orphan the child before establishing a new parent
        //

        remove( nc );
        
        int pk = p.nodeType();

        // Only these nodes can be modifiable parents
        assert pk == ATTR || pk == DOCFRAG || pk == DOCUMENT || pk == ELEMENT;

        switch ( nck )
        {
        case ELEMENT :
        case COMMENT :
        case PROCINST :
        {
            if (rc == null)
            {
                Cur cTo = p.tempCur();
                cTo.toEnd();
                Cur cFrom = nc.tempCur();
                cFrom.moveNode( cTo );
                cFrom.release();
                cTo.release();
            }
            else
            {
                int rck = rc.nodeType();

                if (rck == TEXT || rck == CDATA)
                {
                    // Quick and dirty impl....
                    
                    ArrayList charNodes = new ArrayList();
                    
                    while ( rc != null && (rc.nodeType() == TEXT || rc.nodeType() == CDATA ) )
                    {
                        Dom next = nextSibling( rc );
                        charNodes.add( remove( rc ) );
                        rc = next;
                    }

                    if (rc == null)
                        append( nc, p );
                    else
                        insert( nc, rc );

                    rc = nextSibling( nc );

                    for ( int i = 0 ; i < charNodes.size() ; i++ )
                    {
                        Dom n = (Dom) charNodes.get( i );

                        if (rc == null)
                            append( n, p );
                        else
                            insert( n, rc );
                    }
                }
                else if (rck == ENTITYREF)
                {
                    throw new RuntimeException( "Not implemented" );
                }
                else
                {
                    assert rck == ELEMENT || rck == PROCINST || rck == COMMENT;
                    Cur cFrom = nc.tempCur();
                    Cur cTo = rc.tempCur();
                    cFrom.moveNode( cTo );
                    cFrom.release();
                    cTo.release();
                }
            }

            break;
        }
        
        case TEXT :
        case CDATA :
        {
            CharNode n = (CharNode) nc;

            assert n._prev == null && n._next == null;

            CharNode refCharNode = null;
            Cur c = p.tempCur();
            
            if (rc == null)
                c.toEnd();
            else
            {
                int rck = rc.nodeType();
                
                if (rck == TEXT || rck == CDATA)
                    c.moveToCharNode( refCharNode = (CharNode) rc );
                else if (rck == ENTITYREF)
                    throw new RuntimeException( "Not implemented" );
                else
                    c.moveToDom( rc );
            }

            CharNode nodes = c.getCharNodes();

            nodes = CharNode.insertNode( nodes, n, refCharNode );

            c.insertChars( n._src, n._off, n._cch );

            c.setCharNodes( nodes );

            c.release();

            break;
        }

        case ENTITYREF :
        {
            throw new RuntimeException( "Not implemented" );
        }
            
        case DOCTYPE :
        {
            // TODO - don't actually insert this here, associate it with the
            // doc??  Hmm .. Perhaps I should disallow insertion into the tree
            // at all.
            
            throw new RuntimeException( "Not implemented" );
        }
            
        default : throw new RuntimeException( "Unexpected child node type" );
        }
        
        return nc;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _node_appendChild ( Dom p, Node newChild )
    {
        return _node_insertBefore( p, newChild, null );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _node_getParentNode ( Dom n )
    {
        Locale l = n.locale();

        Dom p;
        
        if (l.noSync())         { l.enter(); try { p = node_getParentNode( n ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { p = node_getParentNode( n ); } finally { l.exit(); } }

        return (Node) p;
    }

    public static Dom node_getParentNode ( Dom n )
    {
        Cur c = null;

        switch ( n.nodeType() )
        {
        case DOCUMENT :
        case DOCFRAG :
            break;
            
        case PROCINST :
        case COMMENT :
        case ELEMENT :
        case ATTR :
        {
            if (!(c = n.tempCur()).toParentRaw())
            {
                c.release();
                c = null;
            }

            break;
        }

        case TEXT :
        case CDATA :
        {
            if ((c = n.tempCur()) != null)
                c.toParent();

            break;
        }
            
        case ENTITYREF :
            throw new RuntimeException( "Not impl" );
            
        case ENTITY :
        case DOCTYPE :
        case NOTATION :
            throw new RuntimeException( "Not impl" );
            
        default : throw new RuntimeException( "Unknown kind" );
        }

        //
        // If the parent is a non DOM root (kind: ROOT), then I need to check
        // to see if there are multiple DOM nodes number this root and turn it
        // into a DOM fragment.
        //

        if (c != null && c.kind() == Cur.ROOT)
        {
            throw new RuntimeException( "Not impl" );
        }

        if (c == null)
            return null;
        
        Dom d = c.getDom();
        c.release();
        
        return d;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _node_getFirstChild ( Dom n )
    {
        Locale l = n.locale();

        Dom fc;
        
        if (l.noSync())         { l.enter(); try { fc = node_getFirstChild( n ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { fc = node_getFirstChild( n ); } finally { l.exit(); } }

        return (Node) fc;
    }

    public static Dom node_getFirstChild ( Dom n )
    {
        Dom fc = null;

        switch ( n.nodeType() )
        {
        case TEXT :
        case CDATA :
        case PROCINST :
        case COMMENT :
            break;
            
        case ENTITYREF :
            throw new RuntimeException( "Not impl" );
            
        case ENTITY :
        case DOCTYPE :
        case NOTATION :
            throw new RuntimeException( "Not impl" );
            
        case ELEMENT :
        case DOCUMENT :
        case DOCFRAG :
        case ATTR :
        {
            Cur c = n.tempCur();
            
            c.next();

            if ((fc = c.getCharNodes()) == null)
            {
                c.moveToDom( n );
                
                if (c.toFirstChild())
                    fc = c.getDom();
            }

            c.release();
            
            break;
        }
        }

        // TODO - handle entity refs here ...

        return fc;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _node_getLastChild ( Dom n )
    {
        Locale l = n.locale();

        Dom lc;

        if (l.noSync())         { l.enter(); try { lc = node_getLastChild( n ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { lc = node_getLastChild( n ); } finally { l.exit(); } }

        return (Node) lc;
    }
    
    public static Dom node_getLastChild ( Dom n )
    {
        switch ( n.nodeType() )
        {
            case TEXT :
            case CDATA :
            case PROCINST :
            case COMMENT :
                return null;

            case ENTITYREF :
                throw new RuntimeException( "Not impl" );

            case ENTITY :
            case DOCTYPE :
            case NOTATION :
                throw new RuntimeException( "Not impl" );

            case ELEMENT :
            case DOCUMENT :
            case DOCFRAG :
            case ATTR :
                break;
        }
        
        Dom lc = null;
        CharNode nodes;

        Cur c = n.tempCur();

        if (c.toLastChild())
        {
            lc = c.getDom();
            
            c.toEnd();
            c.next();

            if ((nodes = c.getCharNodes()) != null)
                lc = null;
        }
        else
        {
            c.next();
            nodes = c.getCharNodes();
        }

        if (lc == null && nodes != null)
        {
            while ( nodes._next != null )
                nodes = nodes._next;

            lc = nodes;
        }

        c.release();

        // TODO - handle entity refs here ...

        return lc;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _node_getNextSibling ( Dom n )
    {
        Locale l = n.locale();

        Dom ns;

        if (l.noSync())         { l.enter(); try { ns = node_getNextSibling( n ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { ns = node_getNextSibling( n ); } finally { l.exit(); } }

        return (Node) ns;
    }

    public static Dom node_getNextSibling ( Dom n )
    {
        Dom ns = null;

        switch ( n.nodeType() )
        {
        case DOCUMENT :
        case DOCFRAG :
        case ATTR :
            break;
            
        case TEXT :
        case CDATA :
        {
            CharNode cn = (CharNode) n;

            Cur c;

            if ((c = cn.tempCur()) != null)
            {
                if ((ns = cn._next) == null)
                {
                    if (c.kind() == Cur.TEXT)
                        c.next();

                    if (c.kind() > 0)
                        ns = c.getDom();
                }
                
                c.release();
            }
            
            break;
        }
            
        case PROCINST :
        case COMMENT :
        case ELEMENT :
        {
            Cur c = n.tempCur();

            c.toEnd();
            c.next();

            if ((ns = c.getCharNodes()) == null)
            {
                c.moveToDom( n );
                
                if (c.toNextSibling())
                    ns = c.getDom();
            }

            c.release();

            break;
        }
            
        case ENTITY :
        case NOTATION :
        case ENTITYREF :
        case DOCTYPE :
            throw new RuntimeException( "Not implemented" );
        }

        // TODO - handle entity refs here ...

        return ns;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _node_getPreviousSibling ( Dom n )
    {
        Locale l = n.locale();

        Dom ps;

        if (l.noSync())         { l.enter(); try { ps = node_getPreviousSibling( n ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { ps = node_getPreviousSibling( n ); } finally { l.exit(); } }

        return (Node) ps;
    }
    
    public static Dom node_getPreviousSibling ( Dom n )
    {
        // TODO - horribly inefficient impl .. make this O(1)

        Dom c = firstChild( parent( n ) );

        if (c == n)
            return null;

        for ( ; ; )
        {
            Dom ns = nextSibling( c );

            if (ns == n)
                return c;

            c = ns;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static String _node_getNodeValue ( Dom n )
    {
        Locale l = n.locale();

        if (l.noSync())         { l.enter(); try { return node_getNodeValue( n ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return node_getNodeValue( n ); } finally { l.exit(); } }
    }

    public static String node_getNodeValue ( Dom n )
    {
        String s = null;
        
        switch ( n.nodeType() )
        {
        case ATTR :
        case PROCINST :
        case COMMENT :
        {
            Cur c = n.tempCur();
            s = c.getValueString();
            c.release();

            break;
        }
            
        case TEXT :
        case CDATA :
        {
            CharNode node = (CharNode) n;

            Cur c;

            if ((c = node.tempCur()) == null)
                CharUtil.getString( node._src, node._off, node._cch );
            else
            {
                s = c.getString( node._cch );
                c.release();
            }

            break;
        }
        }
        
        return s;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _childNodes_item ( Dom n, int i )
    {
        Locale l = n.locale();

        Dom d;

        if (l.noSync())         { l.enter(); try { d = childNodes_item( n, i ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { d = childNodes_item( n, i ); } finally { l.exit(); } }

        return (Node) d;
    }
    
    public static Dom childNodes_item ( Dom n, int i )
    {
        if (i < 0)
            return null;
        
        switch ( n.nodeType() )
        {
            case TEXT :
            case CDATA :
            case PROCINST :
            case COMMENT :
                return null;

            case ENTITYREF :
                throw new RuntimeException( "Not impl" );

            case ENTITY :
            case DOCTYPE :
            case NOTATION :
                throw new RuntimeException( "Not impl" );

            case ELEMENT :
            case DOCUMENT :
            case DOCFRAG :
            case ATTR :
                break;
        }

        // TODO - make a couple of caches in master which can cache the
        // last child for a given index for a given node.  Need to so
        // one may iterate efficiently over to lists

        // *Really* inefficient impl for now

        for ( Dom c = node_getFirstChild( n ) ; c != null ; c = node_getNextSibling( c ) )
            if (i-- == 0)
                return c;

        return null;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static int _childNodes_getLength ( Dom n )
    {
        Locale l = n.locale();

        if (l.noSync())         { l.enter(); try { return childNodes_getLength( n ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return childNodes_getLength( n ); } finally { l.exit(); } }
    }
    
    public static int childNodes_getLength ( Dom n )
    {
        switch ( n.nodeType() )
        {
            case TEXT :
            case CDATA :
            case PROCINST :
            case COMMENT :
                return 0;

            case ENTITYREF :
                throw new RuntimeException( "Not impl" );

            case ENTITY :
            case DOCTYPE :
            case NOTATION :
                throw new RuntimeException( "Not impl" );

            case ELEMENT :
            case DOCUMENT :
            case DOCFRAG :
            case ATTR :
                break;
        }

        // TODO - make a couple of caches in master which can cache the
        // last child for a given index for a given node.  Need to so
        // one may iterate efficiently over to lists

        // *Really* inefficient impl for now

        int len = 0;

        for ( Dom c = node_getFirstChild( n ) ; c != null ; c = node_getNextSibling( c ) )
            len++;

        return len;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _attributes_setNamedItem ( Dom e, Node attr )
    {
        Locale l = e.locale();

        if (attr == null)
            throw new NotFoundErr( "Attr to set is null" );

        Dom a;
        
        if (!(attr instanceof Dom) || (a = (Dom) attr).locale() != l)
            throw new WrongDocumentErr( "Attr to set is from another document" );
        
        Dom oldA;

        if (l.noSync())         { l.enter(); try { oldA = attributes_setNamedItem( e, a ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { oldA = attributes_setNamedItem( e, a ); } finally { l.exit(); } }

        return (Node) oldA;
    }
    
    public static Dom attributes_setNamedItem ( Dom e, Dom a )
    {
        if (attr_getOwnerElement( a ) != null)
            throw new InuseAttributeError();

        if (a.nodeType() != ATTR)
            throw new HierarchyRequestErr( "Node is not an attribute" );

        String name = a.name();
        Dom oldAttr = null;

        Cur c = e.tempCur();

        if (c.toFirstAttr())
        {
            while ( c.isAttr() )
            {
                Dom aa = c.getDom();

                boolean hasNext = c.toNextSibling();
                
                if (aa.name().equals( name ))
                {
                    if (oldAttr == null)
                        oldAttr = aa;
                    else
                        removeNode( aa );
                }

                if (!hasNext)
                    break;
            }
        }

        Cur ac = a.tempCur();

        if (oldAttr == null)
        {
            c.moveToDom( e );
            
            if (!c.toFirstChild())
                c.toEnd();

            ac.moveNode( c );
        }
        else
        {
            c.moveToDom( oldAttr );
            ac.moveNode( c );
            removeNode( oldAttr );
        }

        c.release();
        ac.release();
        
        return oldAttr;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static String _attr_getName ( Dom e )
    {
        return e.name();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Element _attr_getOwnerElement ( Dom a )
    {
        Locale l = a.locale();

        Dom e;

        if (l.noSync())         { l.enter(); try { e = attr_getOwnerElement( a ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { e = attr_getOwnerElement( a ); } finally { l.exit(); } }

        return (Element) e;
    }

    public static Dom attr_getOwnerElement ( Dom n )
    {
        Cur c = n.tempCur();

        if (!c.toParentRaw())
            return null;

        Dom p = c.getDom();

        c.release();

        return p;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static String _attr_getValue ( Dom a )
    {
        return _node_getNodeValue( a );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static void _attr_setValue ( Dom a, String value )
    {
        _node_setNodeValue( a, value );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static String _node_getNodeName ( Dom n )
    {
        return n.name();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _attributes_item ( Dom e, int index )
    {
        Locale l = e.locale();

        Dom a;
        
        if (l.noSync())         { l.enter(); try { a = attributes_item( e, index ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { a = attributes_item( e, index ); } finally { l.exit(); } }

        return (Node) a;
    }
    
    public static Dom attributes_item ( Dom e, int index )
    {
        if (index < 0)
            return null;
        
        Cur c = e.tempCur();

        Dom a = null;

        if (c.toFirstAttr())
        {
            for ( ; ; )
            {
                if (index-- == 0)
                {
                    a = c.getDom();
                    break;
                }

                if (!c.toNextSibling())
                    break;
            }
        }

        c.release();

        return a;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static int _attributes_getLength ( Dom e )
    {
        Locale l = e.locale();

        if (l.noSync())         { l.enter(); try { return attributes_getLength( e ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return attributes_getLength( e ); } finally { l.exit(); } }
    }
    
    public static int attributes_getLength ( Dom e )
    {
        Cur c = e.tempCur();

        int n = 0;

        if (c.toFirstAttr())
        {
            for ( ; ; )
            {
                n++;
                
                if (!c.toNextSibling())
                    break;
            }
        }

        c.release();

        return n;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _attributes_getNamedItem ( Dom e, String name )
    {
        Locale l = e.locale();

        Dom n;

        if (l.noSync())         { l.enter(); try { n = attributes_getNamedItem( e, name ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { n = attributes_getNamedItem( e, name ); } finally { l.exit(); } }

        return (Node) n;
    }
    
    public static Dom attributes_getNamedItem ( Dom e, String name )
    {
        Dom a = null;

        Cur c = e.tempCur();

        if (c.toFirstAttr())
        {
            for ( ; ; )
            {
                Dom d = c.getDom();
                
                if (d.name().equals( name ))
                {
                    a = d;
                    break;
                }
                
                if (!c.toNextSibling())
                    break;
            }
        }

        c.release();

        return a;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _attributes_getNamedItemNS ( Dom e, String uri, String local )
    {
        Locale l = e.locale();

        Dom n;

        if (l.noSync())         { l.enter(); try { n = attributes_getNamedItemNS( e, uri, local ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { n = attributes_getNamedItemNS( e, uri, local ); } finally { l.exit(); } }

        return (Node) n;
    }
    
    public static Dom attributes_getNamedItemNS ( Dom e, String uri, String local )
    {
        Dom a = null;

        Cur c = e.tempCur();

        if (c.toFirstAttr())
        {
            for ( ; ; )
            {
                Dom d = c.getDom();

                QName n = d.qName();

                if (n.getNamespaceURI().equals( uri ) && n.getLocalPart().equals( local ))
                {
                    a = d;
                    break;
                }

                if (!c.toNextSibling())
                    break;
            }
        }

        c.release();

        return a;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _attributes_removeNamedItem ( Dom e, String name )
    {
        Locale l = e.locale();

        Dom n;

        if (l.noSync())         { l.enter(); try { n = attributes_removeNamedItem( e, name ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { n = attributes_removeNamedItem( e, name ); } finally { l.exit(); } }

        return (Node) n;
    }
    
    public static Dom attributes_removeNamedItem ( Dom e, String name )
    {
        Dom oldAttr = null;

        Cur c = e.tempCur();

        if (c.toFirstAttr())
        {
            while ( c.isAttr() )
            {
                Dom aa = c.getDom();

                boolean hasNext = c.toNextSibling();

                if (aa.name().equals( name ))
                {
                    if (oldAttr == null)
                        oldAttr = aa;
                    
                    removeNode( aa );
                }

                if (!hasNext)
                    break;
            }
        }
        
        c.release();

        if (oldAttr == null)
            throw new NotFoundErr( "Named item not found: " + name );

        return oldAttr;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _attributes_removeNamedItemNS ( Dom e, String uri, String local )
    {
        Locale l = e.locale();

        Dom n;

        if (l.noSync())         { l.enter(); try { n = attributes_removeNamedItemNS( e, uri, local ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { n = attributes_removeNamedItemNS( e, uri, local ); } finally { l.exit(); } }

        return (Node) n;
    }
    
    public static Dom attributes_removeNamedItemNS ( Dom e, String uri, String local )
    {
        Dom oldAttr = null;

        Cur c = e.tempCur();

        if (c.toFirstAttr())
        {
            while ( c.isAttr() )
            {
                Dom aa = c.getDom();

                boolean hasNext = c.toNextSibling();

                QName qn = aa.qName();

                if (qn.getNamespaceURI().equals( uri ) && qn.getLocalPart().equals( local ))
                {
                    if (oldAttr == null)
                        oldAttr = aa;
                    
                    removeNode( aa );
                }

                if (!hasNext)
                    break;
            }
        }

        c.release();

        if (oldAttr == null)
            throw new NotFoundErr( "Named item not found: uri=" + uri + ", local=" + local );

        return oldAttr;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _attributes_setNamedItemNS ( Dom e, Node attr )
    {
        Locale l = e.locale();

        if (attr == null)
            throw new NotFoundErr( "Attr to set is null" );

        Dom a;

        if (!(attr instanceof Dom) || (a = (Dom) attr).locale() != l)
            throw new WrongDocumentErr( "Attr to set is from another document" );

        Dom oldA;

        if (l.noSync())         { l.enter(); try { oldA = attributes_setNamedItemNS( e, a ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { oldA = attributes_setNamedItemNS( e, a ); } finally { l.exit(); } }

        return (Node) oldA;
    }
    
    public static Dom attributes_setNamedItemNS ( Dom e, Dom a )
    {
        if (attr_getOwnerElement( a ) != null)
            throw new InuseAttributeError();

        if (a.nodeType() != ATTR)
            throw new HierarchyRequestErr( "Node is not an attribute" );

        QName name = a.qName();
        Dom oldAttr = null;

        Cur c = e.tempCur();

        if (c.toFirstAttr())
        {
            while ( c.isAttr() )
            {
                Dom aa = c.getDom();

                boolean hasNext = c.toNextSibling();

                if (aa.name().equals( name ))
                {
                    if (oldAttr == null)
                        oldAttr = aa;
                    else
                        removeNode( aa );
                }

                if (!hasNext)
                    break;
            }
        }

        Cur ac = a.tempCur();

        if (oldAttr == null)
        {
            c.moveToDom( e );

            c.next();

            ac.moveNode( c );
        }
        else
        {
            c.moveToDom( oldAttr );
            ac.moveNode( c );
            removeNode( oldAttr );
        }

        c.release();
        ac.release();

        return oldAttr;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _node_cloneNode ( Dom n, boolean deep )
    {
        Locale l = n.locale();

        Dom c;

        if (l.noSync())         { l.enter(); try { c = node_cloneNode( n, deep, n.locale() ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { c = node_cloneNode( n, deep, n.locale() ); } finally { l.exit(); } }

        return (Node) c;
    }
    
    public static Dom node_cloneNode ( Dom n, boolean deep, Locale l )
    {
        if (!deep)
        {
            throw new RuntimeException( "Not impl" );
        }

        Dom clone;
        
        switch ( n.nodeType() )
        {
        case DOCUMENT :
        case DOCFRAG :
        case ATTR :
        case ELEMENT :
        case PROCINST :
        case COMMENT :
        {
            Cur cClone = l.tempCur();
            Cur cSrc = n.tempCur();
            cSrc.copyNode( cClone );
            clone = cClone.getDom();
            cClone.release();
            cSrc.release();

            break;
        }

        case TEXT :
        case CDATA :
        {
            Cur c = n.tempCur();
            
            CharNode cn = n.nodeType() == TEXT ? l.createTextNode() : l.createCdataNode();

            cn._src = c.getChars( ((CharNode) n)._cch );
            cn._off = c._offSrc;
            cn._cch = c._cchSrc;
                          
            clone = cn;
            
            c.release();
            
            break;
        }
            
        case ENTITYREF :
        case ENTITY :
        case DOCTYPE :
        case NOTATION :
            throw new RuntimeException( "Not impl" );
            
        default : throw new RuntimeException( "Unknown kind" );
        }

        return clone;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static String _node_getLocalName ( Dom n )
    {
        return n.qName().getLocalPart();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static String _node_getNamespaceURI ( Dom n )
    {
        // TODO - should return the correct namespace for xmlns ...
        return n.qName().getNamespaceURI();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static short _node_getNodeType ( Dom n )
    {
        return (short) n.nodeType();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static String _node_getPrefix ( Dom n )
    {
        return n.qName().getPrefix();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static boolean _node_hasAttributes ( Dom n )
    {
        Locale l = n.locale();

        if (l.noSync())         { l.enter(); try { return node_hasAttributes( n ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return node_hasAttributes( n ); } finally { l.exit(); } }
    }
    
    public static boolean node_hasAttributes ( Dom n )
    {
        boolean hasAttrs = false;
        
        if (n.nodeType() == ELEMENT)
        {
            Cur c = n.tempCur();
            
            if (c.toFirstAttr())
                hasAttrs = true;

            c.release();
        }

        return hasAttrs;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static boolean _node_hasChildNodes ( Dom n )
    {
        // TODO - make this faster
        return _node_getFirstChild( n ) != null;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static boolean _node_isSupported ( Dom n, String feature, String version )
    {
        throw new RuntimeException( "Not implemented" );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static void _node_normalize ( Dom n )
    {
        throw new RuntimeException( "Not implemented" );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static void _node_setNodeValue ( Dom n, String nodeValue )
    {
        Locale l = n.locale();

        if (l.noSync())         { l.enter(); try { node_setNodeValue( n, nodeValue ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { node_setNodeValue( n, nodeValue ); } finally { l.exit(); } }
    }
    
    public static void node_setNodeValue ( Dom n, String nodeValue )
    {
        switch ( n.nodeType() )
        {
            case TEXT :
            case CDATA :
            {
                CharNode cn = (CharNode) n;

                Cur c;

                if ((c = cn.tempCur()) != null)
                {
                    c.moveChars( null, cn._cch );
                    c.insertChars( nodeValue, 0, cn._cch = nodeValue.length() );
                    c.release();
                }
                else
                {
                    cn._src = nodeValue;
                    cn._off = 0;
                    cn._cch = nodeValue.length();
                }

                break;
            }
                
            case PROCINST :
            case COMMENT :
            case ATTR :
            {
                Cur c = n.tempCur();
                c.next();
                // What should I do with existing text nodes?
                //  - leave them there and lay the text over them?
                //  - orphan them iwth a copy of the text?
                //  - orphan them with no text?
                //  - text nodes suck
                c.getChars( -1 );
                c.moveChars( null, c._cchSrc );
                c.insertChars( nodeValue, 0, nodeValue.length() );
                c.release();

                break;
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _document_importNode ( Dom d, Node n, boolean deep )
    {
        Locale l = d.locale();
        Dom i;

//        // TODO - need to wrap this in sync ..
//        if (n instanceof Dom)
//            i = node_cloneNode( (Dom) n, deep, m );
//        else
// TODO -- I'm importing my own nodes through DOM methods!  -- make this faster
        {
            if (l.noSync())         { l.enter(); try { i = document_importNode( d, n, deep ); } finally { l.exit(); } }
            else synchronized ( l ) { l.enter(); try { i = document_importNode( d, n, deep ); } finally { l.exit(); } }
        }

        return (Node) i;
    }

    public static Dom document_importNode ( Dom d, Node n, boolean deep )
    {
        Dom i;

        boolean copyChildren = false;
        
        switch ( n.getNodeType() )
        {
        case DOCUMENT :
            throw new NotSupportedError( "Document nodes may not be imported" );

        case DOCTYPE :
            throw new NotSupportedError( "Document type nodes may not be imported" );

        case ELEMENT :
        {
            String local = n.getLocalName();

            if (local == null || local.length() == 0)
                i = document_createElement( d, n.getNodeName() );
            else
            {
                i = document_createElementNS( d, n.getNamespaceURI(), local );
                
                // TODO - unify creating element and setting prefix for perf ...

                String prefix = n.getPrefix();
                
                if (prefix != null)
                    node_setPrefix( i, prefix );
            }

            NamedNodeMap attrs = n.getAttributes();

            for ( int a = 0 ; a < attrs.getLength() ; a++ )
                attributes_setNamedItem( i, document_importNode( d, attrs.item( a ), true ) );

            copyChildren = deep;
            
            break;
        }

        case ATTR :
        {
            String local = n.getLocalName();

            if (local == null || local.length() == 0)
                i = document_createAttribute( d, n.getNodeName() );
            else
            {
                i = document_createAttributeNS( d, n.getNamespaceURI(), local );
                
                // TODO - unify creating attr and setting prefix for perf ...

                String prefix = n.getPrefix();
                
                if (prefix != null)
                    node_setPrefix( i, prefix );
            }

            copyChildren = true;
            
            break;
        }
        
        case DOCFRAG :
        {
            i = document_createDocumentFragment( d );
            
            copyChildren = deep;

            break;
        }
        
        case PROCINST :
        {
            i = document_createProcessingInstruction( d, n.getNodeName(), n.getNodeValue() );
            break;
        }
        
        case COMMENT :
        {
            i = document_createComment( d, n.getNodeValue() );
            break;
        }
        
        case TEXT :
        {
            i = document_createTextNode( d, n.getNodeValue() );
            break;
        }
        
        case CDATA :
        {
            i = document_createCDATASection( d, n.getNodeValue() );
            break;
        }
            
        case ENTITYREF :
        case ENTITY :
        case NOTATION :
            throw new RuntimeException( "Not impl" );

        default : throw new RuntimeException( "Unknown kind" );
        }

        if (copyChildren)
        {
            NodeList children = n.getChildNodes();
            
            for ( int c = 0 ; c < children.getLength() ; c++ )
                node_insertBefore( i, document_importNode( d, children.item( c ), true ), null);
        }

        return i;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static NodeList _document_getElementsByTagName ( Dom d, String name )
    {
        Locale l = d.locale();

        NodeList nl;

        if (l.noSync())         { l.enter(); try { nl = document_getElementsByTagName( d, name ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { nl = document_getElementsByTagName( d, name ); } finally { l.exit(); } }

        return nl;
    }
    
    public static NodeList document_getElementsByTagName ( Dom d, String name )
    {
        return new ElementsByTagNameNodeList( d, name );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static NodeList _document_getElementsByTagNameNS ( Dom d, String uri, String local )
    {
        Locale l = d.locale();

        NodeList nl;

        if (l.noSync())         { l.enter(); try { nl = document_getElementsByTagNameNS( d, uri, local ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { nl = document_getElementsByTagNameNS( d, uri, local ); } finally { l.exit(); } }

        return nl;
    }
    
    public static NodeList document_getElementsByTagNameNS ( Dom d, String uri, String local )
    {
        return new ElementsByTagNameNSNodeList( d, uri, local );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static void _characterData_appendData ( Dom cd, String arg )
    {
        // TODO - fix this *really* cheesy/bad/lousy perf impl
        //        also fix all the funcitons which follow

        if (arg == null || arg.length() == 0)
            _node_setNodeValue( cd, _node_getNodeValue( cd ) + arg );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static void _characterData_deleteData ( Dom c, int offset, int count )
    {
        String s = _characterData_getData( c );

        if (offset > s.length() || count < 0)
            throw new IndexSizeError();

        if (offset + count > s.length())
            count = s.length() - offset;

        if (count > 0)
            _characterData_setData( c, s.substring( 0, offset ) + s.substring( offset + count ) );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static String _characterData_getData ( Dom c )
    {
        return _node_getNodeValue( c );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static int _characterData_getLength ( Dom c )
    {
        return _characterData_getData( c ).length();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static void _characterData_insertData ( Dom c, int offset, String arg )
    {
        String s = _characterData_getData( c );
        
        if (offset > s.length())
            throw new IndexSizeError();

        if (arg != null && arg.length() > 0)
            _characterData_setData( c, s.substring( 0, offset ) + arg + s.substring( offset ) );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static void _characterData_replaceData ( Dom c, int offset, int count, String arg )
    {
        String s = _characterData_getData( c );

        if (offset > s.length() || count < 0)
            throw new IndexSizeError();

        if (offset + count > s.length())
            count = s.length() - offset;

        if (count > 0)
        {
            _characterData_setData(
                c, s.substring( 0, offset ) + arg + s.substring( offset + count ) );
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static void _characterData_setData ( Dom c, String data )
    {
        _node_setNodeValue( c, data );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static String _characterData_substringData ( Dom c, int offset, int count )
    {
        String s = _characterData_getData( c );

        if (offset > s.length() || count < 0)
            throw new IndexSizeError();

        if (offset + count > s.length())
            count = s.length() - offset;

        return s.substring( offset, offset + count );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Text _text_splitText ( Dom t, int offset )
    {
        assert t.nodeType() == TEXT;
        
        String s = _characterData_getData( t );

        if (offset > s.length())
            throw new IndexSizeError();

        _characterData_deleteData( t, offset, s.length() - offset );

        // Don't need to pass a doc here, any node will do..
        
        Dom t2 = (Dom) _document_createTextNode( t, s.substring( offset ) );

        Dom p = (Dom) _node_getParentNode( t );

        if (p != null)
            _node_insertBefore( p, (Text) t2, _node_getNextSibling( t ) );

        return (Text) t2;
    }












    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static void _node_setPrefix ( Dom n, String prefix )
    {
        Locale l = n.locale();

        if (l.noSync())         { l.enter(); try { node_setPrefix( n, prefix ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { node_setPrefix( n, prefix ); } finally { l.exit(); } }
    }
    
    public static void node_setPrefix ( Dom n, String prefix )
    {
        // TODO - make it possible to set the prefix of an xmlns
        // TODO - test to make use prefix: xml maps to the predefined namespace
        // if set???? hmmm ... perhaps I should not allow the setting of any
        // prefixes which start with xml unless the namespace is the predefined
        // one and the prefix is 'xml' all other prefixes which start with
        // 'xml' should fail.

        if (n.nodeType() == ELEMENT || n.nodeType() == ATTR)
        {
            Cur c = n.tempCur();
            QName name = c.getName();
            // TODO - make sure the prefix is not null, contains proper chars,
            // etc
            c.setName( n.locale().makeQName( name.getNamespaceURI(), name.getLocalPart(), prefix ) );
            c.release();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static CDATASection _document_createCDATASection ( Dom d, String data )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public static Dom document_createCDATASection ( Dom d, String data )
    {
        throw new RuntimeException( "Not implemented" );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static EntityReference _document_createEntityReference ( Dom d, String name )
    {
        throw new RuntimeException( "Not implemented" );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static ProcessingInstruction _document_createProcessingInstruction ( Dom d, String target, String data )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public static Dom document_createProcessingInstruction ( Dom d, String target, String data )
    {
        throw new RuntimeException( "Not implemented" );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Element _document_getElementById ( Dom d, String elementId )
    {
        throw new RuntimeException( "Not implemented" );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static boolean _attr_getSpecified ( Dom a )
    {
        throw new RuntimeException( "Not implemented" );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static String _processingInstruction_getData ( Dom p )
    {
        throw new RuntimeException( "Not implemented" );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static String _processingInstruction_getTarget ( Dom p )
    {
        throw new RuntimeException( "Not implemented" );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static void _processingInstruction_setData ( Dom p, String data )
    {
        throw new RuntimeException( "Not implemented" );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    
    public static XMLStreamReader _getXmlStreamReader ( Dom n )
    {
        Locale l = n.locale();

        if (l.noSync())         { l.enter(); try { return getXmlStreamReader( n ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return getXmlStreamReader( n ); } finally { l.exit(); } }
    }
    
    public static XMLStreamReader getXmlStreamReader ( Dom n )
    {
        XMLStreamReader xs;
        
        switch ( n.nodeType() )
        {
        case DOCUMENT :
        case DOCFRAG :
        case ATTR :
        case ELEMENT :
        case PROCINST :
        case COMMENT :
        {
            Cur c = n.tempCur();
            xs = Jsr173.newXmlStreamReader( c );
            c.release();
            break;
        }
            
        case TEXT :
        case CDATA :
        {
            CharNode cn = (CharNode) n;

            Cur c;

            if ((c = cn.tempCur()) == null)
            {
                c = n.locale().tempCur();
                
                xs = Jsr173.newXmlStreamReader( c, cn._src, cn._off, cn._cch );
            }
            else
            {
                xs =
                    Jsr173.newXmlStreamReader(
                        c , c.getChars( cn._cch ), c._offSrc, c._cchSrc );
                
            }

            c.release();
            
            break;
        }
            
        case ENTITYREF :
        case ENTITY :
        case DOCTYPE :
        case NOTATION :
            throw new RuntimeException( "Not impl" );
            
        default : throw new RuntimeException( "Unknown kind" );
        }

        return xs;
    }


    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static XmlCursor _getXmlCursor ( Dom n )
    {
        Locale l = n.locale();

        if (l.noSync())         { l.enter(); try { return getXmlCursor( n ); } finally { l.exit(); } }
        else synchronized ( l ) { l.enter(); try { return getXmlCursor( n ); } finally { l.exit(); } }
    }

    public static XmlCursor getXmlCursor ( Dom n )
    {
        Cur c = n.tempCur();

        Cursor xc = new Cursor( c );

        c.release();

        return xc;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //

    private abstract static class ElementsNodeList implements NodeList
    {
        ElementsNodeList ( Dom root )
        {
            assert root.nodeType() == DOCUMENT || root.nodeType() == ELEMENT;

            _root = root;
            _locale = _root.locale();
            _version = 0;
        }

        public int getLength ( )
        {
            ensureElements();

            return _elements.size();
        }
        
        public Node item ( int i )
        {
            ensureElements();

            return i < 0 || i >= _elements.size() ? (Node) null : (Node) _elements.get( i );
        }
        
        private void ensureElements ( )
        {
            if (_version == _locale.version())
                return;

            _version = _locale.version();

            _elements = new ArrayList();

            Locale l = _locale;

            if (l.noSync())         { l.enter(); try { addElements( _root ); } finally { l.exit(); } }
            else synchronized ( l ) { l.enter(); try { addElements( _root ); } finally { l.exit(); } }
        }

        private void addElements ( Dom node )
        {
            for ( Dom c = firstChild( node ) ; c != null ; c = nextSibling( c ) )
            {
                if (c.nodeType() == ELEMENT)
                {
                    if (match( c ))
                        _elements.add( c );

                    addElements( c );
                }
            }
        }

        protected abstract boolean match ( Dom element );

        private Dom       _root;
        private Locale    _locale;
        private long      _version;
        private ArrayList _elements;
    }

    private static class ElementsByTagNameNodeList extends ElementsNodeList
    {
        ElementsByTagNameNodeList ( Dom root, String name )
        {
            super( root );

            _name = name;
        }

        protected boolean match ( Dom element )
        {
            return _name.equals( "*" ) ? true : _node_getNodeName( element ).equals( _name );
        }

        private String _name;
    }
    
    private static class ElementsByTagNameNSNodeList extends ElementsNodeList
    {
        ElementsByTagNameNSNodeList ( Dom root, String uri, String local )
        {
            super( root );

            _uri = uri == null ? "" : uri;
            _local = local;
        }

        protected boolean match ( Dom element )
        {
            if (!(_uri.equals( "*" ) ? true : _node_getNamespaceURI( element ).equals( _uri )))
                return false;

            return _local.equals( "*" ) ? true : _node_getLocalName( element ).equals( _local );
        }

        private String _uri;
        private String _local;
    }
    
    private static final class EmptyNodeList implements NodeList
    {
        public int getLength ( ) { return 0; }
        public Node item ( int i ) { return null; }
    }

    public static NodeList _emptyNodeList = new EmptyNodeList();
    
    public static class SaajTextNode extends TextNode implements javax.xml.soap.Text
    {
        public SaajTextNode ( Locale l )
        {
            super( l );
        }

        public boolean isComment ( ) { return false; }
        
        public void detachNode ( ) { SaajImpl._soapNode_detachNode( this ); }
        public void recycleNode ( ) { SaajImpl._soapNode_recycleNode( this ); }
        public String getValue ( ) { return SaajImpl._soapNode_getValue( this ); }
        public void setValue ( String value ) { SaajImpl._soapNode_setValue( this, value ); }
        public SOAPElement getParentElement ( ) { return SaajImpl._soapNode_getParentElement( this ); }
        public void setParentElement ( SOAPElement p ) { SaajImpl._soapNode_setParentElement( this, p ); }
    }
    
    public static class SaajCdataNode extends CdataNode implements javax.xml.soap.Text
    {
        public SaajCdataNode ( Locale l )
        {
            super( l );
        }

        public boolean isComment ( ) { return false; }
        
        public void detachNode ( ) { SaajImpl._soapNode_detachNode( this ); }
        public void recycleNode ( ) { SaajImpl._soapNode_recycleNode( this ); }
        public String getValue ( ) { return SaajImpl._soapNode_getValue( this ); }
        public void setValue ( String value ) { SaajImpl._soapNode_setValue( this, value ); }
        public SOAPElement getParentElement ( ) { return SaajImpl._soapNode_getParentElement( this ); }
        public void setParentElement ( SOAPElement p ) { SaajImpl._soapNode_setParentElement( this, p ); }
    }
}
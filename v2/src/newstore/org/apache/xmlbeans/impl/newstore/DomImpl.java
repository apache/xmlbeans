package org.apache.xmlbeans.impl.newstore;

import javax.xml.stream.XMLStreamReader;

import javax.xml.namespace.QName;

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.newstore.xcur.Master;
import org.apache.xmlbeans.impl.newstore.xcur.Xcur;

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

import org.apache.xmlbeans.impl.newstore.SaajImpl;

import org.apache.xmlbeans.impl.newstore.Jsr173;

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

    public interface Dom
    {
        Master master    ( );
        int    nodeType  ( );
        Xcur   tempCur   ( );

        String name      ( );
        QName  qName     ( );

//        Dom    altParent ( ); // TODO - have a setAltParent

        void dump ( PrintStream o );
        void dump ( );

        // These will simply delegate to DomImpl non-gateway methods
        Dom firstChild  ( );
        Dom nextSibling ( );
        Dom parent      ( );
        Dom remove      ( );
        Dom insert      ( Dom b );
        Dom append      ( Dom p );
    }

    //
    //
    //

    public static Dom domAppend ( Dom n, Dom p )
    {
        node_insertBefore( p, n, null );

        return n;
    }

    public static Dom domInsert ( Dom n, Dom b )
    {
        node_insertBefore( b.parent(), n, b );

        return n;
    }

    public static Dom domRemove ( Dom n )
    {
        Dom p = n.parent();

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

        while ( (child = child.parent()) != null )
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
        Xcur xTo = n.tempCur();
        Xcur xFrom = n.tempCur();

        xFrom.toEnd();
        xFrom.next();

        CharNode fromNodes = xFrom.getCharNodes();

        if (fromNodes != null)
            xTo.setCharNodes( CharNode.appendNodes( xTo.getCharNodes(), fromNodes ) );

        xTo.moveNode( null );

        xTo.release();
        xFrom.release();
    }

    public static TextNode createTextNode ( Master m )
    {
        return m._saaj == null ? new TextNode( m ) : new SaajTextNode( m );
    }
    
    public static CdataNode createCdataNode ( Master m )
    {
        return m._saaj == null ? new CdataNode( m ) : new SaajCdataNode( m );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Document _domImplementation_createDocument (
        Master m, String u, String n, DocumentType t )
    {
        Document d;

        try
        {
            if (m.noSync())         { m.enter(); d = domImplementation_createDocument( m, u, n, t ); }
            else synchronized ( m ) { m.enter(); d = domImplementation_createDocument( m, u, n, t ); }
        }
        finally
        {
            m.exit();
        }

        return d;
    }

    public static Document domImplementation_createDocument (
        Master m, String namespaceURI, String qualifiedName, DocumentType doctype )
    {
        Xcur x = m.tempCur();
        
        x.createRoot( Xcur.DOMDOC );

        Dom d = x.getDom();
        
        x.next();
        
        x.createElement( m.makeQualifiedQName( namespaceURI, qualifiedName ) );
        
        if (doctype != null)
            throw new RuntimeException( "Not impl" );

        x.release();
        
        return (Document) d;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static DocumentType _domImplementation_createDocumentType (
        Master m, String qname, String publicId, String systemId )
    {
        throw new RuntimeException( "Not impl" );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static boolean _domImplementation_hasFeature (
        Master m, String feature, String version )
    {
        throw new RuntimeException( "Not impl" );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static DOMImplementation _document_getImplementation ( Dom d )
    {
        return (DOMImplementation) d.master();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Element _document_createElementNS ( Dom d, String uri, String qname )
    {
        Master m = d.master();

        Dom e;

        try
        {
            if (m.noSync())         { m.enter(); e = document_createElementNS( d, uri, qname ); }
            else synchronized ( m ) { m.enter(); e = document_createElementNS( d, uri, qname ); }
        }
        finally
        {
            m.exit();
        }

        return (Element) e;
    }

    public static Dom document_createElementNS ( Dom d, String uri, String qname )
    {
        // TODO - validate the name here
        Master m = d.master();
        
        Xcur x = m.tempCur();
        
        x.createElement( m.makeQualifiedQName( uri, qname ) );

        Dom e = x.getDom();
        
        x.release();
        
        return e;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Element _document_createElement ( Dom d, String name )
    {
        Master m = d.master();

        Dom e;

        try
        {
            if (m.noSync())         { m.enter(); e = document_createElement( d, name ); }
            else synchronized ( m ) { m.enter(); e = document_createElement( d, name ); }
        }
        finally
        {
            m.exit();
        }

        return (Element) e;
    }

    public static Dom document_createElement ( Dom d, String name )
    {
        // TODO - validate the name here
        
        Master m = d.master();

        Xcur x = m.tempCur();

        x.createElement( m.makeQualifiedQName( "", name ) );

        Dom e = x.getDom();

        x.release();

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
        TextNode t = createTextNode( d.master() );

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
        Master m = d.master();

        Dom c;

        try
        {
            if (m.noSync())         { m.enter(); c = document_createComment( d, data ); }
            else synchronized ( m ) { m.enter(); c = document_createComment( d, data ); }
        }
        finally
        {
            m.exit();
        }

        return (Comment) c;
    }
    
    public static Dom document_createComment ( Dom d, String data )
    {
        Master m = d.master();

        Xcur x = m.tempCur();

        x.createComment();

        Dom c = x.getDom();

        if (data != null)
        {
            x.next();
            x.insertChars( data, 0, data.length() );
        }

        x.release();

        return c;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static DocumentFragment _document_createDocumentFragment ( Dom d )
    {
        Master m = d.master();

        Dom f;

        try
        {
            if (m.noSync())         { m.enter(); f = document_createDocumentFragment( d ); }
            else synchronized ( m ) { m.enter(); f = document_createDocumentFragment( d ); }
        }
        finally
        {
            m.exit();
        }

        return (DocumentFragment) f;
    }
    
    public static Dom document_createDocumentFragment ( Dom d )
    {
        Xcur x = d.master().tempCur();

        x.createRoot( Xcur.DOMFRAG );

        Dom f = x.getDom();
        
        x.release();

        return f;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Attr _document_createAttribute ( Dom d, String name )
    {
        Master m = d.master();

        Dom a;

        try
        {
            if (m.noSync())         { m.enter(); a = document_createAttribute( d, name ); }
            else synchronized ( m ) { m.enter(); a = document_createAttribute( d, name ); }
        }
        finally
        {
            m.exit();
        }

        return (Attr) a;
    }

    public static Dom document_createAttribute ( Dom d, String name )
    {
        // TODO - validate the name here

        Master m = d.master();

        Xcur x = m.tempCur();

        x.createAttr( m.makeQualifiedQName( "", name ) );

        Dom e = x.getDom();

        x.release();

        return e;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Attr _document_createAttributeNS ( Dom d, String uri, String qname )
    {
        Master m = d.master();

        Dom a;

        try
        {
            if (m.noSync())         { m.enter(); a = document_createAttributeNS( d, uri, qname ); }
            else synchronized ( m ) { m.enter(); a = document_createAttributeNS( d, uri, qname ); }
        }
        finally
        {
            m.exit();
        }

        return (Attr) a;
    }
    
    public static Dom document_createAttributeNS ( Dom d, String uri, String qname )
    {
        // TODO - validate the name here

        Master m = d.master();

        Xcur x = m.tempCur();

        x.createAttr( m.makeQualifiedQName( uri, qname ) );

        Dom e = x.getDom();

        x.release();

        return e;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    
    public static Element _document_getDocumentElement ( Dom d )
    {
        Master m = d.master();

        Dom e;

        try
        {
            if (m.noSync())         { m.enter(); e = document_getDocumentElement( d ); }
            else synchronized ( m ) { m.enter(); e = document_getDocumentElement( d ); }
        }
        finally
        {
            m.exit();
        }

        return (Element) e;
    }

    public static Dom document_getDocumentElement ( Dom d )
    {
        for ( d = d.firstChild() ; d != null ; d = d.nextSibling() )
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
        Master m = n.master();

        Dom d;

        try
        {
            if (m.noSync())         { m.enter(); d = node_getOwnerDocument( n ); }
            else synchronized ( m ) { m.enter(); d = node_getOwnerDocument( n ); }
        }
        finally
        {
            m.exit();
        }

        return (Document) d;
    }
    
    public static Dom node_getOwnerDocument ( Dom n )
    {
        Master m = n.master();

        if (m._ownerDoc == null)
        {
            Xcur x = m.tempCur();
            x.createRoot( Xcur.DOMDOC );
            m._ownerDoc = x.getDom();
            x.release();
        }

        return m._ownerDoc;
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

        Master m = e.master();

        try
        {
            if (m.noSync())         { m.enter(); element_setAttribute( e, name, value ); }
            else synchronized ( m ) { m.enter(); element_setAttribute( e, name, value ); }
        }
        finally
        {
            m.exit();
        }
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
        Master m = e.master();

        try
        {
            if (m.noSync())         { m.enter(); element_setAttributeNS( e, uri, local, value ); }
            else synchronized ( m ) { m.enter(); element_setAttributeNS( e, uri, local, value ); }
        }
        finally
        {
            m.exit();
        }
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

    public static Node _node_removeChild ( Dom p, Node child )
    {
        Master m = p.master();

        if (child == null)
            throw new NotFoundErr( "Child to remove is null" );

        Dom c;
        
        if (!(child instanceof Dom) || (c = (Dom) child).master() != m)
            throw new WrongDocumentErr( "Child to remove is from another document" );

        Dom d;

        try
        {
            if (m.noSync())         { m.enter(); d = node_removeChild( p, c ); }
            else synchronized ( m ) { m.enter(); d = node_removeChild( p, c ); }
        }
        finally
        {
            m.exit();
        }

        return (Node) d;
    }

    public static Dom node_removeChild ( Dom p, Dom c )
    {
        if (c.parent() != p)
            throw new NotFoundErr( "Child to remove is not a child of given parent" );
        
        switch ( c.nodeType() )
        {
        case DOCUMENT :
        case DOCFRAG :
        case ATTR :
            throw new IllegalStateException();
            
        case ELEMENT :
        case PROCINST :
        case COMMENT :
            removeNode( c );
            break;

        case TEXT :
        case CDATA :
        {
            Xcur x = c.tempCur();
            
            CharNode nodes = x.getCharNodes();

            CharNode cn = (CharNode) c;

            assert cn._src instanceof Dom;

            cn.setText( x.moveChars( null, cn._cch ), x._offSrc, x._cchSrc );
            
            x.setCharNodes( CharNode.remove( nodes, cn ) );

            x.release();

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

        return c;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _node_replaceChild ( Dom p, Node newChild, Node oldChild )
    {
        Master m = p.master();

        if (newChild == null)
            throw new NotFoundErr( "Child to add is null" );

        Dom nc;

        if (!(newChild instanceof Dom) || (nc = (Dom) newChild).master() != m)
            throw new WrongDocumentErr( "Child to add is from another document" );

        Dom oc = null;

        if (!(oldChild instanceof Dom) || (oc = (Dom) oldChild).master() != m)
            throw new WrongDocumentErr( "Chidl to replace is from another document" );

        Dom d;

        try
        {
            if (m.noSync())         { m.enter(); d = node_insertBefore( p, nc, oc ); }
            else synchronized ( m ) { m.enter(); d = node_insertBefore( p, nc, oc ); }
        }
        finally
        {
            m.exit();
        }

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
        Master m = p.master();

        if (newChild == null)
            throw new NotFoundErr( "Child to add is null" );

        Dom nc;
        
        if (!(newChild instanceof Dom) || (nc = (Dom) newChild).master() != m)
            throw new WrongDocumentErr( "Child to add is from another document" );

        Dom rc = null;

        if (refChild != null)
        {
            if (!(refChild instanceof Dom) || (rc = (Dom) refChild).master() != m)
                throw new WrongDocumentErr( "Reference child is from another document" );
        }

        Dom d;

        try
        {
            if (m.noSync())         { m.enter(); d = node_insertBefore( p, nc, rc ); }
            else synchronized ( m ) { m.enter(); d = node_insertBefore( p, nc, rc ); }
        }
        finally
        {
            m.exit();
        }

        return (Node) d;
    }

    public static Dom node_insertBefore ( Dom p, Dom nc, Dom rc )
    {
        assert nc != null;

        if (rc != null && rc.parent() != p)
            throw new NotFoundErr( "RefChild is not a child of this node" );

        // TODO - obey readonly status of a substree

        int nck = nc.nodeType();

        if (nck == DOCFRAG)
        {
            for ( Dom c = nc.firstChild() ; c != null ; c = c.nextSibling() )
                validateNewChild( p, c );

            for ( Dom c = nc.firstChild() ; c != null ;  )
            {
                Dom n = c.nextSibling();
                c.insert( rc );
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

        nc.remove();
        
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
                Xcur xTo = p.tempCur();
                xTo.toEnd();
                Xcur xFrom = nc.tempCur();
                xFrom.moveNode( xTo );
                xFrom.release();
                xTo.release();
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
                        Dom next = rc.nextSibling();
                        charNodes.add( rc.remove() );
                        rc = next;
                    }

                    if (rc == null)
                        nc.append( p );
                    else
                        nc.insert( rc );

                    rc = nc.nextSibling();

                    for ( int i = 0 ; i < charNodes.size() ; i++ )
                    {
                        Dom n = (Dom) charNodes.get( i );

                        if (rc == null)
                            n.append( p );
                        else
                            n.insert( rc );
                    }
                }
                else if (rck == ENTITYREF)
                {
                    throw new RuntimeException( "Not implemented" );
                }
                else
                {
                    assert rck == ELEMENT || rck == PROCINST || rck == COMMENT;
                    Xcur xFrom = nc.tempCur();
                    Xcur xTo = rc.tempCur();
                    xFrom.moveNode( xTo );
                    xFrom.release();
                    xTo.release();
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
            Xcur xp = p.tempCur();
            
            if (rc == null)
                xp.toEnd();
            else
            {
                int rck = rc.nodeType();
                
                if (rck == TEXT || rck == CDATA)
                    xp.moveToCharNode( refCharNode = (CharNode) rc );
                else if (rck == ENTITYREF)
                    throw new RuntimeException( "Not implemented" );
                else
                    xp.moveToDom( rc );
            }

            CharNode nodes = xp.getCharNodes();

            nodes = CharNode.insertNode( nodes, n, refCharNode );

            xp.insertChars( n._src, n._off, n._cch );

            xp.setCharNodes( nodes );

            xp.release();

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
        Master m = n.master();

        Dom p;
        
        try
        {
            if (m.noSync())         { m.enter(); p = node_getParentNode( n ); }
            else synchronized ( m ) { m.enter(); p = node_getParentNode( n ); }
        }
        finally
        {
            m.exit();
        }

        return (Node) p;
    }

    public static Dom node_getParentNode ( Dom n )
    {
//        Dom ap = n.altParent();
//
//        if (ap != null)
//            return ap;
        
        Xcur x = null;

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
            if (!(x = n.tempCur()).toParentRaw())
            {
                x.release();
                x = null;
            }

            break;
        }

        case TEXT :
        case CDATA :
        {
            if ((x = n.tempCur()) != null)
                x.toParent();

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

        if (x != null && x.kind() == Xcur.ROOT)
        {
            throw new RuntimeException( "Not impl" );
        }

        if (x == null)
            return null;
        
        Dom d = x.getDom();
        x.release();
        
        return d;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _node_getFirstChild ( Dom n )
    {
        Master m = n.master();

        Dom fc;
        
        try
        {
            if (m.noSync())         { m.enter(); fc = node_getFirstChild( n ); }
            else synchronized ( m ) { m.enter(); fc = node_getFirstChild( n ); }
        }
        finally
        {
            m.exit();
        }

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
            Xcur x = n.tempCur();
            
            x.next();

            if ((fc = x.getCharNodes()) == null)
            {
                x.moveToDom( n );
                
                if (x.toFirstChild())
                    fc = x.getDom();
            }

            x.release();
            
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
        Master m = n.master();

        Dom lc;

        try
        {
            if (m.noSync())         { m.enter(); lc = node_getLastChild( n ); }
            else synchronized ( m ) { m.enter(); lc = node_getLastChild( n ); }
        }
        finally
        {
            m.exit();
        }

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

        Xcur x = n.tempCur();

        if (x.toLastChild())
        {
            lc = x.getDom();
            
            x.toEnd();
            x.next();

            if ((nodes = x.getCharNodes()) != null)
                lc = null;
        }
        else
        {
            x.next();
            nodes = x.getCharNodes();
        }

        if (lc == null && nodes != null)
        {
            while ( nodes._next != null )
                nodes = nodes._next;

            lc = nodes;
        }

        x.release();

        // TODO - handle entity refs here ...

        return lc;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _node_getNextSibling ( Dom n )
    {
        Master m = n.master();

        Dom ns;

        try
        {
            if (m.noSync())         { m.enter(); ns = node_getNextSibling( n ); }
            else synchronized ( m ) { m.enter(); ns = node_getNextSibling( n ); }
        }
        finally
        {
            m.exit();
        }

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
            CharNode c = (CharNode) n;

            Xcur x;

            if ((x = c.tempCur()) != null)
            {
                if ((ns = c._next) == null)
                {
                    if (x.kind() == Xcur.TEXT)
                        x.next();

                    if (x.kind() > 0)
                        ns = x.getDom();
                }
                
                x.release();
            }
            
            break;
        }
            
        case PROCINST :
        case COMMENT :
        case ELEMENT :
        {
            Xcur x = n.tempCur();

            x.toEnd();
            x.next();

            if ((ns = x.getCharNodes()) == null)
            {
                x.moveToDom( n );
                
                if (x.toNextSibling())
                    ns = x.getDom();
            }

            x.release();

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
        Master m = n.master();

        Dom ps;

        try
        {
            if (m.noSync())         { m.enter(); ps = node_getPreviousSibling( n ); }
            else synchronized ( m ) { m.enter(); ps = node_getPreviousSibling( n ); }
        }
        finally
        {
            m.exit();
        }

        return (Node) ps;
    }
    
    public static Dom node_getPreviousSibling ( Dom n )
    {
        // TODO - horribly inefficient impl .. make this O(1)

        Dom c = n.parent().firstChild();

        if (c == n)
            return null;

        for ( ; ; )
        {
            Dom ns = c.nextSibling();

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
        Master m = n.master();

        try
        {
            if (m.noSync())         { m.enter(); return node_getNodeValue( n ); }
            else synchronized ( m ) { m.enter(); return node_getNodeValue( n ); }
        }
        finally
        {
            m.exit();
        }
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
            Xcur x = n.tempCur();
            s = x.getValueString();
            x.release();

            break;
        }
            
        case TEXT :
        case CDATA :
        {
            CharNode node = (CharNode) n;

            Xcur x;

            if ((x = node.tempCur()) == null)
                s = node.stringValue();
            else
            {
                s = x.getString( node._cch );
                x.release();
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
        Master m = n.master();

        Dom d;

        try
        {
            if (m.noSync())         { m.enter(); d = childNodes_item( n, i ); }
            else synchronized ( m ) { m.enter(); d = childNodes_item( n, i ); }
        }
        finally
        {
            m.exit();
        }

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
        Master m = n.master();

        try
        {
            if (m.noSync())         { m.enter(); return childNodes_getLength( n ); }
            else synchronized ( m ) { m.enter(); return childNodes_getLength( n ); }
        }
        finally
        {
            m.exit();
        }
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

        int l = 0;

        for ( Dom c = node_getFirstChild( n ) ; c != null ; c = node_getNextSibling( c ) )
            l++;

        return l;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _attributes_setNamedItem ( Dom e, Node attr )
    {
        Master m = e.master();

        if (attr == null)
            throw new NotFoundErr( "Attr to set is null" );

        Dom a;
        
        if (!(attr instanceof Dom) || (a = (Dom) attr).master() != m)
            throw new WrongDocumentErr( "Attr to set is from another document" );
        
        Dom oldA;

        try
        {
            if (m.noSync())         { m.enter(); oldA = attributes_setNamedItem( e, a ); }
            else synchronized ( m ) { m.enter(); oldA = attributes_setNamedItem( e, a ); }
        }
        finally
        {
            m.exit();
        }

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

        Xcur x = e.tempCur();

        if (x.toFirstAttr())
        {
            while ( x.isAttr() )
            {
                Dom aa = x.getDom();

                boolean hasNext = x.toNextSibling();
                
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

        Xcur ax = a.tempCur();

        if (oldAttr == null)
        {
            x.moveToDom( e );
            
            if (!x.toFirstChild())
                x.toEnd();

            ax.moveNode( x );
        }
        else
        {
            x.moveToDom( oldAttr );
            ax.moveNode( x );
            removeNode( oldAttr );
        }

        x.release();
        ax.release();
        
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
        Master m = a.master();

        Dom e;

        try
        {
            if (m.noSync())         { m.enter(); e = attr_getOwnerElement( a ); }
            else synchronized ( m ) { m.enter(); e = attr_getOwnerElement( a ); }
        }
        finally
        {
            m.exit();
        }

        return (Element) e;
    }

    public static Dom attr_getOwnerElement ( Dom n )
    {
        Xcur x = n.tempCur();

        if (!x.toParentRaw())
            return null;

        Dom p = x.getDom();

        x.release();

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
        Master m = e.master();

        Dom a;
        
        try
        {
            if (m.noSync())         { m.enter(); a = attributes_item( e, index ); }
            else synchronized ( m ) { m.enter(); a = attributes_item( e, index ); }
        }
        finally
        {
            m.exit();
        }

        return (Node) a;
    }
    
    public static Dom attributes_item ( Dom e, int index )
    {
        if (index < 0)
            return null;
        
        Xcur x = e.tempCur();

        Dom a = null;

        if (x.toFirstAttr())
        {
            for ( ; ; )
            {
                if (index-- == 0)
                {
                    a = x.getDom();
                    break;
                }

                if (!x.toNextSibling())
                    break;
            }
        }

        x.release();

        return a;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static int _attributes_getLength ( Dom e )
    {
        Master m = e.master();

        try
        {
            if (m.noSync())         { m.enter(); return attributes_getLength( e ); }
            else synchronized ( m ) { m.enter(); return attributes_getLength( e ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static int attributes_getLength ( Dom e )
    {
        Xcur x = e.tempCur();

        int n = 0;

        if (x.toFirstAttr())
        {
            for ( ; ; )
            {
                n++;
                
                if (!x.toNextSibling())
                    break;
            }
        }

        x.release();

        return n;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _attributes_getNamedItem ( Dom e, String name )
    {
        Master m = e.master();

        Dom n;

        try
        {
            if (m.noSync())         { m.enter(); n = attributes_getNamedItem( e, name ); }
            else synchronized ( m ) { m.enter(); n = attributes_getNamedItem( e, name ); }
        }
        finally
        {
            m.exit();
        }

        return (Node) n;
    }
    
    public static Dom attributes_getNamedItem ( Dom e, String name )
    {
        Dom a = null;

        Xcur x = e.tempCur();

        if (x.toFirstAttr())
        {
            for ( ; ; )
            {
                Dom d = x.getDom();
                
                if (d.name().equals( name ))
                {
                    a = d;
                    break;
                }
                
                if (!x.toNextSibling())
                    break;
            }
        }

        x.release();

        return a;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _attributes_getNamedItemNS ( Dom e, String uri, String local )
    {
        Master m = e.master();

        Dom n;

        try
        {
            if (m.noSync())         { m.enter(); n = attributes_getNamedItemNS( e, uri, local ); }
            else synchronized ( m ) { m.enter(); n = attributes_getNamedItemNS( e, uri, local ); }
        }
        finally
        {
            m.exit();
        }

        return (Node) n;
    }
    
    public static Dom attributes_getNamedItemNS ( Dom e, String uri, String local )
    {
        Dom a = null;

        Xcur x = e.tempCur();

        if (x.toFirstAttr())
        {
            for ( ; ; )
            {
                Dom d = x.getDom();

                QName n = d.qName();

                if (n.getNamespaceURI().equals( uri ) && n.getLocalPart().equals( local ))
                {
                    a = d;
                    break;
                }

                if (!x.toNextSibling())
                    break;
            }
        }

        x.release();

        return a;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _attributes_removeNamedItem ( Dom e, String name )
    {
        Master m = e.master();

        Dom n;

        try
        {
            if (m.noSync())         { m.enter(); n = attributes_removeNamedItem( e, name ); }
            else synchronized ( m ) { m.enter(); n = attributes_removeNamedItem( e, name ); }
        }
        finally
        {
            m.exit();
        }

        return (Node) n;
    }
    
    public static Dom attributes_removeNamedItem ( Dom e, String name )
    {
        Dom oldAttr = null;

        Xcur x = e.tempCur();

        if (x.toFirstAttr())
        {
            while ( x.isAttr() )
            {
                Dom aa = x.getDom();

                boolean hasNext = x.toNextSibling();

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
        
        x.release();

        if (oldAttr == null)
            throw new NotFoundErr( "Named item not found: " + name );

        return oldAttr;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _attributes_removeNamedItemNS ( Dom e, String uri, String local )
    {
        Master m = e.master();

        Dom n;

        try
        {
            if (m.noSync())         { m.enter(); n = attributes_removeNamedItemNS( e, uri, local ); }
            else synchronized ( m ) { m.enter(); n = attributes_removeNamedItemNS( e, uri, local ); }
        }
        finally
        {
            m.exit();
        }

        return (Node) n;
    }
    
    public static Dom attributes_removeNamedItemNS ( Dom e, String uri, String local )
    {
        Dom oldAttr = null;

        Xcur x = e.tempCur();

        if (x.toFirstAttr())
        {
            while ( x.isAttr() )
            {
                Dom aa = x.getDom();

                boolean hasNext = x.toNextSibling();

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

        x.release();

        if (oldAttr == null)
            throw new NotFoundErr( "Named item not found: uri=" + uri + ", local=" + local );

        return oldAttr;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _attributes_setNamedItemNS ( Dom e, Node attr )
    {
        Master m = e.master();

        if (attr == null)
            throw new NotFoundErr( "Attr to set is null" );

        Dom a;

        if (!(attr instanceof Dom) || (a = (Dom) attr).master() != m)
            throw new WrongDocumentErr( "Attr to set is from another document" );

        Dom oldA;

        try
        {
            if (m.noSync())         { m.enter(); oldA = attributes_setNamedItemNS( e, a ); }
            else synchronized ( m ) { m.enter(); oldA = attributes_setNamedItemNS( e, a ); }
        }
        finally
        {
            m.exit();
        }

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

        Xcur x = e.tempCur();

        if (x.toFirstAttr())
        {
            while ( x.isAttr() )
            {
                Dom aa = x.getDom();

                boolean hasNext = x.toNextSibling();

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

        Xcur ax = a.tempCur();

        if (oldAttr == null)
        {
            x.moveToDom( e );

            x.next();

            ax.moveNode( x );
        }
        else
        {
            x.moveToDom( oldAttr );
            ax.moveNode( x );
            removeNode( oldAttr );
        }

        x.release();
        ax.release();

        return oldAttr;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _node_cloneNode ( Dom n, boolean deep )
    {
        Master m = n.master();

        Dom c;

        try
        {
            if (m.noSync())         { m.enter(); c = node_cloneNode( n, deep, n.master() ); }
            else synchronized ( m ) { m.enter(); c = node_cloneNode( n, deep, n.master() ); }
        }
        finally
        {
            m.exit();
        }

        return (Node) c;
    }
    
    public static Dom node_cloneNode ( Dom n, boolean deep, Master tm )
    {
        Master sm = n.master();
        
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
            Xcur xClone = tm.tempCur();
            Xcur xSrc = n.tempCur();
            xSrc.copyNode( xClone );
            clone = xClone.getDom();
            xClone.release();
            xSrc.release();

            break;
        }

        case TEXT :
        case CDATA :
        {
            Xcur x = n.tempCur();
            
            CharNode cn = n.nodeType() == TEXT ? createTextNode( tm ) : createCdataNode( tm );

            cn._src = x.getChars( ((CharNode) n)._cch );
            cn._off = x._offSrc;
            cn._cch = x._cchSrc;
                          
            clone = cn;
            
            x.release();
            
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
        Master m = n.master();

        try
        {
            if (m.noSync())         { m.enter(); return node_hasAttributes( n ); }
            else synchronized ( m ) { m.enter(); return node_hasAttributes( n ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static boolean node_hasAttributes ( Dom n )
    {
        boolean hasAttrs = false;
        
        if (n.nodeType() == ELEMENT)
        {
            Xcur x = n.tempCur();
            
            if (x.toFirstAttr())
                hasAttrs = true;

            x.release();
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
        Master m = n.master();

        try
        {
            if (m.noSync())         { m.enter(); node_setNodeValue( n, nodeValue ); }
            else synchronized ( m ) { m.enter(); node_setNodeValue( n, nodeValue ); }
        }
        finally
        {
            m.exit();
        }
    }
    
    public static void node_setNodeValue ( Dom n, String nodeValue )
    {
        switch ( n.nodeType() )
        {
            case TEXT :
            case CDATA :
            {
                CharNode cn = (CharNode) n;

                Xcur x;

                if ((x = cn.tempCur()) != null)
                {
                    x.moveChars( null, cn._cch );
                    x.insertChars( nodeValue, 0, cn._cch = nodeValue.length() );
                    x.release();
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
                Xcur x = n.tempCur();
                x.next();
                // What should I do with existing text nodes?
                //  - leave them there and lay the text over them?
                //  - orphan them iwth a copy of the text?
                //  - orphan them with no text?
                //  - text nodes suck
                x.getChars( -1 );
                x.moveChars( null, x._cchSrc );
                x.insertChars( nodeValue, 0, nodeValue.length() );
                x.release();

                break;
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static Node _document_importNode ( Dom d, Node n, boolean deep )
    {
        Master m = d.master();
        Dom i;

//        // TODO - need to wrap this in sync ..
//        if (n instanceof Dom)
//            i = node_cloneNode( (Dom) n, deep, m );
//        else
// TODO -- I'm importing my own nodes through DOM methods!  -- make this faster
        {
            try
            {
                if (m.noSync())         { m.enter(); i = document_importNode( d, n, deep ); }
                else synchronized ( m ) { m.enter(); i = document_importNode( d, n, deep ); }
            }
            finally
            {
                m.exit();
            }
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
        Master m = n.master();

        try
        {
            if (m.noSync())         { m.enter(); node_setPrefix( n, prefix ); }
            else synchronized ( m ) { m.enter(); node_setPrefix( n, prefix ); }
        }
        finally
        {
            m.exit();
        }
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
            Xcur x = n.tempCur();
            QName name = x.getName();
            // TODO - make sure the prefix is not null, contains proper chars,
            // etc
            x.setName( n.master().makeQName( name.getNamespaceURI(), name.getLocalPart(), prefix ) );
            x.release();
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

    public static NodeList _document_getElementsByTagName ( Dom d, String tagname )
    {
        throw new RuntimeException( "Not implemented" );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static NodeList _document_getElementsByTagNameNS ( Dom d, String namespaceURI, String localName )
    {
        throw new RuntimeException( "Not implemented" );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static NodeList _element_getElementsByTagName ( Dom e, String name )
    {
        throw new RuntimeException( "Not implemented" );
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////

    public static NodeList _element_getElementsByTagNameNS ( Dom e, String namespaceURI, String localName )
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
        Master m = n.master();

        try
        {
            if (m.noSync())         { m.enter(); return getXmlStreamReader( n ); }
            else synchronized ( m ) { m.enter(); return getXmlStreamReader( n ); }
        }
        finally
        {
            m.exit();
        }
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
            Xcur x = n.tempCur();
            xs = Jsr173.newXmlStreamReader( x );
            x.release();
            break;
        }
            
        case TEXT :
        case CDATA :
        {
            CharNode cn = (CharNode) n;

            Xcur x;

            if ((x = cn.tempCur()) == null)
            {
                x = n.master().tempCur();
                
                xs = Jsr173.newXmlStreamReader( x, cn._src, cn._off, cn._cch );
            }
            else
            {
                xs =
                    Jsr173.newXmlStreamReader(
                        x , x.getChars( cn._cch ), x._offSrc, x._cchSrc );
                
            }

            x.release();
            
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

    //
    //
    //

    private static class EmptyNodeList implements NodeList
    {
        public int getLength ( ) { return 0; }
        public Node item ( int i ) { return null; }
    }

    public static NodeList _emptyNodeList = new EmptyNodeList();
    
    public abstract static class CharNode implements Dom
    {
        CharNode ( Master m )
        {
            _master = m;
        }

//        CharNode ( Master m, int cch )
//        {
//            this( m );
//            _cch = cch;
//        }

        public static CharNode remove ( CharNode nodes, CharNode node )
        {
            if (nodes == node)
                nodes = node._next;
            else
            {
                node._prev._next = node._next;
                
                if (node._next != null)
                    node._next = node._next._prev;
            }

            node._prev = node._next = null;
            
            return nodes;
        }

        public static CharNode insertNode ( CharNode nodes, CharNode newNode, CharNode before )
        {
            assert newNode != null;
            assert newNode._prev == null && newNode._next == null;
            
            if (nodes == null)
            {
                assert before == null;
                nodes = newNode;
            }
            else if (nodes == before)
            {
                nodes._prev = newNode;
                newNode._next = nodes;
                nodes = newNode;
            }
            else
            {
                CharNode n = nodes;

                while ( n._next != before )
                    n = n._next;

                if ((newNode._next = n._next) != null)
                    n._next._prev = newNode;
                
                newNode._prev = n;
                n._next = newNode;
            }

            return nodes;
        }
        
        public static CharNode appendNode ( CharNode nodes, CharNode newNode )
        {
            return insertNode( nodes, newNode, null );
        }
        
        public static CharNode appendNodes ( CharNode nodes, CharNode newNodes )
        {
            assert newNodes != null;
            assert newNodes._prev == null;

            if (nodes == null)
                return newNodes;

            CharNode n = nodes;

            while ( n._next != null )
                n = n._next;

            n._next = newNodes;
            newNodes._prev = n;

            return nodes;
        }

        public static CharNode copyNodes ( CharNode nodes, Object newSrc )
        {
            CharNode newNodes = null;

            for ( CharNode n = null ; nodes != null ; nodes = nodes._next )
            {
                CharNode newNode;

                if (nodes instanceof TextNode)
                    newNode = createTextNode( nodes._master );
                else
                    newNode = createCdataNode( nodes._master );

                // How to deal with entity refs??

                newNode._src = newSrc;
                newNode._off = nodes._off;
                newNode._cch = nodes._cch;

                if (newNodes == null)
                    newNodes = newNode;

                if (n != null)
                {
                    n._next = newNode;
                    newNode._prev = n;
                }
                
                n = newNode;
            }

            return newNodes;
        }

        public String stringValue ( )
        {
            assert _src instanceof String || _src instanceof char[];
            
            String s;
            
            if (_src instanceof String)
            {
                s = (String) _src;
                
                if (_off != 0 || _cch != s.length())
                    s = s.substring( _off, _off + _cch );
            }
            else
            {
                _src = s = new String( (char[]) _src, _off, _cch );
                _off = 0;
            }

            return s;
        }

        public void dump ( PrintStream o )
        {
            o.print( "CharNode: \"" + stringValue() + "\"" );
        }

        public void dump ( )
        {
            dump( System.out );
        }

        public Master master    ( ) { return _master; }
        
//        public Dom    altParent ( ) { return _altParent; }
        
        public Xcur tempCur ( )
        {
            Xcur x;
            
            if (_src instanceof Dom)
            {
                x = _master.tempCur();
                x.moveToCharNode( this );
            }
            else
                x = null;

            return x;
        }

        public void setText ( Object src, int off, int cch )
        {
            _src = src;
            _off = off;
            _cch = cch;
        }
        
        public QName qName ( ) { throw new IllegalStateException(); }
        
        public Dom firstChild  ( )       { return DomImpl.node_getFirstChild ( this ); }
        public Dom nextSibling ( )       { return DomImpl.node_getNextSibling( this ); }
        public Dom parent      ( )       { return DomImpl.node_getParentNode( this );  }
        public Dom remove      ( )       { return DomImpl.domRemove( this );           }
        public Dom insert      ( Dom b ) { return DomImpl.domInsert( this, b );        }
        public Dom append      ( Dom p ) { return DomImpl.domAppend( this, p );        }

        public Node appendChild ( Node newChild ) { return DomImpl._node_appendChild( this, newChild ); }
        public Node cloneNode ( boolean deep ) { return DomImpl._node_cloneNode( this, deep ); }
        public NamedNodeMap getAttributes ( ) { return null; }
        public NodeList getChildNodes ( ) { return _emptyNodeList; }
        public Node getParentNode ( ) { return DomImpl._node_getParentNode( this ); }
        public Node removeChild ( Node oldChild ) { return DomImpl._node_removeChild( this, oldChild ); }
        public Node getFirstChild ( ) { return DomImpl._node_getFirstChild( this ); }
        public Node getLastChild ( ) { return DomImpl._node_getLastChild( this ); }
        public String getLocalName ( ) { return DomImpl._node_getLocalName( this ); }
        public String getNamespaceURI ( ) { return DomImpl._node_getNamespaceURI( this ); }
        public Node getNextSibling ( ) { return DomImpl._node_getNextSibling( this ); }
        public String getNodeName ( ) { return DomImpl._node_getNodeName( this ); }
        public short getNodeType ( ) { return DomImpl._node_getNodeType( this ); }
        public String getNodeValue ( ) { return DomImpl._node_getNodeValue( this ); }
        public Document getOwnerDocument ( ) { return DomImpl._node_getOwnerDocument( this ); }
        public String getPrefix ( ) { return DomImpl._node_getPrefix( this ); }
        public Node getPreviousSibling ( ) { return DomImpl._node_getPreviousSibling( this ); }
        public boolean hasAttributes ( ) { return DomImpl._node_hasAttributes( this ); }
        public boolean hasChildNodes ( ) { return DomImpl._node_hasChildNodes( this ); }
        public Node insertBefore ( Node newChild, Node refChild ) { return DomImpl._node_insertBefore( this, newChild, refChild ); }
        public boolean isSupported ( String feature, String version ) { return DomImpl._node_isSupported( this, feature, version ); }
        public void normalize ( ) { DomImpl._node_normalize( this ); }
        public Node replaceChild ( Node newChild, Node oldChild ) { return DomImpl._node_replaceChild( this, newChild, oldChild ); }
        public void setNodeValue ( String nodeValue ) { DomImpl._node_setNodeValue( this, nodeValue ); }
        public void setPrefix ( String prefix ) { DomImpl._node_setPrefix( this, prefix ); }
        
        public void appendData ( String arg ) { DomImpl._characterData_appendData( this, arg ); }
        public void deleteData ( int offset, int count ) { DomImpl._characterData_deleteData( this, offset, count ); }
        public String getData ( ) { return DomImpl._characterData_getData( this ); }
        public int getLength ( ) { return DomImpl._characterData_getLength( this ); }
        public void insertData ( int offset, String arg ) { DomImpl._characterData_insertData( this, offset, arg ); }
        public void replaceData ( int offset, int count, String arg ) { DomImpl._characterData_replaceData( this, offset, count, arg ); }
        public void setData ( String data ) { DomImpl._characterData_setData( this, data ); }
        public String substringData ( int offset, int count ) { return DomImpl._characterData_substringData( this, offset, count ); }
        
        private final Master _master;
        private       Dom    _altParent;
        
        public Object _src;
        public int    _off;
        public int    _cch;

        public CharNode _next;
        public CharNode _prev;
    }
    
    public static class TextNode extends CharNode implements Text
    {
        public TextNode ( Master m )
        {
            super( m );
        }
        
//        public TextNode ( Master m, int cch )
//        {
//            super( m, cch );
//        }
        
        public int nodeType ( ) { return DomImpl.TEXT; }
        
        public String name ( ) { return "#text"; }
        
        public Text splitText ( int offset ) { return DomImpl._text_splitText ( this, offset ); }
    }
    
    public static class CdataNode extends TextNode implements CDATASection
    {
        public CdataNode ( Master m )
        {
            super( m );
        }
        
//        public CdataNode ( Master m, int cch )
//        {
//            super( m, cch );
//        }
        
        public int nodeKind ( ) { return DomImpl.CDATA; }
        
        public String name ( ) { return "#cdata-section"; }
    }
    
    public static class SaajTextNode extends TextNode implements javax.xml.soap.Text
    {
        SaajTextNode ( Master m )
        {
            super( m );
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
        SaajCdataNode ( Master m )
        {
            super( m );
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
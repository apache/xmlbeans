package org.apache.xmlbeans.impl.newstore.pub.store;

import javax.xml.namespace.QName;
import java.io.PrintStream;

import org.apache.xmlbeans.impl.newstore.DomImpl;
import org.apache.xmlbeans.impl.newstore.CharUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.CharacterData;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.CDATASection;
import org.w3c.dom.NodeList;

public interface Dom
{
    Locale locale   ( );
    int    nodeType ( );
    Cur    tempCur  ( );
    String name     ( );
    QName  qName    ( );

    void dump ( PrintStream o );
    void dump ( );
    
    //
    //
    //

    public static abstract class CharNode implements Dom, Node, CharacterData
    {
        public CharNode ( Locale l )
        {
            _locale = l;
        }
        
        public Locale locale ( )
        {
            return _locale;
        }

        public QName qName ( ) { throw new IllegalStateException(); }

        public Cur tempCur ( )
        {
            Cur c;

            if (_src instanceof Dom)
            {
                c = _locale.tempCur();
                c.moveToCharNode( this );
            }
            else
                c = null;

            return c;
        }

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
                    newNode = nodes._locale.createTextNode();
                else
                    newNode = nodes._locale.createCdataNode();

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

        public void dump ( PrintStream o )
        {
            o.print( "CharNode: \"" + CharUtil.getString( _src, _off, _cch ) + "\"" );
        }

        public void dump ( )
        {
            dump( System.out );
        }

        public Node appendChild ( Node newChild ) { return DomImpl._node_appendChild( this, newChild ); }
        public Node cloneNode ( boolean deep ) { return DomImpl._node_cloneNode( this, deep ); }
        public NamedNodeMap getAttributes ( ) { return null; }
        public NodeList getChildNodes ( ) { return DomImpl._emptyNodeList; }
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

        public Locale _locale;

        public CharNode _next;
        public CharNode _prev;

        public Object _src;
        public int    _off;
        public int    _cch;
    }
    
    public static class TextNode extends CharNode implements Text
    {
        public TextNode ( Locale l )
        {
            super( l );
        }

        public int nodeType ( ) { return DomImpl.TEXT; }

        public String name ( ) { return "#text"; }

        public Text splitText ( int offset ) { return DomImpl._text_splitText ( this, offset ); }
    }

    public static class CdataNode extends TextNode implements CDATASection
    {
        public CdataNode ( Locale l )
        {
            super( l );
        }

        public int nodeKind ( ) { return DomImpl.CDATA; }

        public String name ( ) { return "#cdata-section"; }
    }
}

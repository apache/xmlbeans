package org.apache.xmlbeans.impl.newstore;

import javax.xml.namespace.QName;

import javax.xml.stream.XMLStreamReader;

import weblogic.xml.stream.XMLInputStream;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlDocumentProperties;

import java.util.Map;
import java.util.Collection;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.File;
import java.io.IOException;

import org.w3c.dom.Node;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.SAXException;

public class Cursor implements XmlCursor
{
    public Object monitor ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public XmlDocumentProperties documentProperties ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public XmlCursor newCursor ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public XMLStreamReader newXMLStreamReader ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public XMLStreamReader newXMLStreamReader ( XmlOptions options )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public XMLInputStream newXMLInputStream ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public String xmlText ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public InputStream newInputStream ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public Reader newReader ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public Node newDomNode ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void save ( ContentHandler ch, LexicalHandler lh ) throws SAXException
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void save ( File file ) throws IOException
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void save ( OutputStream os ) throws IOException
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void save ( Writer w ) throws IOException
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public XMLInputStream newXMLInputStream ( XmlOptions options )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public String xmlText ( XmlOptions options )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public InputStream newInputStream ( XmlOptions options )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public Reader newReader( XmlOptions options )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public Node newDomNode ( XmlOptions options )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void save ( ContentHandler ch, LexicalHandler lh, XmlOptions options ) throws SAXException
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void save ( File file, XmlOptions options ) throws IOException
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void save ( OutputStream os, XmlOptions options ) throws IOException
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void save ( Writer w, XmlOptions options ) throws IOException
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void dispose ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toCursor ( XmlCursor moveTo )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void push ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean pop ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void selectPath ( String path )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void selectPath ( String path, XmlOptions options )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean hasNextSelection ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toNextSelection ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toSelection ( int i )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public int getSelectionCount ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void addToSelection ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void clearSelections ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toBookmark ( XmlBookmark bookmark )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public XmlBookmark toNextBookmark ( Object key )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public XmlBookmark toPrevBookmark ( Object key )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public QName getName ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void setName ( QName name )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public String namespaceForPrefix ( String prefix )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public String prefixForNamespace ( String namespaceURI )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void getAllNamespaces ( Map addToThis )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public XmlObject getObject ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public TokenType currentTokenType ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean isStartdoc ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean isEnddoc ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean isStart ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean isEnd ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean isText ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean isAttr ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean isNamespace ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean isComment ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean isProcinst ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean isContainer ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean isFinish ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean isAnyAttr ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public TokenType prevTokenType ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean hasNextToken ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean hasPrevToken ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public TokenType toNextToken ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public TokenType toPrevToken ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public TokenType toFirstContentToken ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public TokenType toEndToken ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public int toNextChar ( int maxCharacterCount )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public int toPrevChar ( int maxCharacterCount )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toNextSibling ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toPrevSibling ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toParent ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toFirstChild ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toLastChild ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toChild ( String name )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toChild ( String namespace, String name )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toChild ( QName name )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toChild ( int index )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toChild ( QName name, int index )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toNextSibling ( String name )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toNextSibling ( String namespace, String name )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toNextSibling ( QName name )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toFirstAttribute ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toLastAttribute ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toNextAttribute ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean toPrevAttribute ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public String getAttributeText ( QName attrName )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean setAttributeText ( QName attrName, String value )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean removeAttribute ( QName attrName )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public String getTextValue ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public int getTextValue ( char[] returnedChars, int offset, int maxCharacterCount )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void setTextValue ( String text )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void setTextValue ( char[] sourceChars, int offset, int length )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public String getChars ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public int getChars ( char[] returnedChars, int offset, int maxCharacterCount )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void toStartDoc ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void toEndDoc ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean isInSameDocument ( XmlCursor cursor )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public int comparePosition ( XmlCursor cursor )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean isLeftOf ( XmlCursor cursor )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean isAtSamePositionAs ( XmlCursor cursor )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean isRightOf ( XmlCursor cursor )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public XmlCursor execQuery ( String query )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public XmlCursor execQuery ( String query, XmlOptions options )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public ChangeStamp getDocChangeStamp ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void setBookmark ( XmlBookmark bookmark )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public XmlBookmark getBookmark ( Object key )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void clearBookmark ( Object key )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void getAllBookmarkRefs ( Collection listToFill )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean removeXml ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean moveXml ( XmlCursor toHere )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean copyXml ( XmlCursor toHere )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean removeXmlContents ( )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean moveXmlContents ( XmlCursor toHere )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public boolean copyXmlContents ( XmlCursor toHere )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public int removeChars ( int maxCharacterCount )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public int moveChars ( int maxCharacterCount, XmlCursor toHere )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public int copyChars ( int maxCharacterCount, XmlCursor toHere )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void insertChars ( String text )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void insertElement ( QName name )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void insertElement ( String localName )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void insertElement ( String localName, String uri )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void beginElement ( QName name )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void beginElement ( String localName )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void beginElement ( String localName, String uri )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void insertElementWithText ( QName name, String text )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void insertElementWithText ( String localName, String text )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void insertElementWithText ( String localName, String uri, String text )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void insertAttribute ( String localName )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void insertAttribute ( String localName, String uri )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void insertAttribute ( QName name )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void insertAttributeWithValue ( String Name, String value )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void insertAttributeWithValue ( String name, String uri, String value )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void insertAttributeWithValue ( QName name, String value )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void insertNamespace ( String prefix, String namespace )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void insertComment ( String text )
    {
        throw new RuntimeException( "Not implemented" );
    }
    
    public void insertProcInst ( String target, String text )
    {
        throw new RuntimeException( "Not implemented" );
    }
}
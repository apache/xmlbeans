/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
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
 * originally based on software copyright (c) 2001, Sun
 * Microsystems., http://www.sun.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * @author G. Todd Miller
 */

package org.apache.xmlbeans.impl.common;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Comment;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Stack;
import java.util.Vector;

public class Sax2Dom
        extends DefaultHandler
        implements ContentHandler, LexicalHandler
{
    public static final String EMPTYSTRING = "";
    public static final String XML_PREFIX = "xml";
    public static final String XMLNS_PREFIX = "xmlns";
    public static final String XMLNS_STRING = "xmlns:";
    public static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";

    private Node _root = null;
    private Document _document = null;
    private Stack _nodeStk = new Stack();
    private Vector _namespaceDecls = null;

    public Sax2Dom() throws ParserConfigurationException
    {
        final DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        _document = factory.newDocumentBuilder().newDocument();
        _root = _document;
    }

    public Sax2Dom(Node root) throws ParserConfigurationException
    {
        _root = root;
        if (root instanceof Document)
        {
            _document = (Document) root;
        }
        else if (root != null)
        {
            _document = root.getOwnerDocument();
        }
        else
        {
            final DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            _document = factory.newDocumentBuilder().newDocument();
            _root = _document;
        }
    }

    public Node getDOM()
    {
        return _root;
    }

    public void characters(char[] ch, int start, int length)
    {
        final Node last = (Node) _nodeStk.peek();

        // No text nodes can be children of root (DOM006 exception)
        if (last != _document)
        {
            final String text = new String(ch, start, length);
            last.appendChild(_document.createTextNode(text));
        }
    }

    public void startDocument()
    {
        _nodeStk.push(_root);
    }

    public void endDocument()
    {
        _nodeStk.pop();
    }

    public void startElement(String namespace, String localName, String qName,
                             Attributes attrs)
    {
        final Element tmp = (Element) _document.createElementNS(namespace, qName);

        // Add namespace declarations first
        if (_namespaceDecls != null)
        {
            final int nDecls = _namespaceDecls.size();
            for (int i = 0; i < nDecls; i++)
            {
                final String prefix = (String) _namespaceDecls.elementAt(i++);

                if (prefix == null || prefix.equals(EMPTYSTRING))
                {
                    tmp.setAttributeNS(XMLNS_URI, XMLNS_PREFIX,
                            (String) _namespaceDecls.elementAt(i));
                }
                else
                {
                    tmp.setAttributeNS(XMLNS_URI, XMLNS_STRING + prefix,
                            (String) _namespaceDecls.elementAt(i));
                }
            }
            _namespaceDecls.clear();
        }

        // Add attributes to element
        final int nattrs = attrs.getLength();
        for (int i = 0; i < nattrs; i++)
        {
            if (attrs.getLocalName(i) == null)
            {
                tmp.setAttribute(attrs.getQName(i), attrs.getValue(i));
            }
            else
            {
                tmp.setAttributeNS(attrs.getURI(i), attrs.getQName(i),
                        attrs.getValue(i));
            }
        }

        // Append this new node onto current stack node
        Node last = (Node) _nodeStk.peek();
        last.appendChild(tmp);

        // Push this node onto stack
        _nodeStk.push(tmp);
    }

    public void endElement(String namespace, String localName, String qName)
    {
        _nodeStk.pop();
    }

    public void startPrefixMapping(String prefix, String uri)
    {
        if (_namespaceDecls == null)
        {
            _namespaceDecls = new Vector(2);
        }
        _namespaceDecls.addElement(prefix);
        _namespaceDecls.addElement(uri);
    }

    public void endPrefixMapping(String prefix)
    {
        // do nothing
    }

    /**
     * This class is only used internally so this method should never
     * be called.
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
    {
    }

    /**
     * adds processing instruction node to DOM.
     */
    public void processingInstruction(String target, String data)
    {
        final Node last = (Node) _nodeStk.peek();
        ProcessingInstruction pi = _document.createProcessingInstruction(
                target, data);
        if (pi != null) last.appendChild(pi);
    }

    /**
     * This class is only used internally so this method should never
     * be called.
     */
    public void setDocumentLocator(Locator locator)
    {
    }

    /**
     * This class is only used internally so this method should never
     * be called.
     */
    public void skippedEntity(String name)
    {
    }


    /**
     * Lexical Handler method to create comment node in DOM tree.
     */
    public void comment(char[] ch, int start, int length)
    {
        final Node last = (Node) _nodeStk.peek();
        Comment comment = _document.createComment(new String(ch, start, length));
        if (comment != null) last.appendChild(comment);
    }

    // Lexical Handler methods- not implemented
    public void startCDATA()
    {
    }

    public void endCDATA()
    {
    }

    public void startEntity(java.lang.String name)
    {
    }

    public void endEntity(String name)
    {
    }

    public void startDTD(String name, String publicId, String systemId)
            throws SAXException
    {
    }

    public void endDTD()
    {
    }
}

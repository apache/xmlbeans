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

package org.apache.xmlbeans.impl.xpath.jaxen;
import org.jaxen.XPath;
import org.jaxen.Navigator;
import org.jaxen.DefaultNavigator;
import org.jaxen.FunctionCallException;
import org.jaxen.UnsupportedAxisException;
//import org.jaxen.NamedAccessNavigator;
import org.jaxen.util.AncestorAxisIterator;
import org.jaxen.util.FollowingSiblingAxisIterator;
import org.jaxen.util.PrecedingSiblingAxisIterator;
import org.jaxen.util.FollowingAxisIterator;
import org.jaxen.util.PrecedingAxisIterator;
import org.jaxen.util.SelfAxisIterator;
import org.jaxen.util.DescendantOrSelfAxisIterator;
import org.jaxen.util.AncestorOrSelfAxisIterator;
import org.jaxen.util.SingleObjectIterator;
import org.jaxen.util.DescendantAxisIterator;
import org.jaxen.saxpath.SAXPathException;

import org.apache.xmlbeans.XmlCursor;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * Author: Cezar Andrei (cezar.andrei at bea.com)
 * Date: Oct 10, 2003
 */
public class XBeansNavigator
    extends DefaultNavigator
//    implements NamedAccessNavigator
{
    private XmlCursor _xc;

    private XBeansNavigator()
    {}

    /** Retrieve an instance of this <code>DocumentNavigator</code>.
     */
    public static Navigator getInstance()
    {
        return new XBeansNavigator();
    }


    static class JaxenNode
        extends XmlCursor.XmlBookmark
    {
        public String toString()
        {
            XmlCursor xc = this.createCursor();
            return "{Node:" + xc.currentTokenType().toString() + " " + xc.toString() + "}";
        }
    }


    //
    // DefaultNavigator implementation
    //

    /** Retrieve the namespace URI of the given element node.
     *
     *  @param element The context element node.
     *
     *  @return The namespace URI of the element node.
     */
    public String getElementNamespaceUri(Object element)
    {
        ((JaxenNode)element).toBookmark(_xc);
        return _xc.getName().getNamespaceURI();
    }

    /** Retrieve the name of the given element node.
     *
     *  @param element The context element node.
     *
     *  @return The name of the element node.
     */
    public String getElementName(Object element)
    {
        ((JaxenNode)element).toBookmark(_xc);
        return _xc.getName().getLocalPart();
    }

    /** Retrieve the QName of the given element node.
     *
     *  @param element The context element node.
     *
     *  @return The QName of the element node.
     */
    public String getElementQName(Object element)
    {
        ((JaxenNode)element).toBookmark(_xc);
        String prefix = _xc.getName().getPrefix();
        return ( !"".equals(prefix) ? prefix + ":" : "" ) + _xc.getName().getLocalPart();
    }

    /** Retrieve the namespace URI of the given attribute node.
     *
     *  @param attr The context attribute node.
     *
     *  @return The namespace URI of the attribute node.
     */
    public String getAttributeNamespaceUri(Object attr)
    {
        ((JaxenNode)attr).toBookmark(_xc);
        return _xc.getName().getNamespaceURI();
    }

    /** Retrieve the name of the given attribute node.
     *
     *  @param attr The context attribute node.
     *
     *  @return The name of the attribute node.
     */
    public String getAttributeName(Object attr)
    {
        ((JaxenNode)attr).toBookmark(_xc);
        return _xc.getName().getLocalPart();
    }

    /** Retrieve the QName of the given attribute node.
     *
     *  @param attr The context attribute node.
     *
     *  @return The QName of the attribute node.
     */
    public String getAttributeQName(Object attr)
    {
        ((JaxenNode)attr).toBookmark(_xc);
        String uri = _xc.getName().getNamespaceURI();
        String prefix = _xc.prefixForNamespace(uri);
        return ( !"".equals(prefix) ? prefix + ":" : "" ) + _xc.getName().getLocalPart();
    }

    /** Returns whether the given object is a document node. A document node
     *  is the node that is selected by the xpath expression <code>/</code>.
     *
     *  @param object The object to test.
     *
     *  @return <code>true</code> if the object is a document node,
     *          else <code>false</code>
     */
    public boolean isDocument(Object object)
    {
        ((JaxenNode)object).toBookmark(_xc);
        return _xc.isStartdoc();
    }

    /** Returns whether the given object is an element node.
     *
     *  @param object The object to test.
     *
     *  @return <code>true</code> if the object is an element node,
     *          else <code>false</code>
     */
    public boolean isElement(Object object)
    {
        ((JaxenNode)object).toBookmark(_xc);
        return _xc.isStart();
    }

    /** Returns whether the given object is an attribute node.
     *
     *  @param object The object to test.
     *
     *  @return <code>true</code> if the object is an attribute node,
     *          else <code>false</code>
     */
    public boolean isAttribute(Object object)
    {
        ((JaxenNode)object).toBookmark(_xc);
        return _xc.isAttr();
    }

    /** Returns whether the given object is a namespace node.
     *
     *  @param object The object to test.
     *
     *  @return <code>true</code> if the object is a namespace node,
     *          else <code>false</code>
     */
    public boolean isNamespace(Object object)
    {
        ((JaxenNode)object).toBookmark(_xc);
        return _xc.isNamespace();
    }

    /** Returns whether the given object is a comment node.
     *
     *  @param object The object to test.
     *
     *  @return <code>true</code> if the object is a comment node,
     *          else <code>false</code>
     */
    public boolean isComment(Object object)
    {
        ((JaxenNode)object).toBookmark(_xc);
        return _xc.isComment();
    }

    /** Returns whether the given object is a text node.
     *
     *  @param object The object to test.
     *
     *  @return <code>true</code> if the object is a text node,
     *          else <code>false</code>
     */
    public boolean isText(Object object)
    {
        ((JaxenNode)object).toBookmark(_xc);
        return _xc.isText();
    }

    /** Returns whether the given object is a processing-instruction node.
     *
     *  @param object The object to test.
     *
     *  @return <code>true</code> if the object is a processing-instruction node,
     *          else <code>false</code>
     */
    public boolean isProcessingInstruction(Object object)
    {
        ((JaxenNode)object).toBookmark(_xc);
        return _xc.isProcinst();
    }

    /** Retrieve the string-value of a comment node.
     *
     *  @param comment The comment node.
     *
     *  @return The string-value of the node.
     */
    public String getCommentStringValue(Object comment)
    {
        ((JaxenNode)comment).toBookmark(_xc);
        return _xc.getTextValue();
    }

    /** Retrieve the string-value of an element node.
     *
     *  @param element The comment node.
     *
     *  @return The string-value of the node.
     */
    public String getElementStringValue(Object element)
    {
        ((JaxenNode)element).toBookmark(_xc);
        return _xc.getTextValue();
    }

    /** Retrieve the string-value of an attribute node.
     *
     *  @param attr The attribute node.
     *
     *  @return The string-value of the node.
     */
    public String getAttributeStringValue(Object attr)
    {
        ((JaxenNode)attr).toBookmark(_xc);
        return _xc.getTextValue();
    }

    /** Retrieve the string-value of a namespace node.
     *
     *  @param ns The namespace node.
     *
     *  @return The string-value of the node.
     */
    public String getNamespaceStringValue(Object ns)
    {
        ((JaxenNode)ns).toBookmark(_xc);
        return _xc.getName().getNamespaceURI();
    }

    /** Retrieve the string-value of a text node.
     *
     *  @param txt The text node.
     *
     *  @return The string-value of the node.
     */
    public String getTextStringValue(Object txt)
    {
        ((JaxenNode)txt).toBookmark(_xc);
        return _xc.getTextValue();
    }

    /** Retrieve the namespace prefix of a namespace node.
     *
     *  @param ns The namespace node.
     *
     *  @return The prefix associated with the node.
     */
    public String getNamespacePrefix(Object ns)
    {
        ((JaxenNode)ns).toBookmark(_xc);
        return _xc.getName().getLocalPart();
    }

    /** Returns a parsed form of the given xpath string, which will be suitable
     *  for queries on documents that use the same navigator as this one.
     *
     *  @see org.jaxen.XPath
     *
     *  @param xpath The xpath expression.
     *
     *  @return A new XPath expression object.
     *
     *  @throws org.jaxen.saxpath.SAXPathException If an error occurs while parsing the
     *          xpath expression.
     */
    public XPath parseXPath(String xpath)
            throws SAXPathException
    {
        return new XBeansXPath(xpath);
    }

    //
    // Overwritten methods
    //

    public static class ChildIterator implements Iterator
    {
        private JaxenNode _nextNode = null;
        private XmlCursor _xc = null;

        ChildIterator(XmlCursor xc)
        {
            _xc = xc;
            XmlCursor.TokenType tk = _xc.toFirstContentToken(); //not including atts and ns-es
            if (tk.isFinish())
                _nextNode = null;
            else
                _nextNode = getBookmarkInThisPlace(_xc);
        }

        public boolean hasNext()
        {
            if (_nextNode == null)
                return false;

            return true;
        }

        public Object next()
        {
            if (_nextNode == null)
                return null;

            JaxenNode res = _nextNode;

            res.toBookmark(_xc);

            if (_xc.currentTokenType() == XmlCursor.TokenType.START)
            {
                _xc.toEndToken();
            }

            if (_xc.toNextToken().isFinish())
                _nextNode = null;
            else
                _nextNode = getBookmarkInThisPlace(_xc);

            return res;
        }

        public void remove() { throw new RuntimeException("optional method not implemented"); }
    }

    public Iterator getChildAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        ((JaxenNode)contextNode).toBookmark(_xc);
        return new ChildIterator(_xc);
    }

    public Iterator getDescendantAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        return new DescendantAxisIterator( contextNode, this );
        /*
            ((JaxenNode)contextNode).toBookmark(_xc);
            return new DescendantIterator(_xc);
        */
    }

    public Iterator getParentAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        ((JaxenNode)contextNode).toBookmark(_xc);
        if (_xc.toParent())
            return new SingleObjectIterator(getBookmarkInThisPlace(_xc));
        else
            return null;
    }

    public Iterator getAncestorAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        return new AncestorAxisIterator( contextNode, this );
    }

    public Iterator getFollowingSiblingAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        return new FollowingSiblingAxisIterator( contextNode, this );
    }

    public Iterator getPrecedingSiblingAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        return new PrecedingSiblingAxisIterator( contextNode, this );
    }

    public Iterator getFollowingAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        return new FollowingAxisIterator( contextNode, this );
    }

    public Iterator getPrecedingAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        return new PrecedingAxisIterator( contextNode, this );
    }

    public static class AttributeIterator implements Iterator
    {
        private JaxenNode _nextNode = null;
        private XmlCursor _xc = null;

        AttributeIterator(XmlCursor xc)
        {
            _xc = xc;
            if (!_xc.toFirstAttribute())
                _nextNode = null;
            else
                _nextNode = getBookmarkInThisPlace(_xc);
        }

        public boolean hasNext()
        {
            if (_nextNode == null)
                return false;

            return true;
        }

        public Object next()
        {
            if (_nextNode == null)
                return null;

            JaxenNode res = _nextNode;

            res.toBookmark(_xc);

            if (!_xc.toNextAttribute())
                _nextNode = null;
            else
                _nextNode = getBookmarkInThisPlace(_xc);

            return res;
        }

        public void remove() { throw new RuntimeException("optional method not implemented"); }
    }

    public Iterator getAttributeAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        ((JaxenNode)contextNode).toBookmark(_xc);
        return new AttributeIterator(_xc);
    }

    public static class NamespaceIterator implements Iterator
    {
        private JaxenNode _nextNode = null;
        private XmlCursor _xc = null;

        NamespaceIterator(XmlCursor xc)
        {
            _xc = xc;
            while (true)
            {
                XmlCursor.TokenType tk = _xc.toNextToken();
                if (tk == XmlCursor.TokenType.ATTR)
                    continue;
                if (tk == XmlCursor.TokenType.NAMESPACE)
                {
                    _nextNode = getBookmarkInThisPlace(_xc);
                    break;
                }

                _nextNode = null;
                return;
            }
        }

        public boolean hasNext()
        {
            if (_nextNode == null)
                return false;

            return true;
        }

        public Object next()
        {
            if (_nextNode == null)
                return null;

            JaxenNode res = _nextNode;
            res.toBookmark(_xc);

            if (_xc.toNextToken().isNamespace())
                _nextNode = getBookmarkInThisPlace(_xc);
            else
                _nextNode = null;

            return res;
        }

        public void remove() { throw new RuntimeException("optional method not implemented"); }
    }

    public Iterator getNamespaceAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        ((JaxenNode)contextNode).toBookmark(_xc);
        return new NamespaceIterator(_xc);
    }

    public Iterator getSelfAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        return new SelfAxisIterator( contextNode );
    }

    public static class DescendantOrSelfAxisItr implements Iterator
    {
        private JaxenNode _nextNode = null;
        private XmlCursor _xc = null;

        DescendantOrSelfAxisItr(XmlCursor xc)
        {
            _xc = xc;
            XmlCursor.TokenType tk = _xc.toFirstContentToken(); //not including atts and ns-es
            if (tk.isFinish())
                _nextNode = null;
            else
                _nextNode = getBookmarkInThisPlace(_xc);
        }

        public boolean hasNext()
        {
            if (_nextNode == null)
                return false;

            return true;
        }

        public Object next()
        {
            if (_nextNode == null)
                return null;

            JaxenNode res = _nextNode;

            res.toBookmark(_xc);

            if (_xc.currentTokenType() == XmlCursor.TokenType.START)
            {
                _xc.toEndToken();
            }

            if (_xc.toNextToken().isFinish())
                _nextNode = null;
            else
                _nextNode = getBookmarkInThisPlace(_xc);

            return res;
        }

        public void remove() { throw new RuntimeException("optional method not implemented"); }
    }

    public Iterator getDescendantOrSelfAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        return new DescendantOrSelfAxisIterator( contextNode, this );
        //((JaxenNode)contextNode).toBookmark(_xc);
        //return new DescendantOrSelfAxisIterator(_xc);
    }

    public Iterator getAncestorOrSelfAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        return new AncestorOrSelfAxisIterator( contextNode, this );
        //((JaxenNode)contextNode).toBookmark(_xc);
        //return new AncestorOrSelfAxisIterator(_xc);
    }

    public Object getDocumentNode(Object contextNode)
    {
            ((JaxenNode)contextNode).toBookmark(_xc);
            _xc.toStartDoc();
            return getBookmarkInThisPlace(_xc);
    }

    public String translateNamespacePrefixToUri(String prefix, Object element)
    {
            ((JaxenNode)element).toBookmark(_xc);
            return _xc.namespaceForPrefix(prefix);
    }

    public String getProcessingInstructionTarget(Object obj)
    {
        ((JaxenNode)obj).toBookmark(_xc);
        return _xc.getTextValue();
    }

    public String getProcessingInstructionData(Object obj)
    {
        ((JaxenNode)obj).toBookmark(_xc);
        return _xc.getTextValue();
    }


    public Object getDocument(String url) throws FunctionCallException
    {
        return null;
    }

    /**
     *  Default implementation that can not find elements. Override in subclass
     *  if subclass does know about attribute types.
     *
     *  @param object   a node from the document in which to look for the
     *                       id
     *  @param elementId   id to look for
     *
     *  @return   null
     */
    public Object getElementById(Object object, String elementId)
    {
        return null;
    }

    public static class NamedChildIterator implements Iterator
    {
        private JaxenNode _nextNode = null;
        private QName _qname;
        private XmlCursor _xc = null;

        NamedChildIterator(XmlCursor xc, String localName, String namespaceURI)
        {
            _xc = xc;
            _qname = new QName( (namespaceURI==null ? "" : namespaceURI), localName);

            if (!_xc.toFirstChild())
            {
                _nextNode = null;
                return;
            }
            if (!(_xc.currentTokenType()==XmlCursor.TokenType.START &&
                  _xc.getName().equals(_qname)
               ))
            {
                if (!_xc.toNextSibling(_qname))
                {
                    _nextNode = null;
                    return;
                }
            }
            _nextNode = getBookmarkInThisPlace(_xc);
        }

        public boolean hasNext()
        {
            if (_nextNode == null)
                return false;

            return true;
        }

        public Object next()
        {
            if (_nextNode == null)
                return null;

            JaxenNode res = _nextNode;
            res.toBookmark(_xc);

            if (!_xc.toNextSibling(_qname))
                _nextNode = null;
            else
                _nextNode = getBookmarkInThisPlace(_xc);

            return res;
        }

        public void remove() { throw new RuntimeException("optional method not implemented"); }
    }

    public Iterator getChildAxisIterator(
            Object contextNode,
            String localName, String namespacePrefix, String namespaceURI)
            throws UnsupportedAxisException
    {
        ((JaxenNode)contextNode).toBookmark(_xc);
        return new NamedChildIterator(_xc, localName, namespaceURI);
    }

    public static class NamedAttributeIterator implements Iterator
    {
        private JaxenNode _nextNode = null;
        private QName _qname;
        private XmlCursor _xc;

        NamedAttributeIterator(XmlCursor xc, String local, String uri)
        {
            _xc = xc;
            _qname = new QName(( uri==null ? "" : uri), local);

            XmlCursor.TokenType tk = _xc.toNextToken();
            if (tk!=XmlCursor.TokenType.ATTR)
            {
                _nextNode = null;
                return;
            }

            if ( _xc.getName().equals(_qname) )
            {
                _nextNode = getBookmarkInThisPlace(_xc);
                return;
            }
            _nextNode = move(_xc);
            return;
        }

        public boolean hasNext()
        {
            if (_nextNode == null)
                return false;

            return true;
        }

        public Object next()
        {
            if (_nextNode == null)
                return null;

            JaxenNode res = _nextNode;
            res.toBookmark(_xc);

            _nextNode = move(_xc);
            return res;
        }

        private JaxenNode move(XmlCursor xc)
        {
            if (xc.currentTokenType() != XmlCursor.TokenType.ATTR )
            {
                return null;
            }

            while(xc.toNextToken()==XmlCursor.TokenType.ATTR)
            {
                if ( xc.getName().equals(_qname) )
                {
                    return getBookmarkInThisPlace(xc);
                }
            }
            return null;
        }

        public void remove() { throw new RuntimeException("optional method not implemented"); }
    }

    public Iterator getAttributeAxisIterator(
            Object contextNode,
            String localName, String namespacePrefix, String namespaceURI)
            throws UnsupportedAxisException
    {
        ((JaxenNode)contextNode).toBookmark(_xc);
        return new NamedAttributeIterator(_xc, localName, namespaceURI);
    }

    static JaxenNode getBookmarkInThisPlace(XmlCursor xc)
    {
        JaxenNode rez = (JaxenNode)xc.getBookmark(JaxenNode.class);
        if (rez==null)
        {
            rez = new JaxenNode();
            xc.setBookmark(rez);
        }

        return rez;
    }

    XmlCursor getCursor()
    {
        return _xc;
    }

    void setCursor(XmlCursor xc)
    {
        this._xc = xc;
    }
}

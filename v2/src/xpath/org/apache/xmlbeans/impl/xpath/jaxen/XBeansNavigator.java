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
import org.jaxen.saxpath.SAXPathException;

import org.apache.xmlbeans.XmlCursor;

import java.util.Iterator;

/**
 * Author: Cezar Andrei (cezar.andrei@bea.com)
 * Date: Oct 10, 2003
 */
public class XBeansNavigator extends DefaultNavigator //implements NamedAccessNavigator
{
    /** Singleton implementation.
     */
    private static class Singleton
    {
        /** Singleton instance.
         */
        private static XBeansNavigator instance = new XBeansNavigator();
    }

    /** Retrieve the singleton instance of this <code>DocumentNavigator</code>.
     */
    public static Navigator getInstance()
    {
        return XBeansNavigator.Singleton.instance;
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
        if( element instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)element);
            return xc.getName().getNamespaceURI();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }

    /** Retrieve the name of the given element node.
     *
     *  @param element The context element node.
     *
     *  @return The name of the element node.
     */
    public String getElementName(Object element)
    {
        if( element instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)element);
            return xc.getName().getLocalPart();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }

    /** Retrieve the QName of the given element node.
     *
     *  @param element The context element node.
     *
     *  @return The QName of the element node.
     */
    public String getElementQName(Object element)
    {
        if( element instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)element);
            String uri = xc.getName().getNamespaceURI();
            String prefix = xc.prefixForNamespace(uri);
            return ( !"".equals(prefix) ? prefix + ":" : "" ) + xc.getName().getLocalPart();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }

    /** Retrieve the namespace URI of the given attribute node.
     *
     *  @param attr The context attribute node.
     *
     *  @return The namespace URI of the attribute node.
     */
    public String getAttributeNamespaceUri(Object attr)
    {
        if( attr instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)attr);
            return xc.getName().getNamespaceURI();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }

    /** Retrieve the name of the given attribute node.
     *
     *  @param attr The context attribute node.
     *
     *  @return The name of the attribute node.
     */
    public String getAttributeName(Object attr)
    {
        if( attr instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)attr);
            return xc.getName().getLocalPart();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }

    /** Retrieve the QName of the given attribute node.
     *
     *  @param attr The context attribute node.
     *
     *  @return The QName of the attribute node.
     */
    public String getAttributeQName(Object attr)
    {
        if( attr instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)attr);
            String uri = xc.getName().getNamespaceURI();
            String prefix = xc.prefixForNamespace(uri);
            return ( !"".equals(prefix) ? prefix + ":" : "" ) + xc.getName().getLocalPart();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
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
        if( object instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)object);
            return xc.isStartdoc();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
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
        if( object instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)object);
            return xc.isStart();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
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
        if( object instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)object);
            return xc.isAttr();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
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
        if( object instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)object);
            return xc.isNamespace();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
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
        if( object instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)object);
            return xc.isComment();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
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
        if( object instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)object);
            return xc.isText();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
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
        if( object instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)object);
            return xc.isProcinst();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }

    /** Retrieve the string-value of a comment node.
     *
     *  @param comment The comment node.
     *
     *  @return The string-value of the node.
     */
    public String getCommentStringValue(Object comment)
    {
        if( comment instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)comment);
            return xc.getTextValue();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }

    /** Retrieve the string-value of an element node.
     *
     *  @param element The comment node.
     *
     *  @return The string-value of the node.
     */
    public String getElementStringValue(Object element)
    {
        if( element instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)element);
            return xc.getTextValue();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }

    /** Retrieve the string-value of an attribute node.
     *
     *  @param attr The attribute node.
     *
     *  @return The string-value of the node.
     */
    public String getAttributeStringValue(Object attr)
    {
        if( attr instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)attr);
            return xc.getTextValue();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }

    /** Retrieve the string-value of a namespace node.
     *
     *  @param ns The namespace node.
     *
     *  @return The string-value of the node.
     */
    public String getNamespaceStringValue(Object ns)
    {
        if( ns instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)ns);
            return xc.getName().getNamespaceURI();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }

    /** Retrieve the string-value of a text node.
     *
     *  @param txt The text node.
     *
     *  @return The string-value of the node.
     */
    public String getTextStringValue(Object txt)
    {
        if( txt instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)txt);
            return xc.getTextValue();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }

    /** Retrieve the namespace prefix of a namespace node.
     *
     *  @param ns The namespace node.
     *
     *  @return The prefix associated with the node.
     */
    public String getNamespacePrefix(Object ns)
    {
        if( ns instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)ns);
            return xc.getName().getLocalPart();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
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
        private XmlCursor _xc = null;

        ChildIterator(XmlCursor xc)
        {
            _xc = xc.newCursor();
            XmlCursor.TokenType tk = _xc.toNextToken(); //including atts and ns-es not sure if they should be included
            if (tk.isEnd() || tk.isEnddoc() || tk.isNone())
            {
                _xc.dispose();
                _xc = null;
            }
        }

        public boolean hasNext()
        {
            if (_xc == null)
                return false;

            if (_xc.currentTokenType() == XmlCursor.TokenType.END ||
                _xc.currentTokenType() == XmlCursor.TokenType.ENDDOC ||
                _xc.currentTokenType() == XmlCursor.TokenType.NONE )
            {
                    _xc.dispose();
                    _xc = null;
                    return false;
            }

            return true;
        }

        public Object next()
        {
            if (_xc == null)
                return null;

            if (_xc.currentTokenType() == XmlCursor.TokenType.END ||
                _xc.currentTokenType() == XmlCursor.TokenType.ENDDOC ||
                _xc.currentTokenType() == XmlCursor.TokenType.NONE )
            {
                    _xc.dispose();
                    _xc = null;
                    return null;
            }

            XmlCursor res = _xc.newCursor();

            if (_xc.currentTokenType() == XmlCursor.TokenType.START)
            {
                _xc.toEndToken();
            }

            switch(_xc.toNextToken().intValue())
            {
                case XmlCursor.TokenType.INT_END:
                case XmlCursor.TokenType.INT_ENDDOC:
                    _xc.dispose();
                    _xc = null;
                default:
                    return res;
            }
        }

        public void remove() { throw new RuntimeException("optional method not implemented"); }
    }

    public static class ChildIteratorFast implements Iterator
    {
        private XmlCursor _xc = null;
        private boolean inTheRightPlace = false;

        ChildIteratorFast(XmlCursor xc)
        {
            _xc = xc.newCursor();
            XmlCursor.TokenType tk = _xc.toNextToken(); //including atts and ns-es not sure if they should be included
            if (tk.isEnd() || tk.isEnddoc() || tk.isNone())
            {
                _xc.dispose();
                _xc = null;
            }
            inTheRightPlace = true;
        }

        public boolean hasNext()
        {
            if (!inTheRightPlace)
                move();

            if (_xc == null)
                return false;

            if (_xc.currentTokenType() == XmlCursor.TokenType.END ||
                _xc.currentTokenType() == XmlCursor.TokenType.ENDDOC ||
                _xc.currentTokenType() == XmlCursor.TokenType.NONE )
            {
                    _xc.dispose();
                    _xc = null;
                    return false;
            }

            return true;
        }

        public Object next()
        {
            if (!inTheRightPlace)
                move();

            if (_xc == null)
                return null;

            if (_xc.currentTokenType() == XmlCursor.TokenType.END ||
                _xc.currentTokenType() == XmlCursor.TokenType.ENDDOC ||
                _xc.currentTokenType() == XmlCursor.TokenType.NONE )
            {
                    _xc.dispose();
                    _xc = null;
                    return null;
            }
            inTheRightPlace = false;
            return _xc.newCursor();
        }

        private void move()
        {
            if (_xc == null)
                return;

            if (_xc.currentTokenType() == XmlCursor.TokenType.START)
            {
                _xc.toEndToken();
            }

            switch(_xc.toNextToken().intValue())
            {
                case XmlCursor.TokenType.INT_END:
                case XmlCursor.TokenType.INT_ENDDOC:
                    _xc.dispose();
                    _xc = null;
                default:
                    inTheRightPlace = true;
                    return;
            }
        }

        public void remove() { throw new RuntimeException("optional method not implemented"); }
    }

    public Iterator getChildAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        if( contextNode instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)contextNode);
            return new ChildIteratorFast(xc);
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }

    public static class DescendantIterator implements Iterator
    {
        private XmlCursor _xc = null;
        private int _depth = 0;

        DescendantIterator(XmlCursor xc)
        {
            _xc = xc.newCursor();
            _depth = 1;
            XmlCursor.TokenType tk = _xc.toNextToken(); //including atts and ns-es not sure if they should be included
            if (tk.isEnd() || tk.isEnddoc() || tk.isNone())
            {
                _xc.dispose();
                _xc = null;
                _depth = 0;
            }
        }

        public boolean hasNext()
        {
            if (_xc == null)
                return false;

            if ((_depth == 1 && _xc.currentTokenType() == XmlCursor.TokenType.END) ||
                _xc.currentTokenType() == XmlCursor.TokenType.ENDDOC ||
                _xc.currentTokenType() == XmlCursor.TokenType.NONE )
            {
                    _xc.dispose();
                    _xc = null;
                    return false;
            }

            return true;
        }

        public Object next()
        {
            if (_xc == null)
                return null;

            if ((_depth == 1 && _xc.currentTokenType() == XmlCursor.TokenType.END) ||
                _xc.currentTokenType() == XmlCursor.TokenType.ENDDOC ||
                _xc.currentTokenType() == XmlCursor.TokenType.NONE )
            {
                    _xc.dispose();
                    _xc = null;
                    return null;
            }

            XmlCursor res = _xc.newCursor();

            if (_xc.currentTokenType() == XmlCursor.TokenType.START)
            {
                _depth ++;
            }
            else if (_xc.currentTokenType() == XmlCursor.TokenType.END)
            {
                _depth --;
            }

            switch(_xc.toNextToken().intValue())
            {
                case XmlCursor.TokenType.INT_END:
                    if (_depth > 1)
                        return res;
                case XmlCursor.TokenType.INT_ENDDOC:
                    _xc.dispose();
                    _xc = null;
                default:
                    return res;
            }
        }

        public void remove() { throw new RuntimeException("optional method not implemented"); }
    }

    public Iterator getDescendantAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        //return new DescendantAxisIterator( contextNode, this );
        if( contextNode instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)contextNode);
            return new DescendantIterator(xc);
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }

    public Iterator getParentAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        if( contextNode instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)contextNode).newCursor();
            xc.toParent();
            return new SingleObjectIterator(xc);
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
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
        private XmlCursor _xc = null;

        AttributeIterator(XmlCursor xc)
        {
            _xc = xc.newCursor();
            if (!_xc.toFirstAttribute())
            {
                _xc.dispose();
                _xc = null;
            }
        }

        public boolean hasNext()
        {
            if (_xc == null)
                return false;

            if (_xc.currentTokenType() != XmlCursor.TokenType.ATTR )
            {
                    _xc.dispose();
                    _xc = null;
                    return false;
            }

            return true;
        }

        public Object next()
        {
            if (_xc == null)
                return null;

            if (_xc.currentTokenType() != XmlCursor.TokenType.ATTR )
            {
                    _xc.dispose();
                    _xc = null;
                    return null;
            }

            XmlCursor res = _xc.newCursor();

            switch(_xc.toNextToken().intValue())
            {
                case XmlCursor.TokenType.INT_ATTR:
                    return res;
                default:
                    _xc.dispose();
                    _xc = null;
                    return res;
            }
        }

        public void remove() { throw new RuntimeException("optional method not implemented"); }
    }

    public Iterator getAttributeAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        if( contextNode instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)contextNode);
            return new AttributeIterator(xc);
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }

    public static class NamespaceIterator implements Iterator
    {
        private XmlCursor _xc = null;

        NamespaceIterator(XmlCursor xc)
        {
            _xc = xc.newCursor();
            while (true)
            {
                XmlCursor.TokenType tk = _xc.toNextToken();
                if (tk == XmlCursor.TokenType.ATTR)
                    continue;
                if (tk == XmlCursor.TokenType.NAMESPACE)
                    break;

                _xc.dispose();
                _xc = null;
                return;
            }
        }

        public boolean hasNext()
        {
            if (_xc == null)
                return false;

            if (_xc.currentTokenType() != XmlCursor.TokenType.NAMESPACE )
            {
                    _xc.dispose();
                    _xc = null;
                    return false;
            }

            return true;
        }

        public Object next()
        {
            if (_xc == null)
                return null;

            if (_xc.currentTokenType() != XmlCursor.TokenType.NAMESPACE )
            {
                    _xc.dispose();
                    _xc = null;
                    return null;
            }

            XmlCursor res = _xc.newCursor();

            switch(_xc.toNextToken().intValue())
            {
                case XmlCursor.TokenType.INT_NAMESPACE:
                    return res;
                default:
                    _xc.dispose();
                    _xc = null;
                    return res;
            }
        }

        public void remove() { throw new RuntimeException("optional method not implemented"); }
    }

    public Iterator getNamespaceAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        if( contextNode instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)contextNode);
            return new NamespaceIterator(xc);
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }

    public Iterator getSelfAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        return new SelfAxisIterator( contextNode );
    }

    public Iterator getDescendantOrSelfAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        return new DescendantOrSelfAxisIterator( contextNode, this );
    }

    public Iterator getAncestorOrSelfAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        return new AncestorOrSelfAxisIterator( contextNode, this );
    }

    public Object getDocumentNode(Object contextNode)
    {
        if( contextNode instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)contextNode).newCursor();
            xc.toStartDoc();
            return xc;
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }

    public String translateNamespacePrefixToUri(String prefix, Object element)
    {
        if( element instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)element);
            return xc.namespaceForPrefix(prefix);
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }

    public String getProcessingInstructionTarget(Object obj)
    {
        if( obj instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)obj);
            return xc.getTextValue();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }

    public String getProcessingInstructionData(Object obj)
    {
        if( obj instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)obj);
            return xc.getTextValue();
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
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


    public static class NamedChildIteratorFast implements Iterator
    {
        private XmlCursor _xc = null;
        private String _local, _uri;
        private boolean inTheRightPlace = false;

        NamedChildIteratorFast(XmlCursor xc, String localName, String namespaceURI)
        {
            _xc = xc.newCursor();
            _local = localName;
            _uri = (namespaceURI==null ? "" : namespaceURI);

            if (!_xc.toFirstChild())
            {
                _xc.dispose();
                _xc = null;
                return;
            }
            if (!(_xc.currentTokenType()==XmlCursor.TokenType.START &&
                _xc.getName().getLocalPart().equals(_local) &&
                    _xc.getName().getNamespaceURI().equals(_uri)
               ))
            {
                if (!_xc.toNextSibling(_uri, _local))
                {
                    _xc.dispose();
                    _xc = null;
                }
            }
            inTheRightPlace = true;
        }

        public boolean hasNext()
        {
            if (!inTheRightPlace)
                move();

            if (_xc == null)
                return false;

            return true;
        }

        public Object next()
        {
            if (!inTheRightPlace)
                move();

            if (_xc == null)
                return null;

            inTheRightPlace = false;
            return _xc.newCursor();
        }

        private void move()
        {
            if (_xc == null)
                return;

            if (!_xc.toNextSibling(_uri, _local))
            {
                _xc.dispose();
                _xc = null;
            }
            inTheRightPlace = true;
            return;
        }

        public void remove() { throw new RuntimeException("optional method not implemented"); }
    }

    public Iterator getChildAxisIterator(
            Object contextNode,
            String localName, String namespacePrefix, String namespaceURI)
            throws UnsupportedAxisException
    {
        if( contextNode instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)contextNode);
            return new NamedChildIteratorFast(xc, localName, namespaceURI);
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }

    public static class NamedAttributeIterator implements Iterator
    {
        private XmlCursor _xc = null;
        private String _local, _uri;
        private boolean inTheRightPlace = false;

        NamedAttributeIterator(XmlCursor xc, String local, String uri)
        {
            _xc = xc.newCursor();
            _local = local;
            _uri = uri;
            move();
        }

        public boolean hasNext()
        {
            if (!inTheRightPlace)
                move();

            if (_xc == null)
                return false;

            return true;
        }

        public Object next()
        {
            if (!inTheRightPlace)
                move();

            if (_xc == null)
                return null;

            inTheRightPlace = false;
            return _xc.newCursor();
        }

        private void move()
        {
            if (_xc.currentTokenType() != XmlCursor.TokenType.ATTR )
            {
                _xc.dispose();
                _xc = null;
                return;
            }

            while(_xc.toNextToken()==XmlCursor.TokenType.ATTR)
            {
                if ( _xc.getName().getLocalPart().equals(_local) &&
                     _xc.getName().getNamespaceURI().equals(_uri))
                {
                    inTheRightPlace = true;
                    return;
                }
            }
            _xc.dispose();
            _xc = null;
            inTheRightPlace = true;
        }

        public void remove() { throw new RuntimeException("optional method not implemented"); }
    }

    public Iterator getAttributeAxisIterator(
            Object contextNode,
            String localName, String namespacePrefix, String namespaceURI)
            throws UnsupportedAxisException
    {
        if( contextNode instanceof XmlCursor )
        {
            XmlCursor xc = ((XmlCursor)contextNode);
            return new NamedAttributeIterator(xc, localName, namespaceURI);
        }
        throw new IllegalArgumentException("node must be an XmlObject or an XmlCursor.");
    }
}

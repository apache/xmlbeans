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
 * 4. The names "Axis" and "Apache Software Foundation" must
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
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package javax.xml.soap;

/**
 * A representation of a node (element) in a DOM representation of an XML document
 * that provides some tree manipulation methods.
 * This interface provides methods for getting the value of a node, for
 * getting and setting the parent of a node, and for removing a node.
 */
public interface Node extends org.w3c.dom.Node {

    /**
     * Returns the the value of the immediate child of this <code>Node</code>
     * object if a child exists and its value is text.
     * @return  a <code>String</code> with the text of the immediate child of
     *    this <code>Node</code> object if (1) there is a child and
     *    (2) the child is a <code>Text</code> object;
     *      <code>null</code> otherwise
     */
    public abstract String getValue();

    /**
     * Sets the parent of this <code>Node</code> object to the given
     * <code>SOAPElement</code> object.
     * @param parent the <code>SOAPElement</code> object to be set as
     *  the parent of this <code>Node</code> object
     * @throws SOAPException if there is a problem in setting the
     *                     parent to the given element
     * @see #getParentElement() getParentElement()
     */
    public abstract void setParentElement(SOAPElement parent)
        throws SOAPException;

    /**
     * Returns the parent element of this <code>Node</code> object.
     * This method can throw an <code>UnsupportedOperationException</code>
     * if the tree is not kept in memory.
     * @return  the <code>SOAPElement</code> object that is the parent of
     *    this <code>Node</code> object or <code>null</code> if this
     *    <code>Node</code> object is root
     * @throws java.lang.UnsupportedOperationException if the whole tree is not kept in memory
     * @see #setParentElement(javax.xml.soap.SOAPElement) setParentElement(javax.xml.soap.SOAPElement)
     */
    public abstract SOAPElement getParentElement();

    /**
     * Removes this <code>Node</code> object from the tree. Once
     * removed, this node can be garbage collected if there are no
     * application references to it.
     */
    public abstract void detachNode();

    /**
     * Notifies the implementation that this <code>Node</code>
     * object is no longer being used by the application and that the
     * implementation is free to reuse this object for nodes that may
     * be created later.
     * <P>
     * Calling the method <code>recycleNode</code> implies that the method
     * <code>detachNode</code> has been called previously.
     */
    public abstract void recycleNode();

    /**
     * If this is a Text node then this method will set its value, otherwise it
     * sets the value of the immediate (Text) child of this node. The value of
     * the immediate child of this node can be set only if, there is one child
     * node and that node is a Text node, or if there are no children in which
     * case a child Text node will be created.
     *
     * @param value the text to set
     * @throws IllegalStateException   if the node is not a Text  node and
     *              either has more than one child node or has a child node that
     *              is not a Text node
     */

    public abstract void setValue(String value);
}

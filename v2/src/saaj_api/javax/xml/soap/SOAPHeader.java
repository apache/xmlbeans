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

import java.util.Iterator;

/**
 * <P>A representation of the SOAP header element. A SOAP header
 *   element consists of XML data that affects the way the
 *   application-specific content is processed by the message
 *   provider. For example, transaction semantics, authentication
 *   information, and so on, can be specified as the content of a
 *   <CODE>SOAPHeader</CODE> object.</P>
 *
 *   <P>A <CODE>SOAPEnvelope</CODE> object contains an empty <CODE>
 *   SOAPHeader</CODE> object by default. If the <CODE>
 *   SOAPHeader</CODE> object, which is optional, is not needed, it
 *   can be retrieved and deleted with the following line of code.
 *   The variable <I>se</I> is a <CODE>SOAPEnvelope</CODE>
 *   object.</P>
 * <PRE>
 *     se.getHeader().detachNode();
 * </PRE>
 *   A <CODE>SOAPHeader</CODE> object is created with the <CODE>
 *   SOAPEnvelope</CODE> method <CODE>addHeader</CODE>. This method,
 *   which creates a new header and adds it to the envelope, may be
 *   called only after the existing header has been removed.
 * <PRE>
 *     se.getHeader().detachNode();
 *     SOAPHeader sh = se.addHeader();
 * </PRE>
 *
 *   <P>A <CODE>SOAPHeader</CODE> object can have only <CODE>
 *   SOAPHeaderElement</CODE> objects as its immediate children. The
 *   method <CODE>addHeaderElement</CODE> creates a new <CODE>
 *   HeaderElement</CODE> object and adds it to the <CODE>
 *   SOAPHeader</CODE> object. In the following line of code, the
 *   argument to the method <CODE>addHeaderElement</CODE> is a
 *   <CODE>Name</CODE> object that is the name for the new <CODE>
 *   HeaderElement</CODE> object.</P>
 * <PRE>
 *     SOAPHeaderElement shElement = sh.addHeaderElement(name);
 * </PRE>
 * @see SOAPHeaderElement SOAPHeaderElement
 */
public interface SOAPHeader extends SOAPElement {

    /**
     * Creates a new <CODE>SOAPHeaderElement</CODE> object
     * initialized with the specified name and adds it to this
     * <CODE>SOAPHeader</CODE> object.
     * @param   name a <CODE>Name</CODE> object with
     *     the name of the new <CODE>SOAPHeaderElement</CODE>
     *     object
     * @return the new <CODE>SOAPHeaderElement</CODE> object that
     *     was inserted into this <CODE>SOAPHeader</CODE>
     *     object
     * @throws  SOAPException if a SOAP error occurs
     */
    public abstract SOAPHeaderElement addHeaderElement(Name name)
        throws SOAPException;

    /**
     * Returns a list of all the <CODE>SOAPHeaderElement</CODE>
     * objects in this <CODE>SOAPHeader</CODE> object that have the
     * the specified actor. An actor is a global attribute that
     * indicates the intermediate parties to whom the message should
     * be sent. An actor receives the message and then sends it to
     * the next actor. The default actor is the ultimate intended
     * recipient for the message, so if no actor attribute is
     * included in a <CODE>SOAPHeader</CODE> object, the message is
     * sent to its ultimate destination.
     * @param   actor  a <CODE>String</CODE> giving the
     *     URI of the actor for which to search
     * @return an <CODE>Iterator</CODE> object over all the <CODE>
     *     SOAPHeaderElement</CODE> objects that contain the
     *     specified actor
     * @see #extractHeaderElements(java.lang.String) extractHeaderElements(java.lang.String)
     */
    public abstract Iterator examineHeaderElements(String actor);

    /**
     * Returns a list of all the <CODE>SOAPHeaderElement</CODE>
     *   objects in this <CODE>SOAPHeader</CODE> object that have
     *   the the specified actor and detaches them from this <CODE>
     *   SOAPHeader</CODE> object.
     *
     *   <P>This method allows an actor to process only the parts of
     *   the <CODE>SOAPHeader</CODE> object that apply to it and to
     *   remove them before passing the message on to the next
     *   actor.
     * @param   actor  a <CODE>String</CODE> giving the
     *     URI of the actor for which to search
     * @return an <CODE>Iterator</CODE> object over all the <CODE>
     *     SOAPHeaderElement</CODE> objects that contain the
     *     specified actor
     * @see #examineHeaderElements(java.lang.String) examineHeaderElements(java.lang.String)
     */
    public abstract Iterator extractHeaderElements(String actor);

    /**
     * Returns an <code>Iterator</code> over all the
     * <code>SOAPHeaderElement</code> objects in this <code>SOAPHeader</code>
     * object that have the specified actor and that have a MustUnderstand
     * attribute whose value is equivalent to <code>true</code>.
     *
     * @param actor a <code>String</code> giving the URI of the actor for which
     *              to search
     * @return an <code>Iterator</code> object over all the
     *              <code>SOAPHeaderElement</code> objects that contain the
     *              specified actor and are marked as MustUnderstand
     */
    public abstract Iterator examineMustUnderstandHeaderElements(String actor);

    /**
     * Returns an <code>Iterator</code> over all the
     * <code>SOAPHeaderElement</code> objects in this <code>SOAPHeader</code>
     * object.
     *
     * @return an <code>Iterator</code> object over all the
     *              <code>SOAPHeaderElement</code> objects contained by this
     *              <code>SOAPHeader</code>
     */
    public abstract Iterator examineAllHeaderElements();

    /**
     * Returns an <code>Iterator</code> over all the
     * <code>SOAPHeaderElement</code> objects in this <code>SOAPHeader </code>
     * object and detaches them from this <code>SOAPHeader</code> object.
     *
     * @return an <code>Iterator</code> object over all the
     *              <code>SOAPHeaderElement</code> objects contained by this
     *              <code>SOAPHeader</code>
     */
    public abstract Iterator extractAllHeaderElements();
}

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
 *     <P>An object representing the contents in the SOAP header part
 *   of the SOAP envelope. The immediate children of a <CODE>
 *   SOAPHeader</CODE> object can be represented only as <CODE>
 *   SOAPHeaderElement</CODE> objects.</P>
 *
 *   <P>A <CODE>SOAPHeaderElement</CODE> object can have other
 *   <CODE>SOAPElement</CODE> objects as its children.</P>
 */
public interface SOAPHeaderElement extends SOAPElement {

    /**
     * Sets the actor associated with this <CODE>
     * SOAPHeaderElement</CODE> object to the specified actor. The
     * default value of an actor is: <CODE>
     * SOAPConstants.URI_SOAP_ACTOR_NEXT</CODE>
     * @param  actorURI  a <CODE>String</CODE> giving
     *     the URI of the actor to set
     * @see #getActor() getActor()
     * @throws java.lang.IllegalArgumentException if
     *     there is a problem in setting the actor.
     */
    public abstract void setActor(String actorURI);

    /**
     * Returns the uri of the actor associated with this <CODE>
     * SOAPHeaderElement</CODE> object.
     * @return  a <CODE>String</CODE> giving the URI of the
     *     actor
     * @see #setActor(java.lang.String) setActor(java.lang.String)
     */
    public abstract String getActor();

    /**
     * Sets the mustUnderstand attribute for this <CODE>
     *   SOAPHeaderElement</CODE> object to be on or off.
     *
     *   <P>If the mustUnderstand attribute is on, the actor who
     *   receives the <CODE>SOAPHeaderElement</CODE> must process it
     *   correctly. This ensures, for example, that if the <CODE>
     *   SOAPHeaderElement</CODE> object modifies the message, that
     *   the message is being modified correctly.</P>
     * @param  mustUnderstand  <CODE>true</CODE> to
     *     set the mustUnderstand attribute on; <CODE>false</CODE>
     *     to turn if off
     * @throws java.lang.IllegalArgumentException if
     *     there is a problem in setting the actor.
     * @see #getMustUnderstand() getMustUnderstand()
     */
    public abstract void setMustUnderstand(boolean mustUnderstand);

    /**
     * Returns whether the mustUnderstand attribute for this
     * <CODE>SOAPHeaderElement</CODE> object is turned on.
     * @return  <CODE>true</CODE> if the mustUnderstand attribute of
     *     this <CODE>SOAPHeaderElement</CODE> object is turned on;
     *     <CODE>false</CODE> otherwise
     */
    public abstract boolean getMustUnderstand();
}

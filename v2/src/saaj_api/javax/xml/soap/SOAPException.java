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
 * An exception that signals that a SOAP exception has
 *   occurred. A <CODE>SOAPException</CODE> object may contain a
 *   <CODE>String</CODE> that gives the reason for the exception, an
 *   embedded <CODE>Throwable</CODE> object, or both. This class
 *   provides methods for retrieving reason messages and for
 *   retrieving the embedded <CODE>Throwable</CODE> object.</P>
 *
 *   <P>Typical reasons for throwing a <CODE>SOAPException</CODE>
 *   object are problems such as difficulty setting a header, not
 *   being able to send a message, and not being able to get a
 *   connection with the provider. Reasons for embedding a <CODE>
 *   Throwable</CODE> object include problems such as input/output
 *   errors or a parsing problem, such as an error in parsing a
 *   header.
 */
public class SOAPException extends Exception {

    /**
     * Constructs a <CODE>SOAPException</CODE> object with no
     * reason or embedded <CODE>Throwable</CODE> object.
     */
    public SOAPException() {
        cause = null;
    }

    /**
     * Constructs a <CODE>SOAPException</CODE> object with the
     * given <CODE>String</CODE> as the reason for the exception
     * being thrown.
     * @param  reason  a description of what caused
     *     the exception
     */
    public SOAPException(String reason) {

        super(reason);

        cause = null;
    }

    /**
     * Constructs a <CODE>SOAPException</CODE> object with the
     * given <CODE>String</CODE> as the reason for the exception
     * being thrown and the given <CODE>Throwable</CODE> object as
     * an embedded exception.
     * @param  reason a description of what caused
     *     the exception
     * @param  cause  a <CODE>Throwable</CODE> object
     *     that is to be embedded in this <CODE>SOAPException</CODE>
     *     object
     */
    public SOAPException(String reason, Throwable cause) {

        super(reason);

        initCause(cause);
    }

    /**
     * Constructs a <CODE>SOAPException</CODE> object
     * initialized with the given <CODE>Throwable</CODE>
     * object.
     * @param  cause  a <CODE>Throwable</CODE> object
     *     that is to be embedded in this <CODE>SOAPException</CODE>
     *     object
     */
    public SOAPException(Throwable cause) {

        super(cause.toString());

        initCause(cause);
    }

    /**
     * Returns the detail message for this <CODE>
     *   SOAPException</CODE> object.
     *
     *   <P>If there is an embedded <CODE>Throwable</CODE> object,
     *   and if the <CODE>SOAPException</CODE> object has no detail
     *   message of its own, this method will return the detail
     *   message from the embedded <CODE>Throwable</CODE>
     *   object.</P>
     * @return  the error or warning message for this <CODE>
     *     SOAPException</CODE> or, if it has none, the message of
     *     the embedded <CODE>Throwable</CODE> object, if there is
     *     one
     */
    public String getMessage() {

        String s = super.getMessage();

        if ((s == null) && (cause != null)) {
            return cause.getMessage();
        } else {
            return s;
        }
    }

    /**
     * Returns the <CODE>Throwable</CODE> object embedded in
     * this <CODE>SOAPException</CODE> if there is one. Otherwise,
     * this method returns <CODE>null</CODE>.
     * @return  the embedded <CODE>Throwable</CODE> object or <CODE>
     *     null</CODE> if there is none
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Initializes the <CODE>cause</CODE> field of this <CODE>
     *   SOAPException</CODE> object with the given <CODE>
     *   Throwable</CODE> object.
     *
     *   <P>This method can be called at most once. It is generally
     *   called from within the constructor or immediately after the
     *   constructor has returned a new <CODE>SOAPException</CODE>
     *   object. If this <CODE>SOAPException</CODE> object was
     *   created with the constructor {@link #SOAPException(java.lang.Throwable) SOAPException(java.lang.Throwable)}
     *   or {@link #SOAPException(java.lang.String, java.lang.Throwable) SOAPException(java.lang.String, java.lang.Throwable)}, meaning
     *   that its <CODE>cause</CODE> field already has a value, this
     *   method cannot be called even once.
     *
     * @param cause  the <CODE>Throwable</CODE>
     *     object that caused this <CODE>SOAPException</CODE> object
     *     to be thrown. The value of this parameter is saved for
     *     later retrieval by the <A href=
     *     "../../../javax/xml/soap/SOAPException.html#getCause()">
     *     <CODE>getCause()</CODE></A> method. A <TT>null</TT> value
     *     is permitted and indicates that the cause is nonexistent
     *     or unknown.
     * @return a reference to this <CODE>SOAPException</CODE>
     *     instance
     * @throws java.lang.IllegalArgumentException if
     *     <CODE>cause</CODE> is this <CODE>Throwable</CODE> object.
     *     (A <CODE>Throwable</CODE> object cannot be its own
     *     cause.)
     * @throws java.lang.IllegalStateException if this <CODE>
     *     SOAPException</CODE> object was created with {@link #SOAPException(java.lang.Throwable) SOAPException(java.lang.Throwable)}
     *   or {@link #SOAPException(java.lang.String, java.lang.Throwable) SOAPException(java.lang.String, java.lang.Throwable)}, or this
     *     method has already been called on this <CODE>
     *     SOAPException</CODE> object
     */
    public synchronized Throwable initCause(Throwable cause) {

        if (this.cause != null) {
            throw new IllegalStateException("Can't override cause");
        }

        if (cause == this) {
            throw new IllegalArgumentException("Self-causation not permitted");
        } else {
            this.cause = cause;

            return this;
        }
    }

    private Throwable cause;
}

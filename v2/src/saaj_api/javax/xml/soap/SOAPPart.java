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

import javax.xml.transform.Source;
import java.util.Iterator;

/**
 * <P>The container for the SOAP-specific portion of a <CODE>
 * SOAPMessage</CODE> object. All messages are required to have a
 * SOAP part, so when a <CODE>SOAPMessage</CODE> object is
 * created, it will automatically have a <CODE>SOAPPart</CODE>
 * object.</P>
 *
 * <P>A <CODE>SOAPPart</CODE> object is a MIME part and has the
 * MIME headers Content-Id, Content-Location, and Content-Type.
 * Because the value of Content-Type must be "text/xml", a <CODE>
 * SOAPPart</CODE> object automatically has a MIME header of
 * Content-Type with its value set to "text/xml". The value must
 * be "text/xml" because content in the SOAP part of a message
 * must be in XML format. Content that is not of type "text/xml"
 * must be in an <CODE>AttachmentPart</CODE> object rather than in
 * the <CODE>SOAPPart</CODE> object.</P>
 *
 * <P>When a message is sent, its SOAP part must have the MIME
 * header Content-Type set to "text/xml". Or, from the other
 * perspective, the SOAP part of any message that is received must
 * have the MIME header Content-Type with a value of
 * "text/xml".</P>
 *
 * <P>A client can access the <CODE>SOAPPart</CODE> object of a
 * <CODE>SOAPMessage</CODE> object by calling the method <CODE>
 * SOAPMessage.getSOAPPart</CODE>. The following line of code, in
 * which <CODE>message</CODE> is a <CODE>SOAPMessage</CODE>
 * object, retrieves the SOAP part of a message.</P>
 * <PRE>
 * SOAPPart soapPart = message.getSOAPPart();
 * </PRE>
 *
 * <P>A <CODE>SOAPPart</CODE> object contains a <CODE>
 * SOAPEnvelope</CODE> object, which in turn contains a <CODE>
 * SOAPBody</CODE> object and a <CODE>SOAPHeader</CODE> object.
 * The <CODE>SOAPPart</CODE> method <CODE>getEnvelope</CODE> can
 * be used to retrieve the <CODE>SOAPEnvelope</CODE> object.</P>
 */
public abstract class SOAPPart implements org.w3c.dom.Document {

    public SOAPPart() {}

    /**
     * Gets the <CODE>SOAPEnvelope</CODE> object associated with
     * this <CODE>SOAPPart</CODE> object. Once the SOAP envelope is
     * obtained, it can be used to get its contents.
     * @return the <CODE>SOAPEnvelope</CODE> object for this <CODE>
     *     SOAPPart</CODE> object
     * @throws  SOAPException if there is a SOAP error
     */
    public abstract SOAPEnvelope getEnvelope() throws SOAPException;

    /**
     * Retrieves the value of the MIME header whose name is
     * "Content-Id".
     * @return  a <CODE>String</CODE> giving the value of the MIME
     *     header named "Content-Id"
     * @see #setContentId(java.lang.String) setContentId(java.lang.String)
     */
    public String getContentId() {

        String as[] = getMimeHeader("Content-Id");

        if (as != null && as.length > 0) {
            return as[0];
        } else {
            return null;
        }
    }

    /**
     * Retrieves the value of the MIME header whose name is
     * "Content-Location".
     * @return a <CODE>String</CODE> giving the value of the MIME
     *     header whose name is "Content-Location"
     * @see #setContentLocation(java.lang.String) setContentLocation(java.lang.String)
     */
    public String getContentLocation() {

        String as[] = getMimeHeader("Content-Location");

        if (as != null && as.length > 0) {
            return as[0];
        } else {
            return null;
        }
    }

    /**
     * Sets the value of the MIME header named "Content-Id" to
     * the given <CODE>String</CODE>.
     * @param  contentId  a <CODE>String</CODE> giving
     *     the value of the MIME header "Content-Id"
     * @throws java.lang.IllegalArgumentException if
     *     there is a problem in setting the content id
     * @see #getContentId() getContentId()
     */
    public void setContentId(String contentId) {
        setMimeHeader("Content-Id", contentId);
    }

    /**
     * Sets the value of the MIME header "Content-Location" to
     * the given <CODE>String</CODE>.
     * @param  contentLocation a <CODE>String</CODE>
     *     giving the value of the MIME header
     *     "Content-Location"
     * @throws java.lang.IllegalArgumentException if
     *     there is a problem in setting the content location.
     * @see #getContentLocation() getContentLocation()
     */
    public void setContentLocation(String contentLocation) {
        setMimeHeader("Content-Location", contentLocation);
    }

    /**
     * Removes all MIME headers that match the given name.
     * @param  header  a <CODE>String</CODE> giving
     *     the name of the MIME header(s) to be removed
     */
    public abstract void removeMimeHeader(String header);

    /**
     * Removes all the <CODE>MimeHeader</CODE> objects for this
     * <CODE>SOAPEnvelope</CODE> object.
     */
    public abstract void removeAllMimeHeaders();

    /**
     * Gets all the values of the <CODE>MimeHeader</CODE> object
     * in this <CODE>SOAPPart</CODE> object that is identified by
     * the given <CODE>String</CODE>.
     * @param   name  the name of the header; example:
     *     "Content-Type"
     * @return a <CODE>String</CODE> array giving all the values for
     *     the specified header
     * @see #setMimeHeader(java.lang.String, java.lang.String) setMimeHeader(java.lang.String, java.lang.String)
     */
    public abstract String[] getMimeHeader(String name);

    /**
     * Changes the first header entry that matches the given
     *   header name so that its value is the given value, adding a
     *   new header with the given name and value if no existing
     *   header is a match. If there is a match, this method clears
     *   all existing values for the first header that matches and
     *   sets the given value instead. If more than one header has
     *   the given name, this method removes all of the matching
     *   headers after the first one.
     *
     *   <P>Note that RFC822 headers can contain only US-ASCII
     *   characters.</P>
     * @param  name a <CODE>String</CODE> giving the
     *     header name for which to search
     * @param  value a <CODE>String</CODE> giving the
     *     value to be set. This value will be substituted for the
     *     current value(s) of the first header that is a match if
     *     there is one. If there is no match, this value will be
     *     the value for a new <CODE>MimeHeader</CODE> object.
     * @throws java.lang.IllegalArgumentException if
     *     there was a problem with the specified mime header name
     *     or value
     * @throws java.lang.IllegalArgumentException if there was a problem with the specified mime header name or value
     * @see #getMimeHeader(java.lang.String) getMimeHeader(java.lang.String)
     */
    public abstract void setMimeHeader(String name, String value);

    /**
     *  Creates a <CODE>MimeHeader</CODE> object with the specified
     *   name and value and adds it to this <CODE>SOAPPart</CODE>
     *   object. If a <CODE>MimeHeader</CODE> with the specified
     *   name already exists, this method adds the specified value
     *   to the already existing value(s).
     *
     *   <P>Note that RFC822 headers can contain only US-ASCII
     *   characters.</P>
     *
     * @param  name a <CODE>String</CODE> giving the
     *     header name
     * @param  value a <CODE>String</CODE> giving the
     *     value to be set or added
     * @throws java.lang.IllegalArgumentException if
     * there was a problem with the specified mime header name
     *     or value
     */
    public abstract void addMimeHeader(String name, String value);

    /**
     * Retrieves all the headers for this <CODE>SOAPPart</CODE>
     * object as an iterator over the <CODE>MimeHeader</CODE>
     * objects.
     * @return an <CODE>Iterator</CODE> object with all of the Mime
     *     headers for this <CODE>SOAPPart</CODE> object
     */
    public abstract Iterator getAllMimeHeaders();

    /**
     * Retrieves all <CODE>MimeHeader</CODE> objects that match
     * a name in the given array.
     * @param   names a <CODE>String</CODE> array with
     *     the name(s) of the MIME headers to be returned
     * @return all of the MIME headers that match one of the names
     *     in the given array, returned as an <CODE>Iterator</CODE>
     *     object
     */
    public abstract Iterator getMatchingMimeHeaders(String names[]);

    /**
     * Retrieves all <CODE>MimeHeader</CODE> objects whose name
     * does not match a name in the given array.
     * @param   names a <CODE>String</CODE> array with
     *     the name(s) of the MIME headers not to be returned
     * @return all of the MIME headers in this <CODE>SOAPPart</CODE>
     *     object except those that match one of the names in the
     *     given array. The nonmatching MIME headers are returned as
     *     an <CODE>Iterator</CODE> object.
     */
    public abstract Iterator getNonMatchingMimeHeaders(String names[]);

    /**
     * Sets the content of the <CODE>SOAPEnvelope</CODE> object
     * with the data from the given <CODE>Source</CODE> object.
     * @param   source javax.xml.transform.Source</CODE> object with the data to
     *     be set
     * @throws  SOAPException if there is a problem in
     *     setting the source
     * @see #getContent() getContent()
     */
    public abstract void setContent(Source source) throws SOAPException;

    /**
     * Returns the content of the SOAPEnvelope as a JAXP <CODE>
     * Source</CODE> object.
     * @return the content as a <CODE>
     *     javax.xml.transform.Source</CODE> object
     * @throws  SOAPException  if the implementation cannot
     *     convert the specified <CODE>Source</CODE> object
     * @see #setContent(javax.xml.transform.Source) setContent(javax.xml.transform.Source)
     */
    public abstract Source getContent() throws SOAPException;
}

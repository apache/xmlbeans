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
 * A container for <code>DetailEntry</code> objects. <code>DetailEntry</code>
 * objects give detailed error information that is application-specific and
 * related to the <code>SOAPBody</code> object that contains it.
 * <P>
 * A <code>Detail</code> object, which is part of a <code>SOAPFault</code>
 * object, can be retrieved using the method <code>SOAPFault.getDetail</code>.
 * The <code>Detail</code> interface provides two methods. One creates a new
 * <code>DetailEntry</code> object and also automatically adds it to
 * the <code>Detail</code> object. The second method gets a list of the
 * <code>DetailEntry</code> objects contained in a <code>Detail</code>
 * object.
 * <P>
 * The following code fragment, in which <i>sf</i> is a <code>SOAPFault</code>
 * object, gets its <code>Detail</code> object (<i>d</i>), adds a new
 * <code>DetailEntry</code> object to <i>d</i>, and then gets a list of all the
 * <code>DetailEntry</code> objects in <i>d</i>. The code also creates a
 * <code>Name</code> object to pass to the method <code>addDetailEntry</code>.
 * The variable <i>se</i>, used to create the <code>Name</code> object,
 * is a <code>SOAPEnvelope</code> object.
 * <PRE>
 *    Detail d = sf.getDetail();
 *    Name name = se.createName("GetLastTradePrice", "WOMBAT",
 *                                "http://www.wombat.org/trader");
 *    d.addDetailEntry(name);
 *    Iterator it = d.getDetailEntries();
 * </PRE>
 */
public interface Detail extends SOAPFaultElement {

    /**
     * Creates a new <code>DetailEntry</code> object with the given
     * name and adds it to this <code>Detail</code> object.
     * @param   name a <code>Name</code> object identifying the new <code>DetailEntry</code> object
     * @return DetailEntry.
     * @throws SOAPException  thrown when there is a problem in adding a DetailEntry object to this Detail object.
     */
    public abstract DetailEntry addDetailEntry(Name name) throws SOAPException;

    /**
     * Gets a list of the detail entries in this <code>Detail</code> object.
     * @return  an <code>Iterator</code> object over the <code>DetailEntry</code>
     *        objects in this <code>Detail</code> object
     */
    public abstract Iterator getDetailEntries();
}

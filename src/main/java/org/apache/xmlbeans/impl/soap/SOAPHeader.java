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

package org.apache.xmlbeans.impl.soap;

import java.util.Iterator;

/**
 * <P>A representation of the SOAP header element. A SOAP header
 *   element consists of XML data that affects the way the
 *   application-specific content is processed by the message
 *   provider. For example, transaction semantics, authentication
 *   information, and so on, can be specified as the content of a
 *   {@code SOAPHeader} object.</P>
 *
 *   <P>A {@code SOAPEnvelope} object contains an empty {@code
 *   SOAPHeader} object by default. If the {@code
 *   SOAPHeader} object, which is optional, is not needed, it
 *   can be retrieved and deleted with the following line of code.
 *   The variable <I>se</I> is a {@code SOAPEnvelope}
 *   object.</P>
 * <PRE>
 *     se.getHeader().detachNode();
 * </PRE>
 *   A {@code SOAPHeader} object is created with the {@code
 *   SOAPEnvelope} method {@code addHeader}. This method,
 *   which creates a new header and adds it to the envelope, may be
 *   called only after the existing header has been removed.
 * <PRE>
 *     se.getHeader().detachNode();
 *     SOAPHeader sh = se.addHeader();
 * </PRE>
 *
 *   <P>A {@code SOAPHeader} object can have only {@code
 *   SOAPHeaderElement} objects as its immediate children. The
 *   method {@code addHeaderElement} creates a new {@code
 *   HeaderElement} object and adds it to the {@code
 *   SOAPHeader} object. In the following line of code, the
 *   argument to the method {@code addHeaderElement} is a
 *   {@code Name} object that is the name for the new {@code
 *   HeaderElement} object.</P>
 * <PRE>
 *     SOAPHeaderElement shElement = sh.addHeaderElement(name);
 * </PRE>
 * @see SOAPHeaderElement SOAPHeaderElement
 */
public interface SOAPHeader extends SOAPElement {

    /**
     * Creates a new {@code SOAPHeaderElement} object
     * initialized with the specified name and adds it to this
     * {@code SOAPHeader} object.
     * @param   name a {@code Name} object with
     *     the name of the new {@code SOAPHeaderElement}
     *     object
     * @return the new {@code SOAPHeaderElement} object that
     *     was inserted into this {@code SOAPHeader}
     *     object
     * @throws  SOAPException if a SOAP error occurs
     */
    SOAPHeaderElement addHeaderElement(Name name)
        throws SOAPException;

    /**
     * Returns a list of all the {@code SOAPHeaderElement}
     * objects in this {@code SOAPHeader} object that have the
     * the specified actor. An actor is a global attribute that
     * indicates the intermediate parties to whom the message should
     * be sent. An actor receives the message and then sends it to
     * the next actor. The default actor is the ultimate intended
     * recipient for the message, so if no actor attribute is
     * included in a {@code SOAPHeader} object, the message is
     * sent to its ultimate destination.
     * @param   actor  a {@code String} giving the
     *     URI of the actor for which to search
     * @return an {@code Iterator} object over all the {@code
     *     SOAPHeaderElement} objects that contain the
     *     specified actor
     * @see #extractHeaderElements(java.lang.String) extractHeaderElements(java.lang.String)
     */
    Iterator examineHeaderElements(String actor);

    /**
     * Returns a list of all the {@code SOAPHeaderElement}
     *   objects in this {@code SOAPHeader} object that have
     *   the the specified actor and detaches them from this {@code
     *   SOAPHeader} object.
     *
     *   <P>This method allows an actor to process only the parts of
     *   the {@code SOAPHeader} object that apply to it and to
     *   remove them before passing the message on to the next
     *   actor.
     * @param   actor  a {@code String} giving the
     *     URI of the actor for which to search
     * @return an {@code Iterator} object over all the {@code
     *     SOAPHeaderElement} objects that contain the
     *     specified actor
     * @see #examineHeaderElements(java.lang.String) examineHeaderElements(java.lang.String)
     */
    Iterator extractHeaderElements(String actor);

    /**
     * Returns an {@code Iterator} over all the
     * {@code SOAPHeaderElement} objects in this {@code SOAPHeader}
     * object that have the specified actor and that have a MustUnderstand
     * attribute whose value is equivalent to {@code true}.
     *
     * @param actor a {@code String} giving the URI of the actor for which
     *              to search
     * @return an {@code Iterator} object over all the
     *              {@code SOAPHeaderElement} objects that contain the
     *              specified actor and are marked as MustUnderstand
     */
    Iterator examineMustUnderstandHeaderElements(String actor);

    /**
     * Returns an {@code Iterator} over all the
     * {@code SOAPHeaderElement} objects in this {@code SOAPHeader}
     * object.
     *
     * @return an {@code Iterator} object over all the
     *              {@code SOAPHeaderElement} objects contained by this
     *              {@code SOAPHeader}
     */
    Iterator examineAllHeaderElements();

    /**
     * Returns an {@code Iterator} over all the
     * {@code SOAPHeaderElement} objects in this {@code SOAPHeader }
     * object and detaches them from this {@code SOAPHeader} object.
     *
     * @return an {@code Iterator} object over all the
     *              {@code SOAPHeaderElement} objects contained by this
     *              {@code SOAPHeader}
     */
    Iterator extractAllHeaderElements();
}

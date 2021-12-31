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

/**
 *     <P>An object representing the contents in the SOAP header part
 *   of the SOAP envelope. The immediate children of a {@code
 *   SOAPHeader} object can be represented only as {@code
 *   SOAPHeaderElement} objects.</P>
 *
 *   <P>A {@code SOAPHeaderElement} object can have other
 *   {@code SOAPElement} objects as its children.</P>
 */
public interface SOAPHeaderElement extends SOAPElement {

    /**
     * Sets the actor associated with this {@code
     * SOAPHeaderElement} object to the specified actor. The
     * default value of an actor is: {@code
     * SOAPConstants.URI_SOAP_ACTOR_NEXT}
     * @param  actorURI  a {@code String} giving
     *     the URI of the actor to set
     * @see #getActor() getActor()
     * @throws java.lang.IllegalArgumentException if
     *     there is a problem in setting the actor.
     */
    void setActor(String actorURI);

    /**
     * Returns the uri of the actor associated with this {@code
     * SOAPHeaderElement} object.
     * @return  a {@code String} giving the URI of the
     *     actor
     * @see #setActor(java.lang.String) setActor(java.lang.String)
     */
    String getActor();

    /**
     * Sets the mustUnderstand attribute for this {@code
     *   SOAPHeaderElement} object to be on or off.
     *
     *   <P>If the mustUnderstand attribute is on, the actor who
     *   receives the {@code SOAPHeaderElement} must process it
     *   correctly. This ensures, for example, that if the {@code
     *   SOAPHeaderElement} object modifies the message, that
     *   the message is being modified correctly.</P>
     * @param  mustUnderstand  {@code true} to
     *     set the mustUnderstand attribute on; {@code false}
     *     to turn if off
     * @throws java.lang.IllegalArgumentException if
     *     there is a problem in setting the actor.
     * @see #getMustUnderstand() getMustUnderstand()
     */
    void setMustUnderstand(boolean mustUnderstand);

    /**
     * Returns whether the mustUnderstand attribute for this
     * {@code SOAPHeaderElement} object is turned on.
     * @return  {@code true} if the mustUnderstand attribute of
     *     this {@code SOAPHeaderElement} object is turned on;
     *     {@code false} otherwise
     */
    boolean getMustUnderstand();
}

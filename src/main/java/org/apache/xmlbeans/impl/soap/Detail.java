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
 * A container for {@code DetailEntry} objects. {@code DetailEntry}
 * objects give detailed error information that is application-specific and
 * related to the {@code SOAPBody} object that contains it.
 * <P>
 * A {@code Detail} object, which is part of a {@code SOAPFault}
 * object, can be retrieved using the method {@code SOAPFault.getDetail}.
 * The {@code Detail} interface provides two methods. One creates a new
 * {@code DetailEntry} object and also automatically adds it to
 * the {@code Detail} object. The second method gets a list of the
 * {@code DetailEntry} objects contained in a {@code Detail}
 * object.
 * <P>
 * The following code fragment, in which <i>sf</i> is a {@code SOAPFault}
 * object, gets its {@code Detail} object (<i>d</i>), adds a new
 * {@code DetailEntry} object to <i>d</i>, and then gets a list of all the
 * {@code DetailEntry} objects in <i>d</i>. The code also creates a
 * {@code Name} object to pass to the method {@code addDetailEntry}.
 * The variable <i>se</i>, used to create the {@code Name} object,
 * is a {@code SOAPEnvelope} object.
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
     * Creates a new {@code DetailEntry} object with the given
     * name and adds it to this {@code Detail} object.
     * @param   name a {@code Name} object identifying the new {@code DetailEntry} object
     * @return DetailEntry.
     * @throws SOAPException  thrown when there is a problem in adding a DetailEntry object to this Detail object.
     */
    public abstract DetailEntry addDetailEntry(Name name) throws SOAPException;

    /**
     * Gets a list of the detail entries in this {@code Detail} object.
     * @return  an {@code Iterator} object over the {@code DetailEntry}
     *        objects in this {@code Detail} object
     */
    public abstract Iterator<DetailEntry> getDetailEntries();
}

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
 * A {@code SOAPBodyElement} object represents the contents in
 * a {@code SOAPBody} object.  The {@code SOAPFault} interface
 * is a {@code SOAPBodyElement} object that has been defined.
 * <P>
 * A new {@code SOAPBodyElement} object can be created and added
 * to a {@code SOAPBody} object with the {@code SOAPBody}
 * method {@code addBodyElement}. In the following line of code,
 * {@code sb} is a {@code SOAPBody} object, and
 * {@code myName} is a {@code Name} object.
 * <PRE>
 *   SOAPBodyElement sbe = sb.addBodyElement(myName);
 * </PRE>
 */
public interface SOAPBodyElement extends SOAPElement {}

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
package org.apache.xmlbeans.impl.jam.editable.impl.ref;

import org.apache.xmlbeans.impl.jam.JClass;

/**
 * <p>Object which holds a reference to a JClass.  Using this interface
 * (as opposed to referring to the JClass directly) allows us to do lazy
 * type resolution.</p>
 *
 * <p>Note that EClassImpl implements this interface directly (as a reference
 * to itself) as an optimization for the case where we don't need or want
 * lazy type resolution</p>.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface JClassRef {

  public JClass getRefClass();

  public String getQualifiedName();

}

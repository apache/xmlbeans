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

package org.apache.xmlbeans.impl.jam_old.internal;

import org.apache.xmlbeans.impl.jam_old.JClass;
import org.apache.xmlbeans.impl.jam_old.JElement;
import org.apache.xmlbeans.impl.jam_old.JPackage;

/**
 * <p>Adds methods to the JPackage interface that we don't want to
 * expose to API clients.  All JPackage impls must be castable to
 * InternalJPackage.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface InternalJPackage extends JPackage {
  /**
   * <p>Need this so JRootImpl can tell arbitrary package impls that
   * it is their parent.  See JRootImpl constructor.</p>
   */
  public void setParent(JElement parent);

  /**
   * <p>Need this so we can tell arbitrary packages that an arbitrary
   * class is their child.  See JRootImpl constructor.</p>
   */
  public void addClass(JClass clazz);
}



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

package org.apache.xmlbeans.impl.jam;

/**
 * Represents a java package. Provides access to information about the
 * package, the package's comment and tags, and the classes in the
 * package.
 *
 * <p>FIXME how are we handling the default package?  i think it
 * should be a package with an empty string for a name, but we need to
 * make sure this works and document it.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface JPackage extends JElement {

  /**
   * Returns the classes in this package which were specified as part
   * of the JRoot construction.  Note that this does not necessarily
   * include all of the classes that are actually in the package (it
   * may not contain any specified classes at all).
   */
  public JClass[] getClasses();

}

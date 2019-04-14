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
 * <p>Encapsulates the a set of java classes which were which met a set of
 * criteria described in a JamServiceParams object.  A JamService exposes both a
 * particular set of JClasses, as well as a JamClassLoader which can be used
 * to load related classes.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public interface JamService {

  /**
   * Returns a JamClassLoader which can be used to load JClasses from class-
   * and source-file locations specified in the JamServiceParams with which
   * this service was instantiated.  Note that it is possible to use this
   * mechanism to load JClasses that are not returned by getClasses().
   */
  public JamClassLoader getClassLoader();

  /**
   * Returns the names of the classes that were described in the
   * JamServiceParams object used to create this service.  Note that this
   * list will never change for a given service; though it is possible
   * to use the service's JamClassLoader to load other types, this method will
   * always return the initial set of classes.
   */
  public String[] getClassNames();

  /**
   * Returns an iterator of the JClasses named in the array returned by
   * getClassNames().
   */
  public JamClassIterator getClasses();

  /**
   * Returns all of the JClasses returned by getClasses() in a single
   * array.  Use of this method (as opposed to getClasses()) is not advised
   * if you expect to be dealing with a very large set of classes, as it makes
   * it less likely that JClasses will be garbage collected when no longer
   * needed.
   */
  public JClass[] getAllClasses();

  //public void invalidate(JClass clazz);

  //public void invalidateAll();
}

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

import java.util.Iterator;

/**
 * A typed Iterator on a set of JClasses.
 *
 * The use of JamClassIterator (as opposed to arrays or Collections of JClass)
 * is encouraged as it can significantly reduce memory consumption when
 * using JAM to process large numbers of java classes.
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JamClassIterator implements Iterator {

  // ========================================================================
  // Variables

  private JamClassLoader mLoader;
  private String[] mClassNames;
  private int mIndex = 0;

  // ========================================================================
  // Constructor

  /**
   * Constructs a new JamClassIterator
   *
   * @param loader JamClassLoader from which to load the classes
   * @param classes Array of full-qualified classnames to iterate on.
   *
   * @throws IllegalArgumentException if either argument is null.
   */
  public JamClassIterator(JamClassLoader loader, String[] classes) {
    if (loader == null) throw new IllegalArgumentException("null loader");
    if (classes == null) throw new IllegalArgumentException("null classes");
    mLoader = loader;
    mClassNames = classes;
  }

  // ========================================================================
  // Public methods

  /**
   * Returns the next class.  Exactly equivalent to (JClass)next().
   *
   * @throws IndexOutOfBoundsException if there are no classes left to
   * iterate on.
   */
  public JClass nextClass() {
    if (!hasNext()) throw new IndexOutOfBoundsException();
    mIndex++;
    return mLoader.loadClass(mClassNames[mIndex-1]);
  }

  // ========================================================================
  // Iterator implementation

  /**
   * Returns true if classes remain to be iterated upon.
   */
  public boolean hasNext() {
    return mIndex < mClassNames.length;
  }


  /**
   * Returns the next class.
   *
   * @throws IndexOutOfBoundsException if there are no classes left to
   * iterate on.
   */
  public Object next() { return nextClass(); }

  // ========================================================================
  // Public methods

  public int getSize() { return mClassNames.length; }

  // ========================================================================
  // Unsupported methods

  /**
   * Not supported.
   *
   * @throws UnsupportedOperationException
   */
  public void remove() {
    throw new UnsupportedOperationException();
  }
}

/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
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
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
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
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/
package org.apache.xmlbeans.impl.jam;

import java.util.Iterator;

/**
 * A typed Iterator on a set of JClasses.
 *
 * The use of JClassIterator (as opposed to arrays or Collections of JClass)
 * is encouraged as it can significantly reduce memory consumption when
 * using JAM to process large numbers of java classes.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JClassIterator implements Iterator {

  // ========================================================================
  // Variables

  private JClassLoader mLoader;
  private String[] mClassNames;
  private int mIndex = 0;

  // ========================================================================
  // Constructor

  /**
   * Constructs a new JClassIterator
   *
   * @param loader JClassLoader from which to load the classes
   * @param classes Array of full-qualified classnames to iterate on.
   *
   * @throws IllegalArgumentException if either argument is null.
   */
  public JClassIterator(JClassLoader loader, String[] classes) {
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
  // Unsupported methods

  /**
   * @throws UnsupportedOperationException
   */
  public void remove() {
    throw new UnsupportedOperationException();
  }
}

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
package org.apache.xmlbeans.impl.jam.annotation;


/**
 * <p>Base for user-defined annotation classes which provide strongly-typed
 * access to java metadata.  This class implements AnnotationProxy by using
 * reflection get and set properties on the extending class.  See
 * documentation on <code>setMemberValue()</code> and
 * <code>getValue()</code> for details.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class CustomAnnotationProxyBase extends AnnotationProxy {

  // ========================================================================
  // Variables

  private ValueMap mValueMap = null;  // created only on demand

  // ========================================================================
  // Constructors

  protected CustomAnnotationProxyBase() {}

  // ========================================================================
  // Public methods

  /**
   * <p>Sets the member value by introspecting this class and looking for an
   * appropriate setter method.  For example, if the 'name' parameter is
   * 'foo', a method called setFoo will be searched for.  If more than one
   * such method exists, normal java type widening will be performed to select
   * the most appropriate match.  Type conversion will be performed on the
   * 'value' object as necessary.</p>
   *
   * <p>Extending classes are free to override this method if different
   * behavior is required.</p>
   */
  public void setMemberValue(String name, Object value) {
    throw new UnsupportedOperationException("NYI");
  }

  /**
   * <p>Returns an untyped map of the annotation's values.  The map is built
   * by searching for accessor methods on the extending class.  JSR175-style
   * accessors (sans 'get') and java bean getters (with 'get') are both
   * looked for.  If a given property has a method for each style, the
   * 175 style method wins.</p>
   *
   * <p>Extending classes are free to override this method if different
   * behavior is required.</p>
   */
  public ValueMap getValueMap() {
    throw new UnsupportedOperationException("NYI");
  }
}

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

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;


/**
 * <p>Base for user-defined annotation classes which provide strongly-typed
 * access to java metadata.  This class implements AnnotationProxy by using
 * reflection get and set properties on the extending class.  See
 * documentation on <code>setMemberValue()</code> and
 * <code>getValue()</code> for details.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public abstract class TypedAnnotationProxyBase extends AnnotationProxy {

  // ========================================================================
  // Constructors

  protected TypedAnnotationProxyBase() {}

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
    if (name == null) throw new IllegalArgumentException("null name");
    if (value == null) throw new IllegalArgumentException("null value");
    Method m = getSetterFor(name,value.getClass());
    if (m == null) return;
    try {
      m.invoke(this,new Object[] {value});
    } catch (IllegalAccessException e) {
      getLogger().warning(e);
    } catch (InvocationTargetException e) {
      getLogger().warning(e);
    }
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
    //FIXME build it up via reflection, i guess.  Or maybe we should
    //remember what got set via setMemberValue()?  I dunno, it's kind of
    //a weird thing for them to be asking for an untyped version of this
    //annotation for which they've gone to the trouble of building a typed
    //proxy.  Somebody will do it, though, so we need to think about what
    //the right thing to do is and do it.  They can always override if
    //they dont like it.
    throw new UnsupportedOperationException("NYI");
  }

  // ========================================================================
  // Protected methods

  /**
   * <p>Gets the setter that should be used for setting the given member
   * with the given value.  This is part of the setMemberValue()
   * implementation, but is broken out as a separate protected method to
   * provide a convenient override point for extensions to do simple name
   * mappings without having to completely re-implement setMemberValue().</p>
   */
  protected Method getSetterFor(String memberName, Class valueType) {
    try {
      return this.getClass().getMethod("set"+memberName,
                                       new Class[] {valueType});
    } catch(NoSuchMethodException nsme) {
      getLogger().warning(nsme);
      return null;
    }
  }
}

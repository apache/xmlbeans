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

import org.apache.xmlbeans.impl.jam.JAnnotationValue;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.internal.elements.AnnotationValueImpl;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Base for user-defined annotation classes which provide strongly-typed
 * access to java metadata.  This class implements AnnotationProxy by using
 * reflection get and set properties on the extending class.  See
 * documentation on <code>setMemberValue()</code> and
 * <code>getValue()</code> for details.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
/**
 * @deprecated do not use, being deleted
 */
public abstract class TypedAnnotationProxyBase extends AnnotationProxy {

  // ========================================================================
  // Variables

  private List mValues = null;

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
  public void setValue(String name, Object value, JClass type) {
    if (name == null) throw new IllegalArgumentException("null name");
    if (value == null) throw new IllegalArgumentException("null value");
    {
      // hang onto it in case they ask for it later with getValues
      if (mValues == null) mValues = new ArrayList();
      mValues.add(new AnnotationValueImpl
        ((ElementContext)mContext,name,value,type));
    }
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
   * <p>Returns an untyped view of the annotation's values.  These simply
   * reflect the values which have been passed into it from JAM via the
   * setValue method.  This means they will just be a direct reflection of
   * whatever was found on 175 annotation or javadoc tag that is being
   * proxied.</p>
   *
   * <p>Extending classes are encouraged to override this method if different
   * behavior is required.</p>
   */
  public JAnnotationValue[] getValues() {
    if (mValues == null) return new JAnnotationValue[0];
    JAnnotationValue[] out = new JAnnotationValue[mValues.size()];
    mValues.toArray(out);
    return out;
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
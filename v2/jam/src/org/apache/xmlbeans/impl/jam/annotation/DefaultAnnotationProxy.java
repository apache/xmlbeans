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

import java.util.List;
import java.util.ArrayList;

import org.apache.xmlbeans.impl.jam.internal.elements.AnnotationValueImpl;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import org.apache.xmlbeans.impl.jam.JAnnotationValue;
import org.apache.xmlbeans.impl.jam.JClass;

/**
 * <p>Implementation of AnnotationProxy which is used when no user-defined
 * type has been registered for a given annotation..  All it does is stuff
 * values into a ValueMap.  Note that it inherits all of the default tag and
 * annotation processing behaviors from AnnotationProxy.</p>
 *
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class DefaultAnnotationProxy extends AnnotationProxy {

  // ========================================================================
  // Variables

  private List mValues = new ArrayList();

  // ========================================================================
  // Constructors

  public DefaultAnnotationProxy() {}

  // ========================================================================
  // Public methods

  public JAnnotationValue[] getValues() {
    JAnnotationValue[] out = new JAnnotationValue[mValues.size()];
    mValues.toArray(out);
    return out;
  }

  // ========================================================================
  // TypedAnnotationProxyBase implementation

  /**
   * <p>Overrides this behavior to simply stuff the value into our
   * annotation map.  The super class' implementation would try to
   * find a bunch of setters that we don't have.</p>
   */
  public void setValue(String name, Object value, JClass type) {
    if (name == null) throw new IllegalArgumentException("null name");
    if (type == null) throw new IllegalArgumentException("null type");
    if (value == null) throw new IllegalArgumentException("null value");
    name = name.trim();
    mValues.add(new AnnotationValueImpl((ElementContext)getLogger(),//yikes, nasty.  FIXME
                                        name,value,type));
  }
}


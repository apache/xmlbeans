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
  public void setValue(String name, Object value) {
    mValues.add(new AnnotationValueImpl((ElementContext)getLogger(),//yikes, nasty.  FIXME
                                        name,value));
  }


  /**
   * Introspects the src object for annotation member methods, invokes them
   * and creates corresponding EAnnotationMembers in the given dest object.

  private void populateAnnotationMembers(EAnnotation dest,
                                         Object src,
                                         Class srcClass)
  {
    Method[] methods = srcClass.getDeclaredMethods();
    for(int i=0; i<methods.length; i++) {
      if (methods[i].getParameterTypes().length > 0) continue;
      EAnnotationMember member = dest.addNewMember();
      member.setSimpleName(methods[i].getName());
      try {
        member.setValue(methods[i].invoke(src,null));
      } catch(IllegalAccessException iae) {
        iae.printStackTrace(); // this is not expected
      } catch(InvocationTargetException ite) {
        ite.printStackTrace();
      }
    }
    //REVIEW will it be a superclass or an interface?  this might be broken
    srcClass = srcClass.getSuperclass();
    if (srcClass != null &&
            !srcClass.getName().equals("java.lang.annotation.Annotation") &&
            !srcClass.getName().equals("java.lang.Object")) {
      populateAnnotationMembers(dest,src,srcClass);
    }
  }
   */

}


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
package org.apache.xmlbeans.impl.jam.visitor;

import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JProperty;
import org.apache.xmlbeans.impl.jam.internal.elements.PropertyImpl;
import org.apache.xmlbeans.impl.jam.mutable.MClass;
import org.apache.xmlbeans.impl.jam.mutable.MMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class PropertyInitializer extends MVisitor {

  // ========================================================================
  // Element visitor implementation

  public void visit(MClass clazz) {
    Map name2prop = new HashMap();
    MMethod[] methods = clazz.getMutableMethods();

    for(int i=0; i<methods.length; i++) {
      String name = methods[i].getSimpleName();
      //
      // process getters
      //
      if (name.startsWith("get") && name.length() > 3 ||
        name.startsWith("is") && name.length() > 2) {
        JClass typ = methods[i].getReturnType();
        if (typ == null) continue; // must have a typ and have
        if (methods[i].getParameters().length > 0) continue; //no params
        if (name.startsWith("get")) {
          name = name.substring(3);
        } else {
          name = name.substring(2);
        }
        JProperty prop = (JProperty)name2prop.get(name);
        if (prop == null) {
          prop = clazz.addNewProperty(name,methods[i],null);
          name2prop.put(name,prop);
        } else {
          if (typ.equals(prop.getType())) {
            ((PropertyImpl)prop).setGetter(methods[i]); // cheater
          }
        }
      }

      //
      // process setters
      //
      if (name.startsWith("set") && name.length() > 3) {
        if (methods[i].getParameters().length != 1) continue; //1 param reqd
        JClass type = methods[i].getParameters()[0].getType();
        name = name.substring(3);
        JProperty prop = (JProperty)name2prop.get(name);
        if (prop == null) {
          prop = clazz.addNewProperty(name,methods[i],null);
          name2prop.put(name,prop);
        } else {
          if (type.equals(prop.getType())) {
            // if it's the same type, cool - just add the getter
            ((PropertyImpl)prop).setSetter(methods[i]); // with a sneaky cast
          }
        }
      }
    }
  }
}

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

package org.apache.xmlbeans.impl.binding.bts;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collection;

/**
 * Base class
 */
public abstract class BaseBindingLoader implements BindingLoader {

  // ========================================================================
  // Variables

  private final Map bindingTypes = new LinkedHashMap();    // name-pair -> BindingType
  private final Map xmlFromJava = new LinkedHashMap();     // javaName -> pair
  private final Map xmlFromJavaElement = new LinkedHashMap(); // javaName -> pair (xml element)
  private final Map javaFromXmlPojo = new LinkedHashMap(); // xmlName -> pair (pojo)
  private final Map javaFromXmlObj = new LinkedHashMap();  // xmlName -> pair (xmlobj)

  // ========================================================================
  // BindingLoader implementation

  public BindingType getBindingType(BindingTypeName btName) {
    return (BindingType) bindingTypes.get(btName);
  }

  public BindingTypeName lookupPojoFor(XmlTypeName xName) {
    return (BindingTypeName) javaFromXmlPojo.get(xName);
  }

  public BindingTypeName lookupXmlObjectFor(XmlTypeName xName) {
    return (BindingTypeName) javaFromXmlObj.get(xName);
  }

  public BindingTypeName lookupTypeFor(JavaTypeName jName) {
    return (BindingTypeName) xmlFromJava.get(jName);
  }

  public BindingTypeName lookupElementFor(JavaTypeName jName) {
    return (BindingTypeName) xmlFromJavaElement.get(jName);
  }

  // ========================================================================
  // Other public methods

  public Collection bindingTypes() {
    return bindingTypes.values();
  }

  public Collection typeMappedJavaTypes() {
    return xmlFromJava.keySet();
  }

  public Collection elementMappedJavaTypes() {
    return xmlFromJavaElement.keySet();
  }

  public Collection pojoMappedXmlTypes() {
    return javaFromXmlPojo.keySet();
  }

  public Collection xmlObjectMappedXmlTypes() {
    return javaFromXmlObj.keySet();
  }


  // ========================================================================
  // Protected methods

  protected void addBindingType(BindingType bType) {
    bindingTypes.put(bType.getName(), bType);
  }

  protected void addPojoFor(XmlTypeName xName, BindingTypeName btName) {
    assert(!btName.getJavaName().isXmlObject());
    javaFromXmlPojo.put(xName, btName);
  }

  protected void addXmlObjectFor(XmlTypeName xName, BindingTypeName btName) {
    assert(btName.getJavaName().isXmlObject());
    javaFromXmlObj.put(xName, btName);
  }

  protected void addTypeFor(JavaTypeName jName, BindingTypeName btName) {
    assert(btName.getXmlName().isSchemaType());
    xmlFromJava.put(jName, btName);
  }

  protected void addElementFor(JavaTypeName jName, BindingTypeName btName) {
    assert(btName.getXmlName().getComponentType() == XmlTypeName.ELEMENT) :
            "not an element: " + btName;
    xmlFromJavaElement.put(jName, btName);
  }
}

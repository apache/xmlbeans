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

package org.apache.xmlbeans.impl.jam.annogen.internal.joust;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class AnnotationImpl implements Annotation {

  // ========================================================================
  // Variables

  private Map mNameToValue = new HashMap();
  private String mType;
  //we want to remember the order they were added in
  private List mKeyList = new ArrayList();

  // ========================================================================
  // Constructors

  /*package*/ AnnotationImpl(String type) {
    if (type == null) throw new IllegalArgumentException("null type");
    mType = type;
  }

  // ========================================================================
  // Package methods

  /*package*/ String getType() { return mType; }

  /*package*/ Iterator getPropertyNames() {
    return mKeyList.iterator();
  }

  /*package*/ Object getValue(String name) {
    return mNameToValue.get(name);
  }

  /*package*/ String getValueDeclaration(String name) {
    Object o = getValue(name);
    if (o == null) return null;
    if (o instanceof String) return "\""+o+"\"";
    if (o instanceof Character) return "'"+o+"'";
    return o.toString();
  }

  // ========================================================================
  // Annotation implementation

  public void setValue(String name, Annotation ann) {
    add(name,ann);
  }

  public void setValue(String name, boolean value) {
    add(name,Boolean.valueOf(value));
  }

  public void setValue(String name, String value) {
    add(name,value);
  }

  public void setValue(String name, byte value) {
    add(name,new Byte(value));
  }

  public void setValue(String name, int value) {
    add(name,new Integer(value));
  }

  public void setValue(String name, long value) {
    add(name,new Long(value));
  }

  public void setValue(String name, char value) {
    add(name,new Character(value));
  }

  // ========================================================================
  // Private methods

  private void add(String name, Object value) {
    if (name == null) throw new IllegalArgumentException("null name");
    if (value == null) throw new IllegalArgumentException("null value");
    mKeyList.add(name);
    mNameToValue.put(name,value);
  }
}
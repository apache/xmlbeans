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

import java.io.Serializable;

/**
 * Represents a property.  Every property corresponds to a
 * Java getter/setter or a field.  On the XML side, there
 * are different forms of properties, some which bind based
 * on sequencing, and others which bind based on name.
 */
public abstract class BindingProperty
  implements Serializable
{

  // ========================================================================
  // Variables

  private BindingTypeName typeName;
  private MethodName getter;
  private MethodName setter;
  private MethodName issetter;
  private String field;

  private JavaTypeName collection;
  private JavaInstanceFactory javaInstanceFactory;

  private static final long serialVersionUID = 1L;

  // ========================================================================
  // Constructors

  /**
   * This kind of constructor is used when making a new one out of the blue.
   *
   * Subclasses should call super(..) when defining constructors that init new BindingTypes.
   */
  protected BindingProperty()
  {
  }

  // ========================================================================
  // Public methods

  public boolean isField()
  {
    return getField() != null;
  }

  public BindingTypeName getTypeName()
  {
    return typeName;
  }

  public void setBindingType(BindingType bType)
  {
    setTypeName(bType.getName());
  }

  public MethodName getGetterName()
  {
    return isField() ? null : getGetter();
  }

  public void setGetterName(MethodName mn)
  {
    setGetter(mn);
  }

  public boolean hasSetter()
  {
    return !isField() && getSetter() != null;
  }

  public MethodName getSetterName()
  {
    return isField() ? null : getSetter();
  }

  public void setSetterName(MethodName mn)
  {
    setSetter(mn);
  }

  public boolean hasIssetter()
  {
    return !isField() && getIssetter() != null;
  }

  public MethodName getIssetterName()
  {
    return isField() ? null : getIssetter();
  }

  public void setIssetterName(MethodName mn)
  {
    setIssetter(mn);
  }

  public String getFieldName()
  {
    return getField();
  }

  public void setFieldName(String field)
  {
    this.setField(field);
  }

  public JavaTypeName getCollectionClass()
  {
    return getCollection();
  }

  public void setCollectionClass(JavaTypeName jName)
  {
    setCollection(jName);
  }

  public String toString()
  {
    return getClass().getName() +
      " [" +
      (isField() ? getFieldName() : getGetterName().getSimpleName()) +
      "]";
  }

  public JavaInstanceFactory getJavaInstanceFactory()
  {
    return javaInstanceFactory;
  }

  public void setJavaInstanceFactory(JavaInstanceFactory javaInstanceFactory)
  {
    this.javaInstanceFactory = javaInstanceFactory;
  }

  public void setTypeName(BindingTypeName typeName)
  {
    this.typeName = typeName;
  }

  public MethodName getGetter()
  {
    return getter;
  }

  public void setGetter(MethodName getter)
  {
    this.getter = getter;
  }

  public MethodName getSetter()
  {
    return setter;
  }

  public void setSetter(MethodName setter)
  {
    this.setter = setter;
  }

  public MethodName getIssetter()
  {
    return issetter;
  }

  public void setIssetter(MethodName issetter)
  {
    this.issetter = issetter;
  }

  public String getField()
  {
    return field;
  }

  public void setField(String field)
  {
    this.field = field;
  }

  public JavaTypeName getCollection()
  {
    return collection;
  }

  public void setCollection(JavaTypeName collection)
  {
    this.collection = collection;
  }
}

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
package org.apache.xmlbeans.impl.jam.editable;

import org.apache.xmlbeans.impl.jam.JClass;

/**
 * Editable representation of a java class or interface.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface EClass extends EMember, JClass {

  /**
   * Sets the class which this class extends.  Pass null to make the class
   * extend nothing.
   *
   * @throws IllegalArgumentException if the name is not a valid class name.
   */
  public void setSuperclass(String qualifiedClassName);

  /**
   * Sets the class which this class extends.  Pass null to make the class
   * extend nothing.
   *
   * @throws IllegalArgumentException if the given class cannot be extended
   * (i.e. final classes, interfaces, void, primitives, arrays).
   */
  public void setSuperclass(JClass clazz);

  /**
   * Adds to the list of interfaces implemented by this class.
   *
   * @throws IllegalArgumentException if the parameter is null or is not
   * a valid class name.
   */
  public void addInterface(String className);

  /**
   * Adds to the list of interfaces implemented by this class.
   *
   * @throws IllegalArgumentException if the given class cannot be implemented
   * (i.e. is not an interface).
   */
  public void addInterface(JClass interf);

  /**
   * Removes a named interface from the list of interfaces implemented by
   * this class.  Does nothing if the class does not implement the named
   * interface.
   *
   * @throws IllegalArgumentException if the parameter is null or is not
   * a valid class name.
   */
  public void removeInterface(String className);

  /**
   * Removes an interface from the list of interfaces implemented by
   * this class.  Does nothing if the class does not implement the named
   * interface.
   *
   * @throws IllegalArgumentException if the parameter is null or is not
   * an interface.
   */
  public void removeInterface(JClass interf);

  /**
   * Creates a new constructor, adds it to this class, and returns it.
   */
  public EConstructor addNewConstructor();

  /**
   * Removes a constructor from this class.  Does nothing if the given
   * constructor is not on this class.
   */
  public void removeConstructor(EConstructor constr);

  /**
   * Returns the constructors declared on this class.  This does not include
   * constructors from any base class or interface.  This is simply a more
   * strongly-typed version of getDeclaredConstructors().
   */
  public EConstructor[] getEditableConstructors();

  /**
   * Creates a new field, adds it to this class, and returns it.
   */
  public EField addNewField(String typeName, String fieldName);

  /**
   * Creates a new field, adds it to this class, and returns it.
   */
  public EField addNewField(JClass type, String name);

  /**
   * Removes the given field from this class.  Does nothing if this class
   * does not contain the field.
   */
  public void removeField(EField field);

  /**
   * Returns the fields declared on this class.  This does not include
   * fields from any base class or interface.  This is simply a more
   * strongly-typed version of getDeclaredFields().
   */
  public EField[] getEditableFields();

  /**
   * Creates a new method, adds it to this class, and returns it.
   */
  public EMethod addNewMethod(String name);

  /**
   * Removes the given method from this class.  Does nothing if this class
   * does not contain the method.
   */
  public void removeMethod(EMethod method);

  /**
   * Returns the EditableMethods declared on this class.  This does not include
   * methods from any base class or interface.  This is simply a more
   * strongly-typed version of getDeclaredMethods().
   */
  public EMethod[] getEditableMethods();

  // not sure that these are something we want to do.  is a property really
  // an inherent part of a java type?

//  public EProperty createProperty(EMethod getter, EMethod setter);

//  public void removeProperty(EProperty prop);
}
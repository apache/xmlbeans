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
import org.apache.xmlbeans.impl.jam.JInvokable;

/**
 * <p>Editable representation of a member which can be invoked, i.e.
 * and EMethod or an EConstructor.</p>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface EInvokable extends JInvokable, EMember {
  /**
   * <p>Adds a declaration of a checked exception of the given type.</p>
   *
   * @throws java.lang.IllegalArgumentException if the parameter is null or is not
   * a valid class name.
   */
  public void addException(String qualifiedClassName);

  /**
   * <p>Adds a declaration of a checked exception of the given type.</p>
   *
   * @throws java.lang.IllegalArgumentException if the parameter is null or represents
   * a class which does not extend throwable.
   */
  public void addException(JClass exceptionClass);

  /**
   * Removes a declaration of a checked exception of the named class.  Does
   * nothing if no such declaration exists.
   *
   * @throws java.lang.IllegalArgumentException if the parameter is null or is not
   * a valid class name.
   */
  public void removeException(String qualifiedClassName);

  /**
   * Removes a declaration of a checked exception of the given class.  Does
   * nothing if no such declaration exists.
   *
   * @throws java.lang.IllegalArgumentException if the parameter is null.
   */
  public void removeException(JClass exceptionClass);

  /**
   * Creates a new parameter on this method of type java.lang.Object and
   * with a default name.
   */
  public EParameter addNewParameter();

  /**
   * Removes the given parameter.  Does nothing if the parameter is not
   * present on this method.
   *
   * @throws java.lang.IllegalArgumentException if either parameter is null.
   */
  public void removeParameter(EParameter parameter);

  /**
   * Returns all of the parameters on this method, or an empty array if there
   * are none.  This is simply a more strongly-typed version of
   * getParameters().
   */
  public EParameter[] getEditableParameters();
}

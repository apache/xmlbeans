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

package org.apache.xmlbeans.impl.jam;

/**
 * Object which can load representations of a java.lang.Class.
 * Analagous to a java.lang.ClassLoader.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface JClassLoader {

  /**
   * <p>Returns a representation of the named class.  If the class is
   * not under the same root as this JElement root, a representation
   * of it will be synthesized via reflection (see note in class
   * comments).  The 'named' parameter must be a fully-qualified class
   * name in the classfile 'Field Descriptor' format, a simple
   * primitive name (e.g. 'long' or 'int'), or 'void'.</p>
   *
   * <p>A note regarding fully-qualified class names: if you're
   * looking up a non-array type by name, you can just pass the
   * regular, fully-qualified name.  If you're looking up an array
   * type, you need to use the 'Field Descriptor' format as described
   * in secion 4.3.2 of the VM spec.  This is the same name format
   * that is returned by JClass.getFieldDescriptor.</p>
   *
   * <p>Note that this method always returns a class.  If neither a
   * sourcefile not a classfile could be located for the named class,
   * a stubbed-out JClass will be returned with the isUnresolved()
   * flag set to true.  This JClass will have a name (as determined by
   * the given descriptor), but no other information about it will be
   * available.</p>
   */
  public JClass loadClass(String fieldDescriptor);

  public JPackage getPackage(String name);

  /**
   * Returns the JAnnotationLoader which should be used for retrieving
   * supplemental annotations
   */
  public JAnnotationLoader getAnnotationLoader();


  /**
   * Returns this JClassLoaders's parent.
   */
  public JClassLoader getParent();


}

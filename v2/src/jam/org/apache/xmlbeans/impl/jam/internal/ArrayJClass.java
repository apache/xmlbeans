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

package org.apache.xmlbeans.impl.jam.internal;

import org.apache.xmlbeans.impl.jam.*;

import java.io.StringWriter;


/**
 * JClass for array types.  These are synthesized at runtime by the
 * JAM framework
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public final class ArrayJClass extends BuiltinJClass {

  // ========================================================================
  // Variables

  private int mDimensions;
  private JClass mComponentType;

  // ========================================================================
  // Factory methods

  public static JClass createClassFor(String arrayFD, JClassLoader loader)
          throws ClassNotFoundException {
    if (!arrayFD.startsWith("[")) {
      throw new IllegalArgumentException("must be an array type: " + arrayFD);
    }
    // if it's an array type, we have to be careful
    String componentType;
    if (arrayFD.endsWith(";")) {
      // if it's an array of complex types, we need to construct
      // an ArrayJClass wrapper and go back into the context to
      // get the component type, since a source description for it
      // might be available
      int dims = arrayFD.indexOf("L");
      if (dims != -1 && dims < arrayFD.length() - 2) {
        componentType = arrayFD.substring(dims + 1, arrayFD.length() - 1);
        return new ArrayJClass(loader.loadClass(componentType), dims);
      } else {
        // name is malformed
        throw new ClassNotFoundException(arrayFD);
      }
    } else {
      int dims = arrayFD.lastIndexOf("[") + 1;
      JClass primType = PrimitiveJClass.getPrimitiveClassForName
              (arrayFD.substring(dims, dims + 1));
      if (primType == null) throw new ClassNotFoundException(arrayFD);
      return new ArrayJClass(primType, dims);
    }
  }

  // ========================================================================
  // Constructors

  /**
   * Constructs a JDClass for the given ClassDoc in the given context.
   */
  private ArrayJClass(JClass componentType, int dimensions) {
    if (dimensions < 1) {
      throw new IllegalArgumentException("dimensions=" + dimensions);
    }
    if (componentType == null) {
      throw new IllegalArgumentException("null componentType");
    }
    mComponentType = componentType;
    mDimensions = dimensions;
  }

  // ========================================================================
  // JElement implementation

  public JElement getParent() {
    return null;
  }

  public JElement[] getChildren() {
    return null;
  }

  public String getSimpleName() {
    String out = getQualifiedName();
    int lastDot = out.lastIndexOf('.');
    return (lastDot == -1) ? out : out.substring(lastDot + 1);
  }

  public String getQualifiedName() {
    StringWriter out = new StringWriter();
    out.write(mComponentType.getQualifiedName());
    for (int i = 0; i < mDimensions; i++) out.write("[]");
    return out.toString();
  }

  public JAnnotation[] getAnnotations() {
    return BaseJElement.NO_ANNOTATION;
  }

  public JAnnotation[] getAnnotations(String named) {
    return BaseJElement.NO_ANNOTATION;
  }

  public JAnnotation getAnnotation(String named) {
    return null;
  }

  public JComment[] getComments() {
    return BaseJElement.NO_COMMENT;
  }

  // ========================================================================
  // JMember implementation

  public int getModifiers() {
    return mComponentType.getModifiers();
  }

  public boolean isPackagePrivate() {
    return mComponentType.isPackagePrivate();
  }

  public boolean isProtected() {
    return mComponentType.isProtected();
  }

  public boolean isPublic() {
    return mComponentType.isPublic();
  }

  public boolean isPrivate() {
    return mComponentType.isPrivate();
  }

  public JSourcePosition getSourcePosition() {
    return null;
  }

  public JClass getContainingClass() {
    return null;
  }

  // ========================================================================
  // JClass implementation

  public JClassLoader getClassLoader() {
    return mComponentType.getClassLoader();
  }

  public JClass forName(String fd) throws ClassNotFoundException {
    return mComponentType.forName(fd);
  }

  public boolean isArray() {
    return true;
  }

  public JClass getArrayComponentType() {
    return mComponentType;
  }

  public int getArrayDimensions() {
    return mDimensions;
  }

  public JClass getSuperclass() {
    return ObjectJClass.getInstance();
  }

  public boolean isAssignableFrom(JClass c) {
    return c.isArray() &&
            (c.getArrayDimensions() == mDimensions) &&
            (mComponentType.isAssignableFrom(c.getArrayComponentType()));
  }

  public String getFieldDescriptor() {
    //REVIEW should we cache this result?
    StringWriter out = new StringWriter();
    for (int i = 0; i < mDimensions; i++) out.write("[");
    if (mComponentType.isPrimitive()) {
      out.write(mComponentType.getFieldDescriptor());
    } else {
      out.write("L");
      out.write(mComponentType.getQualifiedName());
      out.write(";");
    }
    return out.toString();
  }
}

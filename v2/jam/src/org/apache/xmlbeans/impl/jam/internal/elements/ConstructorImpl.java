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

package org.apache.xmlbeans.impl.jam.internal.elements;

import org.apache.xmlbeans.impl.jam.visitor.MVisitor;
import org.apache.xmlbeans.impl.jam.visitor.JVisitor;
import org.apache.xmlbeans.impl.jam.mutable.MConstructor;
import org.apache.xmlbeans.impl.jam.JParameter;

import java.lang.reflect.Modifier;
import java.io.StringWriter;

/**
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public final class ConstructorImpl extends InvokableImpl implements MConstructor {

  // ========================================================================
  // Constructors

  /*package*/ ConstructorImpl(ClassImpl containingClass) {
    super(containingClass);
    setSimpleName(containingClass.getSimpleName());
  }

  // ========================================================================
  // JElement implementation

  public void accept(MVisitor visitor) { visitor.visit(this); }

  public void accept(JVisitor visitor) { visitor.visit(this); }

  public String getQualifiedName() {
    StringWriter sbuf = new StringWriter();
    sbuf.write(Modifier.toString(getModifiers()));
    sbuf.write(' ');
    sbuf.write(getSimpleName());
    sbuf.write('(');
    {
    JParameter[] params = getParameters();
    if (params != null && params.length > 0) {
      for(int i=0; i<params.length; i++) {
        sbuf.write(params[i].getType().getQualifiedName());
        if (i<params.length-1) sbuf.write(',');
      }
    }
    }
    sbuf.write(')');
    /* REVIEW the docs on java.lang.reflect.Constructor don't say include
       the exceptions.  That seems wrong, but we'll go with it for now.
    {
      JClass[] thrown = getExceptionTypes();
      if (thrown != null && thrown.length > 0) {
        sbuf.write(" throws ");
        for(int i=0; i<thrown.length; i++) {
          sbuf.write(thrown[i].getQualifiedName());
          if (i<thrown.length-1) sbuf.write(',');
        }
      }
    }
    */
    return sbuf.toString();
  }

}
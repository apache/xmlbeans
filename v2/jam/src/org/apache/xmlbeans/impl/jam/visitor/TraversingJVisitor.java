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

import org.apache.xmlbeans.impl.jam.JPackage;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JConstructor;
import org.apache.xmlbeans.impl.jam.JField;
import org.apache.xmlbeans.impl.jam.JMethod;
import org.apache.xmlbeans.impl.jam.JParameter;
import org.apache.xmlbeans.impl.jam.JAnnotation;
import org.apache.xmlbeans.impl.jam.JComment;
import org.apache.xmlbeans.impl.jam.JAnnotatedElement;
import org.apache.xmlbeans.impl.jam.JInvokable;

/**
 * <p>Calls accept() with the given visitor, and then recursively calls
 * acceptAndWalk for each of our component elements2, if any.  Calling this
 * on an MClass will cause the MClass to accept the visitor, and then
 * all of it's declared fields constructors, and methods.  The parameter
 * for each constructor and method will also in turn be accepted.
 * Any annotations for each of these elements types will also be visited
 * after their other children have been visited.  Note that inherited
 * members are never visited, nor are referenced classes (e.g. referenced
 * via inheritance or member types).  </p>
 *
 * <table border='1'>
 * <tr><td><b>Element</b></td><td><b>Sub-elements traversal</b></td></tr>
 * <tr><td>Package       </td><td>Classes, Annotations, Comments</td></tr>
 * <tr><td>Class         </td><td>Fields, Constructors, Methods, Annotations, Comments</td></tr>
 * <tr><td>Field         </td><td>Annotations, Comments</td></tr>
 * <tr><td>Constructor   </td><td>Parameters, Annotations, Comments</td></tr>
 * <tr><td>Method        </td><td>Parameters, Annotations, Comments</td></tr>
 * <tr><td>Parameter     </td><td>Annotations, Comments</td></tr>
 * <tr><td>Annotation    </td><td>[none]</td></tr>
 * <tr><td>Comment       </td><td>[none]</td></tr>
 * </table>
 *
 * </p>
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class TraversingJVisitor extends JVisitor {

  // ========================================================================
  // Variables

  private JVisitor mDelegate;

  // ========================================================================
  // Constructors

  public TraversingJVisitor(JVisitor jv) {
    if (jv == null) throw new IllegalArgumentException("null jv");
    mDelegate = jv;
  }

  // ========================================================================
  // JVisitor implementation

  public void visit(JPackage pkg) {
    pkg.accept(mDelegate);
    JClass[] c = pkg.getClasses();
    for(int i=0; i<c.length; i++) visit(c[i]);
    visitAnnotations(pkg);
    visitComment(pkg);
  }

  public void visit(JClass clazz) {
    clazz.accept(mDelegate);
    {
      JField[] f = clazz.getDeclaredFields();
      for(int i=0; i<f.length; i++) visit(f[i]);
    }{
      JConstructor[] c = clazz.getConstructors();
      for(int i=0; i<c.length; i++) visit(c[i]);
    }{
      JMethod[] m = clazz.getMethods();
      for(int i=0; i<m.length; i++) visit(m[i]);
    }
    visitAnnotations(clazz);
    visitComment(clazz);
  }

  // ========================================================================
  // JElement implementation

  public void visit(JField field) {
    field.accept(mDelegate);
    visitAnnotations(field);
    visitComment(field);
  }

  public void visit(JConstructor ctor) {
    ctor.accept(mDelegate);
    visitParameters(ctor);
    visitAnnotations(ctor);
    visitComment(ctor);
  }

  public void visit(JMethod method) {
    method.accept(mDelegate);
    visitParameters(method);
    visitAnnotations(method);
    visitComment(method);
  }

  public void visit(JParameter param) {
    param.accept(mDelegate);
    visitAnnotations(param);
    visitComment(param);
  }

  public void visit(JAnnotation ann) { ann.accept(mDelegate); }

  public void visit(JComment comment) { comment.accept(mDelegate); }

  // ========================================================================
  // Private methods

  private void visitParameters(JInvokable iv) {
    JParameter[] p = iv.getParameters();
    for(int i=0; i<p.length; i++) visit(p[i]);
  }

  private void visitAnnotations(JAnnotatedElement ae) {
    JAnnotation[] anns = ae.getAnnotations();
    for(int i=0; i<anns.length; i++) visit(anns[i]);
  }

  private void visitComment(JAnnotatedElement e) {
    JComment c = e.getComment();
    if (c != null) visit(c);
  }
}
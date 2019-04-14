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

import org.apache.xmlbeans.impl.jam.mutable.MAnnotatedElement;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotation;
import org.apache.xmlbeans.impl.jam.mutable.MClass;
import org.apache.xmlbeans.impl.jam.mutable.MComment;
import org.apache.xmlbeans.impl.jam.mutable.MConstructor;
import org.apache.xmlbeans.impl.jam.mutable.MField;
import org.apache.xmlbeans.impl.jam.mutable.MInvokable;
import org.apache.xmlbeans.impl.jam.mutable.MMethod;
import org.apache.xmlbeans.impl.jam.mutable.MPackage;
import org.apache.xmlbeans.impl.jam.mutable.MParameter;

/**
 * <p>An adaptor which helps another MVisitor visit a JElement and its
 * children, recursively.  Note that inherited class or annotations members
 * are never visited, nor are referenced classes (e.g. referenced via member
 * types).  The following table lists each element and the child types
 * which are traversed.</p>

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
public class TraversingMVisitor extends MVisitor {

  // ========================================================================
  // Variables

  private MVisitor mDelegate;

  // ========================================================================
  // Constructors

  public TraversingMVisitor(MVisitor jv) {
    if (jv == null) throw new IllegalArgumentException("null jv");
    mDelegate = jv;
  }

  // ========================================================================
  // JVisitor implementation

  public void visit(MPackage pkg) {
    pkg.accept(mDelegate);
    MClass[] c = pkg.getMutableClasses();
    for(int i=0; i<c.length; i++) visit(c[i]);
    visitAnnotations(pkg);
    visitComment(pkg);
  }

  public void visit(MClass clazz) {
    clazz.accept(mDelegate);
    {
      MField[] f = clazz.getMutableFields();
      for(int i=0; i<f.length; i++) visit(f[i]);
    }{
      MConstructor[] c = clazz.getMutableConstructors();
      for(int i=0; i<c.length; i++) visit(c[i]);
    }{
      MMethod[] m = clazz.getMutableMethods();
      for(int i=0; i<m.length; i++) visit(m[i]);
    }
    visitAnnotations(clazz);
    visitComment(clazz);
  }

  // ========================================================================
  // MElement implementation

  public void visit(MField field) {
    field.accept(mDelegate);
    visitAnnotations(field);
    visitComment(field);
  }

  public void visit(MConstructor ctor) {
    ctor.accept(mDelegate);
    visitParameters(ctor);
    visitAnnotations(ctor);
    visitComment(ctor);
  }

  public void visit(MMethod method) {
    method.accept(mDelegate);
    visitParameters(method);
    visitAnnotations(method);
    visitComment(method);
  }

  public void visit(MParameter param) {
    param.accept(mDelegate);
    visitAnnotations(param);
    visitComment(param);
  }

  public void visit(MAnnotation ann) { ann.accept(mDelegate); }

  public void visit(MComment comment) { comment.accept(mDelegate); }

  // ========================================================================
  // Private methods

  private void visitParameters(MInvokable iv) {
    MParameter[] p = iv.getMutableParameters();
    for(int i=0; i<p.length; i++) visit(p[i]);
  }

  private void visitAnnotations(MAnnotatedElement ae) {
    MAnnotation[] anns = ae.getMutableAnnotations();
    for(int i=0; i<anns.length; i++) visit(anns[i]);
  }

  private void visitComment(MAnnotatedElement e) {
    MComment c = e.getMutableComment();
    if (c != null) visit(c);
  }
}
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

import org.apache.xmlbeans.impl.jam.mutable.*;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class CompositeElementVisitor extends ElementVisitor {

  // ========================================================================
  // Variables

  private ElementVisitor[] mVisitors;

  // ========================================================================
  // Constructors

  public CompositeElementVisitor(ElementVisitor[] visitors) {
    if (visitors == null) throw new IllegalArgumentException("null visitors");
    mVisitors = visitors;
  }

  // ========================================================================
  // ElementVisitor implementation

  public void visit(MPackage pkg) {
    for(int i=0; i<mVisitors.length; i++) mVisitors[i].visit(pkg);
  }

  public void visit(MClass clazz) {
    for(int i=0; i<mVisitors.length; i++) mVisitors[i].visit(clazz);
  }

  public void visit(MConstructor ctor) {
    for(int i=0; i<mVisitors.length; i++) mVisitors[i].visit(ctor);
  }

  public void visit(MField field) {
    for(int i=0; i<mVisitors.length; i++) mVisitors[i].visit(field);
  }

  public void visit(MMethod method) {
    for(int i=0; i<mVisitors.length; i++) mVisitors[i].visit(method);
  }

  public void visit(MParameter param) {
    for(int i=0; i<mVisitors.length; i++) mVisitors[i].visit(param);
  }

  public void visit(MAnnotation ann) {
    for(int i=0; i<mVisitors.length; i++) mVisitors[i].visit(ann);
  }

  public void visit(MComment comment) {
    for(int i=0; i<mVisitors.length; i++) mVisitors[i].visit(comment);
  }



}

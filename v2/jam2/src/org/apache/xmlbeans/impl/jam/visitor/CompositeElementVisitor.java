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

import org.apache.xmlbeans.impl.jam.editable.*;

/**
 * @author Patrick Calahan <pcal@bea.com>
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

  public void visit(EPackage pkg) {
    for(int i=0; i<mVisitors.length; i++) mVisitors[i].visit(pkg);
  }

  public void visit(EClass clazz) {
    for(int i=0; i<mVisitors.length; i++) mVisitors[i].visit(clazz);
  }

  public void visit(EConstructor ctor) {
    for(int i=0; i<mVisitors.length; i++) mVisitors[i].visit(ctor);
  }

  public void visit(EField field) {
    for(int i=0; i<mVisitors.length; i++) mVisitors[i].visit(field);
  }

  public void visit(EMethod method) {
    for(int i=0; i<mVisitors.length; i++) mVisitors[i].visit(method);
  }

  public void visit(EParameter param) {
    for(int i=0; i<mVisitors.length; i++) mVisitors[i].visit(param);
  }

  public void visit(EAnnotation ann) {
    for(int i=0; i<mVisitors.length; i++) mVisitors[i].visit(ann);
  }

  public void visit(EComment comment) {
    for(int i=0; i<mVisitors.length; i++) mVisitors[i].visit(comment);
  }



}

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
 * <p>To be extended by classes which wish to traverse a set of JClasses.</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public abstract class ElementVisitor {

  public void visit(MPackage pkg) {}

  public void visit(MClass clazz) {}

  public void visit(MConstructor ctor) {}

  public void visit(MField field) {}

  public void visit(MMethod method) {}

  public void visit(MParameter param) {}

  public void visit(MAnnotation ann) {}

  public void visit(MComment comment) {}

}

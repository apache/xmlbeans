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
package org.apache.xmlbeans.impl.jam.editable;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface EElementVisitor {

  public void visit(EClass clazz);

  public void visit(EConstructor ctor);

  public void visit(EField field);

  public void visit(EMethod method);

  public void visit(EParameter param);

  public void visit(EAnnotation param);

  public void visit(EComment param);

//FIXME  implement this?   public void visit(EPackage param);



}

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
package org.apache.xmlbeans.impl.jam;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public interface JElementVisitor {

  public void visit(JPackage param);

  public void visit(JClass clazz);

  public void visit(JConstructor ctor);

  public void visit(JField field);

  public void visit(JMethod method);

  public void visit(JParameter param);

  public void visit(JAnnotation param);

  public void visit(JComment param);

}

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

package org.apache.xmlbeans.impl.jam_old.editable.impl;

import org.apache.xmlbeans.impl.jam_old.editable.EConstructor;
import org.apache.xmlbeans.impl.jam_old.editable.EParameter;
import org.apache.xmlbeans.impl.jam_old.editable.EElementVisitor;
import org.apache.xmlbeans.impl.jam_old.JClass;
import org.apache.xmlbeans.impl.jam_old.JParameter;

import java.util.List;
import java.util.ArrayList;
import java.io.StringWriter;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class EConstructorImpl extends EInvokableImpl implements EConstructor {

  // ========================================================================
  // Constructors

  /*package*/ EConstructorImpl(EClassImpl containingClass) {
    super(containingClass.getSimpleName(),containingClass);
  }

  // ========================================================================
  // EElement implementation

  public void accept(EElementVisitor visitor) {
    visitor.visit(this);
  }

  public void acceptAndWalk(EElementVisitor visitor) {
    accept(visitor);
    acceptAndWalkAll(visitor,getEditableParameters());
    acceptAndWalkAll(visitor,getEditableAnnotations());
  }


}
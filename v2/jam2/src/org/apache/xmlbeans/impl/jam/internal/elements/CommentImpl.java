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

import org.apache.xmlbeans.impl.jam.JElementVisitor;
import org.apache.xmlbeans.impl.jam.editable.EComment;
import org.apache.xmlbeans.impl.jam.editable.EElementVisitor;

/**
 * <p>Implementation
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public final class CommentImpl extends ElementImpl implements EComment {

  // ========================================================================
  // Variables

  private String mText = null;

  // ========================================================================
  // Constructors

  /*package*/ CommentImpl(ElementImpl parent) { super(parent); }

  // ========================================================================
  // EComment implementation

  public void setText(String text) { mText = text; }

  public String getText() { return (mText == null) ? "" : mText; }

  // ========================================================================
  // EElement implementation

  public void accept(EElementVisitor visitor) { visitor.visit(this); }

  public void acceptAndWalk(EElementVisitor visitor) { accept(visitor); }

  // ========================================================================
  // JElement implementation

  public void accept(JElementVisitor visitor) { visitor.visit(this); }

  public void acceptAndWalk(JElementVisitor visitor) { accept(visitor); }

  public String getQualifiedName() {
    return getParent().getQualifiedName()+".{comment}"; //REVIEW
  }

}

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

package org.apache.xmlbeans.impl.jam_old.internal.javadoc;


import org.apache.xmlbeans.impl.jam_old.JElement;
import org.apache.xmlbeans.impl.jam_old.JSourcePosition;
import org.apache.xmlbeans.impl.jam_old.internal.BaseJAnnotation;

/**
 * Javadoc-backed implementation of org.apache.xmlbeans.impl.jam_old.Annotation
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JDAnnotation extends BaseJAnnotation
{
  // ========================================================================
  // Variables
  private JSourcePosition mSourcePosition = null;

//  private com.sun.javadoc.Tag mTag;
  //
  // ========================================================================
  // Constructors
  
//  public JDAnnotation(JElement parent, String name, String value, JSourcePosition sp) {
//    super(parent, name, value);
//    mSourcePosition = sp;
//  }
//  
//  public JDAnnotation(JElement parent,com.sun.javadoc.Tag tag) 
//  {
//    this(parent, tag, tag.name(), tag.text());
//  }
//
//  public JDAnnotation(JElement parent, com.sun.javadoc.ParamTag tag) 
//  {
//    this(parent, tag, tag.name(), tag.parameterComment()); //REVIEW?
//  }

  public JDAnnotation(JElement parent,
//		       com.sun.javadoc.Tag tag, 
		       String name, 
		       String value,
           JSourcePosition sourcePosition) 
  {
    super(parent, trimAtSign(name),value);
    mSourcePosition = sourcePosition;
//    mTag = tag;
  }

  // ========================================================================
  // JElement impl

  // we want to lazily instantiate these since they probably won't get
  // used often.

  public JSourcePosition getSourcePosition() { 
    return mSourcePosition;
//    return JDFactory.getInstance().createSourcePosition(mTag.position());
    //FIXME but still want lazy instantiation
  }

  // ========================================================================
  // Private methods

  private static String trimAtSign(String name) {
    if (name.charAt(0) == '@' && name.length() > 1) {
      return name.substring(1).trim();
    } else {
      return name;
    }
  }

}

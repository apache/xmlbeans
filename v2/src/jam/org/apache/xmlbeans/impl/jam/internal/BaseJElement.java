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

package org.apache.xmlbeans.impl.jam.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.xmlbeans.impl.jam.*;

/**
 * Base class for implementing interfaces which implement JElement.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class BaseJElement implements JElement {

  // ========================================================================
  // Constants

  // help reduce object waste
  public static final JElement[] NO_NODE = new JElement[0];
  public static final JClass[] NO_CLASS = new JClass[0];
  public static final JField[] NO_FIELD = new JField[0];
  public static final JConstructor[] NO_CONSTRUCTOR = new JConstructor[0];
  public static final JMethod[] NO_METHOD = new JMethod[0];
  public static final JParameter[] NO_PARAMETER = new JParameter[0];
  public static final JPackage[] NO_PACKAGE = new JPackage[0];
  public static final JAnnotation[] NO_ANNOTATION = new JAnnotation[0];

  public static final JComment[] NO_COMMENT = new JComment[0];
  public static final JProperty[] NO_PROPERTY = new JProperty[0];

  private static final String ANNOTATION_SEPARATOR = "@";

  // ========================================================================
  // Variables

  private JAnnotation[] mAnns = null;
  private JComment[] mComments = null;
  private List mTempList = null;

  //FIXME this is a really gross quick hack to get this stuff working.
  //we should be getting this from our classloader.
  private JAnnotationLoader mExternalAnnotations = null;

  // ========================================================================
  // Constructors

  protected BaseJElement() {
  }

  private List getTempList() {
    return new ArrayList();
    /*
    if (mTempList == null) {
      mTempList = new ArrayList();
    } else {
      mTempList.clear();
    }
    return mTempList;
    */
  }

  /**
   * @deprecated this is a quick hack, please remove
   */
  public void setAnnotationLoader(JAnnotationLoader l) {
    mExternalAnnotations = l;
  }

  // ========================================================================
  // JAnnotation implementation

  /**
   * NOTE TO IMPLEMENTORS: you really should not override this method; override
   * getLocalAnnotations instead!!! This method takes responsibility for
   * returning the union of the localAnnotations and the annotations provided
   * by any external JAnnotationLoader; if you override this method yourself,
   * your impl will not support external annotations.
   *
   * @return
   */
  public /*final*/ JAnnotation[] getAnnotations() {
    if (true || mAnns == null) { //FIXME always do this until we fixing JAnnotationLoader thing
      List list = getTempList();
      if (mExternalAnnotations != null) {
        mExternalAnnotations.getAnnotations(this,list);
      }
      getLocalAnnotations(list);
      if (list.size() == 0) {
        mAnns = NO_ANNOTATION;
      } else {
        mAnns = new JAnnotation[list.size()];
        list.toArray(mAnns);
      }
    }
    return mAnns; // FIXME do we need to return a copy?
  }

  public final JAnnotation[] getAnnotations(String named) {
    List list = getTempList();
    gatherAnnotations(named, list);
    JAnnotation[] out = new JAnnotation[list.size()];
    list.toArray(out);
    return out;
  }

  public final JComment[] getComments() {
    if (mComments == null) {
      List list = getTempList();
      getLocalComments(list);
      /*      if (mContext.getExtraMarkupStore() != null) {
      mContext.getExtraMarkupStore().getComments(this,list);
      }*/
      if (list.size() == 0) {
        mComments = NO_COMMENT;
      } else {
        mComments = new JComment[list.size()];
        list.toArray(mComments);
      }
    }
    return mComments; // FIXME do we need to return a copy?
  }

  public final JAnnotation getAnnotation(String named) {
    JAnnotation[] anns = getAnnotations(named);
    return (anns.length == 0) ? null : anns[0];
  }

  // ========================================================================
  // Object implementation

  public String toString() {
    return getQualifiedName();
  }

  // ========================================================================
  // Methods to be implemented by subclasses

  /**
   * Extending class must implement this by adding all of the
   * annotations which apply to this object to the given 'out'
   * collection.  Implement as a no-op if annotations are not
   * applicable.
   */
  protected abstract void getLocalAnnotations(Collection out);

  /**
   * Extending class must implement this by adding all of the
   * annotations which apply to this object to the given 'out'
   * collection.  Implement as a no-op if comments are not applicable.
   */
  protected abstract void getLocalComments(Collection out);

  // ========================================================================
  // Private methods

  private void gatherAnnotations(String fullName, Collection out) {
    JAnnotation[] anns = getAnnotations();
    if (anns == NO_ANNOTATION) return;
    int dot = fullName.indexOf(ANNOTATION_SEPARATOR);
    String subname = (dot == -1) ? fullName : fullName.substring(0, dot);
    String postfix = (dot == -1 || dot == fullName.length() - 1) ?
            null : fullName.substring(dot + 1);
    // System.out.println("===== "+subname+"  "+postfix);
    for (int i = 0; i < anns.length; i++) {
      if (anns[i].getName().equals(subname)) {
        if (postfix == null) {
          out.add(anns[i]);
        } else {
          // REVIEW nasty cast.  Alternative is to expose this method
          // via a private interface, but that seems like overkill.
          ((BaseJElement) anns[i]).gatherAnnotations(postfix, out);
        }
      }
    }
  }


  // ========================================================================
  // Object implementation

  // REVIEW this needs more thought.

  public boolean equals(Object o) {
    return (o instanceof JElement) &&
            getQualifiedName().equals(((JElement) o).getQualifiedName());
  }

  public int hashCode() {
    return getQualifiedName().hashCode();
  }

}

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
package org.apache.xmlbeans.impl.binding.compile.internal;

import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JPackage;
import org.apache.xmlbeans.impl.jam.JAnnotatedElement;
import org.apache.xmlbeans.impl.jam.JAnnotation;
import org.apache.xmlbeans.impl.jam.JAnnotationValue;
import org.apache.xmlbeans.impl.binding.compile.internal.annotations.ClassBindingInfo;
import org.apache.xmlbeans.impl.binding.compile.internal.annotations.TypeTarget;
import org.apache.xmlbeans.impl.binding.compile.internal.annotations.TopLevelElementTarget;
import org.apache.xmlbeans.impl.binding.compile.Java2Schema;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class Java2SchemaAnnotationHelper {

  // ========================================================================
  // Singleton

  public static Java2SchemaAnnotationHelper getInstance() { return INSTANCE; }

  private static Java2SchemaAnnotationHelper INSTANCE = new Java2SchemaAnnotationHelper();

  private Java2SchemaAnnotationHelper() {}

  // ========================================================================
  // Constants

  private static final String JAVA_URI_SCHEME      = "java:";
  private static final String JAVA_NAMESPACE_URI   = "language_builtins";
  //private static final String JAVA_PACKAGE_PREFIX  = "java.";

  // ========================================================================
  // Public methods

  public QName getTargetTypeName(JClass clazz) {
    String tns = getTargetNamespace(clazz);
    String local = getTargetLocalName(clazz);
    return new QName(tns,local);
  }

  public QName[] getTargetElements(JClass clazz) {
    List list = null;
    { // old school
      String rootName = getAnnotation(clazz,Java2Schema.TAG_CT_ROOT,null);
      if (rootName != null) {
        list = new ArrayList();
        list.add(new QName(getTargetNamespace(clazz), rootName));
      }
    }
    { // new school
      ClassBindingInfo info =
        (ClassBindingInfo)clazz.getAnnotationProxy(ClassBindingInfo.class);
      if (info != null) {
        TypeTarget tt = info.getDefaultTargetType();
        if (tt != null) {
          TopLevelElementTarget[] e = tt.getTopLevelElements();
          if (e != null && e.length > 0) {
            if (list == null) list = new ArrayList();
            for(int i=0; i<e.length; i++) {
              list.add(new QName(e[i].getNamespaceUri(),e[i].getLocalName()));
            }
          }
        }
      }
    }
    if (list == null) return null;
    QName[] out = new QName[list.size()];
    list.toArray(out);
    return out;
  }

  public boolean isExclude(JClass clazz) {
    if (getAnnotation(clazz,Java2Schema.TAG_CT_EXCLUDE,false)) return true;
    ClassBindingInfo info =
      (ClassBindingInfo)clazz.getAnnotationProxy(ClassBindingInfo.class);
    return (info == null) ? false : info.isExclude();
  }

  public boolean isIgnoreSuper(JClass clazz) {
    if (getAnnotation(clazz,Java2Schema.TAG_CT_IGNORESUPER,false)) return true;
    ClassBindingInfo info =
      (ClassBindingInfo)clazz.getAnnotationProxy(ClassBindingInfo.class);
    if (info == null) return false;
    TypeTarget tt = info.getDefaultTargetType();
    return (tt == null) ? false : tt.isIgnoreJavaInheritance();
  }

  /**
   * Returns the string value of a named annotation, or the provided default
   * if the annotation is not present.
   * REVIEW seems like having this functionality in jam would be nice
   */
  public String getAnnotation(JAnnotatedElement elem,
                                      String annName,
                                      String dflt)
  {
    //System.out.print("checking for "+annName+" on "+elem.getQualifiedName());
    JAnnotation ann = getAnnotation(elem,annName);
    if (ann == null) {
      //System.out.println("...no annotation");
      return dflt;
    }
    JAnnotationValue val = ann.getValue(JAnnotation.SINGLE_VALUE_NAME);
    if (val == null) {
      //System.out.println("...no value!!!");
      return dflt;
    }
    //System.out.println("\n\n\n...value of "+annName+" is "+val.asString()+"!!!!!!!!!");
    return val.asString();
  }

  /**
   * Returns the boolean value of a named annotation, or the provided default
   * if the annotation is not present.
   * REVIEW seems like having this functionality in jam would be nice
   */
  public boolean getAnnotation(JAnnotatedElement elem,
                               String annName,
                               boolean dflt)
  {
    //System.out.print("checking for "+annName+" on "+elem.getQualifiedName());
    JAnnotation ann = getAnnotation(elem,annName);
    if (ann == null) {
      //System.out.println("...no annotation");
      return dflt;
    }
    JAnnotationValue val = ann.getValue(JAnnotation.SINGLE_VALUE_NAME);
    if (val == null || val.asString().length() == 0) {
      //System.out.println("\n\n\n...no value, returning true!!!");
      //this is a little bit gross.  the logic here is that if the tag is
      //present but empty, it actually is a true value.  E.g., an empty
      //@exclude tag means "yes, do exclude."
      return true;
    }
    //System.out.println("\n\n\n...value of "+annName+" is "+val.asBoolean()+"!!!!!!!!!");
    return val.asBoolean();
  }

  //FIXME this is temporary until we get the tags/175 sorted out
  public JAnnotation getAnnotation(JAnnotatedElement e, String named) {
    JAnnotation[] tags = e.getAllJavadocTags();
    for(int i=0; i<tags.length; i++) {
      if (tags[i].getSimpleName().equals(named)) return tags[i];
    }
    return null;
  }


  // ========================================================================
  // Private methods

  private String getTargetLocalName(JClass clazz) {
    String name = getAnnotation(clazz,Java2Schema.TAG_CT_TYPENAME,null);
    if (name != null) return name;
    ClassBindingInfo info =
      (ClassBindingInfo)clazz.getAnnotationProxy(ClassBindingInfo.class);
    if (info != null) {
      TypeTarget tt = info.getDefaultTargetType();
      name = tt.getLocalName();
      if (name != null) return name;
    }
    return clazz.getSimpleName();
  }

  /**
   * Returns a target namespace that should be used for the given class.
   * This takes annotations into consideration.
   */
  private String getTargetNamespace(JClass clazz) {
    String val = getAnnotation(clazz,Java2Schema.TAG_CT_TARGETNS,null);
    if (val != null) {
      return val;
    }
    ClassBindingInfo info =
      (ClassBindingInfo)clazz.getAnnotationProxy(ClassBindingInfo.class);
    if (info != null) {
      TypeTarget tt = info.getDefaultTargetType();
      val = tt.getNamespaceUri();
      if (val != null) return val;
    }
    // Ok, they didn't specify it in the markup, so we have to
    // synthesize it from the classname.
    String pkg_name;
    if (clazz.isPrimitiveType()) {
      pkg_name = JAVA_NAMESPACE_URI;
    } else {
      JPackage pkg = clazz.getContainingPackage();
      pkg_name = (pkg == null) ? "" : pkg.getQualifiedName();
      /*if (pkg_name.startsWith(JAVA_PACKAGE_PREFIX)) {
        pkg_name = JAVA_NAMESPACE_URI+'.'+
                pkg_name.substring(JAVA_PACKAGE_PREFIX.length());
      }*/
    }
    return JAVA_URI_SCHEME + pkg_name;
  }
}

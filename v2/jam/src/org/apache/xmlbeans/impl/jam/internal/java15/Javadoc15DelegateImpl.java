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
package org.apache.xmlbeans.impl.jam.internal.java15;

import org.apache.xmlbeans.impl.jam.mutable.MAnnotatedElement;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotation;
import org.apache.xmlbeans.impl.jam.internal.javadoc.Javadoc15Delegate;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import org.apache.xmlbeans.impl.jam.JClass;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Type;
import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.ClassDoc;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class Javadoc15DelegateImpl implements Javadoc15Delegate {

  // ========================================================================
  // Variables

  private ElementContext mContext = null;

  // ========================================================================
  // Javadoc15Delegate implementation

  public void init(ElementContext ctx) {
    if (mContext != null) {
      throw new IllegalStateException("init called more than once");
    }
    mContext = ctx;
  }

  public void extractAnnotations(MAnnotatedElement dest,
                                 ProgramElementDoc src) {
    if (mContext == null) throw new IllegalStateException("init not called");
    if (dest == null) throw new IllegalArgumentException("null dest");
    extractAnnotations(dest,src.annotations());
  }

  public void extractAnnotations(MAnnotatedElement dest, Parameter src) {
    //FIXME javadoc doesn't yet support parameter annotations
    //pcal 3/15/04
    //
    //extractAnnotations(dest,src.annotations());
  }

  public boolean isEnum(ClassDoc cd) {
    return cd.isEnum();
  }

  // ========================================================================
  // Private methods

  private void extractAnnotations(MAnnotatedElement dest,
                                  AnnotationDesc[] anns)
  {
    if (anns == null) return; //?
    for(int i=0; i<anns.length; i++) {
      MAnnotation destAnn = dest.addAnnotationForType
        (anns[i].annotationType().asClassDoc().qualifiedName());
      populateAnnotation(destAnn,anns[i]);
    }
  }

  private void populateAnnotation(MAnnotation dest, AnnotationDesc src) {
    AnnotationDesc.MemberValuePair[] mvps = src.memberValues();
    for(int i=0; i<mvps.length; i++) {
      Type jmt = mvps[i].member().returnType();
      String typeName = jmt.qualifiedTypeName();
      String name = mvps[i].member().name();
      AnnotationValue aval = mvps[i].value();
      Object valueObj = aval.value();
      if (mContext.getLogger().isVerbose(this)) {
        mContext.getLogger().verbose(name+" is a "+typeName+" with valueObj "+
                                     valueObj+", class is "+valueObj.getClass());
      }
      if (valueObj instanceof AnnotationDesc) {
        MAnnotation nested = dest.createNestedValue(name,typeName);
        populateAnnotation(nested,(AnnotationDesc)valueObj);
      } else if (valueObj instanceof Number || valueObj instanceof Boolean) {
        JClass type = mContext.getClassLoader().loadClass(jmt.typeName());
        dest.setSimpleValue(name,valueObj,type);
      } else if (valueObj instanceof FieldDoc) {
        // this means it's an enum constant
        JClass type = mContext.getClassLoader().loadClass
          (((FieldDoc)valueObj).containingClass().qualifiedName());
        String val = ((FieldDoc)valueObj).name(); //REVIEW is this right?
        dest.setSimpleValue(name,val,type);
      } else if (valueObj instanceof ClassDoc) {
        ClassDoc foo;
        // it's a class
        JClass clazz = mContext.getClassLoader().loadClass
          (((FieldDoc)valueObj).containingClass().qualifiedName());
        dest.setSimpleValue(name,clazz,clazz);
      } else if (valueObj instanceof String) {
        JClass type = mContext.getClassLoader().loadClass("java.lang.String");
        String v = ((String)valueObj).trim();
        if (v.startsWith("\"") && v.endsWith("\"")) {
          //javadoc gives us the quotes, which seems kinda dumb.  just deal.
          valueObj = v.substring(1,v.length()-1);
        }
        dest.setSimpleValue(name,valueObj,type);
      } else if (valueObj instanceof AnnotationValue[]) {
        //FIXME
        mContext.getLogger().warning("Array member types are still  NYI!!! "+
                                     "("+name+")");

      }


    }
  }


}

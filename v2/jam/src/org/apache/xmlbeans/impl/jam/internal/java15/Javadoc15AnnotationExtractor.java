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
import org.apache.xmlbeans.impl.jam.annotation.AnnotationProxy;
import org.apache.xmlbeans.impl.jam.internal.javadoc.JavadocAnnotationExtractor;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.Parameter;


/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class Javadoc15AnnotationExtractor implements JavadocAnnotationExtractor {

  public void extractAnnotations(MAnnotatedElement dest,
                                 ProgramElementDoc src) {
    extractAnnotations(dest,src.annotations());
  }

  public void extractAnnotations(MAnnotatedElement dest, Parameter src) {
    //FIXME javadoc doesn't yet support parameter annotations
    //pcal 3/15/04
    //
    //extractAnnotations(dest,src.annotations());
  }

  // ========================================================================
  // Private methods


  private void extractAnnotations(MAnnotatedElement dest,
                                  AnnotationDesc[] anns)
  {
    if (anns == null) return;
    for(int i=0; i<anns.length; i++) {
      MAnnotation destAnn = dest.addAnnotationForType
        (anns[i].annotationType().asClassDoc().qualifiedName());
      AnnotationProxy destProxy = destAnn.getMutableProxy();
      AnnotationDesc.MemberValuePair[] mvps = anns[i].memberValues();
      for(int j=0; j<mvps.length; j++) {
        destProxy.setValue(mvps[j].member().name(),
                           mvps[j].value());
        //FIXME deal with nested data
      }
    }
  }
}
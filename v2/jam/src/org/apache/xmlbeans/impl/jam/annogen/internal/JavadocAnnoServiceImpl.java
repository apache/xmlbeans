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
package org.apache.xmlbeans.impl.jam.annogen.internal;

import org.apache.xmlbeans.impl.jam.annogen.JavadocAnnoService;
import org.apache.xmlbeans.impl.jam.annogen.provider.*;
import org.apache.xmlbeans.impl.jam.provider.JamLogger;
import org.apache.xmlbeans.impl.jam.internal.javadoc.Javadoc15Delegate;

import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.ExecutableMemberDoc;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JavadocAnnoServiceImpl
    extends AnnoServiceBase implements JavadocAnnoService {

  // ========================================================================
  // Variables

  private ElementIdPool mIdFactory = ElementIdPool.getInstance();
  private JamLogger mLogger;
  private Javadoc15Delegate mDelegate = null;

  // ========================================================================
  // Constructors

  public JavadocAnnoServiceImpl(AnnoServiceParamsImpl asp) {
    super(asp);
    mLogger = asp.getLogger();
    mDelegate = Javadoc15Delegate.Factory.create(mLogger);
  }

  // ========================================================================
  // AnnoServiceBase implementation

  protected void getIndigenousAnnotations(ElementId id, AnnoProxySet out) {
    if (mDelegate == null) return;
    ProgramElementDoc ped = ((JavadocElementId)id).getAnnotatedElement();
    int paramNum = id.getParameterNumber();
    if (paramNum == ElementId.NO_PARAMETER) {
      mDelegate.extractAnnotations(out,ped);
    } else {
      mDelegate.extractAnnotations(out,(ExecutableMemberDoc)ped,paramNum);
    }
  }

  // ========================================================================
  // JavadocAnnoService implementation

  public Object getAnnotation(Class annoClass, ProgramElementDoc ped) {
    ElementId id = mIdFactory.getIdFor(ped);
    return super.getAnnotation(annoClass,id);
  }
}

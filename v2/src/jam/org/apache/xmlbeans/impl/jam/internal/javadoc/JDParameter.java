/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package org.apache.xmlbeans.impl.jam.internal.javadoc;


import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.xmlbeans.impl.jam.*;
import org.apache.xmlbeans.impl.jam.internal.BaseJElement;

/**
 * Javadoc-backed implementation of JParameter.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class JDParameter extends BaseJElement implements JParameter 
{

  // ========================================================================
  // Variables

  private Parameter mParameter;
  private JDExecutableMember mParent;
  private JClassLoader mLoader;

  // ========================================================================
  // Constructors
  
  public JDParameter(Parameter p,
                     JDExecutableMember parent,
                     JClassLoader loader) {
    mParameter = p;
    mParent = parent;
    mLoader = loader;
  }

  // ========================================================================
  // JElement implementation

  public JElement getParent() { return mParent; }

  public JElement[] getChildren() { return null; }
  
  public String getSimpleName() { 
    return mParameter.name();
  }

  public String getQualifiedName() { 
    return mParameter.name(); 
  }

  public JSourcePosition getSourcePosition() { 
    return mParent.getSourcePosition(); //close enough
  }

  // ========================================================================
  // BaseJElement implementation

  protected void getLocalAnnotations(Collection out) { 
    ParamTag[] tags = mParent.getMember().paramTags();
    if (tags == null || tags.length == 0) return;
    tags = getParamTagsFor(tags,mParameter);
    if (tags == null || tags.length == 0) return;
    for(int i=0; i<tags.length; i++) {
      ParamTag t = tags[i];
      JSourcePosition sp = JDFactory.getInstance().createSourcePosition(t.position());
      out.add(JDFactory.getInstance().createAnnotation(this, t.name(), t.parameterComment(), sp));
    }
  }

  // javadoc doesn't recognize @param comments, unfortunately.  FIXME
  // we could be clever and figure them out ourselves, i suppose.
  protected void getLocalComments(Collection out) {}

  // ========================================================================
  // JParameter implementation

  public JClass getType() {
    return JDClassLoader.getClassFor(mParameter.type(),mLoader);
  }

  // ========================================================================
  // Private methods

  /**
   * Given a set of ParamTags, returns those which apply to the given
   * parameter.
   */
  private ParamTag[] getParamTagsFor(ParamTag[] all, Parameter p) {
    List list = null;
    for(int i=0; i<all.length; i++) {
      if (all[i].parameterName().equals(p.name())) {
	if (list == null) list = new ArrayList();
	list.add(all[i]);
      }
    }
    if (list == null) return null;
    ParamTag[] out = new ParamTag[list.size()];
    list.toArray(out);
    return out;
  }

}

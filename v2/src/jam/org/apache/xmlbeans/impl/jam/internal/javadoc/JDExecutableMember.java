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


import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Tag;
import java.util.Collection;
import org.apache.xmlbeans.impl.jam.*;

/**
 * Abstract base class for JDConstructor and JDMethod.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class JDExecutableMember extends JDMember {

  // ========================================================================
  // Variables

  private ExecutableMemberDoc mMember;
  private JParameter[] mParameters = null;

  // ========================================================================
  // Constructors

  /**
   *
   */
  protected JDExecutableMember(ExecutableMemberDoc m, JClassLoader loader) {
    super(m,loader);
    mMember = m;
  }

  // ========================================================================
  // JElement implementation

  public JElement[] getChildren() { return getParameters(); }

  /**
   * Override this so that we can exclude @param tags - these get hund
   * on the JParameters.
   */
  protected void getLocalAnnotations(Collection out) {
    com.sun.javadoc.Tag[] tags = mMember.tags();
    if (tags == null || tags.length == 0) return;
    for(int i=0; i<tags.length; i++) {
      if (!(tags[i] instanceof ParamTag)) {
          Tag t = tags[i];
          JSourcePosition sp = JDFactory.getInstance().createSourcePosition(mMember.position());
          out.add(JDFactory.getInstance().createAnnotation(this, t.name(), t.text(), sp));
      }
    }
  }

  // ========================================================================
  // JExecutableMember implementation

  public JParameter[] getParameters() {
    ParamTag[] tags = mMember.paramTags();
    if (mParameters == null) {
      Parameter[] params = mMember.parameters();
      if (params == null || params.length == 0) {
	mParameters = NO_PARAMETER;
      } else {
	mParameters = new JDParameter[params.length];
	for(int i=0; i<params.length; i++) {
	  mParameters[i] = JDFactory.getInstance().createParameter(params[i],
					   this,
					   mLoader);
	}
      }
    }
    return mParameters;
  }

  public JClass[] getExceptionTypes() {
    return JDClass.getClasses(mMember.thrownExceptions(),mLoader);
  }

  // ========================================================================
  // Package methods

  // this is here for the benefit of JDParameter.getLocalAnnotations()
  /*package*/ ExecutableMemberDoc getMember() { return mMember; }

}

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
package org.apache.xmlbeans.impl.jam.annotation;

import org.apache.xmlbeans.impl.jam.mutable.MAnnotatedElement;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotation;
import org.apache.xmlbeans.impl.jam.mutable.MSourcePosition;
import org.apache.xmlbeans.impl.jam.provider.JamServiceContext;
import org.apache.xmlbeans.impl.jam.provider.JamLogger;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JAnnotation;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import com.sun.javadoc.Tag;
import com.sun.javadoc.SourcePosition;

import java.util.StringTokenizer;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public abstract class JavadocTagParser {

  // ========================================================================
  // Variables

  private JamServiceContext mContext = null;

  // ========================================================================
  // Public methods

  /**
   * <p>Called by JAM to initialize the proxy.  Do not try to call this
   * yourself.</p>
   */
  public void init(JamServiceContext ctx) {
    if (ctx == null) throw new IllegalArgumentException("null logger");
    if (mContext != null) throw new IllegalStateException
      ("JavadocTagParser.init() called twice");
    mContext = ctx;
  }

  // ========================================================================
  // Abstract methods

  public abstract void parse(MAnnotatedElement target, Tag tag);

  // ========================================================================
  // Protected methods

  protected MAnnotation[] createAnnotations(MAnnotatedElement target, Tag tag) {
    String tagName = tag.name().trim().substring(1);
    //MAnnotation out = target.addAnnotation(tagName);
    MAnnotation current = target.getMutableAnnotation(tagName);
    if (current == null) {
      current = target.findOrCreateAnnotation(tagName);
      setPosition(current,tag);
    }
    MAnnotation literal = target.addLiteralAnnotation(tagName);
    setPosition(literal,tag);
//    setSingleValueText(literal,tag);
    return new MAnnotation[] {literal,current};
  }


  // subclasses might want to override this to change the way tag values
  // are mapped into typed values
  protected void setValue(MAnnotation[] anns,
                          String memberName,
                          String value) {
    value = value.trim();
    memberName = memberName.trim();
    for(int i=0; i<anns.length; i++) {
      if (anns[i].getValue(memberName) == null) {
        // first one wins
        anns[i].setSimpleValue(memberName,value,getStringType());
      }
    }
  }

  protected JamLogger getLogger() { return mContext.getLogger(); }

  protected JClass getStringType() {
    return ((ElementContext)mContext).getClassLoader().
      loadClass("java.lang.String");
  }

  //set the value of SINGLE_VALUE_NAME to be the raw tag text.  This of
  //course will be overridden if the tag contains a member value named
  //'value' - oh well
  protected void setSingleValueText(MAnnotation[] targets, Tag tag) {
    String tagText = tag.text().trim();
    for(int i=0; i<targets.length; i++) {
    targets[i].setSimpleValue
      (JAnnotation.SINGLE_VALUE_NAME,tagText,getStringType());
    }
  }

  // ========================================================================
  // Private methods

  private void setPosition(MAnnotation target, Tag tag) {
    //add source position info, if available
    SourcePosition pos = tag.position();
    if (pos != null) {
      MSourcePosition mpos = target.createSourcePosition();
      mpos.setLine(pos.line());
      mpos.setColumn(pos.column());
      if (pos.file() != null) mpos.setSourceURI(pos.file().toURI());
    }
  }

}

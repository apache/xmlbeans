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
package org.apache.xmlbeans.impl.binding.compile.internal.annotations;

import org.apache.xmlbeans.impl.jam.annotation.TypedAnnotationProxyBase;

import javax.xml.namespace.QName;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public abstract class NamedTarget extends TypedAnnotationProxyBase {

  // ========================================================================
  // Variables

  private String mLocalName;
  private String mNamespace;

  // ========================================================================
  // Constructors

  protected NamedTarget() {}

  protected NamedTarget(QName qn) {
    if (qn == null) throw new IllegalArgumentException("null qname");
    mLocalName = qn.getLocalPart();
    mNamespace = qn.getNamespaceURI();
  }

  protected NamedTarget(String ns, String local) {
    if (ns == null) throw new IllegalArgumentException("null namespace");
    if (local == null) throw new IllegalArgumentException("null local name");
    mLocalName = local;
    mNamespace = ns;
  }

  // ========================================================================
  // Accessors

  public String getLocalName() { return mLocalName; }

  public String getNamespaceUri() { return mNamespace; }

}

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

package org.apache.xmlbeans.impl.binding.bts;

import org.apache.xmlbeans.impl.binding.bts.BindingProperty;

/**
 * A property lined up by Schema particle.  Used by JaxbBean
 */
public class ParticleProperty extends BindingProperty {

  // ========================================================================
  // Constructors

  public ParticleProperty() {
    super();
  }

  public ParticleProperty(org.apache.xml.xmlbeans.bindingConfig.BindingProperty node) {
    super(node);
  }

  // ========================================================================
  // Public methods

  public XmlTypeName getXmlName() {
    return getTypeName().getXmlName();
  }
}

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

import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;


/**
 * Represents a BindingLoader whose contents are loaded from a
 * single binding-config file. (See binding-config.xsd)
 */
public class BindingFile
  extends BaseBindingLoader
{

  private static final long serialVersionUID = 1L;
  
  // ========================================================================
  // Factory

  // ========================================================================
  // Constructors

  /**
   * This constructor is used when making a new one out of the blue.
   */
  public BindingFile()
  {
    // nothing to do - all maps are empty
  }

  // ========================================================================
  // Public methods


  public void addBindingType(BindingType bType,
                             boolean fromJavaDefault,
                             boolean fromXmlDefault)
  {
    addBindingType(bType);
    if (fromXmlDefault) {
      if (bType.getName().getJavaName().isXmlObject())
        addXmlObjectFor(bType.getName().getXmlName(), bType.getName());
      else
        addPojoFor(bType.getName().getXmlName(), bType.getName());
    }
    if (fromJavaDefault) {
      if (bType.getName().getXmlName().getComponentType() == XmlTypeName.ELEMENT)
        addElementFor(bType.getName().getJavaName(), bType.getName());
      else
        addTypeFor(bType.getName().getJavaName(), bType.getName());
    }
  }

  /**
   * Loader
   */
  public static BindingFile forSer(InputStream ser)
    throws IOException, ClassNotFoundException
  {
    ObjectInputStream ois = new ObjectInputStream(ser);
    final Object obj = ois.readObject();
    BindingFile bf = (BindingFile) obj;
    ois.close();
    return bf;
  }

  // ========================================================================
  // Private methods

}

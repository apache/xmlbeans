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
package org.apache.xmlbeans.impl.jeti.output;

import javax.xml.stream.XMLStreamWriter;
import java.io.Writer;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class OutputService {

  /**
   * <p>Returns a Writer into which generated java source can be written.</p>
   *
   * @param resourceName path to the file (relative to the output root) to
   * generate.
   * @param qualifiedClassName Name of the java class to be codegenned
   */
  public Writer createJavaSourceWriter(String resourceName,
                                       String qualifiedClassName) {
    throw new UnsupportedOperationException();
  }

  /**
   * <p>Returns an XMLStreamWriter on a resource relative to the
   * output root.</p>
   *
   * REVIEW i think we may need to introduce something
   * to model the structure of whatever it is the tool is trying to output.
   *
   */
  public XMLStreamWriter createXmlStreamWriter(String resourceName) {
    throw new UnsupportedOperationException();
  }


}

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
package org.apache.xmlbeans.impl.jam.provider;

import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.internal.parser.ParserClassBuilder;
import org.apache.xmlbeans.impl.jam.internal.JClassLoaderImpl;

import java.io.IOException;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class NewJResultFactory extends DefaultJResultFactory {

  /**
   * <p>Creates the sourcefile classloader to be used given the input params.
   * Returns null if no source files are specified in the params.  Subclasses
   * may override to change the way in which java sources are loaded.</p>
   */
  protected JClassLoader createSourceLoader(JResultParamsImpl params,
                                            JClassLoader parent)
          throws IOException
  {
    ParserClassBuilder pcb = new ParserClassBuilder(params);
    return new JClassLoaderImpl(pcb,null);
  }

}

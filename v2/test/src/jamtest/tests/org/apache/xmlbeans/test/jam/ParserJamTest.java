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
package org.apache.xmlbeans.test.jam;

import org.apache.xmlbeans.impl.jam.JResult;
import org.apache.xmlbeans.impl.jam.JResultFactory;
import org.apache.xmlbeans.impl.jam.JResultParams;
import org.apache.xmlbeans.impl.jam.provider.NewJResultFactory;

import java.io.IOException;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class ParserJamTest extends JamTestBase {

  // ========================================================================
  // Constructors

  public ParserJamTest(String name) {
    super(name);
  }

  // ========================================================================
  // JamTestBase implementation

  protected JResult getResultToTest() throws IOException {
    JResultFactory jsf = new NewJResultFactory();
    JResultParams params = jsf.createResultParams();
    params.includeSourceFiles(getDummyclassesSourceRoot(),"**/*.java");
    return jsf.createResult(params);
  }

  protected boolean isAnnotationsAvailable() {
    return true;
  }

  protected boolean isParameterNamesKnown() {
    return true;
  }
}
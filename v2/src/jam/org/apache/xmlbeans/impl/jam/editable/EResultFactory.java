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

package org.apache.xmlbeans.impl.jam.editable;

import org.apache.xmlbeans.impl.jam.JResult;
import org.apache.xmlbeans.impl.jam.internal.JamPrinter;
import org.apache.xmlbeans.impl.jam.editable.impl.EResultParamsImpl;
import org.apache.xmlbeans.impl.jam.editable.impl.EResultImpl;

import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class EResultFactory {

  // ========================================================================
  // Constants

  private static final EResultFactory INSTANCE = new EResultFactory();

  // ========================================================================
  // Singleton

  /**
   * Return the factory singleton.
   */
  public static EResultFactory getInstance() { return INSTANCE; }

  private EResultFactory() {}

  // ========================================================================
  // Public methods

  /**
   * Create a new JResultParams instance.  The params can be populated
   * and then given to the createService method to create a new JResult.
   */
  public EResultParams createServiceParams() {
    return new EResultParamsImpl();
  }

  /**
   * Create a new JResult from the given parameters.
   *
   * @throws IllegalArgumentException if the params is null or not
   * an instance returned by createServiceParams().
   */
  public EResult createService(EResultParams params) {
    return new EResultImpl((EResultParamsImpl)params);
  }

  public static void main(String[] args) {
    PrintWriter out = new PrintWriter(System.out);
    out.println("Running EServiceTest");
    try {
      EResultFactory factory = EResultFactory.getInstance();
      EResultParams params = factory.createServiceParams();
      EResult service = factory.createService(params);
      //
      //dumb test code
      //
      EClass testClass = service.addNewClass("com.bea.pcal","TestClass");
      EClass fooClass = service.addNewClass("com.bea.pcal","Foo");
      testClass.addNewMethod().setReturnType(fooClass);
      testClass.addNewField().setUnqualifiedType("TestClass");
      //

      JamPrinter.newInstance().print(service.getClasses(),out);

    } catch(Exception e) {
      e.printStackTrace();
    }
    out.flush();
    System.out.flush();
    System.err.flush();
  }
}

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

package org.apache.xmlbeans.impl.jam;

import org.apache.xmlbeans.impl.jam.internal.JamPrinter;
import org.apache.xmlbeans.impl.jam.provider.DefaultJResultFactory;

import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;

/**
 * This is the normal entry point into the JAM subsystem.  JServiceFactory
 * is a singleton factory which can create a new JServiceParams and
 * JServices.  Here is a code sample that demonstrates how to use
 * JServiceFactory.
 *
 * <pre>
 * JServiceFactory factory = JServiceFactory.getInstance();
 * JServiceParams params = factory.createServiceParams();
 * params.includeSources(new File("c:/myproject/src","mypackage/*.java"));
 * JService service = factory.createService(params);
 * </pre>
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class JServiceFactory {

  // ========================================================================
  // Constants

  private static final JServiceFactory DEFAULT = new DefaultJResultFactory();

  // ========================================================================
  // Singleton

  /**
   * Return the default factory singleton for this VM.
   */
  public static JServiceFactory getInstance() { return DEFAULT; }

  // ========================================================================
  // Constructors

  protected JServiceFactory() {}

  // ========================================================================
  // Public methods

  /**
   * Create a new JServiceParams instance.  The params can be populated
   * and then given to the createService method to create a new JService.
   */
  public abstract JServiceParams createServiceParams();

  /**
   * Create a new JService from the given parameters.
   *
   * @throws IOException if an IO error occurred while creating the service
   * @throws IllegalArgumentException if the params is null or not
   * an instance returned by createServiceParams().
   */
  public abstract JService createService(JServiceParams params) throws IOException;

  // ========================================================================
  // main() method

  public static void main(String[] args) {
    try {
      JServiceParams sp = getInstance().createServiceParams();
      for(int i=0; i<args.length; i++) {
        sp.includeSourceFiles(new File("."),args[i]);
      }
      JService service = getInstance().createService(sp);
      JamPrinter jp = JamPrinter.newInstance();
      PrintWriter out = new PrintWriter(System.out);
      for(JClassIterator i = service.getClasses(); i.hasNext(); ) {
        out.println("-------- ");
        jp.print(i.nextClass(),out);
      }
      out.flush();
    } catch(Exception e){
      e.printStackTrace();
    }
    System.out.flush();
    System.err.flush();

  }
}

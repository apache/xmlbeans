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
import org.apache.xmlbeans.impl.jam.internal.reflect.ReflectClassBuilder;
import org.apache.xmlbeans.impl.jam.provider.JamServiceFactoryImpl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Start here!  This is the normal entry point into the JAM subsystem.
 * JamServiceFactory is a singleton factory which can create a new
 * JamServiceParams and JServices.  Here is a code sample that demonstrates
 * how to use JamServiceFactory.
 *
 * <pre>
 * // Get the factory singleton
 * JamServiceFactory factory = JamServiceFactory.getInstance();
 *
 * // Use the factory to create an object that we can use to specify what
 * // java types we want to view
 * JamServiceParams params = factory.createServiceParams();
 *
 * // Include the classes under mypackage
 * params.includeSources(new File("c:/myproject/src","mypackage/*.java"));
 *
 * // Create a JamService, which will contain JClasses for the classes found in mypackage
 * JamService service = factory.createService(params);
 * </pre>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public abstract class JamServiceFactory {

  // ========================================================================
  // Constants

  private static final JamServiceFactory DEFAULT = new JamServiceFactoryImpl();

  // ========================================================================
  // Singleton

  /**
   * Return the default factory singleton for this VM.
   */
  public static JamServiceFactory getInstance() { return DEFAULT; }

  // ========================================================================
  // Constructors

  protected JamServiceFactory() {}

  // ========================================================================
  // Public methods

  /**
   * Create a new JamServiceParams instance.  The params can be populated
   * and then given to the createService method to create a new JamService.
   */
  public abstract JamServiceParams createServiceParams();

  /**
   * Create a new JamService from the given parameters.
   *
   * @throws IOException if an IO error occurred while creating the service
   * @throws IllegalArgumentException if the params is null or not
   * an instance returned by createServiceParams().
   */
  public abstract JamService createService(JamServiceParams params) throws IOException;


  // ========================================================================
  // main() method

  public static void main(String[] args) {
    try {
      JamServiceParams sp = getInstance().createServiceParams();
      for(int i=0; i<args.length; i++) {
        sp.includeSourcePattern(new File[] {new File(".")},args[i]);
      }
      JamService service = getInstance().createService(sp);
      JamPrinter jp = JamPrinter.newInstance();
      PrintWriter out = new PrintWriter(System.out);
      for(JamClassIterator i = service.getClasses(); i.hasNext(); ) {
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

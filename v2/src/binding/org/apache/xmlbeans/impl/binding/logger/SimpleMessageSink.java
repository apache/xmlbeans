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

package org.apache.xmlbeans.impl.binding.logger;

import java.io.PrintWriter;

/**
 * Implementation of MessageSink that just spews out to some Writer.
 */
public class SimpleMessageSink implements MessageSink {

  // ========================================================================
  // Constants

  public static final MessageSink STDOUT = new SimpleMessageSink();

  // ========================================================================
  // Variables

  private PrintWriter mOut;

  // ========================================================================
  // Constructors

  public SimpleMessageSink() {
    this(new PrintWriter(System.out));
  }

  public SimpleMessageSink(PrintWriter out) {
    if (out == null) throw new IllegalArgumentException();
    mOut = out;
  }

  // ========================================================================
  // MessageSink implementation

  public void log(Message msg) {
    mOut.print(msg.toString());
    mOut.flush();
  }
}

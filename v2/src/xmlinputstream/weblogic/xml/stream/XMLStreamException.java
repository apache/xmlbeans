/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights 
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer. 
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:  
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must 
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written 
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache 
*    XMLBeans", nor may "Apache" appear in their name, without prior 
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems 
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package weblogic.xml.stream;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import weblogic.utils.NestedThrowable;

/**
 * The base exception for unexpected input during XML handling
 *
 * @since Weblogic XML Input Stream 1.0
 * @version 1.0
 */

public class XMLStreamException 
  extends IOException 
  implements NestedThrowable 
{
  protected Throwable th;

  public XMLStreamException() {}

  public XMLStreamException(String msg) { 
    super(msg); 
  }

  public XMLStreamException(Throwable th) {
    this.th = th;
    
  }

  public XMLStreamException(String msg, Throwable th) {
    super(msg);
    this.th = th;
  }

  /**
   * Gets the nested exception.
   *
   * @return                 Nested exception
   */
  public Throwable getNestedException() {
    return getNested();
  }

  //try to do someting useful
  public String getMessage() {
    String msg = super.getMessage();

    if (msg == null && th != null) {
      return th.getMessage();
    } else {
      return msg;
    }
  }


  // =================================================================
  // NestedThrowable implementation.

  /**
   * Gets the nested Throwable.
   *
   * @return                 Nested exception
   */
  public Throwable getNested() {
    return th;
  }

  public String superToString() {
    return super.toString();
  }

  public void superPrintStackTrace(PrintStream ps) {
    super.printStackTrace(ps);
  }

  public void superPrintStackTrace(PrintWriter pw) {
    super.printStackTrace(pw);
  }

  // End NestedThrowable implementation.
  // =================================================================

  /**
   * Prints the exception message and its nested exception message.
   *
   * @return                 String representation of the exception
   */
  public String toString() {
    return NestedThrowable.Util.toString(this);
  }

  /**
   * Prints the stack trace associated with this exception and
   * its nested exception.
   *
   * @param s                 PrintStream
   */
  public void printStackTrace(PrintStream s) { 
    NestedThrowable.Util.printStackTrace(this, s);
  }

  /**
   * Prints the stack trace associated with this exception and
   * its nested exception.
   *
   * @param s                 PrintStream
   */
  public void printStackTrace(PrintWriter w) { 
    NestedThrowable.Util.printStackTrace(this, w);
  }

  /**
   * Prints the stack trace associated with this exception and
   * its nested exception to System.err.
   *
   * @param s                 PrintStream
   */
  public void printStackTrace() {
    printStackTrace(System.err);
  }
}



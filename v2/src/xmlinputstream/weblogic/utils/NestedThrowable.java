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

package weblogic.utils;

/**
 * The interface implemented by NestedException, NestedError, and
 * NestedRuntimeException largely so Util can provide a standard
 * implementation of toString() and printStackTrace()
 *
 * @deprecated use JDK 1.4 style nested throwables where possible.
 *
 * @author WebLogic
 */

import java.io.PrintWriter;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

public interface NestedThrowable {

  /** Get the nested Throwable. */
  Throwable getNested();

  /** Call super.toString(). [Kludge but necessary.] */
  String superToString();

  /** Call super.printStackTrace(). [Kludge but necessary.] */
  void superPrintStackTrace(PrintStream ps);

  /** Call super.printStackTrace(). [Kludge but necessary.] */
  void superPrintStackTrace(PrintWriter po);

  static class Util {

    private static String EOL = System.getProperty("line.separator");

    /**
     * Prints the exception message and its nested exception message.
     *
     * @return                 String representation of the exception
     */
    public static String toString(NestedThrowable nt) {
      Throwable nested = nt.getNested();
      if (nested == null) {
        return nt.superToString();
      } else {
        return nt.superToString() + " - with nested exception:" + 
          EOL + "[" + nestedToString(nested) + "]";
      }
    }

    private static String nestedToString(Throwable nested) {
      if (nested instanceof InvocationTargetException) {
        InvocationTargetException ite = (InvocationTargetException) nested;
        return nested.toString() + " - with target exception:" + 
          EOL + "[" + ite.getTargetException().toString() +
          "]";
      }
      return nested.toString();
    }

    /**
     * Prints the stack trace associated with this exception and
     * its nested exception.
     *
     * @param s                 PrintStream
     */
    public static void printStackTrace(NestedThrowable nt, PrintStream s) { 
      Throwable nested = nt.getNested();
      if (nested != null) {
        nested.printStackTrace(s);
        s.println("--------------- nested within: ------------------");
      }
      nt.superPrintStackTrace(s);
    }

    /**
     * Prints the stack trace associated with this exception and
     * its nested exception.
     *
     * @param w                 PrintWriter
     */
    public static void printStackTrace(NestedThrowable nt, PrintWriter w) { 
      Throwable nested = nt.getNested();
      if (nested != null) {
        nested.printStackTrace(w);
        w.println("--------------- nested within: ------------------");
      }
      nt.superPrintStackTrace(w);
    }
  }

}

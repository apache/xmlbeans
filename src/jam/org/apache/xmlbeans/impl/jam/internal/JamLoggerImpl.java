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
package org.apache.xmlbeans.impl.jam.internal;

import org.apache.xmlbeans.impl.jam.provider.JamLogger;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JamLoggerImpl implements JamLogger {

  // ========================================================================
  // Variables

  private boolean mShowWarnings = true;
  private Set mVerboseClasses = null;
  private PrintWriter mOut = new PrintWriter(System.out,true);

  //FIXME temp until JamServiceContextImpl is no longer a subclass
  protected void setOut(PrintWriter out) { mOut= out; }

  // ========================================================================
  // JamLogger implementation

  public boolean isVerbose(Object o) {
    if (mVerboseClasses == null) return false;
    Iterator i = mVerboseClasses.iterator();
    while(i.hasNext()) {
      Class c = (Class)i.next();
      if (c.isAssignableFrom(o.getClass())) return true;
    }
    return false;
  }

  public boolean isVerbose(Class aClass) {
    if (mVerboseClasses == null) return false;
    Iterator i = mVerboseClasses.iterator();
    while(i.hasNext()) {
      Class c = (Class)i.next();
      if (c.isAssignableFrom(aClass)) return true;
    }
    return false;
  }

  public void setVerbose(Class c) {
    if (c == null) throw new IllegalArgumentException();
    if (mVerboseClasses == null) mVerboseClasses = new HashSet();
    mVerboseClasses.add(c);
  }

  public void setShowWarnings(boolean b) {
    mShowWarnings = b;
  }
  
  public void verbose(String msg, Object o) {
    if (isVerbose(o)) verbose(msg);
  }

  public void verbose(Throwable t, Object o) {
    if (isVerbose(o)) verbose(t);
  }

  public void verbose(String msg) {
    printVerbosePrefix();
    mOut.println(msg);
  }


  public void verbose(Throwable t) {
    printVerbosePrefix();
    mOut.println();
    t.printStackTrace(mOut);
  }

  public void warning(Throwable t) {
    if (mShowWarnings) {
      mOut.println("[JAM] Warning: unexpected exception thrown: ");
      t.printStackTrace();
    }
  }

  public void warning(String w) {
    if (mShowWarnings) {

      mOut.print("[JAM] Warning: ");
      mOut.println(w);
    }
  }

  public void error(Throwable t) {
    mOut.println("[JAM] Error: unexpected exception thrown: ");
    t.printStackTrace(mOut);
  }

  public void error(String msg) {
    mOut.print("[JAM] Error: ");
    mOut.println(msg);
  }

  // ========================================================================
  // Deprecated methods

  public void setVerbose(boolean v) { setVerbose(Object.class); }

  public boolean isVerbose() { return mVerboseClasses != null; }

  // ========================================================================
  // Private methods

  private void printVerbosePrefix() {
    StackTraceElement[] st = new Exception().getStackTrace();
    mOut.println("[JAM] Verbose: ");
    mOut.print('(');
    mOut.print(shortName(st[2].getClassName()));
    mOut.print('.');
    mOut.print(st[2].getMethodName());
    mOut.print(':');
    mOut.print(st[2].getLineNumber());
    mOut.print(")  ");
  }

  private static String shortName(String className) {
    int index = className.lastIndexOf('.');

    if (index != -1 ) {
      className = className.substring(index+1, className.length());
    }

    return className;
  }

}

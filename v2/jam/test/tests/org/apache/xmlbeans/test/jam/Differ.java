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

import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.Writer;
import java.io.StringWriter;


/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class Differ {

  // ========================================================================
  // Singleton

  public static Differ getInstance() { return INSTANCE; }

  private static final Differ INSTANCE = new Differ();

  private Differ() {}

  // ========================================================================
  // Public methods

  public boolean diff(Reader result,
                      Reader expect,
                      Writer out) throws IOException
  {
    //our glorious diffing algorithm.  FIXME find one we can use
    String resultString = getContentsAsString(result);
    String expectedString = getContentsAsString(expect);
    if (resultString.trim().equals(expectedString.trim())) return true;
    out.write("Result does not match expected value.  Result is:\n");
    out.write(resultString);
    out.write("\n\n Expected:\n");
    out.write(expectedString);
    return false;
  }


  // ========================================================================
  // Private methods

  private static String getContentsAsString(Reader in) throws IOException {
    StringWriter out = new StringWriter();
    BufferedReader reader = new BufferedReader(in);
    char[] buff = new char[1024];
    int len;
    while((len = reader.read(buff)) != -1) out.write(buff,0,len);
    return out.toString();
  }

  /*
  private static String[] getContentsAsArray(Reader in) throws IOException {
    BufferedReader reader = new BufferedReader(in);
    List list = new ArrayList();
    String s;
    while((s = reader.readLine()) != null) list.add(s);
    String[] out = new String[list.size()];
    list.toArray(out);
    return out;
  }
  */
}

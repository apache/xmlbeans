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
import java.util.ArrayList;
import java.util.List;


/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JamDiffer {

  // ========================================================================
  // Singleton

  public static JamDiffer getInstance() { return INSTANCE; }

  private static final JamDiffer INSTANCE = new JamDiffer();

  private JamDiffer() {}

  // ========================================================================
  // Public methods

  public boolean diff(Reader a,
                      Reader b,
                      Writer out) throws IOException
  {
    return true; //FIXME
    /*Object[] aContents = getContents(a);
    Object[] bContents = getContents(b);
    Diff diff = new Diff(aContents, bContents);
    Diff.change script = diff.diff_2(false);
    if (script != null) {
      DiffPrint.NormalPrint p = new DiffPrint.NormalPrint(aContents, bContents);
      p.setOutput(out);
      p.print_script(script);
      return false;
    }
    return true;*/
  }


  // ========================================================================
  // Private methods

  private static String[] getContents(Reader in) throws IOException {
    BufferedReader reader = new BufferedReader(in);
    List list = new ArrayList();
    String s;
    while((s = reader.readLine()) != null) list.add(s);
    String[] out = new String[list.size()];
    list.toArray(out);
    return out;
  }
}

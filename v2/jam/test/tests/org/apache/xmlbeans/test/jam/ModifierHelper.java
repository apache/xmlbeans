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

import java.util.StringTokenizer;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Modifier;

/**
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class ModifierHelper {

  public static int parseModifiers(String modString) {
    StringTokenizer st = new StringTokenizer(modString);
    int mods = 0;
    while(st.hasMoreTokens()) {
      int mask = getMaskFor(st.nextToken().trim());
      mods = (mods | mask);
    }
    return mods;
  }

  public static int getMaskFor(String name) {
    Integer i = (Integer)mToken2Mask.get(name);
    if (i == null) throw new IllegalArgumentException("unknown modifier '"+
                                                      name+"'");
    return i.intValue();
  }

  // ========================================================================
  // Static initialization

  private static Map mToken2Mask;

  private static Object[][] MASKS = {
    {"private",new Integer(Modifier.PRIVATE)},
    {"protected",new Integer(Modifier.PROTECTED)},
    {"public",new Integer(Modifier.PUBLIC)},
    {"abstract",new Integer(Modifier.ABSTRACT)},
    {"final",new Integer(Modifier.FINAL)},
    {"strictfp",new Integer(Modifier.STRICT)},
    {"synchronized",new Integer(Modifier.SYNCHRONIZED)},
    {"final",new Integer(Modifier.FINAL)},
    {"static",new Integer(Modifier.STATIC)},
    {"volatile",new Integer(Modifier.VOLATILE)},
    {"transient",new Integer(Modifier.TRANSIENT)}
  };

  static {
    mToken2Mask = new HashMap();
    for(int i=0; i<MASKS.length; i++) {
      mToken2Mask.put(MASKS[i][0],MASKS[i][1]);
    }
  };



}

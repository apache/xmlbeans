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
package org.apache.xmlbeans.impl.jam.internal.java15;

import org.apache.xmlbeans.impl.jam.internal.javadoc.JavadocRunner;
import com.sun.javadoc.LanguageVersion;

/**
 * This class exists solely so that we can override the languageVersion
 * method while still providing a 1.4-compatible build- and runtime.
 * LanguageVersion is an enum so we can only use this under 1.5.
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JavadocRunner_150 extends JavadocRunner {

  public static LanguageVersion languageVersion() {
    return LanguageVersion.JAVA_1_5;
  }
}

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
package org.apache.xmlbeans.test.jam.dummyclasses;

/**
 * Tests case for javadoc tags is declared more than once in a document.
 */
public class ManyTags {

  /**
   * @foo x=-43 y=124 z=79
   * @foo y=2
   * @foo z=3
   * x=1
   * @foo w = 0
   *
   * @bar x=-4343
   *
   * @baz x=1
   */
  public int getId() { return -1; }
  
}

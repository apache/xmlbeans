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
package org.apache.xmlbeans.impl.jam.annogen;

import org.apache.xmlbeans.impl.jam.annogen.provider.ProxyPopulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public interface AnnotationServiceParams {

  // ========================================================================
  // Public methods

  public void addXmlOverrides(File file) throws FileNotFoundException;

  public void addXmlOverrides(Reader in);

  public void insertPopulator(ProxyPopulator pop);

  public void appendPopulator(ProxyPopulator pop);

}

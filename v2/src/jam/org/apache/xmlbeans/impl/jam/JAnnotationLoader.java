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

package org.apache.xmlbeans.impl.jam;

import java.util.Collection;

/**
 * <p>Interface implemented by supplemental markup providers.  An
 * example would be an properties or XML file which contains external
 * markup.  Another example might be a class which wishes to
 * programmatically add markup to a JAM.</p>
 *
 * @author Patrick Calaham <pcal@bea.com>
 * @deprecated This functionality will soon be supplanted by JStore.
 */
public interface JAnnotationLoader {

  public void getAnnotations(JElement a, Collection out);

}

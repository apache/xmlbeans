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

package dom.common;

import org.apache.xmlbeans.impl.newstore2.Public2;


public class NewLoader extends Loader{

    public org.w3c.dom.Document load(String sXml){
        return (org.w3c.dom.Document)Public2.parse(sXml);
    }
      public org.w3c.dom.Document loadSync(String sXml){
           org.w3c.dom.Document doc=load(sXml);
          Public2.setSync(doc,true);
          return doc;

      }
}

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
package org.apache.xmlbeans.impl.jam.xml;

import org.apache.xmlbeans.impl.jam.JamService;
import org.apache.xmlbeans.impl.jam.JamServiceFactory;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.provider.JamServiceFactoryImpl;
import org.apache.xmlbeans.impl.jam.internal.CachedClassBuilder;
import org.apache.xmlbeans.impl.jam.internal.JamServiceImpl;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.IOException;
import java.io.Writer;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JamXmlUtils {

  // ========================================================================
  // Singleton

  public static final JamXmlUtils getInstance() { return INSTANCE; }

  private static final JamXmlUtils INSTANCE = new JamXmlUtils();

  private JamXmlUtils() {}

  // ========================================================================
  // Public methods

  public JamService createService(InputStream in)
    throws IOException, XMLStreamException
  {
    JamServiceFactoryImpl jsf = (JamServiceFactoryImpl)JamServiceFactory.getInstance();
    CachedClassBuilder cache = new CachedClassBuilder();
    JamService out = jsf.createService(cache);
    JamXmlReader reader = new JamXmlReader(cache,in);
    reader.read();
    ((JamServiceImpl)out).setClassNames(cache.getClassNames());//FIXME!! gross
    return out;
  }

  public void toXml(JClass[] clazzes, Writer writer)
    throws IOException, XMLStreamException
  {
    if (clazzes == null) throw new IllegalArgumentException("null classes");
    if (writer == null) throw new IllegalArgumentException("null writer");
    JamXmlWriter out = new JamXmlWriter(writer);
    out.begin();
    for(int i=0; i<clazzes.length; i++) out.write(clazzes[i]);
    out.end();
  }

}

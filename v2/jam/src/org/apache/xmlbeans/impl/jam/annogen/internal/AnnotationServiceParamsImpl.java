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
package org.apache.xmlbeans.impl.jam.annogen.internal;

import org.apache.xmlbeans.impl.jam.annogen.provider.ProxyPopulator;
import org.apache.xmlbeans.impl.jam.annogen.provider.ProxyTypeMapping;
import org.apache.xmlbeans.impl.jam.annogen.provider.ProxyContext;
import org.apache.xmlbeans.impl.jam.annogen.AnnotationServiceParams;
import org.apache.xmlbeans.impl.jam.annogen.AnnotationServiceFactory;
import org.apache.xmlbeans.impl.jam.provider.JamLogger;
import org.apache.xmlbeans.impl.jam.internal.JamLoggerImpl;

import java.io.Reader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.LinkedList;

/**
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class AnnotationServiceParamsImpl implements
  AnnotationServiceParams, ProxyContext {

  // ========================================================================
  // Variables

  private LinkedList mPopulators = new LinkedList();
  private ProxyTypeMapping mProxyMapping = null;
  private JamLogger mLogger = new JamLoggerImpl();

  // ========================================================================
  // Constructors

  public AnnotationServiceParamsImpl() {
/*
    ProxyPopulator reflectProxy =
      AnnotationServiceFactory.getInstance().getReflectingPopulator();
    if (reflectProxy != null) mPopulators.add(reflectProxy);
*/
    ProxyPopulator javadocProxy =
      AnnotationServiceFactory.getInstance().getJavadocPopulator();
    if (javadocProxy != null) mPopulators.add(javadocProxy);

  }

  // ========================================================================
  // Public methods

  public void addXmlOverrides(File file) throws FileNotFoundException {
    addXmlOverrides(new FileReader(file));
  }

  public void addXmlOverrides(Reader in) {
    throw new IllegalArgumentException("NYI");
    // addNewPopulator(new XmlPopulator(in));
  }

  public void insertPopulator(ProxyPopulator pop) {
    mPopulators.addFirst(pop);
  }

  public void appendPopulator(ProxyPopulator pop) {
    mPopulators.addLast(pop);
  }

  public void setProxyMapping(ProxyTypeMapping pm) {
    mProxyMapping = pm;
    pm.init(this);
  }

  // ========================================================================
  // Internal use only

  public ProxyPopulator[] getPopulators() {
    ProxyPopulator[] out = new ProxyPopulator[mPopulators.size()];
    mPopulators.toArray(out);
    return out;
  }

  public ProxyTypeMapping getProxyMapping() {
    if (mProxyMapping == null) setProxyMapping(new DefaultProxyMapping());
    return mProxyMapping;
  }


  // ========================================================================
  // Provider context implementation

  public JamLogger getLogger() { return mLogger; }

  public ProxyTypeMapping getProxyTypeMapping() { return mProxyMapping; }

  public ClassLoader getClassLoader() {
    return ClassLoader.getSystemClassLoader(); //FIXME
  }
}

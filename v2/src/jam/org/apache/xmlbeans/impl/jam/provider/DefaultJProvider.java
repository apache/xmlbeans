/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache
*    XMLBeans", nor may "Apache" appear in their name, without prior
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2003 BEA Systems
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/
package org.apache.xmlbeans.impl.jam.provider;

import org.apache.xmlbeans.impl.jam.JProvider;
import org.apache.xmlbeans.impl.jam.internal.JServiceParamsImpl;
import org.apache.xmlbeans.impl.jam.internal.reflect.RClassBuilder;
import org.apache.xmlbeans.impl.jam.internal.javadoc.JDClassBuilder;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Singleton which is the DefaultJProvider to be used in the current VM.
 * This is the Provider to which the ServiceFactory delegates.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class DefaultJProvider extends BaseJProvider {

  // ========================================================================
  // Constants

  private static final JProvider INSTANCE = new DefaultJProvider();

  // ========================================================================
  // Singleton

  public static JProvider getInstance() { return INSTANCE; }

  // ========================================================================
  // BaseJProvider implementation

  public JClassBuilder createJClassBuilder(JClassBuilderParams params)
          throws IOException
  {
    List builderList = new ArrayList();
    if (params.getInputSourcepath() != null) {
      builderList.add(createSourceService(params));
    }
    if (params.getInputClasspath() != null) {
      builderList.add(createClassService(params));
    }
    JClassBuilder[] builderArray = new JClassBuilder[builderList.size()];
    builderList.toArray(builderArray);
    return new CompositeJClassBuilder(builderArray);
  }

  // ========================================================================
  // Public methods

  public JClassBuilder createSourceService(JClassBuilderParams jp)
          throws IOException
  {
    //FIXME someday should make the name of the service class to use here
    //settable via a system property
    return JDClassBuilder.create((JServiceParamsImpl)jp);
  }

  public JClassBuilder createClassService(JClassBuilderParams jp)
          throws IOException
  {
    //FIXME someday should make the name of the service class to use here
    //settable via a system property
    JPath cp = jp.getInputClasspath();
    if (cp == null) {
      return RClassBuilder.getSystemClassBuilder();
    } else {
      URL[] urls = cp.toUrlPath();
      return RClassBuilder.getClassBuilderFor(new URLClassLoader(urls));
    }
  }


}
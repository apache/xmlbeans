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

import org.apache.xmlbeans.impl.jam.JamClassLoader;
import org.apache.xmlbeans.impl.jam.visitor.MVisitor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * <p>Structure containing information given to a BaseJProvider subclass in
 * order to instantiate a new JStore.  This interface is the flip-side
 * of JamServiceParams - it provides a view into the params the user specified.
 * In reality, JamServiceContext and JamServiceParams are implemented by the
 * same object, JamServiceContextImpl, but it is cleaner to separate
 * the users' view of that data (which is write-only) from the provider's
 * view (which is read-only).</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public interface JamServiceContext extends JamLogger {

  // ========================================================================
  // Public methods

  /**
   * @return The classpath to be searched when trying to initialize an MClass
   * for a java class which was not in the inputSources or inputClasses,
   * or null.
   */
  public ResourcePath getInputClasspath();

  /**
   * @return The sourcepath to be searched when trying to initialize an MClass
   * for a java class which was not in the inputSources or inputClasses, or
   * null.
   */
  public ResourcePath getInputSourcepath();

  /**
   * @return The classpath to be used in loading external classes on which
   * the service implementation depends, or null.  This is not generally
   * needed.
   */
  public ResourcePath getToolClasspath();

  /**
   * @return an implementation-specific property, as specified by
   * <code>JamServiceParams.setProperty()</code>.
   */
  public String getProperty(String name);

  public JamClassLoader getParentClassLoader();

  public MVisitor getInitializer();

  public boolean isUseSystemClasspath();

  public File[] getSourceFiles() throws IOException;

  public String[] getAllClassnames() throws IOException;

  //public PrintWriter getOut();


  // ========================================================================
  // killme

  /**
   * <p>Returns a subclass of TypedAnnotationProxyBase that should be instantiated
   * and used to proxy annotation metadata when no registered proxy is
   * available.  By default, this is DefaultAnnotationProxy.class, though
   * the user can override this if, for example, they need to change the
   * default javadoc tag parsing behavior.</p>
   *
   * @return
   */

}
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
package org.apache.xmlbeans.impl.binding.tylar;

import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BuiltinBindingLoader;
import org.apache.xmlbeans.impl.binding.bts.PathBindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.JFactory;
import org.apache.xmlbeans.*;
import org.w3.x2001.xmlSchema.SchemaDocument;
import java.net.URI;

/**
 * Base class for simplifying implementation of the Tylar interface.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public abstract class BaseTylarImpl implements Tylar {

  // ========================================================================
  // Partial default Tylar implementation

  public String getDescription() {
    URI uri = getLocation();
    if (uri != null) return uri.toString();
    return "["+this.getClass().getName()+"]";
  }

  public URI getLocation() {
    return null;
  }

  public BindingLoader getBindingLoader() {
    //REVIEW should consider caching this result
    BindingFile[] bfs = getBindingFiles();
    BindingLoader[] loaders = new BindingLoader[bfs.length+1];
    System.arraycopy(bfs,0,loaders,0,bfs.length);
    loaders[loaders.length-1] = BuiltinBindingLoader.getInstance();
    return PathBindingLoader.forPath(loaders);
  }

  public SchemaTypeSystem getSchemaTypeSystem()
  {
    // REVIEW should consider caching this result
    SchemaDocument[] xsds = getSchemas();
    XmlObject[] xxds = new XmlObject[xsds.length];
    for(int i=0; i<xsds.length; i++) xxds[i] = xsds[i].getSchema();
    try {
      return XmlBeans.compileXsd(xxds,XmlBeans.getBuiltinTypeSystem(),null);
    } catch(XmlException xe) {
      // REVIEW we need to enforce an invariant that a tylar with invalid
      // schemas can never be instantiated.
      xe.printStackTrace();
      throw new IllegalStateException(xe.getMessage());
    }
  }

  public JClassLoader getJClassLoader()
  {
    // REVIEW should consider caching this result
    // create a classloader chain that runs throw all of the base tylars
    ClassLoader cl = createClassLoader(ClassLoader.getSystemClassLoader());
    return JFactory.getInstance().createClassLoader(cl,null,null);
  }
}
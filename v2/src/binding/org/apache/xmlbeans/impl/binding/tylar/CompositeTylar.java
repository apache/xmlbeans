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

import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.w3.x2001.xmlSchema.SchemaDocument;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Implementation of Tylar which is a composition of other Tylars.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class CompositeTylar extends BaseTylarImpl {

  // ========================================================================
  // Variables

  private Tylar[] mTylars; //the tylars we are composing

  // ========================================================================
  // Constructors

  /**
   * Constructs a composition of the tylars in the given collection.  Bindings
   * and types will be sought in the tylars in the order in which they are
   * presented in this collection (as returned by Collection.iterator()).
   *
   * @throws IllegalArgumentException if any object in the collection is not
   * a Tylar or the collection is null.
   */
  public CompositeTylar(Collection tylars) {
    if (tylars == null) throw new IllegalArgumentException("null tylars");
    mTylars = new Tylar[tylars.size()];
    int n = 0;
    for(Iterator i = tylars.iterator(); i.hasNext(); n++) {
      Object next = i.next();
      if (next instanceof Tylar) {
        mTylars[n] = (Tylar)next;
      } else {
        throw new IllegalArgumentException("Collection contains a "+
                next.getClass()+" which does not implement Tylar");
      }
    }
  }

  /**
   * Constructs a composition of the given Tylars.  Bindings and types
   * will be sought in the tylars in the order in which they are presented in
   * this array.
   */
  public CompositeTylar(Tylar[] tylars) {
    if (tylars == null) throw new IllegalArgumentException("null tylars");
    mTylars = tylars;
  }

  // ========================================================================
  // Tylar implementation

  public String getDescription() {
    return "CompositeTylar containing "+mTylars.length+" tylars";
  }

  public BindingFile[] getBindingFiles() {
    //REVIEW consider caching
    Collection all = new ArrayList();
    for(int i=0; i<mTylars.length; i++) {
      all.addAll(Arrays.asList(mTylars[i].getBindingFiles()));
    }
    BindingFile[] out = new BindingFile[all.size()];
    all.toArray(out);
    return out;
  }

  public SchemaDocument[] getSchemas() {
    //REVIEW consider caching
    Collection all = new ArrayList();
    for(int i=0; i<mTylars.length; i++) {
      all.addAll(Arrays.asList(mTylars[i].getSchemas()));
    }
    SchemaDocument[] out = new SchemaDocument[all.size()];
    all.toArray(out);
    return out;
  }

  public ClassLoader createClassLoader(ClassLoader cl) {
    //REVIEW consider caching
    for(int i=0; i<mTylars.length; i++) {
      cl = mTylars[i].createClassLoader(cl);
    }
    return cl;
  }
}

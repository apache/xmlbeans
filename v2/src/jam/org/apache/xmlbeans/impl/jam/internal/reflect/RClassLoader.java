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

package org.apache.xmlbeans.impl.jam.internal.reflect;


import java.util.HashMap;
import java.util.Map;
import org.apache.xmlbeans.impl.jam.JAnnotationLoader;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JClassLoader;
import org.apache.xmlbeans.impl.jam.JPackage;
import org.apache.xmlbeans.impl.jam.internal.*;

/**
 * java.lang.ClassLoader-backed implementation of JClassLoader.
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public class RClassLoader implements JClassLoader {

  // ========================================================================
  // Variables

  private ClassLoader mLoader;
  private Map mFd2Class = new HashMap(), mName2Package = new HashMap();
  private JClassLoader mParentLoader;

  // ========================================================================
  // Constructors

  public RClassLoader(ClassLoader c) {
    this(c,null);
  }
  
  public RClassLoader(ClassLoader c, JClassLoader parent) {
    if (c == null) throw new IllegalArgumentException("null classloader");
    mLoader = c;
    mParentLoader = parent; //this can be null
  }

  // ========================================================================
  // JClassLoader implementation

  public JClassLoader getParent() { return mParentLoader; }

  /**
   * 
   */
  public JAnnotationLoader getAnnotationLoader() { return null; }//FIXME

  /**
   * Returns a reflect representation of the named class.  
   */
  public JClass loadClass(String fd) {
    if (fd == null) throw new IllegalArgumentException("null fd");
    //
    //FIXME we should do some more work here to make sure that
    fd = fd.trim();
    // check cache first
    JClass out = (JClass)mFd2Class.get(fd);
    if (out != null) return out;
    // is it an array?
    if (fd.startsWith("[")) {
      mFd2Class.put(fd,out=ArrayJClass.createClassFor(fd,this));
      return out;
    }
    // see if it's a primitive.  REVIEW i think this check should only
    // happen if we're the system RClassLoader.
    out = PrimitiveJClass.getPrimitiveClassForName(fd);
    if (out != null) {
      mFd2Class.put(fd,out);
      return out;
    }
    // see if it's void.  REVIEW i think this check should only happen
    // if we're the system RClassLoader.
    if (VoidJClass.isVoid(fd)) {
      mFd2Class.put(fd,out = VoidJClass.getInstance());
      return out;
    }
    // still no dice.  ok, try to load it
    try {
      mFd2Class.put(fd,out=new RClass(mLoader.loadClass(fd),this));
      return out;
    } catch(ClassNotFoundException ignore) {}
    // doh.  ok, hand off to our parent.  if we don't have one, we're
    // done - it's unresolved.
    if (mParentLoader == null) {
      mFd2Class.put(fd,out = new UnresolvedJClass(fd));
      return out;
    } else {
      return mParentLoader.loadClass(fd);
    }
  }

  public JPackage getPackage(String named) {
    if (named == null) throw new IllegalArgumentException("null name");
    named = named.trim();
    JPackage out = (JPackage)mName2Package.get(named);
    if (out == null) {
      mName2Package.put(named,out = new JPackageImpl(named));
    }
    return out;
  }

  // ========================================================================
  // Package methods

  // this is just a choke point, in case for some reason this logic
  // ever needs to be modified.
  /*package*/ static JClass getClassFor(Class clazz, JClassLoader loader) {
    return loader.loadClass(clazz.getName());
  }

  // ========================================================================
  // Private methods

  private void validateClassName(String className)
          throws IllegalArgumentException
  {
    if (!Character.isJavaIdentifierStart(className.charAt(0))) {
      throw new IllegalArgumentException
              ("Invalid first character in class name: "+className);
    }
    for(int i=1; i<className.length(); i++) {
      char c = className.charAt(i);
      if (c == '.') {
        if (className.charAt(i-1) == '.') {
          throw new IllegalArgumentException
                  ("'..' not allowed in class name: "+className);
        }
        if (i == className.length()-1) {
          throw new IllegalArgumentException
                  ("'.' not allowed at end of class name: "+className);
        }
      } else {
        if (!Character.isJavaIdentifierPart(c)) {
          throw new IllegalArgumentException
                  ("Illegal character '"+c+"' in class name: "+className);
        }
      }
    }
  }
}

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
package org.apache.xmlbeans.impl.jam.internal;

import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JPackage;
import org.apache.xmlbeans.impl.jam.JamClassLoader;
import org.apache.xmlbeans.impl.jam.mutable.MClass;
import org.apache.xmlbeans.impl.jam.visitor.MVisitor;
import org.apache.xmlbeans.impl.jam.visitor.TraversingMVisitor;
import org.apache.xmlbeans.impl.jam.internal.elements.*;
import org.apache.xmlbeans.impl.jam.provider.JamClassBuilder;

import java.util.*;

/**
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JamClassLoaderImpl implements JamClassLoader {

  // ========================================================================
  // Variables

  private Map mName2Package = new HashMap();
  private Map mFd2ClassCache = null;
  private JamClassBuilder mBuilder;
  private MVisitor mInitializer = null;
  private ElementContext mContext;
  private Stack mInitializeStack = new Stack(); //fixme - decide how to store them
  private boolean mAlreadyInitializing = false;

  // ========================================================================
  // Constructor

  public JamClassLoaderImpl(ElementContext context,
                            JamClassBuilder builder,
                            MVisitor initializerOrNull) {
    if (builder == null) throw new IllegalArgumentException("null builder");
    if (context == null) throw new IllegalArgumentException("null builder");
    mBuilder = builder;
    mInitializer = (initializerOrNull == null) ? null : // null is ok, else
      new TraversingMVisitor(initializerOrNull); // wrap it in a walker
    mContext = context;
    initCache();
  }

  // ========================================================================
  // JamClassLoader implementation

  public final JClass loadClass(String fd)
  {
    fd = fd.trim();//REVIEW is this paranoid?
    MClass out = (MClass)mFd2ClassCache.get(fd);
    if (out != null) return out;
    if (fd.startsWith("[")) {
      return ArrayClassImpl.createClassForFD(fd,this);
    }
    // parse out the package and class names - this is kinda broken
    int dot = fd.lastIndexOf('.');
    String pkg;
    String name;
    if (dot == -1) {
      pkg = "";
      name = fd;
    } else {
      pkg  = fd.substring(0,dot);
      name = fd.substring(dot+1);
    }
    out = mBuilder.build(pkg,name);
    if (out == null) {
      //FIXME currently, the unqualified ref stuff will keep calling this,
      //newing up new UnresolvedClassImpls for each import until it finds
      //something.  We need to break out a separate checkClass() method
      //or something for them which returns null rather than UnresolvedClass.
      out = new UnresolvedClassImpl(pkg,name,mContext);
      mContext.warning("Warning: failed to resolve class '"+pkg+"' '"+name+"'");
      mFd2ClassCache.put(fd,out);
      return out;
    }
    mFd2ClassCache.put(fd,out);
    ((ClassImpl)out).setState(ClassImpl.POPULATING);
    mBuilder.populate(out);
    if (mInitializer == null) {
      ((ClassImpl)out).setState(ClassImpl.LOADED);
    } else {
      ((ClassImpl)out).setState(ClassImpl.INITIALIZING);
      // see comments below about this.  we need to document this more openly,
      // since it affects people writing initializers.
      if (mAlreadyInitializing) {
        // we already are running initializers, so we have to do it later
        mInitializeStack.push(out);
      } else {
        out.accept(mInitializer);
        ((ClassImpl)out).setState(ClassImpl.LOADED);
        while(!mInitializeStack.isEmpty()) {
          ClassImpl initme = (ClassImpl)mInitializeStack.pop();
          initme.accept(mInitializer);
          ((ClassImpl)out).setState(ClassImpl.LOADED);
        }
        mAlreadyInitializing = false;
      }
    }
    return out;
  }

  public JPackage getPackage(String named) {
    JPackage out = (JPackage)mName2Package.get(named);
    if (out == null) {
      out = new PackageImpl(mContext,named);
      mName2Package.put(named,out);
    }
    return out;
  }

  // ========================================================================
  // Private methods

  /**
   * <p>Stuff the primitives and void into the cache.</p>
   */
  private void initCache() {
    mFd2ClassCache = new HashMap();
    PrimitiveClassImpl.mapNameToPrimitive(mContext,mFd2ClassCache);
    mFd2ClassCache.put("void",new VoidClassImpl(mContext));
  }

  // ========================================================================
  // Public methods?

  /**
   * Returns an unmodifiable collection containing the JClasses which
   * have been resolved by this JamClassLoader.
   */
  public Collection getResolvedClasses() {
    return Collections.unmodifiableCollection(mFd2ClassCache.values());
  }

  public void addToCache(JClass c) {
    //FIXME hack for mutable classes for now
    mFd2ClassCache.put(c.getQualifiedName(),c);
  }

  //ok, the best thinking here is that when you are in an initializer
  //and you walk to another type, you will get a JClass that has a name
  //but is otherwise empty - it's not initialized.  It's like unresolved
  //except that it still has a chance to be resolved.
  //
  // Internally, the classloader will maintain a stack of classes to be
  // initialized.  When a class is first loaded, the initialization stack
  // is checked.  If it is empty, the class is placed on the stack and
  // initialization is performed on the item on the top of the stack until
  // the stack is empty.

  // If loadClass is called again further down in the stack frame,
  // at least one class will be on the initialization stack.  In this
  // case, the class is placed on the stack but initialization is not
  // performed immediately - the caller original caller higher in the stack
  // frame will do the initialization.

  // This scheme is necessary to prevent problems with cyclical initialization.
  //
//  public boolean isInitialized();


}
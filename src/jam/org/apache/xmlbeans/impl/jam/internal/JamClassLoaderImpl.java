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
import org.apache.xmlbeans.impl.jam.internal.elements.ArrayClassImpl;
import org.apache.xmlbeans.impl.jam.internal.elements.ClassImpl;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import org.apache.xmlbeans.impl.jam.internal.elements.PackageImpl;
import org.apache.xmlbeans.impl.jam.internal.elements.PrimitiveClassImpl;
import org.apache.xmlbeans.impl.jam.internal.elements.UnresolvedClassImpl;
import org.apache.xmlbeans.impl.jam.internal.elements.VoidClassImpl;
import org.apache.xmlbeans.impl.jam.mutable.MClass;
import org.apache.xmlbeans.impl.jam.provider.JamClassBuilder;
import org.apache.xmlbeans.impl.jam.visitor.MVisitor;
import org.apache.xmlbeans.impl.jam.visitor.TraversingMVisitor;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public class JamClassLoaderImpl implements JamClassLoader {

  // ========================================================================
  // Variables

  private Map mName2Package = new HashMap();
  private Map mFd2ClassCache = new HashMap();
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
    fd = fd.trim();
    JClass out = cacheGet(fd);
    if (out != null) return out;
    if (fd.indexOf('[') != -1) { // must be some kind of array name
      String normalFd = ArrayClassImpl.normalizeArrayName(fd);
      out = cacheGet(normalFd); // an array by any other name?
      if (out == null) {
        out = ArrayClassImpl.createClassForFD(normalFd,this);
        cachePut(out,normalFd);
      }
      cachePut(out,fd); // so we know it by the requested name as well
      return out;
    }
    {
      // check for loading inner class by name.  if it's not in the cache
      // yet, that means we need to go get the outer class.  when that's
      // done, the inner class will in the cache (or not).
      int dollar = fd.indexOf('$');
      if (dollar != -1) {
        String outerName = fd.substring(0,dollar);
        ((ClassImpl)loadClass(outerName)).ensureLoaded();
        out = cacheGet(fd);
        // parse out the package and class names - this is kinda broken
        int dot = fd.lastIndexOf('.');
        if (out == null) {
          String pkg;
          String name;
          if (dot == -1) {
            pkg = "";
            name = fd;
          } else {
            pkg  = fd.substring(0,dot);
            name = fd.substring(dot+1);
          }
          out = new UnresolvedClassImpl(pkg,name,mContext);
          mContext.warning("failed to resolve class "+fd);
          cachePut(out);
        }
        return out;
      }
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
      mContext.warning("failed to resolve class "+fd);
      cachePut(out);
      return out;
    }
    cachePut(out);
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
    PrimitiveClassImpl.mapNameToPrimitive(mContext,mFd2ClassCache);
    mFd2ClassCache.put("void",new VoidClassImpl(mContext));
  }

  private void cachePut(JClass clazz) {
    mFd2ClassCache.put(clazz.getFieldDescriptor().trim(),
                       new WeakReference(clazz));
  }

  private void cachePut(JClass clazz, String cachedName) {
    mFd2ClassCache.put(cachedName, new WeakReference(clazz));
  }

  private JClass cacheGet(String fd) {
    Object out = mFd2ClassCache.get(fd.trim());
    if (out == null) return null;
    if (out instanceof JClass) return (JClass)out;
    if (out instanceof WeakReference) {
      out = ((WeakReference)out).get();
      if (out == null) {
        mFd2ClassCache.remove(fd.trim());
        return null;
      } else {
        return (JClass)out;
      }
    }
    throw new IllegalStateException();
  }

  // ========================================================================
  // Public methods?

  //should only be called by ClassImpl
  public void initialize(ClassImpl out) {
    if (mInitializer != null) {
      // see comments below about this.  we need to document this more openly,
      // since it affects people writing initializers.
      if (mAlreadyInitializing) {
        // we already are running initializers, so we have to do this one later
        mInitializeStack.push(out);
      } else {
        out.accept(mInitializer);
        while(!mInitializeStack.isEmpty()) {
          ClassImpl initme = (ClassImpl)mInitializeStack.pop();
          initme.accept(mInitializer);
        }
        mAlreadyInitializing = false;
      }
    }
  }

  /**
   * Returns an unmodifiable collection containing the JClasses which
   * have been resolved by this JamClassLoader.
   */
  public Collection getResolvedClasses() {
    return Collections.unmodifiableCollection(mFd2ClassCache.values());
  }

  public void addToCache(JClass c) {
    //FIXME hack for mutable classes for now.  also for inner classes.
    cachePut((MClass)c);
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
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

package org.apache.xmlbeans.impl.jam_old.internal;


import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Type;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.xmlbeans.impl.jam_old.JAnnotation;
import org.apache.xmlbeans.impl.jam_old.JClass;
import org.apache.xmlbeans.impl.jam_old.JClassLoader;
import org.apache.xmlbeans.impl.jam_old.JComment;
import org.apache.xmlbeans.impl.jam_old.JConstructor;
import org.apache.xmlbeans.impl.jam_old.JField;
import org.apache.xmlbeans.impl.jam_old.JMethod;
import org.apache.xmlbeans.impl.jam_old.JElement;
import org.apache.xmlbeans.impl.jam_old.JPackage;
import org.apache.xmlbeans.impl.jam_old.JSourcePosition;


/**
 * JClass for array types.  These are synthesized at runtime by the
 * JAM framework
 *
 * @author Patrick Calahan <pcal@bea.com>
 */
public final class ArrayJClass extends BuiltinJClass {

  // ========================================================================
  // Variables

  private int mDimensions;
  private JClass mComponentType;

  // ========================================================================
  // Factory methods

  /**
   * Creates an array JClass from a field descriptor as described in the JLS.
   * This is the nasty '[[[Lfoo.bar.Baz;'-style notation.
   */
  public static JClass createClassForFD(String arrayFD, JClassLoader loader)
  {
    if (!arrayFD.startsWith("[")) {
      throw new IllegalArgumentException("must be an array type fd: "+arrayFD);
    }
    // if it's an array type, we have to be careful
    String componentType;
    if (arrayFD.endsWith(";")) {
      // if it's an array of complex types, we need to construct
      // an ArrayJClass wrapper and go back into the context to
      // get the component type, since a source description for it
      // might be available
      int dims = arrayFD.indexOf("L");
      if (dims != -1 && dims<arrayFD.length()-2) {
        componentType = arrayFD.substring(dims+1,arrayFD.length()-1);
        return new ArrayJClass(loader.loadClass(componentType),dims);
      } else {
        // name is effed
        throw new IllegalArgumentException("array type field descriptor '"+
                                           arrayFD+"' is malformed");
      }
    } else {
      int dims = arrayFD.lastIndexOf("[")+1;
      JClass primType = PrimitiveJClass.getPrimitiveClassForName
              (arrayFD.substring(dims,dims+1));
      if (primType == null) {
        // if it didn't end with ';', it has to be a valid primitive
        // type name or it's effed
        throw new IllegalArgumentException("array type field descriptor '"+
                                           arrayFD+"' is malformed");
      }
      return new ArrayJClass(primType,dims);
    }
  }

  // ========================================================================
  // Constructors

  /**
   * Constructs a JDClass for the given ClassDoc in the given context.
   */
  private ArrayJClass(JClass componentType, int dimensions)
  {
    if (dimensions < 1) {
      throw new IllegalArgumentException("dimensions="+dimensions);
    }
    if (componentType == null) {
      throw new IllegalArgumentException("null componentType");
    }
    mComponentType = componentType;
    mDimensions = dimensions; 
  }

  // ========================================================================
  // JElement implementation

  public JElement getParent() { return null; }

  public JElement[] getChildren() { return null; }

  public String getSimpleName() {
    String out = getQualifiedName();
    int lastDot = out.lastIndexOf('.');
    return (lastDot == -1) ? out : out.substring(lastDot+1);
  }

  public String getQualifiedName() {
    StringWriter out = new StringWriter();
    out.write(mComponentType.getQualifiedName());
    for(int i=0; i<mDimensions; i++) out.write("[]");
    return out.toString();
  }

  public JAnnotation[] getAnnotations() {
    return BaseJElement.NO_ANNOTATION;
  }

  public JAnnotation[] getAnnotations(String named) {
    return BaseJElement.NO_ANNOTATION;
  }

  public JAnnotation getAnnotation(String named) { return null; }

  public JComment[] getComments() { return BaseJElement.NO_COMMENT; }

  // ========================================================================
  // JMember implementation

  public int getModifiers() { return mComponentType.getModifiers(); }

  public boolean isPackagePrivate() {
    return mComponentType.isPackagePrivate(); 
  }

  public boolean isProtected() { return mComponentType.isProtected(); }

  public boolean isPublic() { return mComponentType.isPublic(); }

  public boolean isPrivate() { return mComponentType.isPrivate(); }

  public JSourcePosition getSourcePosition() { return null; }

  public JClass getContainingClass() { return null; }

  // ========================================================================
  // JClass implementation

  public JClassLoader getClassLoader() {
    return mComponentType.getClassLoader(); 
  }

  public JClass forName(String fd) {
    return mComponentType.forName(fd);
  }

  public boolean isArray() { return true; }

  public JClass getArrayComponentType() { return mComponentType; }

  public int getArrayDimensions() { return mDimensions; }

  public JClass getSuperclass() { return ObjectJClass.getInstance(); }

  public boolean isAssignableFrom(JClass c) {
    return c.isArray() &&
            (c.getArrayDimensions() == mDimensions) &&
            (mComponentType.isAssignableFrom(c.getArrayComponentType()));
  }

  public String getFieldDescriptor() {
    //REVIEW should we cache this result?
    StringWriter out = new StringWriter();
    for(int i=0; i<mDimensions; i++) out.write("[");
    if (mComponentType.isPrimitive()) {
      out.write(mComponentType.getFieldDescriptor());
    } else {
      out.write("L");
      out.write(mComponentType.getQualifiedName());
      out.write(";");
    }
    return out.toString();
  }
}

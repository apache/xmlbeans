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
package org.apache.xmlbeans.impl.jam;

/**
 * <p>Represents a Java class that may or may not be loaded in the VM.
 * JClass is typically implemented in one of two ways: by wrapping a
 * java.lang.Class or by parsing a source file directly with a tool
 * such as javadoc or javelin.</p>
 *
 * <p>If a JClass represents an inner class, its getParent() method
 * returns the outer class.  Otherwise, it returns the containing
 * package.</p>
 *
 * <p>REVIEW a bunch of these methods (getMethods, getConstructors...)
 * could throw SecurityException if the JClass is backed by
 * java.lang.Class (see javadocs for Class).  We're currently ignoring
 * this, because it seems unlikely and it seems heavyweight.  Seems
 * ok?</p>
 *
 * @author Patrick Calahan &lt;email: pcal-at-bea-dot-com&gt;
 */
public interface JClass extends JMember {

  /**
   * <p>Returns a JPackage which represents the package which contains
   * this JClass.</p>
   */
  public JPackage getContainingPackage();

  /**
   * <p>Returns the Class representing the superclass of the entity
   * (class, interface, primitive type or void) represented by this
   * Class. If this Class represents either the Object class, an
   * interface, a primitive type, or void, then null is returned. If
   * this object represents an array class then the Class object
   * representing the Object class is returned.
   */
  public JClass getSuperclass();

  /**
   * Returns all of the interfaces directly implemented by this class.
   * Returns an empty array if no interfaces are implemented.  REVIEW
   * we probably want getInterfaces and getDeclaredInterfaces.
   */
  public JClass[] getInterfaces();

  /**
   * <p>REVIEW should we consider renaming this to getAllFields()?  I
   * think this makes it more clear but is not consistent with
   * java.lang.Class.</p>
   *
   * <p>Returns an array containing Field objects reflecting all the
   * accessible public fields of the class or interface represented by
   * this Class object. The elements2 in the array returned are not
   * sorted and are not in any particular order. This method returns
   * an array of length 0 if the class or interface has no accessible
   * public fields, or if it represents an array class, a primitive
   * type, or void. </p>
   *
   * <p>Specifically, if this JClass object represents a class, this
   * method returns the public fields of this class and of all its
   * superclasses. If this JClass object represents an interface, this
   * method returns the fields of this interface and of all its
   * superinterfaces. </p>
   *
   * <p>The implicit length field for array class is not reflected by
   * this method. User code should use the methods of class Array to
   * manipulate arrays. </p>
   *
   * <p>See The Java Language Specification, sections 8.2 and
   * 8.3. </p>
   */
  public JField[] getFields();


  /**
   * <p>Returns an array of Field objects reflecting all the fields
   * declared by the class or interface represented by this Class
   * object. This includes public, protected, default (package)
   * access, and private fields, but excludes inherited fields. The
   * elements2 in the array returned are not sorted and are not in any
   * particular order. This method returns an array of length 0 if the
   * class or interface declares no fields, or if this Class object
   * represents a primitive type, an array class, or void. </p>
   *
   * <p>See The Java Language Specification, sections 8.2 and 8.3.</p>
   */
  public JField[] getDeclaredFields();

  /**
   * <p>REVIEW should we consider renaming this to getAllMethods()?  I
   * think this makes it more clear but is not consistent with
   * java.lang.Class.</p>
   *
   * <p>Returns an array containing Method objects reflecting all the
   * public member methods of the class or interface represented by
   * this Class object, including those declared by the class or
   * interface and and those inherited from superclasses and
   * superinterfaces. The elements2 in the array returned are not
   * sorted and are not in any particular order. This method returns
   * an array of length 0 if this Class object represents a class or
   * interface that has no public member methods, or if this Class
   * object represents an array class, primitive type, or void.</p>
   *
   * <p>The class initialization method <clinit> is not included in
   * the returned array. If the class declares multiple public member
   * methods with the same parameter types, they are all included in
   * the returned array. </p>
   */
  public JMethod[] getMethods();

  /**
   * <p>Returns an array of Method objects reflecting all the methods
   * declared by the class or interface represented by this Class
   * object. This includes public, protected, default (package)
   * access, and private methods, but excludes inherited methods. The
   * elements2 in the array returned are not sorted and are not in any
   * particular order. This method returns an array of length 0 if the
   * class or interface declares no methods, or if this Class object
   * represents a primitive type, an array class, or void. The class
   * initialization method <clinit> is not included in the returned
   * array. If the class declares multiple public member methods with
   * the same parameter types, they are all included in the returned
   * array. </p>
   *
   * <p>See The Java Language Specification, section 8.2. </p>
   */
  public JMethod[] getDeclaredMethods();

  /**
   * <p>Returns an array containing Constructor objects reflecting all
   * the public constructors of the class represented by this Class
   * object. An array of length 0 is returned if the class has no
   * public constructors, or if the class is an array class, or if the
   * class reflects a primitive type or void. </p>
   */
  public JConstructor[] getConstructors();

  // This is on java.lang.Class, but is it really useful?
  //
  //  public JConstructor[] getDeclaredConstructors();


  /**
   * Returns a representation of a java bean property as detailed in section
   * 8.3 of the Java Beans specification, 'Design Patterns for Properties.'
   * A JProperty can be thought of as a union of a getter method and
   * corresponding setter method, although only one of these is required
   * (read-only and write-only properties are returned).  Note that
   * public fields are never considered properties, as deetailed in
   * the specification.
   */
  public JProperty[] getProperties();

  /**
   * <p>Returns true if this JClass represents an interface.</p>
   */
  public boolean isInterface();

  /**
   * <p>Returns true if this JClass represents a JSR175 annotation type.</p>
   */
  public boolean isAnnotationType();

  /**
   * Return true if this JClass represents primitive type (int, long,
   * double, and so forth).  Remember that primitive wrapper classes
   * such as java.lang.Integer are NOT considered primitives.
   */
  public boolean isPrimitiveType();

  /**
   * Returns true if this class is a builtin type.  This is true if any
   * of the following returns true: <code>isPrimitive(), isArray(),
   * isVoid(),</code> or </code>isUnresolved.</code>.
   * @return
   */
  public boolean isBuiltinType();

  /**
   * <p>If this JClass represents a primitive (isPrimitiveType() returns true),
   * this method returns the java.lang.Class representing the class of
   * the primitive.  For example, JClass representing an integer returns
   * the equivalent of 'int.class' or 'java.lang.Integer.type.'
   *
   * @return The primitive class, or null if this is JClass is not primitive.
   */
  public Class getPrimitiveClass();

  /**
   * Return true if this class is final.
   */
  public boolean isFinal();

  /**
   * Return true if this class is static.  Note that top-level classes
   * are never static.
   */
  public boolean isStatic();

  /**
   * Return true if this class is abstract.
   */
  public boolean isAbstract();

  /**
   * <p>Returns true if this JClass represents the void type.</p>
   */
  public boolean isVoidType();

  /**
   * <p>Returns true if this JClass represents java.lang.Object.</p>
   */
  public boolean isObjectType();

  /**
   * <p>Returns true if this JClass represents an array type.</p>
   */
  public boolean isArrayType();

  /**
   * <p>Returns the Class representing the component type of an array.
   * If this class does not represent an array class this method
   * returns null.</p>
   *
   * <p>Note that this method differs substantially from
   * <code>java.lang.Class.getComponentType()</code> in the way it
   * treats multidimensional arrays.  Specifically, let
   * <code>fooArrayClass</code> be the class of an n dimensional array
   * of class <code>foo</code> for n > 2.  For the java.lang.Class
   * representation of <code>fooArrayClass</code>,
   * <code>getComponentType()</code> will return a java.lang.Class for
   * an (n-1)-dimensional array of <code>foo</code>s.  By contrast,
   * the JClass representation of <code>fooArrayClass</code> will
   * always simply return a JClass representing <code>foo</code> for
   * any value of n > 1.</p>
   *
   * <p>In other words, this method always returns the 'core' type of
   * the array, effectively hiding away all of the intermediary array
   * types.  Given that JClass provides the additional
   * <code>getArrayDimensions</code> facility, it is felt that this is
   * a much easier convention for tool authors to work with.</p>
   */
  public JClass getArrayComponentType();

  /**
   * <p>If this JClass represents an array type (isArray() == true),
   * returns the number of dimensions in the array.  Otherwise returns
   * zero.</p>
   */
  public int getArrayDimensions();

  /**
   * <p>Determines if the class or interface represented by this Class
   * object is either the same as, or is a superclass or
   * superinterface of, the class or interface represented by the
   * specified Class parameter. It returns true if so; otherwise it
   * returns false. If this Class object represents a primitive type,
   * this method returns true if the specified Class parameter is
   * exactly this Class object; otherwise it returns false.</p>
   *
   * <p>Specifically, this method tests whether the type represented
   * by the specified Class parameter can be converted to the type
   * represented by this Class object via an identity conversion or
   * via a widening reference conversion. See The Java Language
   * Specification, sections 5.1.1 and 5.1.4 , for details.</p>
   */
  public boolean isAssignableFrom(JClass clazz);

  /**
   * Two JClasses are always considered equal as long as their
   * qualified names are the same.
   */
  public boolean equals(Object o);

  /**
   * <p>Returns the inner classes for this class.  The array contains
   * JClass objects representing all the public classes and interfaces
   * that are members of the class represented by this JClass.  This
   * includes public class and interface members inherited from
   * superclasses and public class and interface members declared by
   * the class. This method returns an array of length 0 if this Class
   * object has no public member classes or interfaces. This method
   * also returns an array of length 0 if this JClass object
   * represents a primitive type, an array class, or void. </p>
   */
  public JClass[] getClasses();

  /**
   * <p>If this JClass is an inner class, returns the outer class.  If
   * the class or interface represented by this JClass object is a
   * member of another class, returns the JClass object representing
   * the class in which it was declared. This method returns null if
   * this class or interface is not a member of any other class. If
   * this JClass object represents an array class, a primitive type,
   * or void, then this method returns null.</p>
   */
  public JClass getContainingClass();

  /**
   * <p>Returns the name of this member in the format described in
   * section 4.3.2 of the VM spec, 'Field Descriptors.'  This is the
   * same nasty format returned by java.lang.Class.getName(), and is
   * the format you need to use in calls to Class.forName().  For
   * example, the ClassfileName of the class of a two-dimensional
   * array of strings is <code>[[Ljava.lang.String;</code>.  For
   * details, see
   * http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html</p>
   */
  public String getFieldDescriptor();


  /**
   * <p>Returns the JamClassLoader which loaded this class.</p>
   */
  public JamClassLoader getClassLoader();

  /**
   * Shorthand for myClass.getClassLoader().loadClass(name)
   */
  public JClass forName(String name);

  /**
   * Returns the minmal set of JPackages which contain all of the clases
   * imported by this class.  This includes packages imported via the '*'
   * import notation as well as the packages which contain explicitly
   * imported classes.
   *
   * Note that this is an optional operation; if the source for the
   * class is not available (i.e. this JClass is backed by a
   * java.lang.Class), then this method will return an array of length
   * 0.
   */
  public JPackage[] getImportedPackages();

  /**
   * Returns a list of classes that were explicitly imported by this
   * class.  Note that this is an optional operation; if the source
   * for the class is not available (i.e. this JClass is backed by a
   * java.lang.Class), then this method will return an array of length
   * 0.
   */
  public JClass[] getImportedClasses();

  /**
   * Returns true if a backing entity for this class could not be
   * resolved.  This will be true, for example, of the JClass which
   * you get when requesting a method's return type when no class for
   * that type can be found.  In this case, the JClass will be have
   * only a name - all other properties will be null/empty.
   */
  public boolean isUnresolvedType();



}
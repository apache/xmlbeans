/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2000-2003 The Apache Software Foundation.  All rights 
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
* originally based on software copyright (c) 2000-2003 BEA Systems 
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package org.apache.xmlbeans;

import java.io.File;

/**
 * A finite set of XML Schema component definitions.
 * <p>
 * Every {@link SchemaComponent} such as a {@link SchemaType},
 * {@link SchemaGlobalElement}, {@link SchemaGlobalAttribute},
 * {@link SchemaModelGroup}, {@link SchemaAttributeGroup}, or
 * {@link SchemaIdentityConstraint}, is defined in exactly one
 * SchemaTypeSystem.  (See {@link SchemaComponent#getTypeSystem()}.)
 * A single SchemaTypeSystem can include definitions
 * from any number of namespaces; one SchemaTypeSystem consists simply
 * of a set of component definitions that were compiled together.
 * <p>
 * Since every component is defined in a single SchemaTypeSystem, no
 * SchemaTypeSystem other than {@link XmlBeans#getBuiltinTypeSystem()}
 * includes any of the the built-in types.  That means
 * you cannot ordinarily load instances using a single
 * SchemaTypeSystem by itself. Instead, you will want to combine a path of
 * SchemaTypeSystems together using {@link XmlBeans#typeLoaderUnion}
 * to form a SchemaTypeLoader that can be used for loading instances.
 * <p>
 * For example, the following code compiles the schema in myXSDFile
 * in the presence of only the minimal builtin type system.
 * The resulting SchemaTypeSystem <code>sts</code> contains only the definitions
 * from myXSD file.  In order to load and validate an instance within
 * the context of those types, we must next construct a
 * {@link SchemaTypeLoader} <code>stl</code> that contains both
 * the builtin type system and the types defined within the myXSD file.
 * <pre>
 * SchemaTypeSystem sts = XmlBeans.compileXsd(new XmlObject[]
 *    { XmlObject.Factory.parse(myXSDFile) },
 *    XmlBeans.getBuiltinTypeSystem(),
 *    null);
 * SchemaTypeLoader stl = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[]
 *    { sts, XmlBeans.getBuiltinTypeSystem() });
 * XmlObject mydoc = stl.parse(instanceFile, null, null);
 * System.out.println("Document valid: " + mydoc.validate());
 * </pre>
 * <p>
 * As you can see, for working with instances, you typically want to
 * work with a SchemaTypeLoader constructed from a path rather than
 * a solitary SchemaTypeSystem.  See {@link XmlBeans#loadXsd} for
 * a convenient alternative to {@link XmlBeans#compileXsd}.
 * <p>
 * A SchemaTypeSystem is useful when you need to enumerate the exact set
 * of component definitions derived from a set of XSD files, for example,
 * when you are analyzing the contents of the XSD files themselves.
 * Here is how to use a SchemaTypeSystem to inspect a set of schema
 * definitions:
 * <ol>
 * <li>First, use {@link XmlBeans#compileXsd} to compile any number
 *     of schema files.  If the schema files are valid, result will
 *     be a SchemaTypeSystem that contains all the component definitions
 *     from those files.  It will contain no other component definitions.
 * <li>Alternatively, call {@link SchemaComponent#getTypeSystem} on
 *     a precompiled schema component to discover the SchemaTypeSystem
 *     within which that component was originally compiled.
 * <li>Once you have a SchemaTypeSystem, call:
 *   <ul>
 *   <li> {@link #globalTypes()} for all the global type definitions.
 *   <li> {@link #globalElements()} for all the global element definitions.
 *   <li> {@link #globalAttributes()} for all the global attribute definitions.
 *   <li> {@link #modelGroups()} for all the named model group definitions.
 *   <li> {@link #attributeGroups()} for all the attribute group definitions.
 *   </ul>
 * <li>In addition, there are special types generated for XML Beans thare
 *     are not formally part of the Schema specification:
 *   <ul>
 *   <li> {@link #documentTypes()} returns all the document types.
 *   <li> {@link #attributeTypes()} returns all the attribute types.
 *   </ul>
 * </ol>
 * 
 * <p>
 * A document type is a type that contains a single global element; there
 * is one document type for each global element definition in a
 * SchemaTypeSystem.  In an instance document, only the root XmlObject
 * can have a document type as its type.
 * <p>
 * Similarly, an attribute type is a type that contains a single global
 * attribute, and there is one attribute type for each global attribute
 * definition in a SchemaTypeSystem.  It is possible to have a root
 * XmlObject representing a fragment whose type is an attribute type,
 * but attribute types are present mainly for symmetry and to simplify
 * code such as the type-tree-walking code below.
 * <p>
 * The global component methods above only provide a view of the top-level
 * components of a SchemaTypeSystem and do not include any nested
 * definitions.  To view all the nested definitions, you will want to
 * traverse the entire tree of {@link SchemaType} defintions within a
 * SchemaTypeSystem by examining the {@link SchemaType#getAnonymousTypes()}
 * within each {@link SchemaType} recursively.
 * <p>The following code is a standard treewalk that visits every
 * {@link SchemaType} in the SchemaTypeSystem once, including nested
 * definitions.
 * <pre>
 * List allSeenTypes = new ArrayList();
 * allSeenTypes.addAll(Arrays.asList(typeSystem.documentTypes()));
 * allSeenTypes.addAll(Arrays.asList(typeSystem.attributeTypes()));
 * allSeenTypes.addAll(Arrays.asList(typeSystem.globalTypes()));
 * for (int i = 0; i < allSeenTypes.size(); i++)
 * {
 *     SchemaType sType = (SchemaType)allSeenTypes.get(i);
 *     System.out.prinlnt("Visiting " + sType.toString());
 *     allSeenTypes.addAll(Arrays.asList(sType.getAnonymousTypes()));
 * }
 * </pre>
 *
 * @see SchemaType 
 * @see SchemaTypeLoader 
 * @see XmlBeans#compileXsd
 * @see XmlBeans#typeLoaderUnion
 * @see XmlBeans#getBuiltinTypeSystem
 */ 
public interface SchemaTypeSystem extends SchemaTypeLoader
{
    /**
     * Returns the name of this loader.
     */
    public String getName();

    /**
     * Returns the global types defined in this loader.
     */
    public org.apache.xmlbeans.SchemaType[] globalTypes();

    /**
     * Returns the document types defined in this loader.
     */
    public org.apache.xmlbeans.SchemaType[] documentTypes();

    /**
     * Returns the attribute types defined in this loader.
     */
    public org.apache.xmlbeans.SchemaType[] attributeTypes();

    /**
     * Returns the global elements defined in this loader.
     */
    public SchemaGlobalElement[] globalElements();

    /**
     * Returns the global attributes defined in this loader.
     */
    public SchemaGlobalAttribute[] globalAttributes();

    /**
     * Returns the model groups defined in this loader.
     */
    public SchemaModelGroup[] modelGroups();

    /**
     * Returns the attribute groups defined in this loader.
     */
    public SchemaAttributeGroup[] attributeGroups();

    /**
     * Initializes a type system (resolves all handles within the type system).
     */
    public void resolve();

    /**
     * Locates a type, element, or attribute using the handle.
     */
    public SchemaComponent resolveHandle(String handle);

    /**
     * Locates a type, element, or attribute using the handle.
     */
    public SchemaType typeForHandle(String handle);

    /**
     * Returns the classloader used by this loader for resolving types.
     */
    public ClassLoader getClassLoader();

    /**
     * Saves this type to a directory.
     */
    public void saveToDirectory(File classDir);
}

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

package org.apache.xmlbeans.impl.binding.bts;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.impl.common.XMLChar;
import org.apache.xmlbeans.soap.SOAPArrayType;

import javax.xml.namespace.QName;
import java.io.Serializable;

/**
 * An XmlTypeName is a way of uniquely identifying any
 * logical component in a schema.
 *
 * For a diagram of the kinds of components that can be referenced,
 * see http://www.w3.org/TR/xmlschema-1/components.gif
 *
 * A signature looks like this:
 *
 * a-name1|t|e=name2|p.3|s|p.5|c|p.0|t|e=name3@my-namespace
 *
 * This reads as:g
 * The attribute declaration called "name1" (unqualified) inside
 * the anonymous type of
 * the element declaration called "name2" (qualified) which is
 * the third particle of
 * the sequence which is
 * the fifth particle of
 * the choice which is
 * the zeroth particle of
 * the anonymous type of
 * the element named name3 (qualified) in
 * the namespace my-namespace.
 *
 * Hyphens (-) introduce unqualified names
 * Equals (=) introduce qualified names
 * Dot (.) introduce numbers
 * Anonymous components are just single-letters.
 *
 * There are these types of components
 *
 * n = Notations (named, global only)
 * e = Element decls (named, global or inside t or p)
 * Identity constraint defs
 *   k = key/unique/keyref (named, in elts only)
 * g = Model group defs (named, global only)
 * Model group compositors:
 *   l = All compositors (anon, inside p or g)
 *   s = Sequence compositors (anon, inside p or g)
 *   c = Choice compositors (anon, inside p or g)
 * p = Particles (numbered, inside t or l or s or c)
 * w = Wildcards (anon, inside p only)
 * v = Attribute Uses (named, in type only, inside t)
 * a = Attribute Declarations (named, global or inside t or v)
 * r = Attribute group definitions (named, global only)
 * t = Type definitions (name when global, or anon inside i, m, e, a, t)
 *
 * Not part of the spec, but we treat as components:
 * d = document type definition (named, global only)
 * b = attribute-container type definition (named, global only)
 * m = union member definition (numbered, inside t only)
 * y = soap array of dimension n (numbered, referencing global t or another y only)
 *
 * A canonical signature will shortcut the following:
 *
 * - global elements do not need to be explicitly nested inside their
 *   containing document types (e=name|d=name@namespace -> e=name@namespace)
 * - global attributes do not need to be explicitly nested inside their
 *   containing attribute types (e=name|d=name@namespace -> e=name@namespace)
 * - an element of a type, if its name is unique, may be referenced without the
 *   intervening particle path.  (e.g., e=name2|p.3|s|p.5|c|p.0|t can be
 *   reduced to e=name2|t)
 * - an attribute of a type may be referenced without explicitly putting it
 *   inside an attribute use (e.g., a-name1|v-name1|t -> a-name1|t)
 *
 * Notice SOAP arrays are included in the naming schema, as follows:
 *
 * A soap array type written like this:
 *     x:drg[,,,][,][,,]
 *
 * Has the following signature:
 *     y.3|y.2|y.4|t=drg@foobar
 */
public class XmlTypeName
    implements Serializable
{

  // ========================================================================
  // Constants

  public static final char NOTATION = 'n';
  public static final char ELEMENT = 'e';
  public static final char ID_CONSTRAINT = 'k';
  public static final char MODEL_GROUP = 'g';
  public static final char ALL = 'l';
  public static final char SEQUENCE = 's';
  public static final char CHOICE = 'c';
  public static final char PARTICLE = 'p';
  public static final char WILDCARD = 'w';
  public static final char ATTRIBUTE_USE = 'v';
  public static final char ATTRIBUTE = 'a';
  public static final char ATTRIBUTE_GROUP = 'r';
  public static final char TYPE = 't';
  public static final char DOCUMENT_TYPE = 'd';
  public static final char ATTRIBUTE_TYPE = 'b';
  public static final char MEMBER = 'm';
  public static final char SOAP_ARRAY = 'y';
  public static final char NO_TYPE = 'z';

  private static final long serialVersionUID = 1L;



  // ========================================================================
  // Variables

  private String namespace;
  private String path;

  // ========================================================================
  // Factories

  /**
   * Creates an XMLName based on the given String signature.
   *
   * This signature is described in the javadoc for this class.
   */
  public static XmlTypeName forString(String signature) {
    if (signature == null) {
      throw new IllegalArgumentException("null signature");
    }
    String path;
    String namespace;
    int atSign = signature.indexOf('@');
    if (atSign < 0) {
      namespace = "";
      path = signature;
    } else {
      namespace = signature.substring(atSign + 1);
      path = signature.substring(0, atSign);
    }
    return forPathAndNamespace(path, namespace);
  }

  /**
   * Creates an XMLName for a schema type with the given fully-qualified QName.
   */
  public static XmlTypeName forTypeNamed(QName name) {
    if (name == null) throw new IllegalArgumentException("null qname");
    return forPathAndNamespace(TYPE + "=" + name.getLocalPart(), name.getNamespaceURI());
  }

  /**
   * Creates an XMLName for a global schema element with the given fully-qualified QName.
   */
  public static XmlTypeName forGlobalName(char kind, QName name) {
    if (name == null) throw new IllegalArgumentException("null qname");
    return forPathAndNamespace(kind + "=" + name.getLocalPart(), name.getNamespaceURI());
  }

  /**
   * Creates an XMLName for a nested component
   */
  public static XmlTypeName forNestedName(char kind, String localName, boolean qualified, XmlTypeName outer) {
    if (localName == null) throw new IllegalArgumentException("null localName");
    if (outer == null) throw new IllegalArgumentException("null outer");
    return forPathAndNamespace(kind + (qualified ? "=" : "-") + localName + "|" + outer.path, outer.namespace);
  }

  /**
   * Creates an XMLName for a nested component
   */
  public static XmlTypeName forNestedNumber(char kind, int n, XmlTypeName outer) {
    if (outer == null) throw new IllegalArgumentException("null outer");
    return forPathAndNamespace(kind + "." + n + "|" + outer.path, outer.namespace);
  }

  /**
   * Creates an XMLName for a nested component
   */
  public static XmlTypeName forNestedAnonymous(char kind, XmlTypeName outer) {
    if (outer == null) throw new IllegalArgumentException("null outer");
    return forPathAndNamespace(kind + "|" + outer.path, outer.namespace);
  }

  /**
   * Creates an XMLName for a particular schema type
   */
  public static XmlTypeName forSchemaType(SchemaType sType) {
    if (sType == null) throw new IllegalArgumentException("null sType");
    if (sType.getName() != null)
      return forTypeNamed(sType.getName());

    if (sType.isDocumentType())
      return forGlobalName(DOCUMENT_TYPE, sType.getDocumentElementName());

    if (sType.isAttributeType())
      return forGlobalName(ATTRIBUTE_TYPE, sType.getAttributeTypeAttributeName());

    if (sType.isNoType() || sType.getOuterType() == null) // latter is an error
      return forPathAndNamespace("" + NO_TYPE, "");

    SchemaType outerType = sType.getOuterType();
    XmlTypeName outerName = forSchemaType(outerType);

    if (sType.getContainerField() != null) {
      boolean qualified = sType.getContainerField().getName().getNamespaceURI().length() > 0;
      String localName = sType.getContainerField().getName().getLocalPart();
      char kind = (sType.getContainerField().isAttribute() ? ATTRIBUTE : ELEMENT);
      return forNestedAnonymous(TYPE, forNestedName(kind, localName, qualified, outerName));
    }

    if (outerType.getSimpleVariety() == SchemaType.UNION)
      return forNestedAnonymous(TYPE, forNestedNumber(MEMBER, sType.getAnonymousUnionMemberOrdinal(), outerName));

    return forNestedAnonymous(TYPE, outerName);
  }

  /**
   * Creates one for a SOAPArrayType
   */
  public static XmlTypeName forSoapArrayType(SOAPArrayType sType) {
    if (sType == null) throw new IllegalArgumentException("null sType");
    StringBuffer sb = new StringBuffer();
    sb.append(SOAP_ARRAY + "." + sType.getDimensions().length);
    int[] ranks = sType.getRanks();
    for (int i = ranks.length - 1; i >= 0; i -= 1) {
      sb.append("|" + SOAP_ARRAY + "." + ranks[i]);
    }
    QName name = sType.getQName();
    sb.append("|" + TYPE + "=" + name.getLocalPart());
    return forPathAndNamespace(sb.toString(), name.getNamespaceURI());
  }

  // ========================================================================
  // Public methods

  /**
   * This function is used to see if a path is valid or not.
   */
  public boolean valid() {
    XmlTypeName outerComponent = null;
    int outerType = 0;
    String localName = internalGetStringName();

    boolean hasNumber = internalGetNumber() >= 0;
    boolean hasName = (localName != null);
    boolean isAnonymous = internalIsAnonymous();
    boolean isQualified = internalIsQualified();
    boolean isGlobal = isGlobal();

    if (!isGlobal) {
      outerComponent = getOuterComponent();
      outerType = outerComponent.getComponentType();
    }

    boolean result;

    if (localName != null && !XMLChar.isValidNCName(localName))
      return false;

    switch (getComponentType()) {
      case NOTATION:
        result = (isGlobal && hasName && isQualified);
        break;

      case ELEMENT:
        result = (hasName && (isGlobal && isQualified || outerType == TYPE || outerType == PARTICLE));
        break;

      case ID_CONSTRAINT:
        result = (hasName && outerType == ELEMENT);
        break;

      case MODEL_GROUP:
        result = (hasName && isGlobal);
        break;

      case ALL:
      case SEQUENCE:
      case CHOICE:
        result = (isAnonymous && (outerType == PARTICLE || outerType == MODEL_GROUP));
        break;

      case PARTICLE:
        result = (hasNumber && (outerType == SEQUENCE || outerType == CHOICE || outerType == ALL || outerType == TYPE));
        break;

      case WILDCARD:
        result = (isAnonymous && (outerType == PARTICLE || outerType == TYPE || outerType == ATTRIBUTE_GROUP));
        break;

      case ATTRIBUTE_USE:
        result = (hasName && (outerType == TYPE || outerType == ATTRIBUTE_GROUP));
        break;

      case ATTRIBUTE:
        result = (hasName && (isGlobal && isQualified || outerType == TYPE || outerType == ATTRIBUTE_USE));
        break;

      case ATTRIBUTE_GROUP:
        result = (hasName && isQualified && isGlobal);
        break;

      case TYPE:
        result = ((hasName && isQualified && isGlobal) || (isAnonymous && outerType == TYPE || outerType == ELEMENT || outerType == ATTRIBUTE || outerType == MEMBER));
        break;

      case DOCUMENT_TYPE:
        result = (hasName && isQualified && isGlobal);
        break;

      case ATTRIBUTE_TYPE:
        result = (hasName && isQualified && isGlobal);
        break;

      case MEMBER:
        result = (isAnonymous && outerType == TYPE);
        break;

      case SOAP_ARRAY:
        result = (hasNumber && (outerType == SOAP_ARRAY || outerType == TYPE && outerComponent.isGlobal()));
        break;

      case NO_TYPE:
        result = (isAnonymous && isGlobal && namespace.length() == 0);
        break;

      default:
        result = false;
    }
    if (!result)
      return false;

    if (isGlobal)
      return true;

    return outerComponent.valid();
  }


  /**
   * True if it is a schema type
   */
  public boolean isSchemaType() {
    switch (getComponentType()) {
      case TYPE:
      case DOCUMENT_TYPE:
      case ATTRIBUTE_TYPE:
      case NO_TYPE:
        return true;
      default:
        return false;
    }
  }

  /**
   * Finds a type with the given name.
   */
  public SchemaType findTypeIn(SchemaTypeLoader loader) {
    switch (getComponentType()) {
      case NO_TYPE:
        return XmlBeans.NO_TYPE;
      case DOCUMENT_TYPE:
        return loader.findDocumentType(getQName());
      case ATTRIBUTE_TYPE:
        return loader.findAttributeType(getQName());
      default:
        return null;
      case TYPE:
        break;
    }

    if (isGlobal())
      return loader.findType(getQName());

    XmlTypeName outerName = getOuterComponent();

    // if the component is contained within a type, get it
    for (XmlTypeName outerTypeName = outerName; ; outerTypeName = outerTypeName.getOuterComponent()) {
      if (outerTypeName.isSchemaType()) {
        SchemaType outerType = outerTypeName.findTypeIn(loader);
        switch (outerName.getComponentType()) {
          default:
            throw new IllegalStateException("Illegal type name " + this);

          case TYPE:
            return outerType.getAnonymousTypes()[0];

          case ELEMENT:
            return outerType.getElementType(outerName.getQName(), null, loader);

          case ATTRIBUTE:
            return outerType.getAttributeType(outerName.getQName(), loader);

          case MEMBER:
            return outerType.getAnonymousTypes()[outerName.getNumber()];
        }
      }
      if (outerTypeName.isGlobal()) {
        switch (outerName.getComponentType()) {
          default:
            throw new IllegalStateException("Illegal type name " + this);

          case ELEMENT:
            return loader.findDocumentType(outerTypeName.getQName()).getElementType(outerTypeName.getQName(), null, loader);

          case ATTRIBUTE:
            return loader.findAttributeType(outerTypeName.getQName()).getAttributeType(outerTypeName.getQName(), loader);
        }
      }
    }
  }

  public boolean isGlobal() {
    int index = path.indexOf('|');
    return index < 0;
  }

  public XmlTypeName getOuterComponent() {
    int index = path.indexOf('|');
    if (index < 0)
      return null;
    return forPathAndNamespace(path.substring(index + 1), namespace);
  }

  public int getComponentType() {
    if (path.length() > 0)
      return path.charAt(0);
    return 0; // unknown type
  }

  /**
   * Returns the number locating this component within its parent,
   * or throws an exception if there is none.  Only union members (m)
   * and particles (p) have numbers.
   */
  public int getNumber() {
    int result = internalGetNumber();
    if (result < 0)
      throw new IllegalStateException("Path has no number");
    return result;
  }


  /**
   * Returns the name locating this component within its parent, or
   * returns null if there is none.  Notice that if looking up elements
   * by name in a type, you get the first element defintion when there
   * are multiple ones.  A full particle path can disambiguate.
   *
   * This name will be qualified in a namespace if appropriate, but,
   * for example, local unqualified attributes will have a QName that
   * is unqualified.
   *
   * Element, attributes, identity constraints, model/attribute groups,
   * and notations have names.
   */
  public QName getQName() {
    String localName = internalGetStringName();
    if (localName == null)
      return null;

    if (internalIsQualified())
      return new QName(namespace, localName);
    else
      return new QName(localName);
  }


  // ========================================================================
  // Private methods

  private static XmlTypeName forPathAndNamespace(String path, String namespace) {
    return new XmlTypeName(path, namespace);
  }

  private XmlTypeName(String path, String namespace) {
    if (path == null || namespace == null)
      throw new IllegalArgumentException();

    this.path = path;
    this.namespace = namespace;
  }


  /**
   * Returns negative if there is no number
   */
  private int internalGetNumber() {
    if (path.length() <= 1 || path.charAt(1) != '.')
      return -1;

    int index = path.indexOf('|');
    if (index < 0)
      index = path.length();

    try {
      return Integer.parseInt(path.substring(2, index));
    } catch (Exception e) {
      return -1;
    }
  }

  private String internalGetStringName() {
    if (path.length() <= 1 || path.charAt(1) != '=' && path.charAt(1) != '-')
      return null;

    int index = path.indexOf('|');
    if (index < 0)
      index = path.length();

    return path.substring(2, index);
  }

  private boolean internalIsQualified() {
    return (path.length() > 1 && path.charAt(1) == '=');
  }

  private boolean internalIsAnonymous() {
    return (path.length() <= 1 || path.charAt(1) == '|');
  }

  // ========================================================================
  // Object implementation

  /**
   * Returns the signature string.
   */
  public String toString() {
    if (namespace.length() == 0)
      return path;

    return path + '@' + namespace;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof XmlTypeName)) return false;

    final XmlTypeName xmlName = (XmlTypeName) o;

    if (!namespace.equals(xmlName.namespace)) return false;
    if (!path.equals(xmlName.path)) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = namespace.hashCode();
    result = 29 * result + path.hashCode();
    return result;
  }
}

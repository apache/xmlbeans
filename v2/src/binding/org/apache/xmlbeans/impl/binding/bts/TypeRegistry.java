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

import org.apache.xml.xmlbeans.bindingConfig.JavaMethodName;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.impl.common.XmlWhitespace;

import java.util.Iterator;

final class TypeRegistry
{
  private TypeRegistry()
  {
  }

  static BindingType loadFromBindingTypeNode(org.apache.xml.xmlbeans.bindingConfig.BindingType node)
  {
    final BindingType retval;

    //this is pretty gross, but we can't add visitors to generated classes
    //and note that the whole point is to keep the actual types as minimal as possible
    //in terms of what gets dragged in.
    if (node instanceof org.apache.xml.xmlbeans.bindingConfig.ByNameBean) {
      org.apache.xml.xmlbeans.bindingConfig.ByNameBean bn = (org.apache.xml.xmlbeans.bindingConfig.ByNameBean) node;
      ByNameBean type = new ByNameBean();
      fillTypeFromNode(type, bn);
      retval = type;
    } else if (node instanceof org.apache.xml.xmlbeans.bindingConfig.SimpleContentBean) {
      org.apache.xml.xmlbeans.bindingConfig.SimpleContentBean bn = (org.apache.xml.xmlbeans.bindingConfig.SimpleContentBean) node;
      SimpleContentBean type = new SimpleContentBean();
      fillTypeFromNode(type, bn);
      retval = type;
    } else if (node instanceof org.apache.xml.xmlbeans.bindingConfig.SimpleType) {
      org.apache.xml.xmlbeans.bindingConfig.SimpleType bn = (org.apache.xml.xmlbeans.bindingConfig.SimpleType) node;
      SimpleBindingType type = new SimpleBindingType();
      fillTypeFromNode(type, bn);
      retval = type;
    } else if (node instanceof org.apache.xml.xmlbeans.bindingConfig.WrappedArray) {
      org.apache.xml.xmlbeans.bindingConfig.WrappedArray wnode = (org.apache.xml.xmlbeans.bindingConfig.WrappedArray) node;
      WrappedArrayType type = new WrappedArrayType();
      fillTypeFromNode(type, wnode);
      retval = type;
    } else if (node instanceof org.apache.xml.xmlbeans.bindingConfig.ListArray) {
      org.apache.xml.xmlbeans.bindingConfig.ListArray wnode = (org.apache.xml.xmlbeans.bindingConfig.ListArray) node;
      ListArrayType type = new ListArrayType();
      fillTypeFromNode(type, wnode);
      retval = type;
    } else if (node instanceof org.apache.xml.xmlbeans.bindingConfig.JaxrpcEnumType) {
      org.apache.xml.xmlbeans.bindingConfig.JaxrpcEnumType wnode = (org.apache.xml.xmlbeans.bindingConfig.JaxrpcEnumType) node;
      JaxrpcEnumType type = new JaxrpcEnumType();
      fillTypeFromNode(type, wnode);
      retval = type;
    } else if (node instanceof org.apache.xml.xmlbeans.bindingConfig.SoapArray) {
      org.apache.xml.xmlbeans.bindingConfig.SoapArray wnode = (org.apache.xml.xmlbeans.bindingConfig.SoapArray) node;
      SoapArrayType type = new SoapArrayType();
      fillTypeFromNode(type, wnode);
      retval = type;
    } else if (node instanceof org.apache.xml.xmlbeans.bindingConfig.SimpleDocumentBinding) {
      org.apache.xml.xmlbeans.bindingConfig.SimpleDocumentBinding wnode = (org.apache.xml.xmlbeans.bindingConfig.SimpleDocumentBinding) node;
      SimpleDocumentBinding type = new SimpleDocumentBinding();
      fillTypeFromNode(type, wnode);
      retval = type;
    } else {
      throw new AssertionError("unknown type: " + node.schemaType());
    }

    assert retval != null;

    return retval;
  }

  static BindingTypeName getNameFromNode(org.apache.xml.xmlbeans.bindingConfig.BindingType node)
  {
    return BindingTypeName.forPair(
      JavaTypeName.forString(node.getJavatype()),
      XmlTypeName.forString(node.getXmlcomponent())
    );
  }

  static org.apache.xml.xmlbeans.bindingConfig.BindingType writeBindingType(BindingType type,
                                                                            org.apache.xml.xmlbeans.bindingConfig.BindingType node)
  {
    try {
      type.accept(new TypeWriter(node));
      return node;
    } catch (XmlException e) {
      throw new XmlRuntimeException(e);
    }
  }

  private static org.apache.xml.xmlbeans.bindingConfig.BindingType writeTypeNameInfo(org.apache.xml.xmlbeans.bindingConfig.BindingType node, BindingType type)
  {
    node.setJavatype(type.getName().getJavaName().toString());
    node.setXmlcomponent(type.getName().getXmlName().toString());
    return node;
  }


  private static void writeByNameBean(org.apache.xml.xmlbeans.bindingConfig.BindingType node,
                                      ByNameBean type)
  {
    node = (org.apache.xml.xmlbeans.bindingConfig.BindingType) node.changeType(org.apache.xml.xmlbeans.bindingConfig.ByNameBean.type);
    org.apache.xml.xmlbeans.bindingConfig.ByNameBean bnNode =
      (org.apache.xml.xmlbeans.bindingConfig.ByNameBean) writeTypeNameInfo(node, type);

    final GenericXmlProperty aprop = type.getAnyElementProperty();
    if (aprop != null) {
      PropertyRegistry.writeGenericXmlProperty(aprop, bnNode.addNewAnyProperty());
    }

    final GenericXmlProperty anyprop = type.getAnyAttributeProperty();
    if (anyprop != null) {
      PropertyRegistry.writeGenericXmlProperty(anyprop, bnNode.addNewAnyAttributeProperty());
    }

    for (Iterator i = type.getProperties().iterator(); i.hasNext();) {
      QNameProperty qProp = (QNameProperty) i.next();
      org.apache.xml.xmlbeans.bindingConfig.QnameProperty qpNode = bnNode.addNewQnameProperty();
      PropertyRegistry.writeQNameProperty(qProp, qpNode);
    }
  }

  private static void writeSimpleDocumentBinding(org.apache.xml.xmlbeans.bindingConfig.BindingType node,
                                                 SimpleDocumentBinding type)
  {
    node = (org.apache.xml.xmlbeans.bindingConfig.BindingType) node.changeType(org.apache.xml.xmlbeans.bindingConfig.SimpleDocumentBinding.type);
    org.apache.xml.xmlbeans.bindingConfig.SimpleDocumentBinding sdbNode =
      (org.apache.xml.xmlbeans.bindingConfig.SimpleDocumentBinding) writeTypeNameInfo(node, type);
    sdbNode.setTypeOfElement(type.getTypeOfElement().toString());
  }

  private static void writeSimpleBindingType(org.apache.xml.xmlbeans.bindingConfig.BindingType node,
                                             SimpleBindingType type)
  {
    node = (org.apache.xml.xmlbeans.bindingConfig.BindingType) node.changeType(org.apache.xml.xmlbeans.bindingConfig.SimpleType.type);
    org.apache.xml.xmlbeans.bindingConfig.SimpleType stNode =
      (org.apache.xml.xmlbeans.bindingConfig.SimpleType) writeTypeNameInfo(node, type);

    org.apache.xml.xmlbeans.bindingConfig.AsXmlType as_if = stNode.addNewAsXml();
    as_if.setStringValue(type.getAsIfXmlType().toString());

    switch (type.getWhitespace()) {
      case XmlWhitespace.WS_UNSPECIFIED:
        break;
      case XmlWhitespace.WS_PRESERVE:
        as_if.setWhitespace(org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.PRESERVE);
        break;
      case XmlWhitespace.WS_REPLACE:
        as_if.setWhitespace(org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.REPLACE);
        break;
      case XmlWhitespace.WS_COLLAPSE:
        as_if.setWhitespace(org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.COLLAPSE);
        break;
      default:
        throw new AssertionError("invalid whitespace: " + type.getWhitespace());
    }


    stNode.setAsXml(as_if);
  }

  private static void writeWrappedArrayType(org.apache.xml.xmlbeans.bindingConfig.BindingType node, WrappedArrayType type)
  {
    node = (org.apache.xml.xmlbeans.bindingConfig.BindingType) node.changeType(org.apache.xml.xmlbeans.bindingConfig.WrappedArray.type);
    final org.apache.xml.xmlbeans.bindingConfig.WrappedArray wa =
      (org.apache.xml.xmlbeans.bindingConfig.WrappedArray) writeTypeNameInfo(node, type);

    wa.setItemName(type.getItemName());

    final org.apache.xml.xmlbeans.bindingConfig.Mapping mapping =
      wa.addNewItemType();
    mapping.setJavatype(type.getItemType().getJavaName().toString());
    mapping.setXmlcomponent(type.getItemType().getXmlName().toString());

    wa.setItemNillable(type.isItemNillable());
  }

  private static void writeSimpleContentBean(org.apache.xml.xmlbeans.bindingConfig.BindingType node,
                                             SimpleContentBean type)
  {
    node = (org.apache.xml.xmlbeans.bindingConfig.BindingType) node.changeType(org.apache.xml.xmlbeans.bindingConfig.SimpleContentBean.type);
    org.apache.xml.xmlbeans.bindingConfig.SimpleContentBean bnNode =
      (org.apache.xml.xmlbeans.bindingConfig.SimpleContentBean) writeTypeNameInfo(node, type);

    final SimpleContentProperty scprop = type.getSimpleContentProperty();
    if (scprop == null) {
      throw new IllegalArgumentException("type must have a simple content property");
    }

    final org.apache.xml.xmlbeans.bindingConfig.SimpleContentProperty sc_prop =
      bnNode.addNewSimpleContentProperty();
    PropertyRegistry.writeSimpleContentProperty(scprop, sc_prop);

    final GenericXmlProperty any_prop = type.getAnyAttributeProperty();
    if (any_prop != null) {
      final org.apache.xml.xmlbeans.bindingConfig.GenericXmlProperty gx_prop =
        bnNode.addNewAnyAttributeProperty();
      PropertyRegistry.writeGenericXmlProperty(any_prop, gx_prop);
    }

    for (Iterator i = type.getAttributeProperties().iterator(); i.hasNext();) {
      QNameProperty qProp = (QNameProperty) i.next();
      org.apache.xml.xmlbeans.bindingConfig.QnameProperty qpNode = bnNode.addNewAttributeProperty();
      PropertyRegistry.writeQNameProperty(qProp, qpNode);
    }
  }

  private static void writeJaxrpcEnumType(org.apache.xml.xmlbeans.bindingConfig.BindingType node,
                                          JaxrpcEnumType type)
  {
    node = (org.apache.xml.xmlbeans.bindingConfig.BindingType) node.changeType(org.apache.xml.xmlbeans.bindingConfig.JaxrpcEnumType.type);
    org.apache.xml.xmlbeans.bindingConfig.JaxrpcEnumType jnode =
      (org.apache.xml.xmlbeans.bindingConfig.JaxrpcEnumType) writeTypeNameInfo(node, type);

    if (type.getBaseType() != null) {
      jnode.setBaseJavatype(type.getBaseType().getJavaName().toString());
      jnode.setBaseXmlcomponent(type.getBaseType().getXmlName().toString());
    }

    if (type.getGetValueMethod() != null) {
      writeMethodName(jnode.addNewGetValueMethod(), type.getGetValueMethod());
    }

    if (type.getFromValueMethod() != null) {
      writeMethodName(jnode.addNewFromValueMethod(), type.getFromValueMethod());
    }

    if (type.getToXMLMethod() != null) {
      writeMethodName(jnode.addNewToXMLMethod(), type.getToXMLMethod());
    }

    if (type.getFromStringMethod() != null) {
      writeMethodName(jnode.addNewFromStringMethod(), type.getFromStringMethod());
    }
  }

  /*package*/
  static void writeMethodName(JavaMethodName name_node, MethodName method_name)
  {
    name_node.setMethodName(method_name.getSimpleName());
    final JavaTypeName[] param_types = method_name.getParamTypes();
    if (param_types != null && param_types.length > 0) {
      String[] types = new String[param_types.length];
      for (int i = 0; i < types.length; i++) types[i] = param_types[i].toString();
      name_node.setParamTypeArray(types);
    }
  }

  private static void writeSoapArrayType(org.apache.xml.xmlbeans.bindingConfig.BindingType node,
                                         SoapArrayType type)
  {
    node = (org.apache.xml.xmlbeans.bindingConfig.BindingType) node.changeType(org.apache.xml.xmlbeans.bindingConfig.SoapArray.type);
    final org.apache.xml.xmlbeans.bindingConfig.SoapArray wa =
      (org.apache.xml.xmlbeans.bindingConfig.SoapArray) writeTypeNameInfo(node, type);

    if (type.getItemName() != null)
      wa.setItemName(type.getItemName());

    if (type.getItemType() != null) {
      final org.apache.xml.xmlbeans.bindingConfig.Mapping mapping =
        wa.addNewItemType();

      mapping.setJavatype(type.getItemType().getJavaName().toString());
      mapping.setXmlcomponent(type.getItemType().getXmlName().toString());
    }

    wa.setItemNillable(type.isItemNillable());

    if (type.getRanks() >= 0)
      wa.setRanks(type.getRanks());
  }

  private static void writeListArrayType(org.apache.xml.xmlbeans.bindingConfig.BindingType node,
                                         ListArrayType type)
  {
    node = (org.apache.xml.xmlbeans.bindingConfig.BindingType) node.changeType(org.apache.xml.xmlbeans.bindingConfig.ListArray.type);
    final org.apache.xml.xmlbeans.bindingConfig.ListArray wa =
      (org.apache.xml.xmlbeans.bindingConfig.ListArray) writeTypeNameInfo(node, type);

    final org.apache.xml.xmlbeans.bindingConfig.Mapping mapping =
      wa.addNewItemType();
    mapping.setJavatype(type.getItemType().getJavaName().toString());
    mapping.setXmlcomponent(type.getItemType().getXmlName().toString());
  }

  static void fillTypeFromNode(WrappedArrayType wrappedArrayType,
                               org.apache.xml.xmlbeans.bindingConfig.WrappedArray node)
  {
    wrappedArrayType.setName(getNameFromNode(node));
    wrappedArrayType.setItemName(node.getItemName());

    final org.apache.xml.xmlbeans.bindingConfig.Mapping itype =
      node.getItemType();
    final JavaTypeName jName = JavaTypeName.forString(itype.getJavatype());
    final XmlTypeName xName = XmlTypeName.forString(itype.getXmlcomponent());
    wrappedArrayType.setItemType(BindingTypeName.forPair(jName, xName));
    wrappedArrayType.setItemNillable(node.getItemNillable());
  }

  static void fillTypeFromNode(ByNameBean byNameBean,
                               org.apache.xml.xmlbeans.bindingConfig.BindingType node)
  {
    byNameBean.setName(getNameFromNode(node));

    org.apache.xml.xmlbeans.bindingConfig.ByNameBean bnNode =
      (org.apache.xml.xmlbeans.bindingConfig.ByNameBean) node;

    org.apache.xml.xmlbeans.bindingConfig.GenericXmlProperty gxp =
      bnNode.getAnyProperty();
    if (gxp != null)
      byNameBean.setAnyElementProperty((GenericXmlProperty) PropertyRegistry.forNode(gxp));

    gxp = bnNode.getAnyAttributeProperty();
    if (gxp != null)
      byNameBean.setAnyAttributeProperty((GenericXmlProperty) PropertyRegistry.forNode(gxp));

    org.apache.xml.xmlbeans.bindingConfig.QnameProperty[] propArray =
      bnNode.getQnamePropertyArray();

    for (int i = 0; i < propArray.length; i++) {
      byNameBean.addProperty((QNameProperty) PropertyRegistry.forNode(propArray[i]));
    }
  }

  static void fillTypeFromNode(SimpleContentBean type,
                               org.apache.xml.xmlbeans.bindingConfig.BindingType node)
  {
    type.setName(getNameFromNode(node));

    org.apache.xml.xmlbeans.bindingConfig.SimpleContentBean simpleContentBean =
      (org.apache.xml.xmlbeans.bindingConfig.SimpleContentBean) node;
    org.apache.xml.xmlbeans.bindingConfig.QnameProperty[] propArray =
      simpleContentBean.getAttributePropertyArray();

    for (int i = 0; i < propArray.length; i++) {
      type.addProperty((QNameProperty) PropertyRegistry.forNode(propArray[i]));
    }

    final org.apache.xml.xmlbeans.bindingConfig.SimpleContentProperty scp =
      simpleContentBean.getSimpleContentProperty();

    final SimpleContentProperty bprop =
      (SimpleContentProperty) PropertyRegistry.forNode(scp);
    type.setSimpleContentProperty(bprop);

    final org.apache.xml.xmlbeans.bindingConfig.GenericXmlProperty gxp =
      simpleContentBean.getAnyAttributeProperty();

    if (gxp != null)
      type.setAnyAttributeProperty((GenericXmlProperty) PropertyRegistry.forNode(gxp));
  }

  static void fillTypeFromNode(SimpleBindingType type,
                               org.apache.xml.xmlbeans.bindingConfig.BindingType node)
  {
    type.setName(getNameFromNode(node));
    org.apache.xml.xmlbeans.bindingConfig.SimpleType stNode = (org.apache.xml.xmlbeans.bindingConfig.SimpleType) node;
    org.apache.xml.xmlbeans.bindingConfig.AsXmlType as_xml = stNode.getAsXml();
    type.setAsIfXmlType(XmlTypeName.forString(as_xml.getStringValue()));

    if (as_xml.isSetWhitespace()) {
      org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.Enum ws =
        as_xml.getWhitespace();
      if (ws.equals(org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.PRESERVE)) {
        type.setWhitespace(XmlWhitespace.WS_PRESERVE);
      } else if (ws.equals(org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.REPLACE)) {
        type.setWhitespace(XmlWhitespace.WS_REPLACE);
      } else if (ws.equals(org.apache.xml.xmlbeans.bindingConfig.AsXmlType.Whitespace.COLLAPSE)) {
        type.setWhitespace(XmlWhitespace.WS_COLLAPSE);
      } else {
        throw new AssertionError("invalid whitespace: " + ws);
      }

    }
  }

  static void fillTypeFromNode(ListArrayType type,
                               org.apache.xml.xmlbeans.bindingConfig.ListArray node)
  {
    type.setName(getNameFromNode(node));
    final org.apache.xml.xmlbeans.bindingConfig.Mapping itype =
      node.getItemType();
    final JavaTypeName jName = JavaTypeName.forString(itype.getJavatype());
    final XmlTypeName xName = XmlTypeName.forString(itype.getXmlcomponent());
    type.setItemType(BindingTypeName.forPair(jName, xName));
  }

  static void fillTypeFromNode(JaxrpcEnumType type,
                               org.apache.xml.xmlbeans.bindingConfig.JaxrpcEnumType node)
  {
    type.setName(getNameFromNode(node));

    type.setBaseType(BindingTypeName.forPair(
      JavaTypeName.forString(node.getBaseJavatype()),
      XmlTypeName.forString(node.getBaseXmlcomponent())));

    type.setGetValueMethod(BindingFileUtils.create(node.getGetValueMethod()));
    type.setFromValueMethod(BindingFileUtils.create(node.getFromValueMethod()));
    type.setFromStringMethod(BindingFileUtils.create(node.getFromStringMethod()));

    JavaMethodName toxml_method =
      node.getToXMLMethod();
    if (toxml_method != null) {
      type.setToXMLMethod(BindingFileUtils.create(toxml_method));
    }
  }

  static void fillTypeFromNode(SoapArrayType type,
                               org.apache.xml.xmlbeans.bindingConfig.SoapArray node)
  {
    type.setName(getNameFromNode(node));
    type.setItemName(node.getItemName());

    final org.apache.xml.xmlbeans.bindingConfig.Mapping itype =
      node.getItemType();
    final JavaTypeName jName = JavaTypeName.forString(itype.getJavatype());
    final XmlTypeName xName = XmlTypeName.forString(itype.getXmlcomponent());
    type.setItemType(BindingTypeName.forPair(jName, xName));

    type.setItemNillable(node.getItemNillable());


    if (node.isSetRanks())
      type.setRanks(node.getRanks());
    else
      type.setRanks(-1);
  }

  static void fillTypeFromNode(SimpleDocumentBinding simpleDocumentBinding,
                               org.apache.xml.xmlbeans.bindingConfig.SimpleDocumentBinding node)
  {
    simpleDocumentBinding.setName(getNameFromNode(node));
    simpleDocumentBinding.setTypeOfElement(XmlTypeName.forString(node.getTypeOfElement()));
  }

  private static final class TypeWriter
    implements BindingTypeVisitor
  {
    private final org.apache.xml.xmlbeans.bindingConfig.BindingType node;

    public TypeWriter(org.apache.xml.xmlbeans.bindingConfig.BindingType node)
    {
      assert node != null;
      this.node = node;
    }

    public void visit(BuiltinBindingType builtinBindingType)
      throws XmlException
    {
      throw new AssertionError("cannot write builtin types: " + builtinBindingType);
    }

    public void visit(ByNameBean byNameBean)
      throws XmlException
    {
      writeByNameBean(node, byNameBean);
    }

    public void visit(SimpleContentBean simpleContentBean)
      throws XmlException
    {
      writeSimpleContentBean(node, simpleContentBean);
    }

    public void visit(SimpleBindingType simpleBindingType)
      throws XmlException
    {
      writeSimpleBindingType(node, simpleBindingType);
    }

    public void visit(JaxrpcEnumType jaxrpcEnumType)
      throws XmlException
    {
      writeJaxrpcEnumType(node, jaxrpcEnumType);
    }

    public void visit(SimpleDocumentBinding simpleDocumentBinding)
      throws XmlException
    {
      writeSimpleDocumentBinding(node, simpleDocumentBinding);
    }

    public void visit(WrappedArrayType wrappedArrayType)
      throws XmlException
    {
      writeWrappedArrayType(node, wrappedArrayType);
    }

    public void visit(SoapArrayType soapArrayType)
      throws XmlException
    {
      writeSoapArrayType(node, soapArrayType);
    }

    public void visit(ListArrayType listArrayType)
      throws XmlException
    {
      writeListArrayType(node, listArrayType);
    }
  }
}
